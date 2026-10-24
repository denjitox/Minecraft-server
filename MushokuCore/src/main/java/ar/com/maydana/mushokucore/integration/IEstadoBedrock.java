package ar.com.maydana.mushokucore.integration;

import java.util.UUID;

/**
 * CONTRATO CONGELADO — Fase 0 (agente F0), plan §6.1.
 * <p>Implementación: agente <b>A (infra-core)</b> en {@code FloodgateHook.java} (API de
 * Floodgate, compileOnly). Este interface no se edita sin actualizar el plan (plan §6, regla 2).</p>
 *
 * <p>La capa Bedrock es opcional: si Floodgate no está cargado, todo responde como
 * "no Bedrock" y los comandos/texto siguen siendo el fallback (spec §2.5, §16 L792).
 * Sin formularios en MVP: {@code BedrockFormUi} es 1.3 (spec §3 L132).</p>
 */
public interface IEstadoBedrock {

    /**
     * @return true si el UUID corresponde a un jugador Bedrock (vía Floodgate).
     *         false si Floodgate ausente o el jugador es Java (fallo seguro §2.5).
     */
    boolean esBedrock(UUID jugador);

    /**
     * @return true si Floodgate está cargado y su API responde.
     */
    boolean floodgateDisponible();
}
