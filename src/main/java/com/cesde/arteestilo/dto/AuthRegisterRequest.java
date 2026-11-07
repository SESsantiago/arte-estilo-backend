package com.cesde.arteestilo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Email @Size(max = 150) String correo,
        @NotBlank @Size(min = 6, max = 100) String password,
        @Size(max = 20) String telefono,
        @Size(max = 80) String ciudad
) {}
