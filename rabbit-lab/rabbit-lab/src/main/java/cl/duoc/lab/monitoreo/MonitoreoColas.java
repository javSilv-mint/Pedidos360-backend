package cl.duoc.lab.monitoreo;

import java.time.Instant;

/** Respuesta de GET /api/monitoreo/colas: cola principal, DLQ y estado de alerta. */
public record MonitoreoColas(ColaEstado principal, ColaEstado dlq, boolean alerta, int umbralAlerta, Instant fecha) {
}
