package com.cesde.arteestilo.dto;

public record AuthResponse(
        String token,
        UsuarioResponse usuario
) {}
