package ar.com.maydana.mushokucore.module.npcs;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.config.PluginConfig;
import ar.com.maydana.mushokucore.database.repository.NpcRepository;
import ar.com.maydana.mushokucore.module.commands.Textos;
import ar.com.maydana.mushokucore.util.InventarioUtil;
import ar.com.maydana.mushokucore.util.TimeUtils;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Nucleo de los vecinos.
 *
 * <p>Junta el catalogo de {@code npcs.yml}, las ubicaciones de
 * {@code npcs-locations.yml} y el estado en memoria de las entidades. Las interacciones
 * llegan por la entidad (PDC) o por el bloque ancla, de modo que si el cliente Bedrock no
 * puede clickear el mannequin igual se puede hablar desde {@code /vecino hablar}.</p>
 */
public final class NpcsService {

    private final PluginContext ctx;
    private final JavaPlugin plugin;
    private final Logger log;
    private final PluginConfig config;
    private final NpcRepository repositorio;
    private final NpcReputationService reputacion;
    private final NpcEntityFactory fabrica;
    private final NpcGui gui;

    private final Map<String, NpcDefinition> definiciones = new LinkedHashMap<>();
    private final Map<String, NpcLocation> ubicaciones = new LinkedHashMap<>();
    private final Map<String, UUID> entidades = new ConcurrentHashMap<>();
    private final Set<String> regalosEnProceso = ConcurrentHashMap.newKeySet();

    public NpcsService(PluginContext ctx, NpcRepository repositorio,
                       NpcReputationService reputacion, NpcEntityFactory fabrica) {
        this.ctx = ctx;
        this.plugin = ctx.plugin();
        this.log = plugin.getLogger();
        this.config = ctx.config();
        this.repositorio = repositorio;
        this.reputacion = reputacion;
        this.fabrica = fabrica;
        this.gui = new NpcGui(ctx, this);
    }

    // ------------------------------------------------------------------ catalogo

    /** Carga definiciones y ubicaciones. @return cantidad de definiciones validas */
    public int cargar() {
        definiciones.clear();
        ubicaciones.clear();
        YamlConfiguration yaml = leerYaml("npcs.yml", true);
        if (yaml == null) {
            return 0;
        }
        YamlConfiguration skins = leerYaml("npcs-skins.yml", false);
        ConfigurationSection seccion = yaml.getConfigurationSection("npcs");
        if (seccion == null) {
            log.warning("npcs.yml no tiene la seccion 'npcs'.");
            return 0;
        }
        for (String crudo : seccion.getKeys(false)) {
            ConfigurationSection npc = seccion.getConfigurationSection(crudo);
            if (npc == null) {
                continue;
            }
            String id = crudo.toLowerCase(Locale.ROOT);
            definiciones.put(id, parsear(id, npc, skins));
        }
        cargarUbicaciones();
        return definiciones.size();
    }

    private NpcDefinition parsear(String id, ConfigurationSection yaml, YamlConfiguration skins) {
        String nombre = yaml.getString("nombre", id);
        String titulo = yaml.getString("titulo", "");
        String skinValue = yaml.getString("skin.value", "");
        String skinSignature = yaml.getString("skin.signature", "");
        if ((skinValue == null || skinValue.isBlank()) && skins != null) {
            skinValue = skins.getString(id + ".value", "");
            skinSignature = skins.getString(id + ".signature", "");
        }

        List<NpcDefinition.Regalo> regalos = new ArrayList<>();
        List<Map<?, ?>> regalosYaml = yaml.getMapList("regalos");
        for (int indice = 0; indice < regalosYaml.size(); indice++) {
            Map<?, ?> mapa = regalosYaml.get(indice);
            Material material = material(texto(mapa.get("material"), null));
            int cantidad = entero(mapa.get("cantidad"), 1);
            if (material == null || cantidad <= 0) {
                log.warning("npcs.yml: " + id + ".regalos[" + indice
                        + "] tiene material o cantidad invalida; se omite.");
                continue;
            }
            regalos.add(new NpcDefinition.Regalo(
                    texto(mapa.get("id"), "regalo-" + indice),
                    material, cantidad,
                    entero(mapa.get("reputacion"), 0),
                    entero(mapa.get("cooldown-horas"), 24)));
        }

        List<NpcDefinition.Trueque> trueques = new ArrayList<>();
        List<Map<?, ?>> truequesYaml = yaml.getMapList("trueques");
        for (int indice = 0; indice < truequesYaml.size(); indice++) {
            Map<?, ?> mapa = truequesYaml.get(indice);
            Material entrega = material(texto(mapa.get("entrega"), null));
            Material costo = material(texto(mapa.get("costo"), null));
            int entregaCantidad = entero(mapa.get("entrega-cantidad"), 1);
            int costoCantidad = entero(mapa.get("costo-cantidad"), 1);
            if (entrega == null || costo == null || entregaCantidad <= 0 || costoCantidad <= 0
                    || entregaCantidad > Math.max(1, entrega.getMaxStackSize())) {
                log.warning("npcs.yml: " + id + ".trueques[" + indice
                        + "] tiene material o cantidad invalida; se omite. (entrega-cantidad"
                        + " no puede superar el stack de " + (entrega == null ? "?" : entrega)
                        + ")");
                continue;
            }
            trueques.add(new NpcDefinition.Trueque(
                    texto(mapa.get("id"), "trueque-" + indice),
                    entrega, entregaCantidad, costo, costoCantidad));
        }

        return new NpcDefinition(id, nombre, titulo, skinValue, skinSignature,
                yaml.getStringList("dialogo.baja"),
                yaml.getStringList("dialogo.neutral"),
                yaml.getStringList("dialogo.alta"),
                regalos, trueques);
    }

    private void cargarUbicaciones() {
        File archivo = new File(plugin.getDataFolder(), "npcs-locations.yml");
        if (!archivo.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(archivo);
        for (String crudo : yaml.getKeys(false)) {
            String id = crudo.toLowerCase(Locale.ROOT);
            ConfigurationSection seccion = yaml.getConfigurationSection(crudo);
            if (seccion == null) {
                continue;
            }
            if (!definiciones.containsKey(id)) {
                log.warning("npcs-locations.yml: '" + crudo + "' no existe en npcs.yml; se ignora.");
                continue;
            }
            ubicaciones.put(id, new NpcLocation(
                    seccion.getString("world", config.mundoNombre()),
                    seccion.getDouble("x"),
                    seccion.getDouble("y"),
                    seccion.getDouble("z"),
                    (float) seccion.getDouble("yaw", 0.0),
                    (float) seccion.getDouble("pitch", 0.0)));
        }
    }

    private void guardarUbicaciones() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<String, NpcLocation> entrada : ubicaciones.entrySet()) {
            String base = entrada.getKey();
            NpcLocation ubicacion = entrada.getValue();
            yaml.set(base + ".world", ubicacion.mundo());
            yaml.set(base + ".x", ubicacion.x());
            yaml.set(base + ".y", ubicacion.y());
            yaml.set(base + ".z", ubicacion.z());
            yaml.set(base + ".yaw", ubicacion.yaw());
            yaml.set(base + ".pitch", ubicacion.pitch());
        }
        try {
            yaml.save(new File(plugin.getDataFolder(), "npcs-locations.yml"));
        } catch (IOException excepcion) {
            log.log(Level.SEVERE, "No se pudo guardar npcs-locations.yml", excepcion);
        }
    }

    private YamlConfiguration leerYaml(String nombre, boolean avisarSiFalta) {
        File editable = new File(plugin.getDataFolder(), nombre);
        if (editable.exists()) {
            return YamlConfiguration.loadConfiguration(editable);
        }
        try (var recurso = plugin.getResource(nombre)) {
            if (recurso == null) {
                if (avisarSiFalta) {
                    log.warning(nombre + " no encontrado (ni en el data folder ni en el JAR).");
                }
                return null;
            }
            return YamlConfiguration.loadConfiguration(
                    new java.io.InputStreamReader(recurso, java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception excepcion) {
            log.log(Level.SEVERE, "No se pudo leer " + nombre + " del JAR.", excepcion);
            return null;
        }
    }

    private static String texto(Object valor, String defecto) {
        return valor == null ? defecto : String.valueOf(valor).trim();
    }

    private static int entero(Object valor, int defecto) {
        if (valor instanceof Number numero) {
            return numero.intValue();
        }
        try {
            return valor == null ? defecto : Integer.parseInt(String.valueOf(valor).trim());
        } catch (NumberFormatException invalido) {
            return defecto;
        }
    }

    private static Material material(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        return Material.matchMaterial(nombre.toUpperCase(Locale.ROOT));
    }

    // ------------------------------------------------------------------ entidades

    public void spawnearTodos() {
        for (Map.Entry<String, NpcLocation> entrada : ubicaciones.entrySet()) {
            spawnear(entrada.getKey(), entrada.getValue());
        }
    }

    private void spawnear(String npcId, NpcLocation ubicacion) {
        NpcDefinition definicion = definiciones.get(npcId);
        if (definicion == null) {
            return;
        }
        Location lugar = ubicacion.aLocation();
        if (lugar == null || lugar.getWorld() == null) {
            log.warning("NPC '" + npcId + "': el mundo '" + ubicacion.mundo()
                    + "' no esta cargado; se intentara al cargar el chunk.");
            return;
        }
        World mundo = lugar.getWorld();
        int chunkX = lugar.getBlockX() >> 4;
        int chunkZ = lugar.getBlockZ() >> 4;
        if (!mundo.isChunkLoaded(chunkX, chunkZ)) {
            return;
        }
        Chunk chunk = mundo.getChunkAt(chunkX, chunkZ);
        for (Entity existente : chunk.getEntities()) {
            if (npcId.equals(fabrica.npcId(existente))) {
                entidades.put(npcId, existente.getUniqueId());
                return;
            }
        }
        Entity entidad = fabrica.crear(lugar, definicion);
        entidades.put(npcId, entidad.getUniqueId());
    }

    /** Al cargar un chunk, recupera NPCs de ese chunk y elimina duplicados. */
    public void reconciliar(Chunk chunk) {
        for (Map.Entry<String, NpcLocation> entrada : ubicaciones.entrySet()) {
            Location lugar = entrada.getValue().aLocation();
            if (lugar == null || lugar.getWorld() != chunk.getWorld()) {
                continue;
            }
            if ((lugar.getBlockX() >> 4) == chunk.getX()
                    && (lugar.getBlockZ() >> 4) == chunk.getZ()) {
                spawnear(entrada.getKey(), entrada.getValue());
            }
        }
        Map<String, Entity> vistos = new java.util.HashMap<>();
        for (Entity entidad : chunk.getEntities()) {
            String npcId = fabrica.npcId(entidad);
            if (npcId == null) {
                continue;
            }
            Entity previo = vistos.putIfAbsent(npcId, entidad);
            if (previo != null) {
                entidad.remove();
            } else {
                entidades.put(npcId, entidad.getUniqueId());
            }
        }
    }

    /** Rota a los vecinos hacia el jugador mas cercano (rutina liviana). */
    public void mirarJugadores() {
        for (Map.Entry<String, UUID> entrada : entidades.entrySet()) {
            Entity entidad = Bukkit.getEntity(entrada.getValue());
            if (entidad == null || !entidad.isValid()) {
                continue;
            }
            Player cercano = null;
            double mejor = 64.0;
            for (Player jugador : entidad.getWorld().getPlayers()) {
                double distancia = jugador.getLocation().distanceSquared(entidad.getLocation());
                if (distancia <= mejor) {
                    mejor = distancia;
                    cercano = jugador;
                }
            }
            if (cercano == null) {
                continue;
            }
            double dx = cercano.getLocation().getX() - entidad.getLocation().getX();
            double dz = cercano.getLocation().getZ() - entidad.getLocation().getZ();
            float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            entidad.setRotation(yaw, 0.0f);
        }
    }

    public String npcIdDe(Entity entidad) {
        return fabrica.npcId(entidad);
    }

    public String npcEnBloque(Location lugar) {
        for (Map.Entry<String, NpcLocation> entrada : ubicaciones.entrySet()) {
            if (entrada.getValue().esAncla(lugar)) {
                return entrada.getKey();
            }
        }
        return null;
    }

    /** Delegacion del listener de inventario (el GUI es interno al servicio). */
    public void manejarClickGui(Player jugador, org.bukkit.inventory.Inventory superior, int slot) {
        gui.manejarClick(jugador, superior, slot);
    }

    /** Abre la pantalla de trueques desde comando (fallback de Bedrock). */
    public void abrirTrueques(Player jugador, String npcId) {
        NpcDefinition definicion = definicion(npcId);
        if (definicion != null) {
            gui.abrir(jugador, definicion);
        }
    }

    /** Nivel legible de reputacion segun los umbrales de config.yml. */
    public String nivelReputacion(int valor) {
        if (valor >= config.npcsReputacionAlta()) {
            return "alta";
        }
        if (valor <= config.npcsReputacionBaja()) {
            return "baja";
        }
        return "neutral";
    }

    // ------------------------------------------------------------------ interaccion

    public void interactuar(Player jugador, String npcId, Entity entidad) {
        NpcDefinition definicion = definiciones.get(npcId);
        if (definicion == null) {
            return;
        }
        if (entidad != null) {
            entidades.put(npcId, entidad.getUniqueId());
        }
        ItemStack mano = jugador.getInventory().getItemInMainHand();
        NpcDefinition.Regalo regalo = mano == null || mano.getType().isAir()
                ? null
                : definicion.regaloPara(mano.getType(), mano.getAmount());
        if (regalo != null) {
            entregarRegalo(jugador, definicion, regalo);
            return;
        }
        if (jugador.isSneaking() && !definicion.trueques().isEmpty()) {
            gui.abrir(jugador, definicion);
            return;
        }
        int valor = reputacion.valor(jugador.getUniqueId(), npcId);
        Textos.enviar(jugador, Textos.msg(ctx, "vecinos.dialogo", Map.of(
                "npc", definicion.nombre(),
                "linea", elegirDialogo(definicion, valor))));
    }

    private String elegirDialogo(NpcDefinition definicion, int valor) {
        List<String> lineas;
        if (valor >= config.npcsReputacionAlta()) {
            lineas = definicion.dialogoAlta();
        } else if (valor <= config.npcsReputacionBaja()) {
            lineas = definicion.dialogoBaja();
        } else {
            lineas = definicion.dialogoNeutral();
        }
        if (lineas.isEmpty()) {
            lineas = definicion.dialogoNeutral();
        }
        if (lineas.isEmpty()) {
            return "&7...";
        }
        return lineas.get(ThreadLocalRandom.current().nextInt(lineas.size()));
    }

    private void entregarRegalo(Player jugador, NpcDefinition definicion,
                                NpcDefinition.Regalo regalo) {
        UUID uuid = jugador.getUniqueId();
        String enProceso = uuid + "|" + definicion.id() + "|" + regalo.id();
        if (!regalosEnProceso.add(enProceso)) {
            return;
        }
        String ahora = TimeUtils.ahoraIso();
        repositorio.ultimoRegalo(uuid, definicion.id(), regalo.id())
                .thenAccept(ultimo -> Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        if (!jugador.isOnline()) {
                            return;
                        }
                        if (ultimo != null && enCooldown(ultimo, regalo.cooldownHoras())) {
                            avisar(jugador, "vecinos.regalo-cooldown",
                                    Map.of("npc", definicion.nombre()));
                            return;
                        }
                        ItemStack mano = jugador.getInventory().getItemInMainHand();
                        if (mano == null || mano.getType() != regalo.material()
                                || mano.getAmount() < regalo.cantidad()) {
                            return;
                        }
                        int restante = mano.getAmount() - regalo.cantidad();
                        if (restante <= 0) {
                            jugador.getInventory().setItemInMainHand(null);
                        } else {
                            mano.setAmount(restante);
                        }
                        int nuevo = reputacion.sumar(uuid, definicion.id(), regalo.reputacion());
                        repositorio.registrarRegalo(uuid, definicion.id(), regalo.id(), ahora);
                        auditar(uuid, "npc.regalo", "npc=" + definicion.id(),
                                "regalo=" + regalo.id() + " reputacion=" + nuevo);
                        avisar(jugador, "vecinos.regalo-ok", Map.of(
                                "npc", definicion.nombre(),
                                "reputacion", String.valueOf(nuevo)));
                    } finally {
                        regalosEnProceso.remove(enProceso);
                    }
                }))
                .exceptionally(error -> {
                    regalosEnProceso.remove(enProceso);
                    log.log(Level.WARNING, "No se pudo consultar el ultimo regalo de "
                            + definicion.id(), error);
                    return null;
                });
    }

    private static boolean enCooldown(String iso, int horas) {
        if (horas <= 0) {
            return false;
        }
        try {
            return Instant.parse(iso).plus(horas, ChronoUnit.HOURS).isAfter(Instant.now());
        } catch (DateTimeParseException invalido) {
            return false;
        }
    }

    /** @return true solo si el intercambio se completo */
    public boolean trueque(Player jugador, NpcDefinition definicion, int indice) {
        if (indice < 0 || indice >= definicion.trueques().size()) {
            return false;
        }
        NpcDefinition.Trueque trueque = definicion.trueques().get(indice);
        if (InventarioUtil.contar(jugador, trueque.costo()) < trueque.costoCantidad()) {
            avisar(jugador, "vecinos.trueque-falta", Map.of(
                    "costo", nombrePlano(trueque.costo()) + " x" + trueque.costoCantidad()));
            return false;
        }
        ItemStack entrega = new ItemStack(trueque.entrega(), trueque.entregaCantidad());
        if (!InventarioUtil.puedeEntregar(jugador.getInventory(), entrega)) {
            avisar(jugador, "vecinos.trueque-espacio", Map.of("npc", definicion.nombre()));
            return false;
        }
        if (!InventarioUtil.quitar(jugador, trueque.costo(), trueque.costoCantidad())) {
            avisar(jugador, "vecinos.trueque-falta", Map.of(
                    "costo", nombrePlano(trueque.costo()) + " x" + trueque.costoCantidad()));
            return false;
        }
        InventarioUtil.entregar(jugador, entrega);
        auditar(jugador.getUniqueId(), "npc.trueque", "npc=" + definicion.id(),
                "trueque=" + trueque.id());
        avisar(jugador, "vecinos.trueque-ok", Map.of("npc", definicion.nombre()));
        return true;
    }

    // ------------------------------------------------------------------ administracion

    public boolean colocar(String npcId, Location lugar) {
        String id = npcId == null ? "" : npcId.toLowerCase(Locale.ROOT);
        if (!definiciones.containsKey(id) || lugar.getWorld() == null) {
            return false;
        }
        quitarEntidad(id);
        ubicaciones.put(id, new NpcLocation(lugar.getWorld().getName(), lugar.getX(),
                lugar.getY(), lugar.getZ(), lugar.getYaw(), lugar.getPitch()));
        guardarUbicaciones();
        spawnear(id, ubicaciones.get(id));
        return true;
    }

    public boolean quitar(String npcId) {
        String id = npcId == null ? "" : npcId.toLowerCase(Locale.ROOT);
        if (!ubicaciones.containsKey(id)) {
            return false;
        }
        quitarEntidad(id);
        ubicaciones.remove(id);
        guardarUbicaciones();
        return true;
    }

    private void quitarEntidad(String npcId) {
        UUID uuid = entidades.remove(npcId);
        if (uuid != null) {
            Entity entidad = Bukkit.getEntity(uuid);
            if (entidad != null) {
                entidad.remove();
            }
        }
        NpcLocation guardada = ubicaciones.get(npcId);
        if (guardada == null) {
            return;
        }
        Location lugar = guardada.aLocation();
        if (lugar == null || lugar.getWorld() == null) {
            return;
        }
        int chunkX = lugar.getBlockX() >> 4;
        int chunkZ = lugar.getBlockZ() >> 4;
        if (!lugar.getWorld().isChunkLoaded(chunkX, chunkZ)) {
            return;
        }
        for (Entity entidad : lugar.getWorld().getChunkAt(chunkX, chunkZ).getEntities()) {
            if (npcId.equals(fabrica.npcId(entidad))) {
                entidad.remove();
            }
        }
    }

    /** Recarga npcs.yml, npcs-skins.yml y las ubicaciones; vuelve a spawnear. */
    public int recargar() {
        Set<String> antes = Set.copyOf(definiciones.keySet());
        int cargados = cargar();
        for (String id : antes) {
            if (!definiciones.containsKey(id)) {
                UUID uuid = entidades.remove(id);
                Entity entidad = uuid == null ? null : Bukkit.getEntity(uuid);
                if (entidad != null) {
                    entidad.remove();
                }
                log.info("NPC '" + id + "' ya no esta en npcs.yml: se elimino la entidad.");
            }
        }
        spawnearTodos();
        return cargados;
    }

    public void precargarReputacion(Player jugador) {
        reputacion.precargar(jugador.getUniqueId());
    }

    public void olvidarJugador(Player jugador) {
        reputacion.olvidar(jugador.getUniqueId());
    }

    public void reputacionDe(UUID jugador, String npcId, java.util.function.Consumer<Integer> callback) {
        repositorio.reputacion(jugador, npcId).thenAccept(valor ->
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(valor)));
    }

    public Collection<NpcDefinition> definiciones() {
        return List.copyOf(definiciones.values());
    }

    public NpcDefinition definicion(String npcId) {
        return npcId == null ? null : definiciones.get(npcId.toLowerCase(Locale.ROOT));
    }

    public NpcLocation ubicacion(String npcId) {
        return npcId == null ? null : ubicaciones.get(npcId.toLowerCase(Locale.ROOT));
    }

    public int ubicados() {
        return ubicaciones.size();
    }

    public void detener() {
        entidades.clear();
        regalosEnProceso.clear();
    }

    private void avisar(Player jugador, String clave, Map<String, String> reemplazos) {
        Textos.enviar(jugador, Textos.prefijo(ctx) + Textos.msg(ctx, clave, reemplazos));
    }

    private void auditar(UUID actor, String accion, String target, String detalles) {
        try {
            ctx.audit().registrar(actor, accion, target, detalles);
        } catch (RuntimeException ignorado) {
            log.fine("Auditoria no disponible en " + accion + ": " + ignorado.getMessage());
        }
    }

    private static String nombrePlano(Material material) {
        String base = material.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(base.charAt(0)) + base.substring(1);
    }
}
