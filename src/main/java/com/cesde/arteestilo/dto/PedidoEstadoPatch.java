package com.cesde.arteestilo.dto;

import com.cesde.arteestilo.model.EstadoPago;
import com.cesde.arteestilo.model.EstadoPedido;

public record PedidoEstadoPatch(
        EstadoPedido estado,
        EstadoPago estadoPago
) {}
