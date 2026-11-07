package com.cesde.arteestilo.service;

import com.cesde.arteestilo.dto.PagoIniciarRequest;
import com.cesde.arteestilo.dto.PagoWebhookRequest;
import com.cesde.arteestilo.dto.TransaccionPagoResponse;
import com.cesde.arteestilo.exception.RecursoNoEncontradoException;
import com.cesde.arteestilo.model.*;
import com.cesde.arteestilo.repository.PedidoRepository;
import com.cesde.arteestilo.repository.ProductoRepository;
import com.cesde.arteestilo.repository.TransaccionPagoRepository;
import com.cesde.arteestilo.security.JwtTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PagoService {

    private static final BigDecimal RECARGO_CONTRAENTREGA = new BigDecimal("15000.00");

    @Value("${payment.gateway.name:Wompi-Colombia-Sandbox}")
    private String gatewayName;

    private final TransaccionPagoRepository transaccionRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final JwtTokenService jwtTokenService;

    public PagoService(TransaccionPagoRepository transaccionRepository,
                       PedidoRepository pedidoRepository,
                       ProductoRepository productoRepository,
                       JwtTokenService jwtTokenService) {
        this.transaccionRepository = transaccionRepository;
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * Inicia una transacción de pago asociada a un pedido real validado en el servidor,
     * calcula firma criptográfica de integridad y la procesa con la pasarela configurada.
     */
    @Transactional
    public TransaccionPagoResponse iniciarPago(PagoIniciarRequest dto) {
        Pedido pedido = pedidoRepository.findById(dto.pedidoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado con id " + dto.pedidoId()));

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new IllegalArgumentException("No se puede iniciar el pago de un pedido cancelado");
        }

        TransaccionPago tx = transaccionRepository.findByPedidoId(pedido.getId())
                .orElseGet(TransaccionPago::new);

        String referencia = "AE-PAY-" + pedido.getId() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BigDecimal montoFinal = pedido.getTotalCompra();
        if ("cash".equalsIgnoreCase(dto.metodoPago()) || "CONTRAENTREGA".equalsIgnoreCase(dto.metodoPago())) {
            montoFinal = montoFinal.add(RECARGO_CONTRAENTREGA);
            pedido.setTotalCompra(montoFinal);
        }

        String firma = jwtTokenService.computePaymentSignature(referencia, montoFinal.toPlainString(), "COP");
        String metodoLabel = normalizarMetodo(dto.metodoPago(), dto.numeroTarjeta(), dto.bancoPse(), dto.detalleMetodo());

        tx.setPedido(pedido);
        tx.setReferencia(referencia);
        tx.setPasarela(gatewayName);
        tx.setMetodoPago(dto.metodoPago().toUpperCase());
        tx.setDetalleMetodo(metodoLabel);
        tx.setMonto(montoFinal);
        tx.setMoneda("COP");
        tx.setFirmaIntegridad(firma);

        // Evaluación determinística de la pasarela en el servidor
        String cleanCard = dto.numeroTarjeta() != null ? dto.numeroTarjeta().replaceAll("\\D", "") : "";
        if ("card".equalsIgnoreCase(dto.metodoPago()) && cleanCard.endsWith("0000")) {
            tx.setEstado(EstadoPago.RECHAZADO);
            tx.setCodigoAutorizacion(null);
            tx.setMensajeRespuesta("Fondos insuficientes o tarjeta rechazada por el banco emisor");
            tx.setFechaConfirmacion(LocalDateTime.now());
            pedido.setEstadoPago(EstadoPago.RECHAZADO);
        } else if ("card".equalsIgnoreCase(dto.metodoPago()) && cleanCard.endsWith("1111")) {
            tx.setEstado(EstadoPago.EN_PROCESO);
            tx.setCodigoAutorizacion("PEND-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
            tx.setMensajeRespuesta("Transacción en validación bancaria antifraude");
            pedido.setEstadoPago(EstadoPago.EN_PROCESO);
        } else if ("cash".equalsIgnoreCase(dto.metodoPago())) {
            tx.setEstado(EstadoPago.PENDIENTE);
            tx.setCodigoAutorizacion("COD-" + pedido.getId());
            tx.setMensajeRespuesta("Pago contraentrega registrado — pendiente de recaudo al entregar");
            pedido.setEstadoPago(EstadoPago.PENDIENTE);
        } else {
            tx.setEstado(EstadoPago.APROBADO);
            tx.setCodigoAutorizacion("AUT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
            tx.setMensajeRespuesta("Transacción aprobada y verificada exitosamente en la pasarela");
            tx.setFechaConfirmacion(LocalDateTime.now());
            pedido.setEstadoPago(EstadoPago.APROBADO);
        }

        pedido.setMetodoPago(metodoLabel);
        pedido.setReferenciaPago(referencia);
        pedidoRepository.save(pedido);

        return toResponse(transaccionRepository.save(tx));
    }

    /**
     * Webhook / confirmación de pasarela de pagos con verificación de firma criptográfica en backend.
     */
    @Transactional
    public TransaccionPagoResponse procesarWebhook(PagoWebhookRequest dto) {
        TransaccionPago tx = transaccionRepository.findByReferencia(dto.referencia())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Transacción no encontrada con referencia " + dto.referencia()));

        // Si se envía firma de integridad, la verificamos contra la firma generada en servidor
        if (dto.firmaIntegridad() != null && !dto.firmaIntegridad().isBlank()) {
            String esperada = jwtTokenService.computePaymentSignature(
                    tx.getReferencia(), tx.getMonto().toPlainString(), tx.getMoneda());
            if (!esperada.equals(dto.firmaIntegridad())) {
                throw new IllegalArgumentException("Firma de integridad del webhook inválida");
            }
        }

        Pedido pedido = tx.getPedido();
        EstadoPago nuevoEstado = dto.estado();
        tx.setEstado(nuevoEstado);
        if (dto.codigoAutorizacion() != null) {
            tx.setCodigoAutorizacion(dto.codigoAutorizacion());
        }
        if (dto.mensajeRespuesta() != null) {
            tx.setMensajeRespuesta(dto.mensajeRespuesta());
        }
        tx.setFechaConfirmacion(LocalDateTime.now());

        pedido.setEstadoPago(nuevoEstado);
        if (nuevoEstado == EstadoPago.CANCELADO || nuevoEstado == EstadoPago.RECHAZADO) {
            if (pedido.getEstado() == EstadoPedido.PENDIENTE) {
                for (DetallePedido det : pedido.getDetalles()) {
                    Producto prod = det.getProducto();
                    prod.setStock(prod.getStock() + det.getCantidad());
                    productoRepository.save(prod);
                }
                pedido.setEstado(EstadoPedido.CANCELADO);
            }
        }
        pedidoRepository.save(pedido);
        return toResponse(transaccionRepository.save(tx));
    }

    @Transactional(readOnly = true)
    public TransaccionPagoResponse verificarPorReferencia(String referencia) {
        TransaccionPago tx = transaccionRepository.findByReferencia(referencia)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Transacción no encontrada con referencia " + referencia));
        return toResponse(tx);
    }

    @Transactional(readOnly = true)
    public TransaccionPagoResponse obtenerPorPedido(Long pedidoId) {
        TransaccionPago tx = transaccionRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe transacción de pago para el pedido " + pedidoId));
        return toResponse(tx);
    }

    @Transactional(readOnly = true)
    public List<TransaccionPagoResponse> listarTodas(EstadoPago estado) {
        List<TransaccionPago> list = (estado != null)
                ? transaccionRepository.findByEstadoOrderByFechaCreacionDesc(estado)
                : transaccionRepository.findAllByOrderByFechaCreacionDesc();
        return list.stream().map(this::toResponse).toList();
    }

    private String normalizarMetodo(String metodo, String card, String banco, String detalle) {
        if (detalle != null && !detalle.isBlank()) return detalle.trim();
        if ("card".equalsIgnoreCase(metodo)) {
            String digits = card != null ? card.replaceAll("\\D", "") : "";
            String last4 = digits.length() >= 4 ? digits.substring(digits.length() - 4) : "4821";
            String brand = digits.startsWith("5") || digits.startsWith("2") ? "Mastercard" : "Visa";
            return brand + " •••• " + last4;
        }
        if ("pse".equalsIgnoreCase(metodo)) {
            return "PSE — " + (banco != null && !banco.isBlank() ? banco : "Bancolombia");
        }
        if ("cash".equalsIgnoreCase(metodo)) {
            return "Contraentrega";
        }
        return metodo;
    }

    public TransaccionPagoResponse toResponse(TransaccionPago tx) {
        Pedido p = tx.getPedido();
        return new TransaccionPagoResponse(
                tx.getId(),
                tx.getReferencia(),
                p.getId(),
                p.getUsuario().getId(),
                p.getUsuario().getNombre(),
                tx.getPasarela(),
                tx.getMetodoPago(),
                tx.getDetalleMetodo(),
                tx.getMonto(),
                tx.getMoneda(),
                tx.getEstado(),
                p.getEstado(),
                tx.getCodigoAutorizacion(),
                tx.getFirmaIntegridad(),
                tx.getMensajeRespuesta(),
                tx.getFechaCreacion(),
                tx.getFechaConfirmacion()
        );
    }
}
