package cl.duoc.pedidos360.productos.repository;

import cl.duoc.pedidos360.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByActivoTrue();
    Optional<Producto> findByIdAndActivoTrue(Long id);
    List<Producto> findByCategoriaIgnoreCase(String categoria);
    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    /**
     * Descuento ATOMICO: la condicion "stock >= cantidad" se evalua en la misma sentencia
     * UPDATE, por lo que dos peticiones concurrentes no pueden dejar stock negativo
     * (evita el lost update del patron leer-modificar-guardar). Devuelve filas afectadas.
     * Sera reutilizado por el consumidor RabbitMQ de reserva de stock en la EV3.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Producto p SET p.stock = p.stock - :cantidad "
         + "WHERE p.id = :id AND p.activo = true AND p.stock >= :cantidad")
    int descontarStockSiDisponible(@Param("id") Long id, @Param("cantidad") int cantidad);
}
