package ar.com.maydana.mushokucore.bootstrap;

import ar.com.maydana.mushokucore.module.ModuleLifecycle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Orden de enable/disable de los módulos — Fase 0 (agente F0, plan §2 "bootstrap/ModuleRegistry
 * [PROP] orden de enable/disable").
 *
 * <p>Habilita en el orden de registro (fijo: {@code ranks} → {@code diseases} → {@code commands},
 * plan §6.3: commands va último para poder {@code obtener(IPotenciasService)} e
 * {@code IEnfermedadesService}) y deshabilita en orden inverso.</p>
 *
 * <p>Un fallo al habilitar un módulo <b>no corta a los demás</b> y queda logueado en SEVERE
 * (fallo seguro §2.5). Si la política correcta es deshabilitar el plugin entero
 * (spec §8: catch → disablePlugin), la decide el integrador en Fase 2 — este scaffold
 * registra el error y sigue, para no fabricar un arranque "verde" que ocultó el fallo.</p>
 */
public final class ModuleRegistry {

    private final List<ModuleLifecycle> modulos = new ArrayList<>();
    private final List<String> habilitados = new ArrayList<>();
    private final Logger log;

    public ModuleRegistry(Logger log) {
        this.log = Objects.requireNonNull(log, "log");
    }

    /** Registra un módulo; el orden de llamada es el orden de enable. */
    public void registrar(ModuleLifecycle modulo) {
        modulos.add(Objects.requireNonNull(modulo, "modulo"));
    }

    /**
     * Habilita todos en orden. Cada fallo se loguea (SEVERE) y se continúa con el siguiente.
     * @return ids habilitados con éxito.
     */
    public List<String> habilitarTodos(PluginContext ctx) {
        habilitados.clear();
        for (ModuleLifecycle modulo : modulos) {
            try {
                modulo.enable(ctx);
                habilitados.add(modulo.id());
                log.info("Modulo '" + modulo.id() + "' habilitado.");
            } catch (Exception excepcion) {
                log.log(Level.SEVERE, "Modulo '" + modulo.id()
                        + "' NO se pudo habilitar; los demas siguen (fallo seguro §2.5).", excepcion);
            }
        }
        return List.copyOf(habilitados);
    }

    /** Deshabilita en orden inverso; cada fallo se loguea y se sigue con el resto (§8 L351-355). */
    public void deshabilitarTodos() {
        for (int i = modulos.size() - 1; i >= 0; i--) {
            ModuleLifecycle modulo = modulos.get(i);
            try {
                modulo.disable();
                log.info("Modulo '" + modulo.id() + "' deshabilitado.");
            } catch (Exception excepcion) {
                log.log(Level.SEVERE, "Modulo '" + modulo.id() + "' fallo al deshabilitar.", excepcion);
            }
        }
        habilitados.clear();
    }

    /** @return ids habilitados con éxito en la última corrida de {@link #habilitarTodos}. */
    public List<String> habilitados() {
        return List.copyOf(habilitados);
    }
}
