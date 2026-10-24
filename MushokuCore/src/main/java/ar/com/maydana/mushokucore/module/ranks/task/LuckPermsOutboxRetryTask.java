package ar.com.maydana.mushokucore.module.ranks.task;

import ar.com.maydana.mushokucore.module.ranks.LuckPermsOutbox;

/**
 * Reintento periódico de las operaciones de LuckPerms pendientes — spec §11 L592 ("si
 * LuckPerms falla, dejar una operación pendiente para reintento; nunca perder la información
 * de quién ganó"). Dueño: agente B (plan §5.3: cada 60 s).
 *
 * <p>El vuelco real vive en {@link LuckPermsOutbox#reintentar()}: lee las filas pendientes de
 * {@code luckperms_outbox} (async), y si LuckPerms está disponible aplica y marca, todo en
 * hilos asíncronos. Esta tarea solo dispara el ciclo desde el scheduler asíncrono.</p>
 */
public final class LuckPermsOutboxRetryTask implements Runnable {

    private final LuckPermsOutbox outbox;

    public LuckPermsOutboxRetryTask(LuckPermsOutbox outbox) {
        this.outbox = outbox;
    }

    @Override
    public void run() {
        outbox.reintentar();
    }
}
