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

    @Transactional(readOnly = true)
    public Producto obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        return repository.findByNombreContainingIgnoreCase(nombre);
    }

    @Transactional
    public Producto crear(Producto producto) {
        producto.setId(null);
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
        Producto p = obtenerPorId(id);
        if (p.getStock() < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente para el producto " + p.getNombre());
        }
        p.setStock(p.getStock() - cantidad);
        return repository.save(p);
    }
}
