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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
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
 */
public final class MarketService {

    /** Resultado de una compra. El texto asociado vive en messages.yml. */
    public enum ResultadoCompra {
        OK("mercado.compra-ok"),
        SIN_DIAMANTES("mercado.sin-diamantes"),
        SIN_ESPACIO("mercado.sin-espacio"),
        LIMITE("mercado.limite"),
        ITEM_DESCONOCIDO("mercado.desconocido");

        private final String claveMensaje;

        ResultadoCompra(String claveMensaje) {
            this.claveMensaje = claveMensaje;
        }

        public String claveMensaje() {
            return claveMensaje;
        }
    }

    private static final long VENTANA_MS = 60_000L;

    private final JavaPlugin plugin;
    private final IAuditoria auditoria;
    private final MarketRepository repositorio;
    private final Logger log;
    private final int maxComprasPorMinuto;
    private final Map<String, MarketCategory> categorias = new LinkedHashMap<>();
    private final Map<UUID, Deque<Long>> ventanas = new ConcurrentHashMap<>();
    private String titulo = "&8&lMercado de Mushoku";

    public MarketService(JavaPlugin plugin, MarketRepository repositorio, IAuditoria auditoria,
                         Logger log, int maxComprasPorMinuto) {
        this.plugin = plugin;
        this.repositorio = repositorio;
        this.auditoria = auditoria;
        this.log = log;
        this.maxComprasPorMinuto = Math.max(1, maxComprasPorMinuto);
    }

    // ------------------------------------------------------------------ catalogo

    /**
     * Carga {@code market.yml} desde el data folder y cae al recurso del JAR.
     * @return cantidad de items cargados
     */
    public int cargarCatalogo() {
        YamlConfiguration yaml = leerYaml();
        categorias.clear();
        if (yaml == null) {
            log.warning("market.yml no encontrado (ni en el data folder ni en el JAR). "
                    + "El modulo 'market' queda sin ofertas.");
            return 0;
        }
        String tituloYaml = yaml.getString("titulo");
        if (tituloYaml != null && !tituloYaml.isBlank()) {
            titulo = tituloYaml;
        }
        ConfigurationSection seccion = yaml.getConfigurationSection("categories");
        if (seccion == null) {
            log.warning("market.yml no tiene la seccion 'categories'.");
            return 0;
        }

        List<Integer> usados = new ArrayList<>();
        int items = 0;
        int siguienteSlot = 10;
        for (String id : seccion.getKeys(false)) {
            ConfigurationSection cruda = seccion.getConfigurationSection(id);
            if (cruda == null) {
                continue;
            }
            String tituloCategoria = cruda.getString("title", id);
            Material icono = material(cruda.getString("icon", "CHEST"));
            if (icono == null || !icono.isItem()) {
                icono = Material.CHEST;
            }
            int slot = cruda.getInt("slot", siguienteSlot);
            if (slot < 0 || slot > 26 || usados.contains(slot)) {
                slot = siguienteSlot;
                while (usados.contains(slot) && slot <= 26) {
                    slot++;
                }
                if (slot > 26) {
                    log.warning("market.yml: no quedan slots libres para la categoria '" + id + "'.");
                    continue;
                }
            }
            usados.add(slot);
            siguienteSlot = slot + 1;

            List<MarketItem> ofertas = new ArrayList<>();
            ConfigurationSection itemsYaml = cruda.getConfigurationSection("items");
            if (itemsYaml != null) {
                for (String itemId : itemsYaml.getKeys(false)) {
                    ConfigurationSection itemYaml = itemsYaml.getConfigurationSection(itemId);
                    if (itemYaml == null) {
                        continue;
                    }
                    MarketItem item = parsearItem(id, itemId, itemYaml);
                    if (item == null) {
                        continue;
                    }
                    ofertas.add(item);
                    items++;
                }
            }
            if (ofertas.isEmpty()) {
                log.warning("market.yml: la categoria '" + id + "' no tiene items validos; se omite.");
                continue;
            }
            categorias.put(id.toLowerCase(Locale.ROOT),
                    new MarketCategory(id.toLowerCase(Locale.ROOT), tituloCategoria, icono, slot, ofertas));
        }
        if (categorias.isEmpty()) {
            log.warning("market.yml no dejo ninguna categoria valida. Revisar materiales y precios.");
        }
        return items;
    }

    private MarketItem parsearItem(String categoriaId, String itemId, ConfigurationSection yaml) {
        Material material = material(yaml.getString("material", null));
        if (material == null || !material.isItem()) {
            log.warning("market.yml: " + categoriaId + "." + itemId + " tiene un material invalido ("
                    + yaml.getString("material") + "); se omite.");
            return null;
        }
        int amount = yaml.getInt("amount", 1);
        int precio = yaml.getInt("price-diamonds", 0);
        MarketItem item = new MarketItem(itemId.toLowerCase(Locale.ROOT), material, amount, precio,
                yaml.getString("name"), yaml.getStringList("lore"));
        if (!item.valido()) {
            log.warning("market.yml: " + categoriaId + "." + itemId
                    + " necesita 1 <= amount <= " + Math.max(1, material.getMaxStackSize())
                    + " y price-diamonds > 0; se omite.");
            return null;
        }
        return item;
    }

    private YamlConfiguration leerYaml() {
        File editable = new File(plugin.getDataFolder(), "market.yml");
        if (editable.exists()) {
            return YamlConfiguration.loadConfiguration(editable);
        }
        try (InputStream recurso = plugin.getResource("market.yml")) {
            if (recurso == null) {
                return null;
            }
            return YamlConfiguration.loadConfiguration(
                    new InputStreamReader(recurso, StandardCharsets.UTF_8));
        } catch (Exception excepcion) {
            log.log(Level.SEVERE, "No se pudo leer market.yml del JAR.", excepcion);
            return null;
        }
    }

    private static Material material(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        return Material.matchMaterial(nombre.trim().toUpperCase(Locale.ROOT));
    }

    // ------------------------------------------------------------------ consulta

    public String titulo() {
        return titulo;
    }

    public Collection<MarketCategory> categorias() {
        return List.copyOf(categorias.values());
    }

    public MarketCategory categoria(String id) {
        return id == null ? null : categorias.get(id.toLowerCase(Locale.ROOT));
    }

    public MarketCategory categoriaPorSlot(int slot) {
        for (MarketCategory categoria : categorias.values()) {
            if (categoria.slot() == slot) {
                return categoria;
            }
        }
        return null;
    }

    public MarketItem itemPorSlot(String categoriaId, int slot) {
        MarketCategory categoria = categoria(categoriaId);
        if (categoria == null || slot < 0 || slot >= categoria.items().size()) {
            return null;
        }
        return categoria.items().get(slot);
    }

    public MarketItem buscarItem(String itemId) {
        for (MarketCategory categoria : categorias.values()) {
            MarketItem item = categoria.item(itemId);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    public boolean sinOfertas() {
        return categorias.isEmpty();
    }

    // ------------------------------------------------------------------ compra

    /**
     * Ejecuta la compra en el hilo principal: cuenta y quita diamantes, entrega el item y
     * registra la operacion. Si no hay espacio, devuelve los diamantes antes de salir.
     */
    public ResultadoCompra comprar(Player jugador, MarketItem item) {
        if (item == null || !item.valido()) {
            return ResultadoCompra.ITEM_DESCONOCIDO;
        }
        if (!dentroDelLimite(jugador.getUniqueId())) {
            return ResultadoCompra.LIMITE;
        }
        int tiene = InventarioUtil.contar(jugador, Material.DIAMOND);
        if (tiene < item.priceDiamonds()) {
            return ResultadoCompra.SIN_DIAMANTES;
        }
        if (!InventarioUtil.quitar(jugador, Material.DIAMOND, item.priceDiamonds())) {
            return ResultadoCompra.SIN_DIAMANTES;
        }

        ItemStack entrega = new ItemStack(item.material(), item.amount());
        if (!InventarioUtil.puedeEntregar(jugador.getInventory(), entrega)) {
            InventarioUtil.entregar(jugador, new ItemStack(Material.DIAMOND, item.priceDiamonds()));
            return ResultadoCompra.SIN_ESPACIO;
        }
        InventarioUtil.entregar(jugador, entrega);
        anotarCompra(jugador.getUniqueId());
        repositorio.registrar(jugador.getUniqueId(), item.id(), item.amount(),
                item.priceDiamonds(), TimeUtils.ahoraIso())
                .exceptionally(error -> {
                    log.log(Level.WARNING, "No se pudo registrar la compra de '" + item.id()
                            + "' de " + jugador.getName(), error);
                    return null;
                });
        try {
            auditoria.registrar(jugador.getUniqueId(), "mercado.compra", "item=" + item.id(),
                    "cantidad=" + item.amount() + " diamantes=" + item.priceDiamonds());
        } catch (RuntimeException sinAuditoria) {
            log.fine("Auditoria no disponible en compra de mercado: " + sinAuditoria.getMessage());
        }
        return ResultadoCompra.OK;
    }

    private boolean dentroDelLimite(UUID jugador) {
        Deque<Long> ventana = ventanas.computeIfAbsent(jugador, clave -> new ArrayDeque<>());
        synchronized (ventana) {
            limpiar(ventana);
            return ventana.size() < maxComprasPorMinuto;
        }
    }

    private void anotarCompra(UUID jugador) {
        Deque<Long> ventana = ventanas.computeIfAbsent(jugador, clave -> new ArrayDeque<>());
        synchronized (ventana) {
            limpiar(ventana);
            ventana.addLast(System.currentTimeMillis());
        }
    }

    private static void limpiar(Deque<Long> ventana) {
        long ahora = System.currentTimeMillis();
        while (!ventana.isEmpty() && ahora - ventana.peekFirst() > VENTANA_MS) {
            ventana.pollFirst();
        }
    }
}
