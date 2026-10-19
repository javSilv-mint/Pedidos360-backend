package cl.duoc.lab.admin.exception;

import cl.duoc.lab.admin.controller.RabbitAdminController;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = RabbitAdminController.class)
public class AdminExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AdminExceptionHandler.class);

    @ExceptionHandler(AdminOperationException.class)
    public ResponseEntity<ApiError> handleOperation(AdminOperationException ex, HttpServletRequest req) {
        return build(ex.getStatus(), ex.getMessage(), List.of(), req);
    }

    /** Cubre @Valid sobre body (MethodArgumentNotValidException) y sobre query params. */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleValidation(BindException ex, HttpServletRequest req) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .sorted()
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos", details, req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "El cuerpo no es un JSON válido o contiene un valor no permitido (revise 'type')",
                List.of(), req);
    }

    @ExceptionHandler(AmqpConnectException.class)
    public ResponseEntity<ApiError> handleBrokerDown(AmqpConnectException ex, HttpServletRequest req) {
        log.error("event=admin.broker.unavailable message={}", ex.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE, "RabbitMQ no está disponible", List.of(), req);
    }

    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<ApiError> handleBroker(AmqpException ex, HttpServletRequest req) {
        log.error("event=admin.broker.error message={}", ex.getMessage());
        return build(HttpStatus.BAD_GATEWAY, "RabbitMQ rechazó la operación: " + ex.getMessage(), List.of(), req);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, List<String> details,
                                           HttpServletRequest req) {
        ApiError body = new ApiError(Instant.now().toString(), status.value(),
                status.getReasonPhrase(), message, details, req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}