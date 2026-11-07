package com.cesde.arteestilo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecuperarPasswordRequest(
        @NotBlank @Email String correo,
        @Size(min = 6, max = 100) String nuevaPassword
) {}
