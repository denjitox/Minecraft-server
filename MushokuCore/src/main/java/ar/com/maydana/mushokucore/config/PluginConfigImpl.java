package ar.com.maydana.mushokucore.config;

import java.util.LinkedHashSet;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Implementación inmutable de {@link PluginConfig} — agente A (infra-core), plan §6.1/§4.2.
 * Se construye UNA vez a partir del YAML validado por {@link ConfigService} y no cambia
 * en toda la vida del plugin (reload = reconstruir + re-proveer, decisión del integrador).
 *
 * <p>Todos los defaults coinciden con el config.yml del jar (plan §4.2, resource
 * verificado en disco 2026-10-17). Las claves marcadas [DECISIÓN Dn] usan la propuesta
 * del plan §9; el operador puede cambiarlas en config.yml.</p>
 */
final class PluginConfigImpl implements PluginConfig {

    private final int schema;
    private final boolean enabled;
    private final String storageType;
    private final String storageFile;
    private final int busyTimeoutMs;
    private final boolean modRanks;
    private final boolean modFactions;
    private final boolean modDiseases;
    private final boolean modRelics;
    private final boolean modDimensions;
    private final boolean modBedrock;
    // market / npcs
    private final boolean modMarket;
    private final boolean modNpcs;
    private final int mercadoMaxComprasMinuto;
    private final int npcsReputacionAlta;
    private final int npcsReputacionBaja;
    private final int npcsRutinaSegundos;
    private final String mundoNombre;
    private final int bordeDiametro;
    private final int centroX;
    private final int centroZ;
    private final int minY;
    private final int maxY;
    private final boolean netherSellado;
    private final boolean endSellado;
    private final int diasDesbloqueo;
    private final boolean bypassAdmin;
    private final int miembrosMin;
    private final int miembrosMax;
    private final int claimsBase;
    private final int claimsPorMiembro;
    private final int claimsMax;
    private final int diasBloqueo;
    private final int diasDesertor;
    private final String nacionOrsted;
    private final String nacionApostoles;
    private final int pvpCooldownHoras;
    private final int contribucionExpiraMin;
    private final double contribucionMinimaPct;
    private final boolean anuncioGlobal;
    private final int victimaMinConectada;
    private final Set<String> mundosExcluidosPvp;
    private final Set<String> mundosValidosJefes;
    private final int flushSegundos;
    private final int reconciliacionMin;
    private final int chequeoTicks;
    private final double multiplicadorOrsted;
    private final String permisoOrsted;
    private final boolean forms;
    private final boolean fallbackComando;
    private final int auditRetencionDias;
    // regional-difficulty
    private final boolean modRegionalDifficulty;
    private final boolean regionalDifficultyHabilitado;
    private final int regionalDifficultyCentroX;
    private final int regionalDifficultyCentroZ;
    private final boolean regionalDifficultyInterpolar;
    private final int regionalDifficultyBlendDistancia;
    private final java.util.List<RegionalDifficultyZone> regionalDifficultyZonas;
    private final java.util.Map<String, RegionalDifficultyDimension> regionalDifficultyDimensiones;
    private final java.util.Set<String> regionalDifficultyExcluidas;
    private final boolean regionalDifficultyTrialSpawnerLimite;
    private final double regionalDifficultyTrialSpawnerMaxHealthMultiplier;

    /**
     * @param yaml          config.yml ya cargado (schema 2) o los defaults del jar.
     * @param forzarApagado true cuando el esquema del archivo no es 2 (fallo seguro §2.5):
     *                      TODOS los módulos quedan en false; nadie escribe en la BD.
     */
    PluginConfigImpl(FileConfiguration yaml, boolean forzarApagado) {
        this.schema = yaml.getInt("schema", 1);
        this.enabled = yaml.getBoolean("enabled", false) && !forzarApagado;
        this.storageType = yaml.getString("storage.type", "sqlite");
        this.storageFile = yaml.getString("storage.file", "mushoku.db");
        this.busyTimeoutMs = yaml.getInt("storage.busy-timeout-ms", 5000);

        this.modRanks = yaml.getBoolean("modules.ranks", false) && !forzarApagado;
        this.modFactions = yaml.getBoolean("modules.factions", false) && !forzarApagado;
        this.modDiseases = yaml.getBoolean("modules.diseases", false) && !forzarApagado;
        this.modRelics = yaml.getBoolean("modules.relics", false) && !forzarApagado;
        this.modDimensions = yaml.getBoolean("modules.dimensions", false) && !forzarApagado;
        this.modBedrock = yaml.getBoolean("modules.bedrock-bridge", false) && !forzarApagado;

        this.modMarket = yaml.getBoolean("modules.market", true) && !forzarApagado;
        this.modNpcs = yaml.getBoolean("modules.npcs", true) && !forzarApagado;
        this.mercadoMaxComprasMinuto = yaml.getInt("market.max-purchases-per-minute", 10);
        this.npcsReputacionAlta = yaml.getInt("npcs.reputation-high", 10);
        this.npcsReputacionBaja = yaml.getInt("npcs.reputation-low", -5);
        this.npcsRutinaSegundos = yaml.getInt("npcs.routine-seconds", 20);

        this.mundoNombre = yaml.getString("world.name", "mushoku_world");
        this.bordeDiametro = yaml.getInt("world.border-diameter", 21800);
        this.centroX = yaml.getInt("world.center-x", 0);
        this.centroZ = yaml.getInt("world.center-z", 0);
        this.minY = yaml.getInt("world.min-y", -64);
        // 511 = DECISIÓN DEL USUARIO 2026-10-17 (V1: el spec §9 decía 320; manda el usuario).
        this.maxY = yaml.getInt("world.max-y", 511);

        this.netherSellado = yaml.getBoolean("dimensions.nether-locked-at-launch", true);
        this.endSellado = yaml.getBoolean("dimensions.end-locked-at-launch", true);
        this.diasDesbloqueo = yaml.getInt("dimensions.minimum-unlock-days", 7);
        this.bypassAdmin = yaml.getBoolean("dimensions.allow-admin-bypass", true);

        this.miembrosMin = yaml.getInt("factions.min-members", 3);
        this.miembrosMax = yaml.getInt("factions.max-members", 8);
        this.claimsBase = yaml.getInt("factions.claims-base", 8);
        this.claimsPorMiembro = yaml.getInt("factions.claims-per-member", 5);
        this.claimsMax = yaml.getInt("factions.claims-max", 48);
        this.diasBloqueo = yaml.getInt("factions.allegiance-lock-days", 14);
        this.diasDesertor = yaml.getInt("factions.deserter-cooldown-days", 7);
        this.nacionOrsted = yaml.getString("factions.fixed-nations.orsted", "Orsted_Corp");
        this.nacionApostoles = yaml.getString("factions.fixed-nations.apostles", "Apostoles_Humanos");

        this.pvpCooldownHoras = yaml.getInt("ranks.pvp-same-victim-cooldown-hours", 12);
        this.contribucionExpiraMin = yaml.getInt("ranks.boss-contribution-expiry-minutes", 60);
        this.contribucionMinimaPct = yaml.getDouble("ranks.minimum-contribution-percent", 1.0);
        this.anuncioGlobal = yaml.getBoolean("ranks.announce-global", true);
        this.victimaMinConectada = yaml.getInt("ranks.min-victim-online-seconds", 60); // [D6]
        this.mundosExcluidosPvp = conjunto(yaml, "ranks.excluded-worlds");              // [D7]
        this.mundosValidosJefes = conjunto(yaml, "ranks.valid-boss-worlds");             // [D7]
        this.flushSegundos = yaml.getInt("ranks.boss-buffer-flush-seconds", 5);
        this.reconciliacionMin = yaml.getInt("ranks.reconcile-minutes", 30);

        this.chequeoTicks = yaml.getInt("diseases.tick-check-ticks", 100);
        this.multiplicadorOrsted = yaml.getDouble("diseases.orsted-chance-multiplier", 0.5);
        this.permisoOrsted = yaml.getString("diseases.orsted-permission", "mt.alianza.orsted"); // [D11]

        this.forms = yaml.getBoolean("bedrock.enable-forms", false); // forms = 1.3 (V10)
        this.fallbackComando = yaml.getBoolean("bedrock.always-provide-command-fallback", true);

        this.auditRetencionDias = yaml.getInt("audit.retention-days", 7); // DECISION D17-D20 (2026-10-18)

        this.modRegionalDifficulty = yaml.getBoolean("modules.regional-difficulty", false) && !forzarApagado;
        this.regionalDifficultyHabilitado = yaml.getBoolean("regional-difficulty.enabled", true);
        this.regionalDifficultyCentroX = yaml.getInt("regional-difficulty.center-x", 0);
        this.regionalDifficultyCentroZ = yaml.getInt("regional-difficulty.center-z", 0);
        this.regionalDifficultyInterpolar = yaml.getBoolean("regional-difficulty.interpolate", true);
        this.regionalDifficultyBlendDistancia = yaml.getInt("regional-difficulty.blend-distance", 200);

        this.regionalDifficultyZonas = parseZonas(yaml);
        this.regionalDifficultyDimensiones = parseDimensiones(yaml);
        this.regionalDifficultyExcluidas = conjunto(yaml, "regional-difficulty.excluded-entities");

        this.regionalDifficultyTrialSpawnerLimite = yaml.getBoolean("regional-difficulty.trial-spawner-limit", true);
        this.regionalDifficultyTrialSpawnerMaxHealthMultiplier = yaml.getDouble("regional-difficulty.trial-spawner-max-health-multiplier", 1.5);
    }

    private java.util.List<RegionalDifficultyZone> parseZonas(FileConfiguration yaml) {
        java.util.List<ZonaParseada> zonasParseadas = new java.util.ArrayList<>();
        int indice = 0;
        for (java.util.Map<?, ?> mapa : yaml.getMapList("regional-difficulty.zones")) {
            indice++;
            Object nombre = mapa.get("name");
            String name = nombre == null ? "zona_" + indice : String.valueOf(nombre);
            int maxRadius = entero(mapa.get("max-radius"), 0);
            int minRadius = entero(mapa.get("min-radius"), Integer.MIN_VALUE);
            double healthMultiplier = decimal(mapa.get("health-multiplier"), 1.0);
            double damageMultiplier = decimal(mapa.get("damage-multiplier"), 1.0);
            if (maxRadius > 0 && Double.isFinite(healthMultiplier) && healthMultiplier > 0.0
                    && Double.isFinite(damageMultiplier) && damageMultiplier > 0.0) {
                zonasParseadas.add(new ZonaParseada(name, minRadius, maxRadius, healthMultiplier, damageMultiplier));
            }
        }
        if (zonasParseadas.isEmpty()) {
            zonasParseadas.add(new ZonaParseada("zona_cercana", 0, 2000, 1.10, 1.10));
            zonasParseadas.add(new ZonaParseada("zona_media", 2001, 6000, 1.20, 1.15));
            zonasParseadas.add(new ZonaParseada("zona_lejana", 6001, 10900, 1.35, 1.25));
        } else {
            zonasParseadas.sort(java.util.Comparator.comparingInt(zona -> zona.maxRadius));
            int ultimoMax = -1;
            for (int i = 0; i < zonasParseadas.size(); i++) {
                ZonaParseada zona = zonasParseadas.get(i);
                int minCalc = zona.minRadius;
                if (minCalc < 0) {
                    if (i == 0) {
                        minCalc = 0;
                    } else {
                        minCalc = ultimoMax + 1;
                    }
                }
                if (minCalc > zona.maxRadius) {
                    minCalc = i == 0 ? 0 : ultimoMax + 1;
                }
                zonasParseadas.set(i, new ZonaParseada(zona.name, minCalc, zona.maxRadius, zona.healthMultiplier, zona.damageMultiplier));
                ultimoMax = zona.maxRadius;
            }
        }

        java.util.List<RegionalDifficultyZone> zonas = new java.util.ArrayList<>();
        for (ZonaParseada zona : zonasParseadas) {
            zonas.add(new RegionalDifficultyZoneImpl(zona.name, zona.minRadius, zona.maxRadius, zona.healthMultiplier, zona.damageMultiplier));
        }
        return java.util.List.copyOf(zonas);
    }

    private static final class ZonaParseada {
        private final String name;
        private final int minRadius;
        private final int maxRadius;
        private final double healthMultiplier;
        private final double damageMultiplier;

        ZonaParseada(String name, int minRadius, int maxRadius, double healthMultiplier, double damageMultiplier) {
            this.name = name;
            this.minRadius = minRadius;
            this.maxRadius = maxRadius;
            this.healthMultiplier = healthMultiplier;
            this.damageMultiplier = damageMultiplier;
        }
    }

    private static int entero(Object valor, int porDefecto) {
        return valor instanceof Number numero ? numero.intValue() : porDefecto;
    }

    private static double decimal(Object valor, double porDefecto) {
        return valor instanceof Number numero ? numero.doubleValue() : porDefecto;
    }

    private java.util.Map<String, RegionalDifficultyDimension> parseDimensiones(FileConfiguration yaml) {
        java.util.Map<String, RegionalDifficultyDimension> dims = new java.util.LinkedHashMap<>();
        ConfigurationSection seccion = yaml.getConfigurationSection("regional-difficulty.dimensions");
        if (seccion != null) {
            for (String key : seccion.getKeys(false)) {
                ConfigurationSection dimSec = seccion.getConfigurationSection(key);
                if (dimSec != null) {
                    double healthMultiplier = dimSec.getDouble("health-multiplier", 1.0);
                    double damageMultiplier = dimSec.getDouble("damage-multiplier", 1.0);
                    dims.put(key, new RegionalDifficultyDimensionImpl(healthMultiplier, damageMultiplier));
                }
            }
        }
        if (dims.isEmpty()) {
            dims.put("the_nether", new RegionalDifficultyDimensionImpl(1.40, 1.30));
            dims.put("the_end", new RegionalDifficultyDimensionImpl(1.50, 1.35));
        }
        return java.util.Map.copyOf(dims);
    }

    private static Set<String> conjunto(FileConfiguration yaml, String ruta) {
        ConfigurationSection seccion = yaml.getConfigurationSection(ruta);
        if (seccion != null) {
            return Set.copyOf(seccion.getKeys(false));
        }
        var lista = yaml.getStringList(ruta);
        return lista.isEmpty() ? Set.of() : Set.copyOf(new LinkedHashSet<>(lista));
    }

    @Override public int schema() { return schema; }
    @Override public boolean enabled() { return enabled; }
    @Override public String storageType() { return storageType; }
    @Override public String storageFile() { return storageFile; }
    @Override public int busyTimeoutMs() { return busyTimeoutMs; }
    @Override public boolean moduloRanks() { return modRanks; }
    @Override public boolean moduloFactions() { return modFactions; }
    @Override public boolean moduloDiseases() { return modDiseases; }
    @Override public boolean moduloRelics() { return modRelics; }
    @Override public boolean moduloDimensions() { return modDimensions; }
    @Override public boolean moduloBedrockBridge() { return modBedrock; }
    @Override public boolean moduloMarket() { return modMarket; }
    @Override public boolean moduloNpcs() { return modNpcs; }
    @Override public int mercadoMaxComprasPorMinuto() { return mercadoMaxComprasMinuto; }
    @Override public int npcsReputacionAlta() { return npcsReputacionAlta; }
    @Override public int npcsReputacionBaja() { return npcsReputacionBaja; }
    @Override public int npcsRutinaSegundos() { return npcsRutinaSegundos; }
    @Override public String mundoNombre() { return mundoNombre; }
    @Override public int bordeDiametro() { return bordeDiametro; }
    @Override public int centroX() { return centroX; }
    @Override public int centroZ() { return centroZ; }
    @Override public int minY() { return minY; }
    @Override public int maxY() { return maxY; }
    @Override public boolean netherSelladoAlArranque() { return netherSellado; }
    @Override public boolean endSelladoAlArranque() { return endSellado; }
    @Override public int diasDesbloqueoMinimo() { return diasDesbloqueo; }
    @Override public boolean bypassAdminPermitido() { return bypassAdmin; }
    @Override public int miembrosMinimos() { return miembrosMin; }
    @Override public int miembrosMaximos() { return miembrosMax; }
    @Override public int claimsBase() { return claimsBase; }
    @Override public int claimsPorMiembro() { return claimsPorMiembro; }
    @Override public int claimsMaximos() { return claimsMax; }
    @Override public int diasBloqueoAlianza() { return diasBloqueo; }
    @Override public int diasCooldownDesertor() { return diasDesertor; }
    @Override public String nacionOrsted() { return nacionOrsted; }
    @Override public String nacionApostoles() { return nacionApostoles; }
    @Override public int pvpCooldownMismaVictimaHoras() { return pvpCooldownHoras; }
    @Override public int contribucionJefeExpiraMinutos() { return contribucionExpiraMin; }
    @Override public double contribucionMinimaPorcentaje() { return contribucionMinimaPct; }
    @Override public boolean anuncioGlobal() { return anuncioGlobal; }
    @Override public int victimaMinimaConectadaSegundos() { return victimaMinConectada; }
    @Override public Set<String> mundosExcluidosPvp() { return mundosExcluidosPvp; }
    @Override public Set<String> mundosValidosJefes() { return mundosValidosJefes; }
    @Override public int bufferFlushSegundos() { return flushSegundos; }
    @Override public int reconciliacionMinutos() { return reconciliacionMin; }
    @Override public int frecuenciaChequeoTicks() { return chequeoTicks; }
    @Override public double multiplicadorOrsted() { return multiplicadorOrsted; }
    @Override public String permisoOrsted() { return permisoOrsted; }
    @Override public boolean formulariosHabilitados() { return forms; }
    @Override public boolean fallbackComandoSiempre() { return fallbackComando; }
    @Override public int auditRetencionDias() { return auditRetencionDias; }

    @Override public boolean moduloRegionalDifficulty() { return modRegionalDifficulty; }
    @Override public boolean regionalDifficultyHabilitado() { return regionalDifficultyHabilitado; }
    @Override public int regionalDifficultyCentroX() { return regionalDifficultyCentroX; }
    @Override public int regionalDifficultyCentroZ() { return regionalDifficultyCentroZ; }
    @Override public boolean regionalDifficultyInterpolar() { return regionalDifficultyInterpolar; }
    @Override public int regionalDifficultyBlendDistancia() { return regionalDifficultyBlendDistancia; }
    @Override public java.util.List<RegionalDifficultyZone> regionalDifficultyZonas() { return regionalDifficultyZonas; }
    @Override public java.util.Map<String, RegionalDifficultyDimension> regionalDifficultyDimensiones() { return regionalDifficultyDimensiones; }
    @Override public java.util.Set<String> regionalDifficultyExcluidas() { return regionalDifficultyExcluidas; }
    @Override public boolean regionalDifficultyTrialSpawnerLimite() { return regionalDifficultyTrialSpawnerLimite; }
    @Override public double regionalDifficultyTrialSpawnerMaxHealthMultiplier() { return regionalDifficultyTrialSpawnerMaxHealthMultiplier; }

    private static final class RegionalDifficultyZoneImpl implements RegionalDifficultyZone {
        private final String name;
        private final int minRadius;
        private final int maxRadius;
        private final double healthMultiplier;
        private final double damageMultiplier;

        RegionalDifficultyZoneImpl(String name, int maxRadius, double healthMultiplier, double damageMultiplier) {
            this(name, 0, maxRadius, healthMultiplier, damageMultiplier);
        }

        RegionalDifficultyZoneImpl(String name, int minRadius, int maxRadius, double healthMultiplier, double damageMultiplier) {
            this.name = name;
            this.minRadius = minRadius;
            this.maxRadius = maxRadius;
            this.healthMultiplier = healthMultiplier;
            this.damageMultiplier = damageMultiplier;
        }

        @Override public String name() { return name; }
        @Override public int minRadius() { return minRadius; }
        @Override public int maxRadius() { return maxRadius; }
        @Override public double healthMultiplier() { return healthMultiplier; }
        @Override public double damageMultiplier() { return damageMultiplier; }
    }

    private static final class RegionalDifficultyDimensionImpl implements RegionalDifficultyDimension {
        private final double healthMultiplier;
        private final double damageMultiplier;

        RegionalDifficultyDimensionImpl(double healthMultiplier, double damageMultiplier) {
            this.healthMultiplier = healthMultiplier;
            this.damageMultiplier = damageMultiplier;
        }

        @Override public double healthMultiplier() { return healthMultiplier; }
        @Override public double damageMultiplier() { return damageMultiplier; }
    }
}
