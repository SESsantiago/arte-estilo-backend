package com.cesde.arteestilo.dto;

import com.cesde.arteestilo.model.RolUsuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nombre,
        String correo,
        RolUsuario rol,
        String telefono,
        String ciudad,
        String biografia,
        String fotoPerfilUrl,
        Boolean estadoActivo,
        LocalDateTime fechaCreacion
) {}
