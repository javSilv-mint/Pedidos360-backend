package cl.duoc.lab.admin.controller;

import cl.duoc.lab.admin.dto.BindingRequest;
import cl.duoc.lab.admin.dto.ExchangeRequest;
import cl.duoc.lab.admin.dto.QueueInfoResponse;
import cl.duoc.lab.admin.dto.QueueRequest;
import cl.duoc.lab.admin.service.RabbitAdminService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rabbit-admin")
public class RabbitAdminController {

    private static final String BASE = "/api/rabbit-admin";

    private final RabbitAdminService service;

    public RabbitAdminController(RabbitAdminService service) {
        this.service = service;
    }

    // ---- Colas ----
    @PostMapping("/queues")
    public ResponseEntity<QueueInfoResponse> createQueue(@Valid @RequestBody QueueRequest request) {
        QueueInfoResponse created = service.createQueue(request);
        return ResponseEntity.created(URI.create(BASE + "/queues/" + created.name())).body(created);
    }

    @GetMapping("/queues/{name}")
    public QueueInfoResponse getQueue(@PathVariable String name) {
        return service.getQueue(name);
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Void> deleteQueue(@PathVariable String name) {
        service.deleteQueue(name);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/queues/{name}/purge")
    public Map<String, Object> purgeQueue(@PathVariable String name) {
        return Map.of("queue", name, "purgedMessages", service.purgeQueue(name));
    }

    // ---- Exchanges ----
    @PostMapping("/exchanges")
    public ResponseEntity<ExchangeRequest> createExchange(@Valid @RequestBody ExchangeRequest request) {
        service.createExchange(request);
        return ResponseEntity.created(URI.create(BASE + "/exchanges/" + request.name())).body(request);
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Void> deleteExchange(@PathVariable String name) {
        service.deleteExchange(name);
        return ResponseEntity.noContent().build();
    }

    // ---- Bindings ----
    @PostMapping("/bindings")
    public ResponseEntity<BindingRequest> createBinding(@Valid @RequestBody BindingRequest request) {
        service.createBinding(request);
        return ResponseEntity.status(201).body(request);
    }

    /** DELETE sin body (buena práctica REST): los datos viajan como query params. */
    @DeleteMapping("/bindings")
    public ResponseEntity<Void> deleteBinding(@Valid BindingRequest request) {
        service.deleteBinding(request);
        return ResponseEntity.noContent().build();
    }
}