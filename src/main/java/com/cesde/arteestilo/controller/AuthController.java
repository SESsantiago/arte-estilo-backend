package com.cesde.arteestilo.controller;

import com.cesde.arteestilo.dto.*;
import com.cesde.arteestilo.security.JwtTokenService;
import com.cesde.arteestilo.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Registro, inicio de sesión, perfil y recuperación de contraseña")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtTokenService jwtTokenService;

    public AuthController(UsuarioService usuarioService, JwtTokenService jwtTokenService) {
        this.usuarioService = usuarioService;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión con correo y contraseña")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthLoginRequest dto) {
        return ResponseEntity.ok(usuarioService.login(dto));
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar una nueva cuenta de usuario")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody AuthRegisterRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrar(dto));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener el perfil del usuario autenticado mediante el token")
    public ResponseEntity<UsuarioResponse> me(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        JwtTokenService.TokenClaims claims = jwtTokenService.parseAndValidate(authHeader);
        return ResponseEntity.ok(usuarioService.obtener(claims.userId()));
    }

    @PostMapping("/recuperar")
    @Operation(summary = "Solicitar recuperación o restablecimiento de contraseña")
    public ResponseEntity<Map<String, String>> recuperar(@Valid @RequestBody RecuperarPasswordRequest dto) {
        usuarioService.recuperarPassword(dto);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Solicitud procesada correctamente para " + dto.correo()
        ));
    }
}
