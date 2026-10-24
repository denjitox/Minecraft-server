package ar.com.maydana.mushokucore.audit;

import ar.com.maydana.mushokucore.database.IDatabase;
import ar.com.maydana.mushokucore.util.TimeUtils;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Implementación de {@link IAuditoria} sobre la tabla {@code audit_log} (spec §10 L525-532)
 * — agente A (infra-core). Toda escritura de datos debe pasar por acá (spec §17 L829-836).
 *
 * <p>Garantías del contrato: el registro es <b>fire-and-forget</b> y <b>jamás lanza</b> al
 * llamador. Si el llamador está en el hilo principal, el INSERT se despacha a un task
 * async del scheduler — así no se viola el guard "cero SQL en hilo principal" (§26 L1039)
 * ni se rompe la firma síncrona de IAuditoria. Los fallos de escritura se loguean; no hay
 * reintentos (audit_log es evidencia, no estado operativo).</p>
 */
public final class AuditService implements IAuditoria {

    private final JavaPlugin plugin;
    private final IDatabase db;

    public AuditService(JavaPlugin plugin, IDatabase db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public void registrar(UUID actor, String accion, String target, String detalles) {
        try {
            if (Bukkit.isPrimaryThread()) {
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                        () -> escribir(actor, accion, target, detalles));
            } else {
                escribir(actor, accion, target, detalles);
            }
        } catch (Throwable absolutamenteTodo) {
            // Contrato IAuditoria: jamás explotar en el llamador (fallo seguro §2.5).
            plugin.getLogger().log(Level.WARNING,
                    "audit.registrar lanzó inesperadamente (" + accion + ")", absolutamenteTodo);
        }
    }

    private void escribir(UUID actor, String accion, String target, String detalles) {
        try {
            db.write(conexion -> {
                try (var preparada = conexion.prepareStatement(
                        "INSERT INTO audit_log (occurred_at, actor_uuid, action, target, details) "
                                + "VALUES (?,?,?,?,?)")) {
                    preparada.setString(1, TimeUtils.ahoraIso());
                    preparada.setString(2, actor == null ? null : actor.toString());
                    preparada.setString(3, accion);
                    preparada.setString(4, target);
                    preparada.setString(5, detalles);
                    preparada.executeUpdate();
                } catch (java.sql.SQLException excepcion) {
                    throw new java.util.concurrent.CompletionException(excepcion);
                }
            }).whenComplete((vacio, fallo) -> {
                if (fallo != null) {
                    plugin.getLogger().log(Level.WARNING,
                            "No se pudo escribir en audit_log (" + accion + ")", fallo);
                }
            });
        } catch (Throwable falloDeCola) {
            // p. ej. base cerrada durante apagado: se loguea y se sigue.
            plugin.getLogger().log(Level.FINE,
                    "No se pudo encolar audit_log (" + accion + ")", falloDeCola);
        }
    }
}
