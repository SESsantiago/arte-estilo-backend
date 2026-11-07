package com.cesde.arteestilo.dto;

import com.cesde.arteestilo.model.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioPatch(
        @Pattern(regexp = ".*\\S.*", message = "no puede estar vacío") @Size(max = 100) String nombre,
        @Email @Size(max = 150) String correo,
        @Size(min = 6, max = 100) String password,
        RolUsuario rol,
        @Size(max = 20) String telefono,
        @Size(max = 80) String ciudad,
        @Size(max = 255) String biografia,
        @Size(max = 255) String fotoPerfilUrl,
        Boolean estadoActivo
) {}
