package ar.com.maydana.mushokucore.database.repository;

import java.sql.SQLException;
import java.util.concurrent.CompletionException;

/**
 * Helper para los lambdas SQL — agente A (infra-core).
 *
 * <p>Motivo: {@code IDatabase.write(Consumer)} y {@code read(Function)} no declaran
 * checked exceptions, pero {@code PreparedStatement} lanza {@link SQLException}. Envolverla
 * en {@link CompletionException} la mete dentro del {@link java.util.concurrent.CompletableFuture}:
 * el futuro termina en estado fallido y el error viaja a {@code whenComplete} del llamador
 * (nunca revienta el hilo de BD ni se pierde el stacktrace). Cumple "si algo falla, se
 * conserva el error completo".</p>
 */
final class Sql {

    private Sql() {
    }

    /** @return siempre una RuntimeException que envuelve la SQLException (para {@code throw}). */
    static CompletionException reenviar(SQLException excepcion) {
        return new CompletionException(excepcion);
    }
}
