package ar.com.maydana.mushokucore.util;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Conversion de los textos legacy de messages.yml ({@code &a}, {@code &l}) a componentes
 * de Adventure, que es lo que acepta la API moderna de {@code ItemMeta} y de nombres de
 * entidad.
 *
 * <p>Los textos de chat siguen saliendo por {@code Textos.enviar} con {@code String}; esto
 * es solo para inventarios y entidades, donde los metodos de {@code String} quedaron
 * deprecados.</p>
 */
public final class Componentes {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacyAmpersand();

    private Componentes() {
    }

    public static Component de(String texto) {
        return LEGACY.deserialize(texto == null ? "" : texto);
    }

    public static List<Component> lista(List<String> lineas) {
        List<Component> resultado = new ArrayList<>();
        if (lineas == null) {
            return resultado;
        }
        for (String linea : lineas) {
            resultado.add(de(linea));
        }
        return resultado;
    }
}
