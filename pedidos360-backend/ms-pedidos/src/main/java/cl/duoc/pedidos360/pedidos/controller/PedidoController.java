package cl.duoc.pedidos360.pedidos.controller;

import cl.duoc.pedidos360.pedidos.dto.CrearPedidoRequest;
import cl.duoc.pedidos360.pedidos.model.EstadoPedido;
import cl.duoc.pedidos360.pedidos.model.Pedido;
import cl.duoc.pedidos360.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService service;

    /** Solo rol Admin (ver SecurityConfig). */
    @GetMapping
    public List<Pedido> listar(@RequestParam(required = false) Long clienteId) {
        return (clienteId == null) ? service.listar() : service.listarPorCliente(clienteId);
    }

    @GetMapping("/{id}")
    public Pedido obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    /** Pedidos del usuario autenticado: el email se toma del JWT, no de un parametro manipulable. */
    @GetMapping("/mis-pedidos")
    public List<Pedido> misPedidos(@AuthenticationPrincipal Jwt jwt) {
        return service.listarPorEmail(emailObligatorio(jwt));
    }

    /** Busqueda administrativa por email. Solo rol Admin. */
    @GetMapping("/por-email")
    public List<Pedido> listarPorEmail(@RequestParam String email) {
        return service.listarPorEmail(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pedido crear(@Valid @RequestBody CrearPedidoRequest request,
                        @AuthenticationPrincipal Jwt jwt) {
        // El email del pedido siempre sale del token cuando esta disponible
        String email = emailDelToken(jwt);
        if (email != null) {
            request.setClienteEmail(email);
        }
        return service.crear(request);
    }

    /** Solo rol Admin. */
    @PatchMapping("/{id}/estado")
    public Pedido cambiarEstado(@PathVariable Long id, @RequestParam EstadoPedido estado) {
        return service.cambiarEstado(id, estado);
    }

    @PatchMapping("/{id}/cancelar")
    public Pedido cancelar(@PathVariable Long id) {
        return service.cancelar(id);
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
