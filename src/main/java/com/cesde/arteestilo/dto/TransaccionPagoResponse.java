package com.cesde.arteestilo.dto;

import com.cesde.arteestilo.model.EstadoPago;
import com.cesde.arteestilo.model.EstadoPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransaccionPagoResponse(
        Long id,
        String referencia,
        Long pedidoId,
        Long usuarioId,
        String usuarioNombre,
        String pasarela,
        String metodoPago,
        String detalleMetodo,
        BigDecimal monto,
        String moneda,
        EstadoPago estado,
        EstadoPedido estadoPedido,
        String codigoAutorizacion,
        String firmaIntegridad,
        String mensajeRespuesta,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaConfirmacion
) {}
