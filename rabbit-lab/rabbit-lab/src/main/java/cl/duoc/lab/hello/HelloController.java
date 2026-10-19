package cl.duoc.lab.hello;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/hello")
public class HelloController {

    private final HelloSender sender;

    public HelloController(HelloSender sender) {
        this.sender = sender;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> send(@Valid @RequestBody HelloRequest request) {
        sender.send(request.message());
        return ResponseEntity.accepted().body(Map.of("estado", "enviado"));
    }
}