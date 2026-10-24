package ar.com.maydana.mushokucore.model;

import java.util.UUID;

/**
 * Contexto mínimo para resolver un placeholder — Fase 0 (agente F0).
 * <p>Reemplaza al tipo {@code PlaceholderAPIAPIContextual} que el plan §6.1 escribía por
 * error (no existe; ver {@code module/PlaceholderFuente.java}). El contrato de placeholders
 * así no depende de PlaceholderAPI: esa dependencia queda en {@code integration/PlaceholderHook}
 * (de D), que traduce la API de PAPI a este contexto.</p>
 *
 * @param jugador UUID del destinatario, o {@code null} cuando el render no tiene jugador
 *                (TAB global, holograma, consola).
 */
public record PlaceholderContext(UUID jugador) {
}
