package ar.com.maydana.mushokucore.integration;

import ar.com.maydana.mushokucore.model.PlaceholderContext;
import ar.com.maydana.mushokucore.module.PlaceholderFuente;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

/**
 * Expansión de PlaceholderAPI del prefijo {@code mushoku} — cierre de la ambigüedad V3
 * (spec §18 L844-854, plan §7 L306; antes era el pendiente de Fase 2 declarado en
 * {@code PlaceholderHook}).
 *
 * <p>Esta clase es la <b>única</b> del plugin que depende de las clases de PlaceholderAPI
 * ({@code me.clip.placeholderapi.*}). Por eso:</p>
 * <ul>
 *   <li>Se carga <b>solo</b> si {@link PlaceholderHook#registrar()} ya verificó que PAPI está
 *       presente ({@code disponible()}); si PAPI no está, esta clase nunca se toca y no hay
 *       {@code NoClassDefFoundError} (la resolución de la clase es perezosa en la JVM).</li>
 *   <li>Requiere el jar {@code PlaceholderAPI-2.12.3.jar} en el <b>classpath de compilación</b>
 *       (ver {@code build.ps1}). En runtime el jar lo aporta el propio plugin
 *       {@code plugins/PlaceholderAPI-*.jar} del servidor: no se empaqueta acá.</li>
 * </ul>
 *
 * <p><b>Cero SQL por render</b> (spec §18 L856, §19 L874): no consulta nada por su cuenta; delega
 * en las {@link PlaceholderFuente} ya registradas en el contexto (caché de servicios), y devuelve
 * {@code null} cuando una fuente no aplica, que es el contrato de PAPI para "sin valor".</p>
 *
 * <p>Firma verificada con {@code javap} sobre {@code servidor/plugins/PlaceholderAPI-2.12.3.jar}
 * (2026-10-18): {@code PlaceholderExpansion extends PlaceholderHook} y expone
 * {@code onRequest(OfflinePlayer, String)}; {@code onPlaceholderRequest(Player, String)} queda para
 * la API vieja.</p>
 */
public final class MushokuExpansion extends PlaceholderExpansion {

    /** Prefijo publicado; el §18 escribe los placeholders como {@code %mushoku_<clave>%}. */
    public static final String IDENTIFICADOR = "mushoku";

    private final String version;
    private final List<PlaceholderFuente> fuentes;

    /**
     * @param version versión del plugin (se reporta a PAPI; sale de {@code plugin.yml}).
     * @param fuentes fuentes registradas en el contexto; cada una declara una clave del §18.
     */
    public MushokuExpansion(String version, List<PlaceholderFuente> fuentes) {
        this.version = Objects.requireNonNull(version, "version");
        this.fuentes = List.copyOf(Objects.requireNonNull(fuentes, "fuentes"));
    }

    @Override
    public String getIdentifier() {
        return IDENTIFICADOR;
    }

    @Override
    public String getAuthor() {
        // Igual que `authors:` de plugin.yml.
        return "AdministracionMushoku";
    }

    @Override
    public String getVersion() {
        return version;
    }

    /** Lista informativa para {@code /papi info mushoku}; con prefijo, como las usa el usuario. */
    @Override
    public List<String> getPlaceholders() {
        List<String> claves = new ArrayList<>();
        for (PlaceholderFuente fuente : fuentes) {
            for (String clave : fuente.claves()) {
                claves.add(IDENTIFICADOR + "_" + clave);
            }
        }
        return claves;
    }

    /** Sin esto la expansión se desregistra al recargar PAPI y no vuelve (comportamiento buscado). */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * Resuelve {@code %mushoku_<parametros>%}.
     *
     * @param jugador jugador del render, o {@code null} ({@code /papi parse --null}, TAB global).
     * @param parametros clave sin prefijo (p. ej. {@code power_1_name}).
     * @return el valor, o {@code null} si la clave no es de MushokuCore o no aplica ahora.
     */
    @Override
    public String onRequest(OfflinePlayer jugador, String parametros) {
        if (parametros == null || parametros.isBlank()) {
            return null;
        }
        String clave = parametros.toLowerCase(Locale.ROOT);
        UUID uuid = jugador == null ? null : jugador.getUniqueId();
        PlaceholderContext pctx = new PlaceholderContext(uuid);
        for (PlaceholderFuente fuente : fuentes) {
            if (!fuente.claves().contains(clave)) {
                continue;
            }
            try {
                Optional<String> valor = fuente.valor(pctx);
                return valor.orElse(null);
            } catch (RuntimeException excepcion) {
                // Un placeholder no puede tumbar a quien lo consume (fallo seguro §2.5); las
                // fuentes ya atrapan, esto es la segunda red por si una fuente futura no lo hace.
                return null;
            }
        }
        return null;
    }
}
