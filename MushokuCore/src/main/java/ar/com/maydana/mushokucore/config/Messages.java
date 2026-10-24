package ar.com.maydana.mushokucore.config;

import java.util.Map;

/**
 * Acceso tipado a messages.yml — Fase 0 (agente F0).
 *
 * <p><b>Discrepancia interna del plan, anotada:</b> la regla 2 del plan §6 hace congelar los
 * interfaces del §6.1, y {@code PluginContext.messages()} necesita el tipo {@code Messages};
 * pero la tabla §6.3 da el archivo {@code Messages.java} al agente A. Resolución de Fase 0:
 * este archivo queda como interface (contrato) y <b>A</b> implementa la lectura en una clase
 * nueva (p. ej. {@code MessagesService.java}). Si el integrador (Fase 2) prefiere otra
 * estructura, se actualiza el plan §6.3 y se avisa — no editar este archivo por cuenta propia.</p>
 *
 * <p>Las claves son rutas anidadas del YAML, p. ej. {@code "potencias.list-header"},
 * {@code "generic.no-permission"}, {@code "enfermedad.infected"} (esqueleto: plan §4.3;
 * el spec no define contenido — [PROPUESTA] los textos conservan los del Skript actual).</p>
 */
public interface Messages {

    /**
     * Texto de la clave.
     * [PROPUESTA F0] Si la clave no existe devuelve la propia clave (nunca null —
     * fallo seguro §2.5, un texto faltante no puede tumbar un comando).
     */
    String texto(String clave);

    /**
     * Texto de la clave con reemplazos {@code {clave} -> valor}, p. ej.
     * {@code {jugador}} → nombre del jugador. Se aplican en el orden del mapa
     * (el esqueleto de messages.yml usa {puesto}, {titulo}, {propietario}, {modulos}…).
     */
    String texto(String clave, Map<String, String> reemplazos);
}
