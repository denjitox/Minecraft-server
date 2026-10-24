package ar.com.maydana.mushokucore.util;

import java.util.UUID;

/**
 * Helpers de UUID — agente A (infra-core), plan §2 {@code util/(TimeUtils, Uuids)}.
 * Identidad por UUID en todo (spec §2.4 L75-77); el nombre es solo presentación.
 */
public final class Uuids {

    private Uuids() {
    }

    /** @return el UUID parseado, o {@code null} si el texto no es un UUID válido (jamás lanza). */
    public static UUID parse(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(texto.trim());
        } catch (IllegalArgumentException excepcion) {
            return null;
        }
    }

    /** @return {@code uuid.toString()} o {@code null} si el UUID es null (para binds SQL). */
    public static String aTexto(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }

    /** true si el texto es un UUID válido (para validar argumentos de comando sin lanzar). */
    public static boolean esValido(String texto) {
        return parse(texto) != null;
    }
}
