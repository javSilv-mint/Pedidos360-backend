package cl.duoc.pedidos360.pedidos.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

/**
 * Identidad del llamador extraida del access token YA verificado por el Resource Server
 * (firma, iss, aud, exp). Centraliza la lectura de claims para que los controladores
 * no dependan de nombres de claims ni de prefijos de autoridades (SRP).
 */
public record UsuarioAutenticado(String email, boolean esAdmin) {

    /** Claims donde Entra ID puede entregar el correo, en orden de preferencia. */
    private static final List<String> CLAIMS_EMAIL = List.of("email", "preferred_username", "upn");
    private static final String AUTORIDAD_ADMIN = "ROLE_Admin";

    public static UsuarioAutenticado desde(JwtAuthenticationToken auth) {
        if (auth == null) {
            throw new IllegalStateException("No hay un token autenticado en la peticion");
        }
        boolean admin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(AUTORIDAD_ADMIN::equals);
        return new UsuarioAutenticado(emailDe(auth.getToken()), admin);
    }

    /** Email obligatorio: sin el claim no se puede aplicar control de propiedad. */
    public String emailObligatorio() {
        if (email == null) {
            throw new IllegalArgumentException(
                "El access token no incluye el claim 'email'. Agregalo como claim opcional en Entra ID.");
        }
        return email;
    }

    private static String emailDe(Jwt jwt) {
        for (String claim : CLAIMS_EMAIL) {
            String valor = jwt.getClaimAsString(claim);
            if (valor != null && !valor.isBlank()) return valor.trim();
        }
        return null;
    }
}
