package com.cesde.arteestilo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PagoIniciarRequest(
        @NotNull Long pedidoId,
        @NotBlank String metodoPago,
        String detalleMetodo,
        String numeroTarjeta,
        String nombreTitular,
        String bancoPse
) {}
