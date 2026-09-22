package cl.duoc.pedidos360.clientes.service;

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

    @Transactional
    public Cliente crear(Cliente cliente) {
        if (repository.existsByRut(cliente.getRut())) {
            throw new IllegalArgumentException("Ya existe un cliente con el RUT " + cliente.getRut());
        }
        cliente.setId(null);
        return repository.save(cliente);
    }

    @Transactional
    public Cliente actualizar(Long id, Cliente datos) {
        Cliente existente = obtenerPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setApellido(datos.getApellido());
        existente.setEmail(datos.getEmail());
        existente.setTelefono(datos.getTelefono());
        existente.setDireccion(datos.getDireccion());
        return repository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        Cliente existente = obtenerPorId(id);
        existente.setActivo(false);
        repository.save(existente);
    }
}
