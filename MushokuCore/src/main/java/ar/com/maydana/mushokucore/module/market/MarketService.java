package ar.com.maydana.mushokucore.module.market;

import ar.com.maydana.mushokucore.audit.IAuditoria;
import ar.com.maydana.mushokucore.database.repository.MarketRepository;
import ar.com.maydana.mushokucore.util.InventarioUtil;
import ar.com.maydana.mushokucore.util.TimeUtils;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Catalogo y compras del mercado de administracion.
 *
 * <p>La moneda son diamantes fisicos del inventario: no hay Vault, ni saldo virtual, ni
 * venta al mercado en esta version. El servicio no habla con el jugador; devuelve un
 * {@link ResultadoCompra} y el comando o la GUI arman el mensaje.</p>
 *
 * <p>Disponibilidad: una oferta puede restringirse por bioma ({@code biomes}) y/o rotar
 * en el tiempo ({@code rotation}, deterministica por dia/semana UTC sin estado en BD).
 * Si una oferta no esta disponible para el jugador, no se muestra ni se puede comprar.</p>
 */
public final class MarketService {

    /** Resultado de una compra. El texto asociado vive en messages.yml. */
    public enum ResultadoCompra {
        OK("mercado.compra-ok"),
        SIN_DIAMANTES("mercado.sin-diamantes"),
        SIN_ESPACIO("mercado.sin-espacio"),
        LIMITE("mercado.limite"),
        NO_DISPONIBLE("mercado.no-disponible"),
        INVALIDO("mercado.invalido");

        private final String claveMensaje;

        ResultadoCompra(String claveMensaje) {
            this.claveMensaje = claveMensaje;
        }

        public String claveMensaje() {
            return claveMensaje;
        }
    }

    private static final long UN_MINUTO_MS = 60_000L;

    private final JavaPlugin plugin;
    private final MarketRepository repo;
    private final IAuditoria audit;
    private final Logger log;
    private final int maxComprasPorMinuto;

    private final Map<UUID, Deque<Long>> comprasRecientes = new ConcurrentHashMap<>();

    private volatile String titulo = "&8&lMercado de Mushoku";
    private volatile Map<String, MarketCategory> categorias = new LinkedHashMap<>();
    private volatile Map<String, MarketItem> indice = new LinkedHashMap<>();

    public MarketService(JavaPlugin plugin, MarketRepository repo, IAuditoria audit, Logger log,
                         int maxComprasPorMinuto) {
        this.plugin = plugin;
        this.repo = repo;
        this.audit = audit;
        this.log = log;
        this.maxComprasPorMinuto = Math.max(1, maxComprasPorMinuto);
    }

    /** Carga o recarga {@code market.yml} desde el data folder del plugin. */
    public synchronized void recargar() {
        File archivo = new File(plugin.getDataFolder(), "market.yml");
        if (!archivo.isFile()) {
            copiarDefault(archivo);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(archivo);
        aplicar(yaml);
    }

    private void copiarDefault(File destino) {
        try (InputStream origen = plugin.getResource("market.yml")) {
            if (origen == null) {
                log.warning("market.yml no existe en el jar ni en disco; el mercado quedara vacio.");
                return;
            }
            if (!destino.getParentFile().exists() && !destino.getParentFile().mkdirs()) {
                log.warning("No se pudo crear la carpeta de datos para market.yml.");
            }
            java.nio.file.Files.copy(origen, destino.toPath());
        } catch (Exception ex) {
            log.log(Level.WARNING, "No se pudo copiar el market.yml por defecto.", ex);
        }
    }

    private void aplicar(YamlConfiguration yaml) {
        titulo = yaml.getString("titulo", "&8&lMercado de Mushoku");
        Map<String, MarketCategory> nuevasCategorias = new LinkedHashMap<>();
        Map<String, MarketItem> nuevoIndice = new LinkedHashMap<>();

        ConfigurationSection raizCategorias = yaml.getConfigurationSection("categories");
        if (raizCategorias == null) {
            this.categorias = nuevasCategorias;
            this.indice = nuevoIndice;
            return;
        }

        for (String idCategoria : raizCategorias.getKeys(false)) {
            ConfigurationSection secCat = raizCategorias.getConfigurationSection(idCategoria);
            if (secCat == null) {
                continue;
            }
            String tituloCat = secCat.getString("title", "&f" + idCategoria);
            Material icono = parsearMaterial(secCat.getString("icon", "CHEST"));
            int slot = secCat.getInt("slot", -1);
            List<MarketItem> items = cargarItems(secCat.getConfigurationSection("items"), idCategoria);

            MarketCategory categoria = new MarketCategory(idCategoria, tituloCat, icono, slot, items);
            nuevasCategorias.put(idCategoria.toLowerCase(Locale.ROOT), categoria);
            for (MarketItem item : items) {
                nuevoIndice.put(item.id().toLowerCase(Locale.ROOT), item);
            }
        }

        this.categorias = nuevasCategorias;
        this.indice = nuevoIndice;
    }

    private List<MarketItem> cargarItems(ConfigurationSection seccion, String idCategoria) {
        List<MarketItem> resultado = new ArrayList<>();
        if (seccion == null) {
            return resultado;
        }
        for (String idItem : seccion.getKeys(false)) {
            ConfigurationSection sec = seccion.getConfigurationSection(idItem);
            if (sec == null) {
                continue;
            }
            MarketItem item = construirItem(idItem, sec, idCategoria);
            if (item != null && item.valido()) {
                resultado.add(item);
            } else {
                log.warning("Oferta invalida omitida en market.yml: " + idCategoria + "." + idItem);
            }
        }
        return resultado;
    }

    private MarketItem construirItem(String idItem, ConfigurationSection sec, String idCategoria) {
        String materialNombre = sec.getString("material", "");
        Material material = parsearMaterial(materialNombre);
        int amount = sec.getInt("amount", 0);
        int precio = sec.getInt("price-diamonds", sec.getInt("price", 0));
        String nombre = sec.getString("name", null);
        List<String> lore = sec.getStringList("lore");
        Set<String> biomes = parsearBiomas(sec);
        MarketRotation rotation = parsearRotacion(sec, idItem);
        if (material == null) {
            log.warning("Material desconocido '" + materialNombre + "' en " + idCategoria + "." + idItem);
            return null;
        }
        return new MarketItem(idItem, material, amount, precio, nombre, lore, biomes, rotation);
    }

    private Set<String> parsearBiomas(ConfigurationSection sec) {
        List<String> lista = new ArrayList<>();
        if (sec.isList("biomes")) {
            lista.addAll(sec.getStringList("biomes"));
        } else if (sec.isString("biomes")) {
            String raw = sec.getString("biomes", "");
            if (!raw.isBlank()) {
                for (String parte : raw.split("[,;]")) {
                    String t = parte.trim();
                    if (!t.isEmpty()) {
                        lista.add(t);
                    }
                }
            }
        } else if (sec.isString("biome")) {
            String raw = sec.getString("biome", "");
            if (!raw.isBlank()) {
                lista.add(raw.trim());
            }
        }
        if (lista.isEmpty()) {
            return Set.of();
        }
        Set<String> out = new LinkedHashSet<>();
        for (String b : lista) {
            if (b != null && !b.isBlank()) {
                out.add(normalizarClave(b));
            }
        }
        return out.isEmpty() ? Set.of() : Set.copyOf(out);
    }

    private MarketRotation parsearRotacion(ConfigurationSection sec, String itemId) {
        ConfigurationSection rot = sec.getConfigurationSection("rotation");
        if (rot == null) {
            // soporte compacto: rotation: daily / weekly
            String simple = sec.getString("rotation", null);
            if (simple == null || simple.isBlank()) {
                return null;
            }
            MarketRotation.Period p = MarketRotation.Period.from(simple);
            return new MarketRotation(p, 1, 0);
        }
        MarketRotation.Period period = MarketRotation.Period.from(rot.getString("period", "daily"));
        int every = Math.max(1, rot.getInt("every", rot.getInt("cada", 1)));
        int offset;
        if (rot.contains("offset")) {
            offset = rot.getInt("offset");
        } else {
            offset = MarketRotation.offsetEstable(itemId, every);
        }
        return new MarketRotation(period, every, offset);
    }

    private Material parsearMaterial(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        Material material = Material.matchMaterial(nombre.trim());
        if (material == null) {
            material = Material.matchMaterial(nombre.trim().toUpperCase(Locale.ROOT));
        }
        return material;
    }

    private static String normalizarClave(String raw) {
        return raw.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    public String titulo() {
        return titulo;
    }

    public Collection<MarketCategory> categorias() {
        return categorias.values();
    }

    public MarketCategory categoria(String id) {
        if (id == null) {
            return null;
        }
        return categorias.get(id.toLowerCase(Locale.ROOT));
    }

    public MarketItem item(String itemId) {
        if (itemId == null) {
            return null;
        }
        return indice.get(itemId.toLowerCase(Locale.ROOT));
    }

    /** Indica si una oferta esta disponible para el jugador (bioma y rotacion). */
    public boolean disponible(MarketItem item, Player jugador) {
        if (item == null || jugador == null) {
            return false;
        }
        if (item.tieneRotacion() && !item.rotation().estaActiva(item.id(), System.currentTimeMillis())) {
            return false;
        }
        if (item.tieneFiltroBioma()) {
            String actual = biomeActual(jugador);
            return actual != null && item.biomes().contains(actual);
        }
        return true;
    }

    /** Bioma actual del jugador, normalizado (lowercase). */
    public String biomeActual(Player jugador) {
        if (jugador == null || jugador.getLocation() == null || jugador.getLocation().getWorld() == null) {
            return null;
        }
        try {
            Biome biome = jugador.getLocation().getBlock().getBiome();
            // Paper/modern: getKey() existe; fallback a name()
            try {
                var key = biome.getKey();
                if (key != null) {
                    return key.getKey().toLowerCase(Locale.ROOT);
                }
            } catch (Throwable ignored) {
                // API antigua sin NamespacedKey#getKey en este contexto
            }
            return biome.name().toLowerCase(Locale.ROOT);
        } catch (Throwable t) {
            return null;
        }
    }

    /** Items de una categoria visibles/disponibles para el jugador. */
    public List<MarketItem> itemsDisponibles(MarketCategory categoria, Player jugador) {
        if (categoria == null) {
            return List.of();
        }
        List<MarketItem> resultado = new ArrayList<>();
        for (MarketItem item : categoria.items()) {
            if (disponible(item, jugador)) {
                resultado.add(item);
            }
        }
        return resultado;
    }

    /** Categorias que tienen al menos un item disponible para el jugador. */
    public List<MarketCategory> categoriasDisponibles(Player jugador) {
        List<MarketCategory> resultado = new ArrayList<>();
        for (MarketCategory cat : categorias.values()) {
            if (!itemsDisponibles(cat, jugador).isEmpty()) {
                resultado.add(cat);
            }
        }
        return resultado;
    }

    public boolean estaVacio() {
        return indice.isEmpty();
    }

    /**
     * Ejecuta la compra en el hilo principal (toca inventario Bukkit). Registra la compra
     * y el evento de auditoria de forma asincrona.
     */
    public ResultadoCompra comprar(Player jugador, String itemId) {
        MarketItem item = item(itemId);
        if (item == null || !item.valido()) {
            return ResultadoCompra.INVALIDO;
        }
        if (!disponible(item, jugador)) {
            return ResultadoCompra.NO_DISPONIBLE;
        }
        if (!puedeComprar(jugador.getUniqueId())) {
            return ResultadoCompra.LIMITE;
        }
        if (InventarioUtil.contar(jugador, Material.DIAMOND) < item.priceDiamonds()) {
            return ResultadoCompra.SIN_DIAMANTES;
        }
        if (!InventarioUtil.cabe(jugador, item.material(), item.amount())) {
            return ResultadoCompra.SIN_ESPACIO;
        }

        if (!InventarioUtil.quitar(jugador, Material.DIAMOND, item.priceDiamonds())) {
            return ResultadoCompra.SIN_DIAMANTES;
        }
        Map<Integer, ItemStack> sobrante = jugador.getInventory()
                .addItem(new ItemStack(item.material(), item.amount()));
        if (!sobrante.isEmpty()) {
            // No deberia pasar tras la comprobacion de espacio, pero devolvemos los diamantes.
            for (ItemStack resto : sobrante.values()) {
                int devuelto = item.amount() - resto.getAmount();
                if (devuelto > 0) {
                    InventarioUtil.quitar(jugador, item.material(), devuelto);
                }
            }
            jugador.getInventory().addItem(new ItemStack(Material.DIAMOND, item.priceDiamonds()));
            return ResultadoCompra.SIN_ESPACIO;
        }

        registrarCompra(jugador.getUniqueId());
        String ahora = TimeUtils.ahoraIso();
        repo.registrar(jugador.getUniqueId(), item.id(), item.amount(), item.priceDiamonds(), ahora)
                .exceptionally(ex -> {
                    log.log(Level.WARNING, "No se pudo registrar la compra en market_purchases.", ex);
                    return null;
                });
        audit.registrar(jugador.getUniqueId(), "MARKET_BUY", null,
                "item=" + item.id() + " amount=" + item.amount() + " price=" + item.priceDiamonds());
        return ResultadoCompra.OK;
    }

    private boolean puedeComprar(UUID uuid) {
        if (maxComprasPorMinuto <= 0) {
            return true;
        }
        long ahora = System.currentTimeMillis();
        Deque<Long> cola = comprasRecientes.computeIfAbsent(uuid, k -> new ArrayDeque<>());
        synchronized (cola) {
            while (!cola.isEmpty() && ahora - cola.peekFirst() > UN_MINUTO_MS) {
                cola.pollFirst();
            }
            return cola.size() < maxComprasPorMinuto;
        }
    }

    private void registrarCompra(UUID uuid) {
        if (maxComprasPorMinuto <= 0) {
            return;
        }
        long ahora = System.currentTimeMillis();
        Deque<Long> cola = comprasRecientes.computeIfAbsent(uuid, k -> new ArrayDeque<>());
        synchronized (cola) {
            while (!cola.isEmpty() && ahora - cola.peekFirst() > UN_MINUTO_MS) {
                cola.pollFirst();
            }
            cola.addLast(ahora);
        }
    }

    /** Cantidad de compras en el ultimo minuto (para mostrar en /mercado info). */
    public int comprasRecientes(UUID uuid) {
        Deque<Long> cola = comprasRecientes.get(uuid);
        if (cola == null) {
            return 0;
        }
        long ahora = System.currentTimeMillis();
        synchronized (cola) {
            while (!cola.isEmpty() && ahora - cola.peekFirst() > UN_MINUTO_MS) {
                cola.pollFirst();
            }
            return cola.size();
        }
    }

    public int limitePorMinuto() {
        return maxComprasPorMinuto;
    }

    /** Carga inicial (llamar en onEnable). */
    public void cargarInicial() {
        recargar();
        // asegurar carpeta de datos (para que market.yml quede visible)
        if (!plugin.getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            plugin.getDataFolder().mkdirs();
        }
    }

    // compat: algunos call sites pueden pasar input stream en el futuro; no usado ahora.
    @SuppressWarnings("unused")
    private YamlConfiguration leer(InputStream in) {
        return YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
    }
}
