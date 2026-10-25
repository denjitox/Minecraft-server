package ar.com.maydana.mushokucore.module.ranks;

import ar.com.maydana.mushokucore.integration.LuckPermsHook;
import ar.com.maydana.mushokucore.integration.TabListRefresher;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import org.bukkit.Bukkit;

/**
 * Outbox de LuckPerms — spec §11 L590-592 ("si LuckPerms falla, dejar una operación pendiente
 * para reintento; nunca perder la información de quién ganó"). Dueño: agente B (plan §6.3).
 *
 * <p>Persiste en {@code luckperms_outbox} (tabla [PROPUESTA] plan §3/V001). Su esquema tiene
 * un solo {@code player_uuid} por fila, así que <b>cada operación va en su propia fila</b>:
 * una fila con {@code group_add} para el nuevo dueño y otra con {@code group_remove} para el
 * anterior.</p>
 *
 * <p>El reintento lo dispara {@code LuckPermsOutboxRetryTask} (60 s, plan §5.3). Las llamadas
 * a LuckPerms ocurren en un <b>hilo asíncrono</b>: la implementación de A
 * ({@code LuckPermsService}) rechaza el hilo principal a propósito (usa {@code .get()} sobre
 * los futuros de la API de LP). Por eso {@link #solicitar} y {@link #reintentar()} despachan
 * con {@code runTaskAsynchronously}; la BD también solo se toca de forma asíncrona
 * (plan §6 regla 3, §26 L1039).</p>
 */
public final class LuckPermsOutbox {

    private final PluginContext ctx;
    private final TabListRefresher tablist;

    public LuckPermsOutbox(PluginContext ctx) {
        this.ctx = ctx;
        this.tablist = new TabListRefresher(ctx.plugin());
    }

    /**
     * Encola una operación pendiente. Nunca lanza al llamador: si la BD falla, se loguea
     * (la garantía de "nunca perder quién ganó" vive en {@code world_powers}; el outbox es
     * la capa de sincronización de grupos, no la fuente de verdad).
     *
     * @param jugador     jugador afectado.
     * @param groupAdd    grupo a conceder (ej. {@code potencia5}) o {@code null}.
     * @param groupRemove grupo a retirar o {@code null}.
     * @param motivoError motivo del encolado ("error_lp" o "lp_no_disponible").
     */
    public void encolar(UUID jugador, String groupAdd, String groupRemove, String motivoError) {
        String ahora = Instant.now().toString();
        ctx.db().write(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO luckperms_outbox(created_at, player_uuid, group_add, group_remove, attempts, last_error, done)"
                            + " VALUES (?,?,?,?,0,?,0)")) {
                ps.setString(1, ahora);
                ps.setString(2, jugador.toString());
                ps.setString(3, groupAdd);
                ps.setString(4, groupRemove);
                ps.setString(5, motivoError);
                ps.executeUpdate();
            } catch (java.sql.SQLException excepcion) {
                throw new java.util.concurrent.CompletionException(excepcion);
            }
        }).whenComplete((v, error) -> {
            if (error != null) {
                ctx.plugin().getLogger().log(Level.SEVERE,
                        "Outbox LP: no se pudo encolar " + groupAdd + "/" + groupRemove
                                + " para " + jugador, error);
            }
        });
    }

    /** Fila pendiente del outbox. */
    private record Pendiente(long id, UUID jugador, String groupAdd, String groupRemove, int intentos) {
    }

    /** Marca una fila como aplicada o incrementa intentos con el error. */
    private void marcar(long id, boolean ok, String errorMsg) {
        ctx.db().write(conn -> {
            try (PreparedStatement ps = ok
                    ? conn.prepareStatement("UPDATE luckperms_outbox SET done=1, last_error=NULL WHERE id=?")
                    : conn.prepareStatement("UPDATE luckperms_outbox SET attempts=attempts+1, last_error=? WHERE id=?")) {
                if (ok) {
                    ps.setLong(1, id);
                } else {
                    ps.setString(1, errorMsg);
                    ps.setLong(2, id);
                }
                ps.executeUpdate();
            } catch (java.sql.SQLException excepcion) {
                throw new java.util.concurrent.CompletionException(excepcion);
            }
        }).whenComplete((v, ex) -> {
            if (ex != null) {
                ctx.plugin().getLogger().log(Level.SEVERE, "Outbox LP: no se pudo marcar la fila " + id, ex);
            }
        });
    }

    /**
     * Reintenta las operaciones pendientes. Llamar desde tarea periódica (60 s, plan §5.3).
     * Si LuckPerms sigue sin estar disponible se sale sin tocar las filas (se reintenta en el
     * próximo ciclo). Cada fila se aplica en un hilo asíncrono; el resultado se persiste async.
     */
    public void reintentar() {
        // Sin .get()/join() en el hilo del servidor (plan §6 regla 3): la lectura es async y
        // la aplicación LP se despacha a otro hilo async.
        ctx.db().read(conn -> {
            List<Pendiente> filas = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, player_uuid, group_add, group_remove, attempts FROM luckperms_outbox"
                            + " WHERE done=0 AND attempts<50 ORDER BY id LIMIT 20");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new Pendiente(rs.getLong(1), UUID.fromString(rs.getString(2)),
                            rs.getString(3), rs.getString(4), rs.getInt(5)));
                }
            } catch (java.sql.SQLException excepcion) {
                throw new java.util.concurrent.CompletionException(excepcion);
            }
            return filas;
        }).thenAccept(pendientes -> {
            if (pendientes == null || pendientes.isEmpty()) {
                return;
            }
            despacharAsync(() -> aplicarPendientes(pendientes));
        }).whenComplete((v, error) -> {
            if (error != null) {
                ctx.plugin().getLogger().log(Level.WARNING,
                        "Outbox LP: lectura de pendientes falló; se reintenta en 60 s.", error);
            }
        });
    }

    /** Despacha a un hilo async; si el plugin ya se está apagando, no hay scheduler: se pierde el ciclo. */
    private void despacharAsync(Runnable tarea) {
        try {
            Bukkit.getScheduler().runTaskAsynchronously(ctx.plugin(), tarea);
        } catch (RuntimeException apagando) {
            // En onDisable el scheduler lanza IllegalPluginAccessException (RuntimeException, NO
            // IllegalStateException) al registrar tareas; no se propaga.
            ctx.plugin().getLogger().fine("Outbox LP: scheduler no disponible (apagado); "
                    + "el reintento queda para el próximo arranque.");
        }
    }

    private void aplicarPendientes(List<Pendiente> pendientes) {
        LuckPermsHook lp;
        try {
            lp = ctx.luckperms();
        } catch (IllegalStateException sinProveer) {
            // Wiring aún no entregó el hook: no encolar ni fallar, solo esperar al próximo ciclo.
            return;
        }
        if (!lp.disponible()) {
            return; // sigue caído: las filas quedan pendientes (§11 L592).
        }
        for (Pendiente fila : pendientes) {
            boolean ok = true;
            String ultimoError = null;
            try {
                if (fila.groupRemove() != null && !fila.groupRemove().isEmpty()) {
                    if (!lp.quitarGrupo(fila.jugador(), fila.groupRemove())) {
                        ok = false;
                        ultimoError = "quitarGrupo devolvio false";
                    }
                }
                if (ok && fila.groupAdd() != null && !fila.groupAdd().isEmpty()) {
                    if (!lp.agregarGrupo(fila.jugador(), fila.groupAdd())) {
                        ok = false;
                        ultimoError = "agregarGrupo devolvio false";
                    }
                }
            } catch (RuntimeException excepcion) {
                ok = false;
                ultimoError = excepcion.getClass().getSimpleName() + ": " + excepcion.getMessage();
            }
            marcar(fila.id(), ok, ultimoError);
            if (ok) {
                tablist.solicitar(fila.jugador()); // repinta el prefijo del tablist en el mismo ciclo
            } else {
                ctx.plugin().getLogger().warning("Outbox LP: reintento fallido para " + fila.jugador()
                        + " (" + fila.groupAdd() + "/" + fila.groupRemove() + "): " + ultimoError);
            }
        }
    }

    /**
     * Aplica de inmediato una operación LP <b>fuera del hilo principal</b> (la implementación
     * de A rechaza el hilo principal). Si LP no está o falla, la operación queda en el outbox
     * para reintento (§11 L592): nunca se pierde quién ganó (el dueño real vive en
     * {@code world_powers}).
     *
     * @param jugador     jugador afectado.
     * @param groupAdd    grupo a conceder (ej. {@code potencia5}) o {@code null}.
     * @param groupRemove grupo a retirar o {@code null}.
     * @param motivoError motivo con el que se encola si falla.
     */
    public void solicitar(UUID jugador, String groupAdd, String groupRemove, String motivoError) {
        despacharAsync(() -> aplicarAhoraOEncolar(jugador, groupAdd, groupRemove, motivoError));
    }

    /**
     * Aplica de inmediato una operación LP (sin pasar por el outbox). Si LP no está o falla,
     * <b>encola</b> para reintento (§11 L592). <b>Llamar solo desde un hilo asíncrono</b>
     * (la implementación de A rechaza el hilo principal): usar {@link #solicitar} desde
     * código que corre en el hilo del servidor.
     *
     * @return true si se aplicó ahora; false si quedó en el outbox.
     */
    public boolean aplicarAhoraOEncolar(UUID jugador, String groupAdd, String groupRemove, String motivoError) {
        LuckPermsHook lp;
        try {
            lp = ctx.luckperms();
        } catch (IllegalStateException sinProveer) {
            encolar(jugador, groupAdd, groupRemove, "hook_sin_proveer");
            return false;
        }
        if (!lp.disponible()) {
            encolar(jugador, groupAdd, groupRemove, "lp_no_disponible");
            return false;
        }
        try {
            boolean ok = true;
            if (groupRemove != null && !groupRemove.isEmpty()) {
                ok = lp.quitarGrupo(jugador, groupRemove);
            }
            if (ok && groupAdd != null && !groupAdd.isEmpty()) {
                ok = lp.agregarGrupo(jugador, groupAdd);
            }
            if (!ok) {
                encolar(jugador, groupAdd, groupRemove, motivoError);
            } else {
                tablist.solicitar(jugador); // prefijo nuevo en el tablist sin esperar el refresco
            }
            return ok;
        } catch (RuntimeException excepcion) {
            encolar(jugador, groupAdd, groupRemove,
                    excepcion.getClass().getSimpleName() + ": " + excepcion.getMessage());
            return false;
        }
    }
}
