package ar.com.maydana.mushokucore.module.commands;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * {@code /visionapostol} — agente D (spec §21 L893: el Skript {@code mt.alianza.*} concedía
 * visión nocturna a los Apóstoles; se porta al plugin). Solo jugadores de la alianza Apóstoles.
 *
 * <p><b>Comportamiento:</b> concede {@code NIGHT_VISION} por <b>5 minutos</b> (6000 ticks) con
 * <b>cooldown de 30 minutos</b> por UUID. El permiso es el mismo nodo que usa el Skript:
 * {@code mt.alianza.apostoles} (es también el marcador de alianza que lee
 * {@code JavaChatUi.alianzaId}).</p>
 *
 * <p><b>Cooldown:</b> por ahora en memoria ({@link ConcurrentHashMap}); se pierde al reiniciar.
 * [A VERIFICAR: persistir el cooldown en la BD (tabla propia) cuando el módulo de alianzas 1.1
 * traiga el estado de alianza; hoy no hay tabla de cooldown de habilidades.]</p>
 *
 * <p><b>Textos:</b> de {@code messages.yml} ({@code visionapostol.*}); fallo seguro §2.5 vía
 * {@link Textos#msg}. <b>Mutación de Bukkit:</b> el comando ya corre en el hilo principal, así
 * que {@code addPotionEffect} es directo (cero SQL; el registro de auditoría es fire-and-forget).</p>
 */
public final class VisionApostolCommand implements CommandExecutor {

    /** Mismo permiso que el Skript actual (mt.alianza.*). */
    private static final String PERMISO_APOSTOLES = "mt.alianza.apostoles";
    /** 5 minutos = 6000 ticks. */
    private static final int DURACION_TICKS = 20 * 60 * 5;
    /** 30 minutos. */
    private static final long COOLDOWN_MS = 30L * 60L * 1000L;

    private final PluginContext ctx;
    private final ConcurrentHashMap<UUID, Long> ultimoUso = new ConcurrentHashMap<>();

    public VisionApostolCommand(PluginContext ctx) {
        this.ctx = Objects.requireNonNull(ctx, "ctx");
    }

    @Override
    public boolean onCommand(CommandSender emisor, Command comando, String etiqueta, String[] args) {
        if (!(emisor instanceof Player jugador)) {
            Textos.enviar(emisor, Textos.msg(ctx, "visionapostol.solo-jugadores"));
            return true;
        }
        if (!jugador.hasPermission(PERMISO_APOSTOLES)) {
            Textos.enviar(jugador, Textos.msg(ctx, "generic.no-permission"));
            return true;
        }

        long ahora = System.currentTimeMillis();
        Long anterior = ultimoUso.get(jugador.getUniqueId());
        if (anterior != null) {
            long restante = COOLDOWN_MS - (ahora - anterior);
            if (restante > 0) {
                long minutos = (restante + 59_999L) / 60_000L; // redondeo hacia arriba
                Textos.enviar(jugador, Textos.msg(ctx, "visionapostol.en-cooldown",
                        Map.of("minutos", String.valueOf(minutos),
                                "segundos", String.valueOf((restante + 999L) / 1000L))));
                return true;
            }
        }

        boolean aplicado = jugador.addPotionEffect(
                new PotionEffect(PotionEffectType.NIGHT_VISION, DURACION_TICKS, 0, true, true, true));
        if (!aplicado) {
            Textos.enviar(jugador, Textos.msg(ctx, "visionapostol.fallo"));
            return true;
        }
        ultimoUso.put(jugador.getUniqueId(), ahora);
        Textos.enviar(jugador, Textos.msg(ctx, "visionapostol.activada"));
        try {
            ctx.audit().registrar(jugador.getUniqueId(), "visionapostol.activar",
                    jugador.getUniqueId().toString(), "vision nocturna 5 min; cooldown 30 min");
        } catch (RuntimeException sinAuditoria) {
            ctx.plugin().getLogger().fine("Auditoria no disponible en /visionapostol: "
                    + sinAuditoria.getMessage());
        }
        return true;
    }
}
