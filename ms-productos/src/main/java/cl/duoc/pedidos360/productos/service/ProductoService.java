package cl.duoc.pedidos360.productos.service;

import cl.duoc.pedidos360.productos.model.Producto;
import cl.duoc.pedidos360.productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository repository;

    @Transactional(readOnly = true)
    public List<Producto> listar() {
        return repository.findByActivoTrue();
    }

    /** Solo productos activos: un producto dado de baja se comporta como inexistente. */
    @Transactional(readOnly = true)
    public Producto obtenerPorId(Long id) {
        return repository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        return repository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre.trim());
    }

    @Transactional
    public Producto crear(Producto producto) {
        producto.setId(null);
        producto.setActivo(true);   // el cliente no decide el estado (mass assignment)
        return repository.save(producto);
    }

    @Transactional
    public Producto actualizar(Long id, Producto datos) {
        Producto existente = obtenerPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());
        existente.setPrecio(datos.getPrecio());
        existente.setStock(datos.getStock());
        existente.setCategoria(datos.getCategoria());
        return repository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        Producto existente = obtenerPorId(id);
        existente.setActivo(false);   // borrado logico
        repository.save(existente);
    }

    @Transactional
    public Producto descontarStock(Long id, int cantidad) {
        if (cantidad < 1) {
            // Sin esta validacion una cantidad negativa AUMENTABA el stock
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor o igual a 1");
        }
        Producto producto = obtenerPorId(id);   // 404 si no existe o esta inactivo
        if (repository.descontarStockSiDisponible(id, cantidad) == 0) {
            throw new IllegalStateException("Stock insuficiente para el producto " + producto.getNombre()
                    + " (disponible: " + producto.getStock() + ", solicitado: " + cantidad + ")");
        }
        return obtenerPorId(id);
    }
}
