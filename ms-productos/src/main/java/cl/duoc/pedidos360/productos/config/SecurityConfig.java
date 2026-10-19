package cl.duoc.pedidos360.productos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Configura el microservicio como OAuth2 Resource Server.
 *
 * - La firma, issuer, audience y expiracion del JWT se validan con las
 *   propiedades spring.security.oauth2.resourceserver.jwt.* (application.properties).
 * - El claim "scp" se transforma en autoridades SCOPE_xxx.
 * - El claim "roles" (App roles del registro del API en Entra ID) se transforma en ROLE_xxx.
 * - Sin token o con token invalido -> 401. Token valido sin scope/rol -> 403.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.security.required-scope:access_as_user}")
    private String requiredScope;

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String[] allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JsonSecurityErrorHandler errores) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                       // API stateless con Bearer token
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()   // preflight CORS
                .requestMatchers("/error").permitAll()                     // deja pasar 404/500 reales
                // Cualquier usuario autenticado con el scope puede consultar el catalogo
                .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**").access(conScope())
                // Crear, editar, eliminar y descontar stock: solo rol Admin
                .requestMatchers("/api/productos", "/api/productos/**").access(adminConScope())
                .anyRequest().denyAll())
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(errores)
                .accessDeniedHandler(errores))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(errores)
                .accessDeniedHandler(errores));
        return http.build();
    }

    private WebExpressionAuthorizationManager conScope() {
        return new WebExpressionAuthorizationManager("hasAuthority('SCOPE_" + requiredScope + "')");
    }

    private WebExpressionAuthorizationManager adminConScope() {
        return new WebExpressionAuthorizationManager(
                "hasAuthority('SCOPE_" + requiredScope + "') and hasRole('Admin')");
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter(); // scp -> SCOPE_
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> autoridades = new ArrayList<>(scopes.convert(jwt));
            autoridades.addAll(roles.convert(jwt));
            return autoridades;
        });
        return converter;
    }

    /** CORS para desarrollo local. En la nube, API Gateway responde el CORS. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(allowedOrigins));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cfg);
        return source;
    }
}
