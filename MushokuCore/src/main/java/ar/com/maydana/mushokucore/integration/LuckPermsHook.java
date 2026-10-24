package ar.com.maydana.mushokucore.integration;

import java.util.UUID;

/**
 * CONTRATO CONGELADO — Fase 0 (agente F0), plan §6.1. Nombre de clase del spec §7 L305.
 *
 * <p><b>Discrepancia interna del plan resuelta a favor de la regla 2 (§6):</b> la tabla §6.3
 * lista {@code integration/LuckPermsHook.java} como dueño del agente A, pero la regla 2 dice
 * que <i>los interfaces del §6.1 los escribe F0 y quedan congelados</i>. Este archivo es el
 * interface (F0). <b>A</b> escribe la implementación en un archivo nuevo (p. ej.
 * {@code LuckPermsService.java}) — avisar al integrador si se prefiero otro nombre.</p>
 *
 * <p>Sin LuckPerms, {@code disponible()} devuelve false y el módulo ranks degrada:
 * sigue iniciando pero no concede grupos (spec §2.5). DECISION D16 (2026-10-18): por eso
 * {@code LuckPerms} quedó como <b>softdepend</b> en plugin.yml, no como depend duro.</p>
 *
 * <p>[DECISIÓN D1 pendiente] grupos por defecto {@code potencia1..7} (compatibilidad con
 * {@code potencias.sk} L23/L27; el spec §11 L573 no los nombra).</p>
 */
public interface LuckPermsHook {

    /** @return true si LuckPerms está cargado y su API es utilizable. */
    boolean disponible();

    /**
     * Agrega al jugador al grupo (p. ej. {@code potencia5}).
     * @return true si la operación se aplicó; false si LP no está o falló (entra al outbox §11 L592).
     */
    boolean agregarGrupo(UUID jugador, String grupo);

    /** @return true si la operación se aplicó. */
    boolean quitarGrupo(UUID jugador, String grupo);

    /**
     * Consulta de permiso. Se usa p. ej. para la reducción de contagio de Orsted (§13 L687)
     * con el permiso de [DECISIÓN D11 pendiente], hoy {@code mt.alianza.orsted}.
     * @return false si LuckPerms no está disponible (fallo seguro §2.5).
     */
    boolean tienePermiso(UUID jugador, String permiso);
}
