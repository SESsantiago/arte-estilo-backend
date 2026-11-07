package com.cesde.arteestilo.dto;

import com.cesde.arteestilo.model.EstadoPago;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PagoWebhookRequest(
        @NotBlank String referencia,
        @NotNull EstadoPago estado,
        String codigoAutorizacion,
        String firmaIntegridad,
        String mensajeRespuesta
) {}
