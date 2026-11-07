package com.cesde.arteestilo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
        @NotBlank @Email String correo,
        @NotBlank String password
) {}
