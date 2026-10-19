package cl.duoc.pedidos360.pedidos.controller;

import cl.duoc.pedidos360.pedidos.dto.CrearPedidoRequest;
import cl.duoc.pedidos360.pedidos.model.EstadoPedido;
import cl.duoc.pedidos360.pedidos.model.Pedido;
import cl.duoc.pedidos360.pedidos.security.UsuarioAutenticado;
import cl.duoc.pedidos360.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * La autorizacion por ROL esta centralizada en SecurityConfig;
 * la autorizacion por PROPIEDAD (dueno del pedido) se aplica en PedidoService.
 */
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

    /** Autenticado: Admin ve cualquiera; el resto solo sus propios pedidos (anti-IDOR). */
    @GetMapping("/{id}")
    public Pedido obtener(@PathVariable Long id, JwtAuthenticationToken auth) {
        return service.obtenerParaUsuario(id, UsuarioAutenticado.desde(auth));
    }

    /** Pedidos del usuario autenticado: el email se toma del JWT, no de un parametro manipulable. */
    @GetMapping("/mis-pedidos")
    public List<Pedido> misPedidos(JwtAuthenticationToken auth) {
        return service.listarPorEmail(UsuarioAutenticado.desde(auth).emailObligatorio());
    }

    /** Busqueda administrativa por email. Solo rol Admin. */
    @GetMapping("/por-email")
    public List<Pedido> listarPorEmail(@RequestParam String email) {
        return service.listarPorEmail(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pedido crear(@Valid @RequestBody CrearPedidoRequest request, JwtAuthenticationToken auth) {
        // Sin claim email -> 400. Nunca se usa un email enviado por el cliente.
        return service.crear(request, UsuarioAutenticado.desde(auth).emailObligatorio());
    }

    /** Solo rol Admin. */
    @PatchMapping("/{id}/estado")
    public Pedido cambiarEstado(@PathVariable Long id, @RequestParam EstadoPedido estado) {
        return service.cambiarEstado(id, estado);
    }

    /** Autenticado: solo el dueno (o Admin) y solo si el estado actual es cancelable. */
    @PatchMapping("/{id}/cancelar")
    public Pedido cancelar(@PathVariable Long id, JwtAuthenticationToken auth) {
        return service.cancelar(id, UsuarioAutenticado.desde(auth));
    }

    /** Solo rol Admin. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
