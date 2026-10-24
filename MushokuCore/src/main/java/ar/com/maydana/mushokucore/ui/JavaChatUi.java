package ar.com.maydana.mushokucore.ui;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.integration.LuckPermsHook;
import ar.com.maydana.mushokucore.model.Allegiance;
import ar.com.maydana.mushokucore.model.Disease;
import ar.com.maydana.mushokucore.model.DueñoPuesto;
import ar.com.maydana.mushokucore.module.commands.Textos;
import ar.com.maydana.mushokucore.module.diseases.IEnfermedadesService;
import ar.com.maydana.mushokucore.module.ranks.IPotenciasService;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * Implementación en texto/chat de {@link PlayerUi} — spec §16 L777-790 ([PROPUESTA D]).
 *
 * <p>Propiedad: el plan §6.3 asigna {@code ui/JavaChatUi.java} a F0, pero Fase 0 no la creó y
 * el javadoc de {@link PlayerUi} la dejó "pendiente, candidato natural D". El encargo de Fase 1
 * del agente D la incluye explícitamente: se crea aquí y se avisa al integrador.</p>
 *
 * <p>Sin formularios Bedrock en MVP (§16 L789, 1.3): Bedrock recibe este mismo texto por
 * Geyser/Floodgate; el respaldo por comando/texto está garantizado (§16 L792, §9 L442).</p>
 *
 * <p>Todas las lecturas de servicios son null-safe: si el módulo ranks/enfermedades o el wiring
 * de A no están disponibles, la UI degrada con un mensaje claro (fallo seguro §2.5). Cero SQL:
 * consume solo los interfaces de caché (§18 L856).</p>
 */
public final class JavaChatUi implements PlayerUi {

    /** Permiso Orsted por defecto (Skript enfermedades.sk L26). [A VERIFICAR - decisión D11] */
    private static final String PERMISO_ORSTED_DEFECTO = "mt.alianza.orsted";

    /**
     * Permiso de Apóstoles hoy en Skript. No hay clave de config para él.
     * [A VERIFICAR - decisión D11/D15] nombre hasta 1.1 (§21 L893: /visionapostol queda en Skript).
     */
    private static final String PERMISO_APOSTOLES = "mt.alianza.apostoles";

    private final PluginContext ctx;

    public JavaChatUi(PluginContext ctx) {
        this.ctx = java.util.Objects.requireNonNull(ctx, "ctx");
    }

    @Override
    public void showPowers(Player player) {
        Textos.enviar(player, Textos.msg(ctx, "potencias.list-header"));
        IPotenciasService potencias = Textos.opcional(() -> ctx.obtener(IPotenciasService.class));
        if (potencias == null) {
            // Módulo ranks apagado/ausente: no inventamos títulos ni dueños (fallo seguro §2.5).
            Textos.enviar(player, "&cLas Siete Potencias no están disponibles ahora mismo.");
            return;
        }
        for (int puesto = 1; puesto <= 7; puesto++) {
            String titulo = potencias.tituloDe(puesto);
            Optional<DueñoPuesto> dueno = potencias.propietario(puesto);
            String propietario = "No conquistado";   // messages.yml comenta este literal
            if (dueno.isPresent() && dueno.get().dueno() != null) {
                String nombre = dueno.get().nombreDueno();
                propietario = (nombre == null || nombre.isBlank())
                        ? dueno.get().dueno().toString()
                        : nombre;
            }
            Textos.enviar(player, Textos.msg(ctx, "potencias.list-entry", Map.of(
                    "puesto", String.valueOf(puesto),
                    "titulo", titulo == null ? "" : titulo,
                    "propietario", propietario)));
        }
        Textos.enviar(player, Textos.msg(ctx, "potencias.list-footer"));
    }

    @Override
    public void showStatus(Player player) {
        Textos.enviar(player, Textos.msg(ctx, "mtestado.header"));
        UUID id = player.getUniqueId();

        // Alianza: desde permisos LuckPerms mientras el módulo factions sea 1.1 (§21 L893).
        // [A VERIFICAR - decisión D15] default A: alianza desde permisos LP.
        Textos.enviar(player, mensajeAlianza(alianzaId(ctx, id)));

        // Puesto de Potencia y bajas válidas: solo si el servicio de B está registrado.
        IPotenciasService potencias = Textos.opcional(() -> ctx.obtener(IPotenciasService.class));
        if (potencias != null) {
            int puesto = potencias.posicionDelJugador(id);
            if (puesto > 0) {
                String titulo = potencias.tituloDe(puesto);
                // [PROPUESTA D - clave messages.yml mtestado.power-line pendiente para Fase 2]
                Textos.enviar(player, "&7Potencia: &f" + puesto + " — "
                        + (titulo == null ? "" : titulo));
            }
            // [PROPUESTA D - clave messages.yml mtestado.valid-kills pendiente para Fase 2]
            Textos.enviar(player, "&7Bajas PvP válidas: &f" + potencias.bajasValidas(id));
        }

        // Enfermedad activa: servicio de C, lectura de caché.
        IEnfermedadesService enfermedades =
                Textos.opcional(() -> ctx.obtener(IEnfermedadesService.class));
        if (enfermedades != null) {
            Optional<Disease> activa = enfermedades.activa(id);
            if (activa.isEmpty()) {
                Textos.enviar(player, Textos.msg(ctx, "mtestado.disease-none"));
            } else {
                Disease d = activa.get();
                String minutos;
                if (d.expira() == null) {
                    minutos = "sin expiracion";   // [PROPUESTA D] expires_at vacío (§10)
                } else {
                    long m = Math.max(0, Duration.between(Instant.now(), d.expira()).toMinutes());
                    minutos = String.valueOf(m);
                }
                Textos.enviar(player, Textos.msg(ctx, "mtestado.disease-active", Map.of(
                        "enfermedad", d.displayName() == null ? d.id() : d.displayName(),
                        "minutos", minutos)));
            }
        } else {
            Textos.enviar(player, "&7Enfermedades: &fmodulo no disponible.");
        }
        Textos.enviar(player, Textos.msg(ctx, "mtestado.footer"));
    }

    @Override
    public void confirmAllegiance(Player player, Allegiance allegiance) {
        // Sin invocar en MVP: las alianzas llegan en 1.1 (plan §6.1, nota final sobre la firma).
        Textos.enviar(player, Textos.prefijo(ctx) + Textos.msg(ctx, "stub.module-future"));
    }

    /**
     * Alianza del jugador vía permisos LuckPerms ([A VERIFICAR - decisión D15] default A).
     * @return {@code orsted}, {@code apostoles} o {@code neutral}; {@code n/d} si LuckPerms
     *         no está disponible (no se fabrica una alianza, fallo seguro §2.5).
     */
    public static String alianzaId(PluginContext ctx, UUID jugador) {
        LuckPermsHook lp = Textos.opcional(ctx::luckperms);
        if (lp == null || !lp.disponible()) {
            return "n/d";
        }
        String permisoOrsted = Textos.opcional(() -> ctx.config().permisoOrsted());
        if (permisoOrsted == null || permisoOrsted.isBlank()) {
            permisoOrsted = PERMISO_ORSTED_DEFECTO;
        }
        if (lp.tienePermiso(jugador, permisoOrsted)) {
            return "orsted";
        }
        if (lp.tienePermiso(jugador, PERMISO_APOSTOLES)) {
            return "apostoles";
        }
        return "neutral";
    }

    private String mensajeAlianza(String alianzaId) {
        return switch (alianzaId) {
            case "orsted" -> Textos.msg(ctx, "mtestado.alliance-orsted");
            case "apostoles" -> Textos.msg(ctx, "mtestado.alliance-apostles");
            case "neutral" -> Textos.msg(ctx, "mtestado.alliance-neutral");
            default -> "&7Alianza: &f" + alianzaId;   // n/d u otro valor futuro
        };
    }
}
