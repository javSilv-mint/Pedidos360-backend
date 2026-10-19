package cl.duoc.lab.pedidos;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/pedidos/eventos")
public class PedidoEventoController {

    private final PedidoEventPublisher publisher;

    public PedidoEventoController(PedidoEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> publicar(@Valid @RequestBody PedidoEventoRequest request) {
        publisher.publicar(request.pedidoId(), request.tipo(), request.emailCliente());
        return ResponseEntity.accepted().body(Map.of("estado", "publicado"));
    }
}