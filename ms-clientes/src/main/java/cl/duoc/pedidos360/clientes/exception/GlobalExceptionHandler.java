package cl.duoc.pedidos360.clientes.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Centraliza los errores de negocio y de entrada y devuelve codigos HTTP correctos
 * con un cuerpo JSON uniforme (mismo formato que JsonSecurityErrorHandler).
 *
 * 400 -> entrada invalida (validacion, tipo incorrecto, parametro faltante, JSON mal formado)
 * 403 -> recurso de otro usuario (control de propiedad, no de rol)
 * 404 -> recurso inexistente
 * 409 -> conflicto de estado o de unicidad (transicion invalida, stock insuficiente, duplicado)
 * Los 401/403 de autenticacion/rol los responde JsonSecurityErrorHandler en la cadena de filtros.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(NoSuchElementException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> peticionInvalida(IllegalArgumentException ex) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> conflicto(IllegalStateException ex) {
        return construir(HttpStatus.CONFLICT, ex.getMessage());
    }

    /** Violacion de UNIQUE/NOT NULL en BD: no se expone el detalle SQL al cliente. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integridad(DataIntegrityViolationException ex) {
        log.warn("Violacion de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return construir(HttpStatus.CONFLICT, "El recurso entra en conflicto con datos existentes");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> tipoInvalido(MethodArgumentTypeMismatchException ex) {
        return construir(HttpStatus.BAD_REQUEST,
                "Valor invalido para el parametro '" + ex.getName() + "': " + ex.getValue());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> parametroFaltante(MissingServletRequestParameterException ex) {
        return construir(HttpStatus.BAD_REQUEST,
                "Falta el parametro obligatorio '" + ex.getParameterName() + "'");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> cuerpoIlegible(HttpMessageNotReadableException ex) {
        return construir(HttpStatus.BAD_REQUEST, "El cuerpo de la peticion no es un JSON valido");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException ex) {
        Map<String, Object> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(e -> errores.put(e.getField(), e.getDefaultMessage()));
        Map<String, Object> body = base(HttpStatus.BAD_REQUEST, "Errores de validacion");
        body.put("errores", errores);
        return ResponseEntity.badRequest().body(body);
    }

    private ResponseEntity<Map<String, Object>> construir(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(base(status, mensaje));
    }

    private Map<String, Object> base(HttpStatus status, String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("mensaje", mensaje);
        return body;
    }
}
