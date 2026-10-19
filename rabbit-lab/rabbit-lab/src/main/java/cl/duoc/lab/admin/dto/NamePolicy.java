package  cl.duoc.lab.admin.dto;

import cl.duoc.lab.admin.exception.AdminOperationException;

/** Reglas únicas de validación de nombres: las usan los DTO y el servicio. */
public final class NamePolicy {

    public static final String NAME_REGEX = "^(?!amq\\.)[A-Za-z0-9._-]{1,255}$";
    public static final String NAME_MESSAGE =
            "Solo letras, números, '.', '_' y '-' (máx. 255 caracteres) y no puede comenzar con 'amq.'";

    public static final String ROUTING_KEY_REGEX = "^[A-Za-z0-9._*#-]{0,255}$";
    public static final String ROUTING_KEY_MESSAGE =
            "Solo letras, números, '.', '_', '-', '*' y '#' (máx. 255 caracteres)";

    private NamePolicy() {
    }

    public static void requireValidName(String name) {
        if (name == null || !name.matches(NAME_REGEX)) {
            throw AdminOperationException.badRequest("Nombre inválido '" + name + "'. " + NAME_MESSAGE);
        }
    }
}