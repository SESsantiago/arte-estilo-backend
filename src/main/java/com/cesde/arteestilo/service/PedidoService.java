package com.cesde.arteestilo.service;

import com.cesde.arteestilo.dto.*;
import com.cesde.arteestilo.exception.MontoInvalidoException;
import com.cesde.arteestilo.exception.PedidoNoCancelableException;
import com.cesde.arteestilo.exception.RecursoNoEncontradoException;
import com.cesde.arteestilo.exception.StockInsuficienteException;
import com.cesde.arteestilo.model.*;
import com.cesde.arteestilo.repository.PedidoRepository;
import com.cesde.arteestilo.repository.ProductoRepository;
import com.cesde.arteestilo.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PedidoService {

    private static final BigDecimal MONTO_DESCUENTO = new BigDecimal("2000000");
    private static final BigDecimal PORCENTAJE_DESCUENTO = new BigDecimal("0.10");

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProductoRepository productoRepository,
                         UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public PedidoResponse crearPedido(PedidoRequest dto) {
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .filter(u -> Boolean.TRUE.equals(u.getEstadoActivo()))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario no encontrado con id " + dto.usuarioId()));

        DireccionEnvioDTO d = dto.direccionEnvio();
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setEstadoPago(EstadoPago.PENDIENTE);
        pedido.setDireccionEnvio(new DireccionEnvio(
                d.direccion(), d.ciudad(), d.departamento(), d.referencia()));

        BigDecimal total = BigDecimal.ZERO;

        for (DetallePedidoRequest item : dto.detalles()) {
            Producto producto = productoRepository.findById(item.productoId())
                    .filter(p -> Boolean.TRUE.equals(p.getEstadoActivo()))
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Producto no encontrado con id " + item.productoId()));

            // ---- Regla 1: validación de stock ----
            if (producto.getStock() < item.cantidad()) {
                throw new StockInsuficienteException(
                        "Stock insuficiente para el producto '" + producto.getNombre() +
                        "'. Disponible: " + producto.getStock() +
                        ", solicitado: " + item.cantidad());
            }
            producto.setStock(producto.getStock() - item.cantidad());
            productoRepository.save(producto);

            BigDecimal precioEfectivo = (producto.getPrecioDescuento() != null && producto.getPrecioDescuento().signum() > 0)
                    ? producto.getPrecioDescuento()
                    : producto.getPrecio();

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(precioEfectivo);
            pedido.getDetalles().add(detalle);

            total = total.add(precioEfectivo.multiply(BigDecimal.valueOf(item.cantidad())));
        }

        // ---- Regla 2: descuento ----
        pedido.setTotalCompra(aplicarDescuento(total));

        return toResponse(pedidoRepository.save(pedido));
    }

    private BigDecimal aplicarDescuento(BigDecimal total) {
        if (total == null || total.signum() < 0) {
            throw new MontoInvalidoException("El monto total del pedido no puede ser negativo");
        }
        if (total.compareTo(MONTO_DESCUENTO) > 0) {
            BigDecimal descuento = total.multiply(PORCENTAJE_DESCUENTO);
            return total.subtract(descuento).setScale(2, RoundingMode.HALF_UP);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public PedidoResponse cancelarPedido(Long pedidoId) {
        Pedido pedido = buscar(pedidoId);
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new PedidoNoCancelableException(
                    "Solo se pueden cancelar pedidos en estado PENDIENTE. Estado actual: "
                            + pedido.getEstado());
        }
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto);
        }
        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setEstadoPago(EstadoPago.CANCELADO);
        if (pedido.getTransaccionPago() != null) {
            pedido.getTransaccionPago().setEstado(EstadoPago.CANCELADO);
        }
        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse cambiarEstado(Long id, PedidoEstadoPatch dto) {
        Pedido pedido = buscar(id);
        if (dto.estado() != null) {
            pedido.setEstado(dto.estado());
        }
        if (dto.estadoPago() != null) {
            pedido.setEstadoPago(dto.estadoPago());
            if (pedido.getTransaccionPago() != null) {
                pedido.getTransaccionPago().setEstado(dto.estadoPago());
            }
        }
        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(Long usuarioId, EstadoPedido estado) {
        List<Pedido> pedidos;
        if (usuarioId != null && estado != null) {
            pedidos = pedidoRepository.findByUsuarioIdAndEstado(usuarioId, estado);
        } else if (usuarioId != null) {
            pedidos = pedidoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        } else if (estado != null) {
            pedidos = pedidoRepository.findByEstadoOrderByFechaCreacionDesc(estado);
        } else {
            pedidos = pedidoRepository.findAllByOrderByFechaCreacionDesc();
        }
        return pedidos.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    private Pedido buscar(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Pedido no encontrado con id " + id));
    }

    public PedidoResponse toResponse(Pedido p) {
        DireccionEnvio d = p.getDireccionEnvio();
        DireccionEnvioDTO direccion = (d == null) ? null : new DireccionEnvioDTO(
                d.getDireccion(), d.getCiudad(), d.getDepartamento(), d.getReferencia());

        List<DetallePedidoResponse> detalles = p.getDetalles().stream()
                .map(x -> new DetallePedidoResponse(
                        x.getProducto().getId(),
                        x.getProducto().getNombre(),
                        x.getProducto().getImagenUrl(),
                        x.getProducto().getCategoria() != null ? x.getProducto().getCategoria().getNombre() : "Colección",
                        x.getCantidad(),
                        x.getPrecioUnitario(),
                        x.getPrecioUnitario().multiply(BigDecimal.valueOf(x.getCantidad()))))
                .toList();

        return new PedidoResponse(
                p.getId(),
                p.getUsuario().getId(),
                p.getUsuario().getNombre(),
                p.getUsuario().getCorreo(),
                p.getEstado(),
                p.getEstadoPago() != null ? p.getEstadoPago() : EstadoPago.PENDIENTE,
                p.getMetodoPago(),
                p.getReferenciaPago(),
                p.getTotalCompra(),
                direccion,
                detalles,
                p.getFechaCreacion());
    }
}
