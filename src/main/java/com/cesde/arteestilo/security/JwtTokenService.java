package com.cesde.arteestilo.security;

import com.cesde.arteestilo.exception.AccesoDenegadoException;
import com.cesde.arteestilo.exception.NoAutorizadoException;
import com.cesde.arteestilo.model.RolUsuario;
import com.cesde.arteestilo.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Service
public class JwtTokenService {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final long EXPIRATION_SECONDS = 86400L * 7; // 7 días

    @Value("${jwt.secret:arte-estilo-secret-key-2026-cesde-full-stack-security}")
    private String secret;

    public record TokenClaims(Long userId, String correo, RolUsuario rol, long exp) {}

    public String generateToken(Usuario usuario) {
        long exp = Instant.now().getEpochSecond() + EXPIRATION_SECONDS;
        RolUsuario rol = usuario.getRol() != null ? usuario.getRol() : RolUsuario.USUARIO;
        String payload = usuario.getId() + "|" + usuario.getCorreo() + "|" + rol.name() + "|" + exp;
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String signature = sign(encodedPayload);
        return encodedPayload + "." + signature;
    }

    public TokenClaims parseAndValidate(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new NoAutorizadoException("Token de autenticación requerido");
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7).trim() : authHeader.trim();
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            throw new NoAutorizadoException("Formato de token inválido");
        }
        String expectedSig = sign(parts[0]);
        if (!MessageDigest.isEqual(
                expectedSig.getBytes(StandardCharsets.UTF_8),
                parts[1].getBytes(StandardCharsets.UTF_8))) {
            throw new NoAutorizadoException("Firma de token inválida");
        }
        String decoded = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String[] fields = decoded.split("\\|");
        if (fields.length != 4) {
            throw new NoAutorizadoException("Contenido de token inválido");
        }
        long exp = Long.parseLong(fields[3]);
        if (Instant.now().getEpochSecond() > exp) {
            throw new NoAutorizadoException("La sesión ha expirado. Inicia sesión nuevamente.");
        }
        return new TokenClaims(
                Long.parseLong(fields[0]),
                fields[1],
                RolUsuario.valueOf(fields[2]),
                exp
        );
    }

    public TokenClaims requireAdmin(String authHeader) {
        TokenClaims claims = parseAndValidate(authHeader);
        if (claims.rol() != RolUsuario.ADMIN) {
            throw new AccesoDenegadoException("Se requieren permisos de Administrador para realizar esta operación");
        }
        return claims;
    }

    public String computePaymentSignature(String referencia, String monto, String moneda) {
        return sign(referencia + "|" + monto + "|" + moneda);
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new IllegalStateException("Error firmando datos de seguridad", e);
        }
    }
}
