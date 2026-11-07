package com.cesde.arteestilo.dto;

import java.math.BigDecimal;

public record DetallePedidoResponse(
        Long productoId,
        String productoNombre,
        String productoImagen,
        String categoriaNombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}
