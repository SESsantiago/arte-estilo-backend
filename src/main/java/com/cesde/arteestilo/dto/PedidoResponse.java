package com.cesde.arteestilo.dto;

import com.cesde.arteestilo.model.EstadoPago;
import com.cesde.arteestilo.model.EstadoPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        Long usuarioId,
        String usuarioNombre,
        String usuarioCorreo,
        EstadoPedido estado,
        EstadoPago estadoPago,
        String metodoPago,
        String referenciaPago,
        BigDecimal totalCompra,
        DireccionEnvioDTO direccionEnvio,
        List<DetallePedidoResponse> detalles,
        LocalDateTime fechaCreacion
) {}
