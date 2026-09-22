package cl.duoc.pedidos360.clientes.repository;

import cl.duoc.pedidos360.clientes.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByActivoTrue();
    Optional<Cliente> findByRut(String rut);
    // Usado por /mi-perfil: busca al cliente por el email
    // que viene en el claim "email" del access token.
    Optional<Cliente> findByEmailIgnoreCase(String email);
    boolean existsByRut(String rut);
}
