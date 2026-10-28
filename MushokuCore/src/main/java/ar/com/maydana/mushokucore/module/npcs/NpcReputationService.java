package ar.com.maydana.mushokucore.module.npcs;

import ar.com.maydana.mushokucore.database.repository.NpcRepository;
import ar.com.maydana.mushokucore.util.TimeUtils;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache en memoria de la reputacion por jugador y vecino. Se precarga en el join para
 * que el dialogo no consulte la base; las escrituras siguen el camino async de
 * {@link NpcRepository}.
 */
public final class NpcReputationService {

    private final NpcRepository repositorio;
    private final Map<String, Integer> cache = new ConcurrentHashMap<>();

    public NpcReputationService(NpcRepository repositorio) {
        this.repositorio = repositorio;
    }

    public int valor(UUID jugador, String npcId) {
        return cache.getOrDefault(clave(jugador, npcId), 0);
    }

    /** Dispara la carga de todas las reputaciones del jugador; no bloquea el join. */
    public void precargar(UUID jugador) {
        repositorio.todas(jugador).thenAccept(mapa -> {
            for (Map.Entry<String, Integer> entrada : mapa.entrySet()) {
                cache.putIfAbsent(clave(jugador, entrada.getKey()), entrada.getValue());
            }
        });
    }

    /** Suma (o resta, con delta negativo) y persiste. @return el valor nuevo */
    public int sumar(UUID jugador, String npcId, int delta) {
        int nuevo = cache.merge(clave(jugador, npcId), delta, Integer::sum);
        repositorio.fijar(jugador, npcId, nuevo, TimeUtils.ahoraIso());
        return nuevo;
    }

    public void olvidar(UUID jugador) {
        String prefijo = jugador + "|";
        cache.keySet().removeIf(clave -> clave.startsWith(prefijo));
    }

    private static String clave(UUID jugador, String npcId) {
        return jugador + "|" + npcId;
    }
}
