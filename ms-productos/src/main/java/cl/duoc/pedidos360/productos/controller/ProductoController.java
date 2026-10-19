package cl.duoc.pedidos360.productos.controller;

import cl.duoc.pedidos360.productos.model.Producto;
import cl.duoc.pedidos360.productos.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService service;

    @GetMapping
    public List<Producto> listar(@RequestParam(required = false) String nombre) {
        return (nombre == null || nombre.isBlank())
                ? service.listar()
                : service.buscarPorNombre(nombre);
    }

    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // Solo rol Admin: la regla esta centralizada en SecurityConfig
    public Producto crear(@Valid @RequestBody Producto producto) {
        return service.crear(producto);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        return service.actualizar(id, producto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/descontar-stock")
    public Producto descontarStock(@PathVariable Long id, @RequestParam int cantidad) {
        return service.descontarStock(id, cantidad);
    }
}
