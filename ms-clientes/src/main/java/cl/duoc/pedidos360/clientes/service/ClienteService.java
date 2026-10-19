package cl.duoc.pedidos360.clientes.service;

import cl.duoc.pedidos360.clientes.dto.ClienteRequest;
import cl.duoc.pedidos360.clientes.model.Cliente;
import cl.duoc.pedidos360.clientes.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repository;

    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return repository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public Cliente obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public Cliente obtenerPorEmail(String email) {
        return repository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado con email: " + email));
    }

    /** Unicidad validada antes de persistir -> 409 claro en vez de 500 por constraint de BD. */
    @Transactional
    public Cliente crear(ClienteRequest datos) {
        if (repository.existsByRut(datos.rut())) {
            throw new IllegalStateException("Ya existe un cliente con el RUT " + datos.rut());
        }
        if (repository.existsByEmailIgnoreCase(datos.email())) {
            throw new IllegalStateException("Ya existe un cliente con el email " + datos.email());
        }
        Cliente cliente = Cliente.builder()
                .rut(datos.rut().toUpperCase())
                .nombre(datos.nombre())
                .apellido(datos.apellido())
                .email(datos.email().trim().toLowerCase())
                .telefono(datos.telefono())
                .direccion(datos.direccion())
                .activo(true)
                .build();
        return repository.save(cliente);
    }

    /** El RUT es identificador natural y no se modifica. */
    @Transactional
    public Cliente actualizar(Long id, ClienteRequest datos) {
        Cliente existente = obtenerPorId(id);
        if (repository.existsByEmailIgnoreCaseAndIdNot(datos.email(), id)) {
            throw new IllegalStateException("El email " + datos.email() + " ya esta en uso por otro cliente");
        }
        existente.setNombre(datos.nombre());
        existente.setApellido(datos.apellido());
        existente.setEmail(datos.email().trim().toLowerCase());
        existente.setTelefono(datos.telefono());
        existente.setDireccion(datos.direccion());
        return repository.save(existente);
    }

    /** Borrado logico. */
    @Transactional
    public void eliminar(Long id) {
        Cliente existente = obtenerPorId(id);
        existente.setActivo(false);
        repository.save(existente);
    }
}
