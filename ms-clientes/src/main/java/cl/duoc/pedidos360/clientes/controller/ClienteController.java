package cl.duoc.pedidos360.clientes.controller;

import cl.duoc.pedidos360.clientes.dto.ClienteRequest;
import cl.duoc.pedidos360.clientes.model.Cliente;
import cl.duoc.pedidos360.clientes.security.UsuarioAutenticado;
import cl.duoc.pedidos360.clientes.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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

    /** Solo rol Admin. */
    @GetMapping("/{id}")
    public Cliente obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    /** Perfil del usuario autenticado, buscado por el email del JWT. */
    @GetMapping("/mi-perfil")
    public Cliente miPerfil(JwtAuthenticationToken auth) {
        return service.obtenerPorEmail(UsuarioAutenticado.desde(auth).emailObligatorio());
    }

    /** Busqueda administrativa por email. Solo rol Admin. */
    @GetMapping("/buscar")
    public Cliente obtenerPorEmail(@RequestParam String email) {
        return service.obtenerPorEmail(email);
    }

    /**
     * Autenticado. Un Admin puede registrar a cualquier cliente; un usuario normal
     * solo puede registrarse A SI MISMO: el email se fuerza al del token (anti-suplantacion).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cliente crear(@Valid @RequestBody ClienteRequest request, JwtAuthenticationToken auth) {
        UsuarioAutenticado usuario = UsuarioAutenticado.desde(auth);
        ClienteRequest efectivo = usuario.esAdmin() ? request : request.conEmail(usuario.emailObligatorio());
        return service.crear(efectivo);
    }

    /** Solo rol Admin. */
    @PutMapping("/{id}")
    public Cliente actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return service.actualizar(id, request);
    }

    /** Solo rol Admin (borrado logico). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
