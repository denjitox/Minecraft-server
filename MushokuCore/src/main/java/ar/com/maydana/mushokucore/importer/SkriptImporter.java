package ar.com.maydana.mushokucore.importer;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.database.IDatabase;
import ar.com.maydana.mushokucore.module.ranks.IPotenciasService;
import ar.com.maydana.mushokucore.util.TimeUtils;
import ar.com.maydana.mushokucore.util.Uuids;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Importador de datos de Skript a MushokuCore — agente D (spec §21 L891-913, plan §7 fila D).
 *
 * <p>Migra lo <b>persistente</b> de {@code potencias.sk} (verificado leyendo el script este turno):</p>
 * <ul>
 *   <li>{@code mt.potencia::<n>::uuid} / {@code ::nombre} → puestos (con <b>{@code IPotenciasService.asignar}</b>,
 *       actor {@code null} = importación, idempotente por revisión; spec §11 L575);</li>
 *   <li>{@code mt.pvp.puntos::<uuid>} → {@code pvp_score.valid_kills};</li>
 *   <li>{@code mt.pvp.ultima::<atacante>::<victima>} → {@code pvp_pair_cooldown.last_valid_kill_at}.</li>
 * </ul>
 *
 * <p>Los daños a jefes ({@code mt.jefe.*}) del Skript son transitorios (se borran al reclamar el
 * puesto, {@code potencias.sk} L43-44): no se migran. {@code enfermedades.sk} no persiste nada
 * (verificado): no hay nada que migrar de C.</p>
 *
 * <p><b>Hilo:</b> las escrituras usan {@link IDatabase#write} compuesto con {@code thenCompose}
 * (jamás {@code .join()}/{@code .get()}); el llamador (comando) corre esto en una tarea async y
 * vuelve al hilo principal solo para el mensaje (§19 L860). Sin SQL en el hilo del servidor.</p>
 *
 * <p><b>Fuente real hoy:</b> {@code servidor/plugins/Skript/variables.csv} es <b>solo cabecera</b>
 * (verificado 2026-10-18): el importador reporta 0 registros y no fabrica filas.</p>
 */
public final class SkriptImporter {

    /** Resumen de una corrida. {@code escrito=false} = simulación (no se tocó la base). */
    public record Resultado(int variables, int puestos, int puntos, int cooldowns,
                            List<String> conflictos, boolean escrito) {
    }

    private static final Pattern POTENCIA =
            Pattern.compile("^mt\\.potencia::(\\d)::(uuid|nombre|fecha)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PUNTOS =
            Pattern.compile("^mt\\.pvp\\.puntos::(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern ULTIMA =
            Pattern.compile("^mt\\.pvp\\.ultima::(.+)::(.+)$", Pattern.CASE_INSENSITIVE);

    private final PluginContext ctx;

    public SkriptImporter(PluginContext ctx) {
        this.ctx = Objects.requireNonNull(ctx, "ctx");
    }

    /**
     * {@code ctx.db()} en un solo punto de asignación: así la variable es <i>effectively final</i>
     * y las lambdas de {@code write} pueden capturarla (si el wiring no la proveyó, {@code null}
     * y el importador degrada sin tocar la base — fallo seguro §2.5).
     */
    private IDatabase dbOpcional() {
        try {
            return ctx.db();
        } catch (IllegalStateException sinProveer) {
            return null;
        }
    }

    /**
     * Procesa el archivo.
     * @param archivo  {@code variables.csv} de Skript.
     * @param escribir {@code false} = simulación (spec §21 L900: importación en modo simulación).
     * @return futuro con el resumen; completo en el hilo de la BD cuando {@code escribir} es verdadero.
     */
    public CompletableFuture<Resultado> procesar(Path archivo, boolean escribir) {
        List<SkriptVariableParser.Variable> variables;
        try {
            variables = SkriptVariableParser.parse(archivo);
        } catch (IOException excepcion) {
            return CompletableFuture.failedFuture(excepcion);
        }

        Map<Integer, UUID> puestosUuid = new TreeMap<>();
        Map<Integer, String> puestosNombre = new HashMap<>();
        Map<UUID, Integer> puntos = new LinkedHashMap<>();
        Map<UUID, Map<UUID, String>> ultimas = new LinkedHashMap<>();
        List<String> conflictos = new ArrayList<>();

        for (SkriptVariableParser.Variable variable : variables) {
            Matcher potencia = POTENCIA.matcher(variable.nombre());
            if (potencia.matches()) {
                int puesto = Integer.parseInt(potencia.group(1));
                String campo = potencia.group(2).toLowerCase(java.util.Locale.ROOT);
                if (puesto < 1 || puesto > 7) {
                    conflictos.add("puesto fuera de rango: " + variable.nombre());
                    continue;
                }
                if (variable.valor() == null) {
                    conflictos.add("tipo no interpretable: " + variable.nombre() + " (" + variable.tipo() + ")");
                    continue;
                }
                if ("uuid".equals(campo)) {
                    UUID id = Uuids.parse(variable.valor());
                    if (id == null) {
                        conflictos.add("UUID invalido en " + variable.nombre() + ": " + variable.valor());
                    } else {
                        puestosUuid.put(puesto, id);
                    }
                } else if ("nombre".equals(campo)) {
                    puestosNombre.put(puesto, variable.valor());
                }
                continue;   // "fecha" se ignora: asignar() fija acquired_at
            }

            Matcher puntosMatcher = PUNTOS.matcher(variable.nombre());
            if (puntosMatcher.matches()) {
                UUID id = Uuids.parse(puntosMatcher.group(1));
                if (id == null || variable.valor() == null) {
                    conflictos.add("puntos invalidos: " + variable.nombre());
                    continue;
                }
                try {
                    puntos.put(id, Integer.parseInt(variable.valor().trim()));
                } catch (NumberFormatException noEsNumero) {
                    conflictos.add("puntos no numericos en " + variable.nombre() + ": " + variable.valor());
                }
                continue;
            }

            Matcher ultima = ULTIMA.matcher(variable.nombre());
            if (ultima.matches()) {
                UUID atacante = Uuids.parse(ultima.group(1));
                UUID victima = Uuids.parse(ultima.group(2));
                if (atacante == null || victima == null || variable.valor() == null) {
                    conflictos.add("cooldown invalido: " + variable.nombre());
                    continue;
                }
                // [A VERIFICAR] el formato de fecha serializado por Skript no se confirmo contra
                // su serializador; se acepta estricto ISO-8601 (el mismo que usa la base, V17).
                Instant instante = TimeUtils.parse(variable.valor());
                if (instante == null) {
                    conflictos.add("fecha no interpretable en " + variable.nombre() + ": " + variable.valor());
                    continue;
                }
                ultimas.computeIfAbsent(atacante, clave -> new LinkedHashMap<>())
                        .put(victima, instante.toString());
                continue;
            }
        }

        int totalCooldowns = ultimas.values().stream().mapToInt(Map::size).sum();
        List<String> conflictosPlan = List.copyOf(conflictos);

        if (!escribir) {
            return CompletableFuture.completedFuture(new Resultado(
                    variables.size(), puestosUuid.size(), puntos.size(), totalCooldowns, conflictosPlan, false));
        }

        // ---- Aplicacion real ----
        int puestosAplicados = 0;
        if (!puestosUuid.isEmpty()) {
            IPotenciasService potencias = ctx.obtener(IPotenciasService.class);
            if (potencias == null) {
                conflictos.add("modulo ranks apagado/ausente: NO se importan puestos (fallo seguro §2.5)");
            } else {
                for (Map.Entry<Integer, UUID> entrada : puestosUuid.entrySet()) {
                    String nombre = puestosNombre.getOrDefault(entrada.getKey(), entrada.getValue().toString());
                    if (potencias.asignar(entrada.getKey(), entrada.getValue(), "import skript", null)) {
                        puestosAplicados++;
                    }
                }
            }
        }

        IDatabase db = dbOpcional();

        CompletableFuture<Void> cola = CompletableFuture.completedFuture(null);
        if (db == null) {
            if (!puntos.isEmpty() || totalCooldowns > 0) {
                conflictos.add("base de datos no disponible: NO se importan puntos/cooldowns (fallo seguro §2.5)");
            }
        } else {
            String ahoraIso = TimeUtils.ahoraIso();
            for (Map.Entry<UUID, Integer> entrada : puntos.entrySet()) {
                final UUID id = entrada.getKey();
                final int bajas = entrada.getValue();
                cola = cola.thenCompose(ignorado -> db.write(conexion -> {
                    try (var ps = conexion.prepareStatement(
                            "INSERT INTO pvp_score (player_uuid, valid_kills, updated_at) VALUES (?,?,?) "
                                    + "ON CONFLICT(player_uuid) DO UPDATE SET valid_kills=?, updated_at=?")) {
                        ps.setString(1, id.toString());
                        ps.setInt(2, bajas);
                        ps.setString(3, ahoraIso);
                        ps.setInt(4, bajas);
                        ps.setString(5, ahoraIso);
                        ps.executeUpdate();
                    } catch (java.sql.SQLException excepcion) {
                        throw new RuntimeException(excepcion);
                    }
                }));
            }
            for (Map.Entry<UUID, Map<UUID, String>> porAtacante : ultimas.entrySet()) {
                for (Map.Entry<UUID, String> porVictima : porAtacante.getValue().entrySet()) {
                    final String atacante = porAtacante.getKey().toString();
                    final String victima = porVictima.getKey().toString();
                    final String cuando = porVictima.getValue();
                    cola = cola.thenCompose(ignorado -> db.write(conexion -> {
                        try (var ps = conexion.prepareStatement(
                                "INSERT INTO pvp_pair_cooldown (attacker_uuid, victim_uuid, last_valid_kill_at) "
                                        + "VALUES (?,?,?) ON CONFLICT(attacker_uuid, victim_uuid) DO UPDATE SET "
                                        + "last_valid_kill_at=?")) {
                            ps.setString(1, atacante);
                            ps.setString(2, victima);
                            ps.setString(3, cuando);
                            ps.setString(4, cuando);
                            ps.executeUpdate();
                        } catch (java.sql.SQLException excepcion) {
                            throw new RuntimeException(excepcion);
                        }
                    }));
                }
            }
        }

        final int puestosFinales = puestosAplicados;
        return cola.handle((ignorado, error) -> {
            List<String> conflictosFinales = new ArrayList<>(conflictos);
            if (error != null) {
                conflictosFinales.add("error de base de datos durante la importacion: " + error.getMessage());
            }
            try {
                ctx.audit().registrar(null, "import.skript",
                        archivo.getFileName() == null ? null : archivo.getFileName().toString(),
                        "puestos=" + puestosFinales + " puntos=" + puntos.size()
                                + " cooldowns=" + totalCooldowns + " conflictos=" + conflictosFinales.size());
            } catch (RuntimeException sinAuditoria) {
                conflictosFinales.add("sin auditoria disponible: " + sinAuditoria.getMessage());
            }
            return new Resultado(variables.size(), puestosFinales, puntos.size(), totalCooldowns,
                    List.copyOf(conflictosFinales), true);
        });
    }
}
