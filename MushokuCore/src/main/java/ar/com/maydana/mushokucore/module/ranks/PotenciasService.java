package ar.com.maydana.mushokucore.module.ranks;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.database.repository.BossDamageRepository;
import ar.com.maydana.mushokucore.database.repository.PowerRepository;
import ar.com.maydana.mushokucore.database.repository.PvpRepository;
import ar.com.maydana.mushokucore.model.DueñoPuesto;
import ar.com.maydana.mushokucore.util.TimeUtils;
import ar.com.maydana.mushokucore.util.Uuids;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Implementación de {@link IPotenciasService} — agente B (potencias), plan §6.1: "F0 lo declara,
 * B lo implementa en una clase aparte".
 *
 * <p><b>Qué hace:</b> mantiene la <b>caché</b> de {@code world_powers} (lecturas sin SQL por
 * render, §18 L856/§19 L874), persiste asignaciones con la transacción idempotente por revisión
 * (§11 L575, plan §6 regla 5), publica el cambio de grupos en LuckPerms vía
 * {@link LuckPermsOutbox} (§11 L572-573/L590-592) y resuelve la primera muerte válida de cada
 * jefe (§11 L563-571) con {@link BossKillResolver} (regla pura).</p>
 *
 * <p><b>Hilos (plan §6 regla 3, §26 L1039):</b> cero SQL en el hilo principal. El guard de
 * {@code DatabaseManager} <b>lanza</b> si {@code read}/{@code write} corren en el hilo
 * principal, así que el servicio es invocable desde cualquier hilo: toda <i>iniciación</i> de
 * I/O pasa por {@link #enHiloAsync}, que la despacha al scheduler asíncrono cuando hace falta
 * (y corre en línea si ya estamos fuera del principal). La caché se actualiza de forma
 * <i>optimista</i> bajo {@link #candado} — así {@code asignar()} puede devolver el boolean
 * síncrono de la firma congelada sin bloquear — y la escritura va a la cola de
 * {@code IDatabase}; si la transacción afecta 0 filas (otro proceso ganó) o falla, se recarga
 * la caché desde disco y queda logueado. Las mutaciones de Bukkit (anuncio global, LuckPerms)
 * se despachan de vuelta al hilo principal donde corresponde.</p>
 *
 * <p><b>Semántica de "libre" (la fija B, ver javadoc de {@link IPotenciasService}):</b>
 * {@link #propietario(int)} devuelve {@code Optional.empty()} cuando el puesto es inválido o
 * está libre; cuando hay dueño, el record trae {@code dueno != null}.</p>
 *
 * <p><b>[PENDIENTE D1]</b> grupos {@code potencia1..7} (compatibilidad con {@code potencias.sk};
 * la propuesta del plan §9). <b>DECISION D7 (2026-10-18):</b> {@code ranks.valid-boss-worlds} =
 * {@code [world]}; si la lista quedara vacía, sólo el mundo principal ({@code world.name}) vale
 * para jefes. <b>[D6 decidido 2026-10-18 / D8 pendiente]</b> se resuelven
 * en {@link PvPKillValidator}/{@link PvPKillListener}, no acá.</p>
 */
public final class PotenciasService implements IPotenciasService {

    /**
     * Títulos de los 7 puestos — spec §11 L541-548, textual.
     * Van en código y no en config porque el spec los fija como constantes del sistema.
     */
    private static final Map<Integer, String> TITULOS = Map.of(
            1, "Dios de la Técnica",
            2, "Dios Dragón",
            3, "Dios de la Lucha",
            4, "Dios Demonio",
            5, "Dios de la Muerte",
            6, "Dios de la Espada",
            7, "Dios del Norte");

    /** Mapa jefe → puesto (§11 L541-548): Warden→1, Ender Dragon→2, Wither→3. */
    private static final Map<String, Integer> PUESTO_DE_JEFE = Map.of(
            "WARDEN", 1,
            "ENDER_DRAGON", 2,
            "WITHER", 3);

    /** Puesto que se recalcula con las bajas PvP válidas (§11 L588). */
    private static final int PUESTO_PVP = 5;

    /** Instantánea de un puesto en caché; {@code dueno == null} = libre. */
    private record Asiento(int posicion, String titulo, UUID dueno, String nombreDueno, int revision) {
    }

    /** Líder del ranking de bajas válidas leído de {@code pvp_score}. */
    private record Lider(UUID jugador, int kills) {
    }

    private final PluginContext ctx;
    private final PowerRepository powers;
    private final BossDamageRepository bossDamage;
    private final PvpRepository pvp;
    private final ContributionTracker tracker;
    private final LuckPermsOutbox outbox;

    private final Object candado = new Object();
    private final Map<Integer, Asiento> asientos = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> puntuaciones = new ConcurrentHashMap<>();

    /** Nombres vistos por {@link #nombreDe} en el hilo principal; las resoluciones async los leen. */
    private final Map<UUID, String> nombres = new ConcurrentHashMap<>();

    /** Cableado por {@link PotenciasModule}: vuelca el buffer de daño ya mismo (§19 L864). */
    private volatile Runnable flushInmediato;
    private volatile boolean cacheLista;

    public PotenciasService(PluginContext ctx, PowerRepository powers, BossDamageRepository bossDamage,
                            PvpRepository pvp, ContributionTracker tracker, LuckPermsOutbox outbox) {
        this.ctx = ctx;
        this.powers = powers;
        this.bossDamage = bossDamage;
        this.pvp = pvp;
        this.tracker = tracker;
        this.outbox = outbox;
    }

    /** Lo llama el módulo con {@code BossBufferFlushTask::flushAhora} (evita el ciclo de dependencias). */
    public void setFlushInmediato(Runnable flushInmediato) {
        this.flushInmediato = flushInmediato;
    }

    // ------------------------------------------------------------------
    // Ciclo de vida (llamado por PotenciasModule)
    // ------------------------------------------------------------------

    /**
     * Siembra las 7 filas de {@code world_powers} si faltan (V001 crea la tabla vacía) y carga
     * la caché de puestos y de bajas válidas; después restaura el buffer de daño desde
     * {@code boss_damage} (§26 L1038). Todo asíncrono.
     */
    public void cargarCache() {
        enHiloAsync(this::cargarCacheAsync);
    }

    private void cargarCacheAsync() {
        sembrarPuestos()
                .thenCompose(vacio -> powers.todos())
                .thenAccept(this::aplicarFilas)
                .whenComplete((vacio, fallo) -> {
                    if (fallo != null) {
                        log().log(Level.SEVERE, "Potencias: no se pudo cargar la cache de world_powers. "
                                + "asignar()/liberar() quedan inoperantes hasta el proximo arranque.", fallo);
                        return;
                    }
                    cacheLista = true;
                    log().info("Potencias: cache de puestos cargada (" + asientos.size() + " filas).");
                    cargarPuntuaciones();
                    restaurarBufferDeDanio();
                });
    }

    /** true si la caché de puestos ya se cargó desde disco (si no, no se asigna nada). */
    public boolean cacheLista() {
        return cacheLista;
    }

    private void aplicarFilas(List<PowerRepository.Fila> filas) {
        synchronized (candado) {
            asientos.clear();
            for (PowerRepository.Fila fila : filas) {
                asientos.put(fila.posicion(), new Asiento(fila.posicion(),
                        fila.titulo() == null || fila.titulo().isBlank()
                                ? tituloDe(fila.posicion()) : fila.titulo(),
                        fila.duenio(), fila.duenioNombre(), fila.revision()));
            }
        }
    }

    /** Insert defensivo de las 7 filas con los títulos del spec; nunca pisa una fila existente. */
    private CompletableFuture<Void> sembrarPuestos() {
        // La V001 (de F0) crea world_powers vacía; el sembrado de los 7 títulos es de B (§11 L541-548).
        return ctx.db().write(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT OR IGNORE INTO world_powers(position, title, revision) VALUES (?,?,0)")) {
                for (Map.Entry<Integer, String> titulo : TITULOS.entrySet()) {
                    ps.setInt(1, titulo.getKey());
                    ps.setString(2, titulo.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            } catch (SQLException excepcion) {
                throw new CompletionException(excepcion);
            }
        });
    }

    private void cargarPuntuaciones() {
        ctx.db().read(conn -> {
            List<Lider> filas = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT player_uuid, valid_kills FROM pvp_score");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID jugador = Uuids.parse(rs.getString(1));
                    if (jugador != null) {
                        filas.add(new Lider(jugador, rs.getInt(2)));
                    }
                }
            } catch (SQLException excepcion) {
                throw new CompletionException(excepcion);
            }
            return filas;
        }).whenComplete((filas, fallo) -> {
            if (fallo != null) {
                log().log(Level.WARNING, "Potencias: no se pudo cargar pvp_score; "
                        + "bajasValidas() arranca en 0 hasta la primera baja de la sesion.", fallo);
                return;
            }
            for (Lider fila : filas) {
                puntuaciones.put(fila.jugador(), fila.kills());
            }
        });
    }

    private void restaurarBufferDeDanio() {
        ctx.db().read(conn -> {
            List<Object[]> filas = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT boss_uuid, boss_type, player_uuid, damage, last_hit_at FROM boss_damage");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getDouble(4), rs.getString(5)});
                }
            } catch (SQLException excepcion) {
                throw new CompletionException(excepcion);
            }
            return filas;
        }).whenComplete((filas, fallo) -> {
            if (fallo != null) {
                log().log(Level.WARNING, "Potencias: no se pudo restaurar el buffer de danio a jefes.", fallo);
                return;
            }
            int restauradas = 0;
            for (Object[] fila : filas) {
                UUID bossUuid = Uuids.parse((String) fila[0]);
                UUID jugador = Uuids.parse((String) fila[2]);
                Instant ultimo = TimeUtils.parse((String) fila[4]);
                if (bossUuid == null || jugador == null) {
                    continue;
                }
                // El mundo no está en boss_damage: la validez de mundo se decide recién en la
                // muerte, con el mundo de la entidad (spec §11 L565).
                tracker.restaurar(bossUuid, (String) fila[1], "", jugador,
                        (Double) fila[3], ultimo == null ? System.currentTimeMillis() : ultimo.toEpochMilli());
                restauradas++;
            }
            if (restauradas > 0) {
                log().info("Potencias: buffer de danio restaurado desde boss_damage (" + restauradas
                        + " filas de " + tracker.peleasAbiertas() + " jefes).");
            }
        });
    }

    // ------------------------------------------------------------------
    // IPotenciasService — lecturas de caché
    // ------------------------------------------------------------------

    @Override
    public Optional<DueñoPuesto> propietario(int puesto) {
        if (puesto < 1 || puesto > 7) {
            return Optional.empty();
        }
        Asiento asiento = asientos.get(puesto);
        if (asiento == null || asiento.dueno() == null) {
            return Optional.empty(); // libre o caché no cargada: el llamador muestra "No conquistado"
        }
        return Optional.of(new DueñoPuesto(puesto, asiento.titulo(),
                asiento.dueno(), asiento.nombreDueno()));
    }

    @Override
    public int posicionDelJugador(UUID jugador) {
        if (jugador == null) {
            return 0;
        }
        for (Asiento asiento : asientos.values()) {
            if (jugador.equals(asiento.dueno())) {
                return asiento.posicion();
            }
        }
        return 0;
    }

    @Override
    public String tituloDe(int puesto) {
        String titulo = TITULOS.get(puesto);
        return titulo == null ? "" : titulo;
    }

    @Override
    public int bajasValidas(UUID jugador) {
        return jugador == null ? 0 : puntuaciones.getOrDefault(jugador, 0);
    }

    // ------------------------------------------------------------------
    // IPotenciasService — escrituras
    // ------------------------------------------------------------------

    @Override
    public boolean asignar(int puesto, UUID jugador, String motivo, UUID actorAdmin) {
        return asignarInterno(puesto, jugador, motivo, actorAdmin, false);
    }

    /**
     * Asignación automática (muerte de jefe, §11 L564: "comprobar que el puesto todavía esté
     * libre"): no pisa a un dueño existente. No está en el interface congelado.
     */
    public boolean asignarSiLibre(int puesto, UUID jugador, String motivo) {
        return asignarInterno(puesto, jugador, motivo, null, true);
    }

    @Override
    public boolean liberar(int puesto, String motivo, UUID actorAdmin) {
        if (puesto < 1 || puesto > 7) {
            return false;
        }
        Asiento anterior;
        synchronized (candado) {
            anterior = asientos.get(puesto);
            if (anterior == null) {
                log().warning("Potencias.liberar(" + puesto + "): cache no cargada; se ignora.");
                return false;
            }
            if (anterior.dueno() == null) {
                return false; // ya estaba libre
            }
            asientos.put(puesto, new Asiento(puesto, anterior.titulo(), null, null, anterior.revision() + 1));
        }
        String titulo = anterior.titulo();
        UUID duenoAnterior = anterior.dueno();

        enHiloAsync(this::flushInmediato); // §19 L864: vaciar datos críticos al cambiar un puesto
        enHiloAsync(() -> powers.liberar(puesto, anterior.revision(), motivo)
                .whenComplete((aplicado, fallo) -> {
                    if (fallo != null) {
                        log().log(Level.SEVERE, "Potencias.liberar(" + puesto + "): la transaccion fallo; "
                                + "se recarga la cache.", fallo);
                        recargarCache();
                    } else if (!Boolean.TRUE.equals(aplicado)) {
                        log().warning("Potencias.liberar(" + puesto + "): 0 filas afectadas "
                                + "(otra escritura gano); se recarga la cache.");
                        recargarCache();
                    }
                }));

        outbox.solicitar(duenoAnterior, null, grupoDe(puesto), "error_lp");
        ctx.audit().registrar(actorAdmin, "potencia.liberar", "puesto=" + puesto,
                "titulo=" + titulo + "; dueno_anterior=" + duenoAnterior + "; motivo=" + motivo);
        log().info("Potencia " + puesto + " (" + titulo + ") liberada por " + etiqueta(actorAdmin)
                + ": " + motivo);
        return true;
    }

    private boolean asignarInterno(int puesto, UUID jugador, String motivo, UUID actorAdmin,
                                   boolean soloSiLibre) {
        if (puesto < 1 || puesto > 7 || jugador == null) {
            return false;
        }
        Asiento anterior;
        Asiento nuevo;
        synchronized (candado) {
            anterior = asientos.get(puesto);
            if (anterior == null) {
                log().warning("Potencias.asignar(" + puesto + "): cache no cargada; se ignora.");
                return false;
            }
            if (soloSiLibre && anterior.dueno() != null) {
                return false; // el puesto ya tiene dueño: no es la primera muerte válida (§11 L564)
            }
            if (jugador.equals(anterior.dueno())) {
                return false; // idempotencia: ya es el dueño, no se duplica el evento
            }
            nuevo = new Asiento(puesto, anterior.titulo(), jugador, nombreDe(jugador),
                    anterior.revision() + 1);
            asientos.put(puesto, nuevo); // optimista: la caché queda consistente enseguida
        }

        String titulo = anterior.titulo();
        UUID duenoAnterior = anterior.dueno();

        enHiloAsync(this::flushInmediato); // §19 L864: vaciar datos críticos al cambiar un puesto
        enHiloAsync(() -> powers.asignar(puesto, titulo, jugador, nuevo.nombreDueno(), motivo,
                anterior.revision(), TimeUtils.ahoraIso()).whenComplete((aplicado, fallo) -> {
                    if (fallo != null) {
                        log().log(Level.SEVERE, "Potencias.asignar(" + puesto + "): la transaccion fallo; "
                                + "se recarga la cache (la cache optimista pudo haber quedado sucia).", fallo);
                        recargarCache();
                    } else if (!Boolean.TRUE.equals(aplicado)) {
                        log().warning("Potencias.asignar(" + puesto + "): 0 filas afectadas "
                                + "(otra escritura gano la carrera); se recarga la cache.");
                        recargarCache();
                    }
                }));

        // §11 L572-573 "cambio de propietario": retirar el grupo anterior y conceder el nuevo.
        // [PENDIENTE D1] nombres de grupo potencia1..7.
        if (duenoAnterior != null && !duenoAnterior.equals(jugador)) {
            outbox.solicitar(duenoAnterior, null, grupoDe(puesto), "error_lp");
        }
        outbox.solicitar(jugador, grupoDe(puesto), null, "error_lp");

        ctx.audit().registrar(actorAdmin, "potencia.asignar", "puesto=" + puesto,
                "titulo=" + titulo + "; nuevo=" + jugador + "; anterior=" + duenoAnterior
                        + "; motivo=" + motivo);
        log().info("Potencia " + puesto + " (" + titulo + ") asignada a " + jugador
                + " (anterior=" + duenoAnterior + "; " + etiqueta(actorAdmin) + "): " + motivo);
        anunciar(puesto, jugador, titulo);
        return true;
    }

    /** Recarga la caché de puestos desde disco (tras un fallo o una carrera de escritura). */
    private void recargarCache() {
        powers.todos().thenAccept(this::aplicarFilas).whenComplete((vacio, fallo) -> {
            if (fallo != null) {
                log().log(Level.SEVERE, "Potencias: la recarga de la cache tambien fallo.", fallo);
            }
        });
    }

    // ------------------------------------------------------------------
    // Resolución de jefes (§11 L552-575) y bajas PvP (§11 L577-588)
    // ------------------------------------------------------------------

    /**
     * Muerte de un jefe (§11 L563): vuelca el buffer de ese jefe (§19 L864), cierra la pelea,
     * verifica mundo permitido y puesto libre, elige al ganador con {@link BossKillResolver} y
     * asigna de forma idempotente. Los datos de la pelea se borran al final: la carrera terminó.
     *
     * <p>Se llama desde el hilo principal ({@code EntityDeathEvent}); la memoria del buffer se
     * captura antes de cualquier I/O, así que ningún golpe se pierde (riesgo técnico del plan §11).</p>
     */
    public void resolverMuerteDeJefe(UUID bossUuid, String tipoJefe, String mundo) {
        if (bossUuid == null || tipoJefe == null) {
            return;
        }
        Integer puesto = PUESTO_DE_JEFE.get(tipoJefe);
        if (puesto == null) {
            return;
        }
        // Captura de memoria en el hilo llamador, antes de cualquier I/O: así ningún golpe se
        // pierde aunque el volcado se despache al hilo asíncrono (§11 L563).
        List<ContributionTracker.GolpeAcumulado> pendientes = tracker.drenarParaVuelco(bossUuid);
        ContributionTracker.Pelea pelea = tracker.cerrar(bossUuid);
        // Cachea los nombres acá, en el hilo principal: la resolución async no puede tocar Bukkit
        // y el ganador se guarda como dueño con su nombre en /potencias.
        if (pelea != null) {
            for (UUID jugador : pelea.danioPorJugador().keySet()) {
                nombreDe(jugador);
            }
        }
        int puestoResuelto = puesto;
        enHiloAsync(() -> resolverMuerteDeJefeAsync(bossUuid, tipoJefe, mundo, puestoResuelto,
                pendientes, pelea));
    }

    private void resolverMuerteDeJefeAsync(UUID bossUuid, String tipoJefe, String mundo, int puesto,
                                           List<ContributionTracker.GolpeAcumulado> pendientes,
                                           ContributionTracker.Pelea pelea) {
        volcar(pendientes); // §19 L864: dejar el registro al día antes de resolver
        if (pelea == null || pelea.danioPorJugador().isEmpty()) {
            log().fine("Muerte de " + tipoJefe + " sin contribuciones registradas (" + bossUuid + ").");
            bossDamage.borrarJefe(bossUuid.toString());
            return;
        }
        if (!mundoValidoParaJefes(mundo)) {
            // DECISION D7: valid-boss-worlds = [world]. No se fabrica una asignación en un mundo
            // no permitido (§11 L566).
            log().info("Muerte de " + tipoJefe + " en mundo no permitido ('" + mundo
                    + "'); no se asigna la Potencia " + puesto + ".");
            ctx.audit().registrar(null, "potencia.boss_mundo_invalido", "puesto=" + puesto,
                    "jefe=" + tipoJefe + "; mundo=" + mundo);
            bossDamage.borrarJefe(bossUuid.toString());
            return;
        }

        Asiento actual = asientos.get(puesto);
        if (actual == null) {
            log().warning("Potencias: cache no cargada; la muerte de " + tipoJefe
                    + " no se resuelve (los datos quedan en boss_damage).");
            return; // sin caché no se puede comprobar que el puesto esté libre: no se decide
        }
        if (actual.dueno() != null) {
            log().fine("Potencia " + puesto + " ya tiene dueno (" + actual.dueno()
                    + "); la muerte de " + tipoJefe + " no es la primera válida.");
            ctx.audit().registrar(null, "potencia.boss_segunda_muerte", "puesto=" + puesto,
                    "jefe=" + tipoJefe + "; dueno=" + actual.dueno());
            bossDamage.borrarJefe(bossUuid.toString());
            return;
        }

        List<BossKillResolver.Contribucion> aportes = new ArrayList<>(pelea.danioPorJugador().size());
        for (Map.Entry<UUID, Double> aporte : pelea.danioPorJugador().entrySet()) {
            aportes.add(new BossKillResolver.Contribucion(aporte.getKey(), aporte.getValue(),
                    pelea.ultimoGolpeMs().getOrDefault(aporte.getKey(), 0L)));
        }
        Optional<BossKillResolver.Contribucion> ganador =
                BossKillResolver.ganador(aportes, ctx.config().contribucionMinimaPorcentaje());
        if (ganador.isEmpty()) {
            log().info("Sin ganador válido para la Potencia " + puesto + " (todos por debajo del "
                    + "mínimo del " + ctx.config().contribucionMinimaPorcentaje() + "%).");
            ctx.audit().registrar(null, "potencia.sin_ganador", "puesto=" + puesto,
                    "jefe=" + tipoJefe + "; aportes=" + aportes.size());
            bossDamage.borrarJefe(bossUuid.toString());
            return;
        }
        UUID ganadorUuid = ganador.get().jugador();
        String motivo = "muerte_valida:" + tipoJefe + ":" + bossUuid;
        boolean asignado = asignarSiLibre(puesto, ganadorUuid, motivo);
        if (!asignado) {
            log().warning("Potencias: la muerte de " + tipoJefe + " no asignó la Potencia " + puesto
                    + " (otra escritura ganó o el dueño cambió); queda como segunda muerte válida.");
            ctx.audit().registrar(null, "potencia.muerte_sin_asignar", "puesto=" + puesto,
                    "jefe=" + tipoJefe + "; candidato=" + ganadorUuid);
        }
        bossDamage.borrarJefe(bossUuid.toString());
    }

    /**
     * Baja PvP ya validada por {@link PvPKillListener}: suma el punto, registra el cooldown de
     * la pareja (§11 L580) y recalcula el puesto 5 (§11 L588).
     */
    public void registrarBajaValida(UUID atacante, UUID victima, String mundo, long ahoraMs) {
        if (atacante == null || victima == null) {
            return;
        }
        puntuaciones.merge(atacante, 1, Integer::sum); // optimista, en memoria
        nombreDe(atacante); // el recálculo del puesto 5 corre async y lee el nombre de esta cache
        enHiloAsync(() -> registrarBajaValidaAsync(atacante, victima, mundo, ahoraMs));
    }

    private void registrarBajaValidaAsync(UUID atacante, UUID victima, String mundo, long ahoraMs) {
        String ahora = Instant.ofEpochMilli(ahoraMs).toString();
        CompletableFuture<Void> suma = pvp.sumarBaja(atacante, ahora);
        CompletableFuture<Void> cooldown = pvp.registrarAsesinato(atacante, victima, ahora);
        ctx.audit().registrar(atacante, "pvp.baja_valida", victima.toString(),
                "mundo=" + mundo + "; bajas_totales=" + bajasValidas(atacante));
        CompletableFuture.allOf(suma, cooldown).whenComplete((vacio, fallo) -> {
            if (fallo != null) {
                log().log(Level.SEVERE, "Potencias: no se pudo persistir la baja PvP "
                        + atacante + " -> " + victima + "; la cache optimista pudo quedar sucia.", fallo);
                return;
            }
            recalcularPuesto5("baja_pvp");
        });
    }

    /**
     * Recalcula el puesto 5 (Dios de la Muerte) con el ranking de bajas válidas (§11 L588).
     * Si nadie tiene bajas válidas, no se asigna ni se libera nada (no se inventa un dueño).
     * Desempate: {@code valid_kills DESC, updated_at ASC} [A VERIFICAR — §11 no fija el desempate
     * del puesto 5; se usa "el primero en registrar ese total", análogo a §11 L570].
     */
    public void recalcularPuesto5(String motivo) {
        enHiloAsync(() -> recalcularPuesto5Async(motivo));
    }

    private void recalcularPuesto5Async(String motivo) {
        ctx.db().read(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT player_uuid, valid_kills FROM pvp_score "
                            + "ORDER BY valid_kills DESC, updated_at ASC LIMIT 1");
                 ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new Lider(Uuids.parse(rs.getString(1)), rs.getInt(2));
            } catch (SQLException excepcion) {
                throw new CompletionException(excepcion);
            }
        }).whenComplete((lider, fallo) -> {
            if (fallo != null) {
                log().log(Level.WARNING, "Potencias: recálculo del puesto 5 falló; se reintenta en la "
                        + "próxima reconciliación.", fallo);
                return;
            }
            if (lider == null || lider.jugador() == null || lider.kills() <= 0) {
                return; // sin bajas válidas no hay dueño que asignar
            }
            puntuaciones.put(lider.jugador(), lider.kills());
            Asiento actual = asientos.get(PUESTO_PVP);
            if (actual == null || lider.jugador().equals(actual.dueno())) {
                return;
            }
            log().info("Puesto 5 recalcula a " + lider.jugador() + " (" + lider.kills()
                    + " bajas válidas, motivo=" + motivo + ").");
            asignar(PUESTO_PVP, lider.jugador(), motivo, null);
        });
    }

    /** Vuelca ya mismo el buffer de daño pendiente (§19 L864); el vuelco arranca en hilo asíncrono. */
    public void volcarPendientes() {
        enHiloAsync(this::flushInmediato);
    }

    private void flushInmediato() {
        Runnable tarea = flushInmediato;
        if (tarea == null) {
            return;
        }
        try {
            tarea.run();
        } catch (RuntimeException excepcion) {
            log().log(Level.WARNING, "Potencias: el vuelco inmediato del buffer falló.", excepcion);
        }
    }

    /** Envía los deltas al upsert aditivo de {@code boss_damage} (hilo de BD, orden garantizado). */
    private void volcar(List<ContributionTracker.GolpeAcumulado> pendientes) {
        for (ContributionTracker.GolpeAcumulado golpe : pendientes) {
            bossDamage.sumarDanio(golpe.bossUuid().toString(), golpe.tipoJefe(), golpe.jugador(),
                    golpe.danio(), golpe.ultimoGolpeIso()).whenComplete((vacio, fallo) -> {
                        if (fallo != null) {
                            log().log(Level.WARNING, "Potencias: no se pudo volcar el daño de "
                                    + golpe.jugador() + " contra " + golpe.bossUuid(), fallo);
                        }
                    });
        }
    }

    /** §11 L565: mundo permitido. DECISION D7: {@code valid-boss-worlds=[world]} ⇒ mundo principal. */
    private boolean mundoValidoParaJefes(String mundo) {
        Set<String> validos = ctx.config().mundosValidosJefes();
        if (validos == null || validos.isEmpty()) {
            return ctx.config().mundoNombre().equals(mundo);
        }
        return validos.contains(mundo);
    }

    /** [PENDIENTE D1] grupo de LuckPerms del puesto (compatibilidad con potencias.sk). */
    private static String grupoDe(int puesto) {
        return "potencia" + puesto;
    }

    /**
     * Nombre de presentación. Bukkit solo se consulta en el hilo principal, y ahí queda
     * cacheado: la muerte de jefe y el recálculo del puesto 5 asignan desde un hilo async.
     */
    private String nombreDe(UUID jugador) {
        if (jugador == null) {
            return null;
        }
        if (!Bukkit.isPrimaryThread()) {
            return nombres.get(jugador);
        }
        Player enLinea = Bukkit.getPlayer(jugador);
        String nombre = enLinea != null ? enLinea.getName() : Bukkit.getOfflinePlayer(jugador).getName();
        if (nombre != null) {
            nombres.put(jugador, nombre);
        }
        return nombre;
    }

    private void anunciar(int puesto, UUID jugador, String titulo) {
        if (!ctx.config().anuncioGlobal()) {
            return;
        }
        String nombre = nombreDe(jugador);
        String texto = mensaje("potencias.announce-new-owner", Map.of(
                "jugador", nombre == null ? jugador.toString() : nombre,
                "puesto", String.valueOf(puesto),
                "titulo", titulo == null ? "" : titulo));
        enHiloPrincipal(() -> Bukkit.broadcastMessage(color(texto)));
    }

    /** Ejecuta una mutación de Bukkit en el hilo principal; si el plugin se apaga, se descarta. */
    private void enHiloPrincipal(Runnable tarea) {
        if (Bukkit.isPrimaryThread()) {
            tarea.run();
            return;
        }
        try {
            Bukkit.getScheduler().runTask(ctx.plugin(), tarea);
        } catch (RuntimeException apagando) {
            // En onDisable Bukkit ya marco el plugin como deshabilitado y el scheduler lanza
            // IllegalPluginAccessException (RuntimeException, NO IllegalStateException) al
            // registrar tareas; un mensaje tardio se descarta sin romper el llamador.
            log().fine("Potencias: scheduler no disponible (apagado); se descarta el mensaje.");
        }
    }

    /**
     * Despacha una <i>iniciación</i> de I/O al hilo asíncrono (plan §6 regla 3, §26 L1039): el
     * guard de {@code DatabaseManager} lanza {@link IllegalStateException} si {@code read}/
     * {@code write}/{@code transaction} arrancan en el hilo principal. Si ya estamos fuera del
     * principal, corre en línea; si el plugin se está apagando, se descarta la tarea.
     */
    private void enHiloAsync(Runnable tarea) {
        if (!Bukkit.isPrimaryThread()) {
            tarea.run();
            return;
        }
        try {
            Bukkit.getScheduler().runTaskAsynchronously(ctx.plugin(), tarea);
        } catch (RuntimeException apagando) {
            // En onDisable el scheduler lanza IllegalPluginAccessException (RuntimeException, NO
            // IllegalStateException) al registrar tareas: la operacion se descarta, no se propaga.
            log().fine("Potencias: scheduler no disponible (apagado); se descarta la operacion asincrona.");
        }
    }

    private String mensaje(String clave, Map<String, String> reemplazos) {
        try {
            return ctx.messages().texto(clave, reemplazos);
        } catch (IllegalStateException sinProveer) {
            return ctx.plugin().getLogger().getName() + " " + clave; // fallo seguro §2.5
        }
    }

    private static String color(String texto) {
        return ChatColor.translateAlternateColorCodes('&', texto == null ? "" : texto);
    }

    private static String etiqueta(UUID actor) {
        return actor == null ? "sistema" : actor.toString();
    }

    private java.util.logging.Logger log() {
        return ctx.plugin().getLogger();
    }
}
