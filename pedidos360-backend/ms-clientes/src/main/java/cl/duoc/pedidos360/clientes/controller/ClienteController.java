package cl.duoc.pedidos360.clientes.controller;

import cl.duoc.pedidos360.clientes.model.Cliente;
import cl.duoc.pedidos360.clientes.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;

    /** Solo rol Admin (ver SecurityConfig). */
    @GetMapping
    public List<Cliente> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Cliente obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    /** Perfil del usuario autenticado, buscado por el email del JWT. */
    @GetMapping("/mi-perfil")
    public Cliente miPerfil(@AuthenticationPrincipal Jwt jwt) {
        return service.obtenerPorEmail(emailObligatorio(jwt));
    }

    /** Busqueda administrativa por email. Solo rol Admin. */
    @GetMapping("/buscar")
    public Cliente obtenerPorEmail(@RequestParam String email) {
        return service.obtenerPorEmail(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cliente crear(@Valid @RequestBody Cliente cliente) {
        return service.crear(cliente);
    }

    @PutMapping("/{id}")
    public Cliente actualizar(@PathVariable Long id, @Valid @RequestBody Cliente cliente) {
        return service.actualizar(id, cliente);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /** Obtiene el email del usuario desde los claims del JWT (email, preferred_username o upn). */
    private static String emailDelToken(Jwt jwt) {
        if (jwt == null) return null;
        for (String claim : List.of("email", "preferred_username", "upn")) {
            String valor = jwt.getClaimAsString(claim);
            if (valor != null && !valor.isBlank()) return valor;
        }
        return null;
    }

    private static String emailObligatorio(Jwt jwt) {
        String email = emailDelToken(jwt);
        if (email == null) {
            throw new IllegalArgumentException(
                "El access token no incluye el claim 'email'. Agregalo como claim opcional en Entra ID.");
        }
        return email;
    }
}
