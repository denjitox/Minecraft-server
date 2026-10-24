package ar.com.maydana.mushokucore.importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Lector de {@code variables.csv} de Skript — agente D (spec §21 L898, plan §2 {@code import/}).
 *
 * <p><b>Desviación del plan anotada:</b> el plan escribe el paquete {@code import/}, pero
 * {@code import} es <b>palabra reservada de Java</b> y no puede ser segmento de paquete; se usa
 * {@code importer/}. Aviso al integrador (regla 2).</p>
 *
 * <p><b>Formato verificado en el código de Skript</b> ({@code ch.njol.skript.variables.FlatFileStorage},
 * consultado este turno, rama {@code master}):
 * cada línea no vacía y no {@code #} es {@code nombre, tipo, valor}, separados por coma, con
 * comillas dobles para escapar; el campo {@code tipo} == {@code null} significa borrado; el
 * {@code valor} es la serialización del tipo codificada en <b>hexadecimal</b> ({@code encode()}),
 * que se decodifica a bytes y se interpreta con el tipo.</p>
 *
 * <p><b>[A VERIFICAR]</b> la interpretación byte→texto de cada tipo (UTF-8 directo) no está
 * confirmada contra el serializador de Skript 2.16.2 (no se leyó esa clase). Hoy la fuente real
 * ({@code servidor/plugins/Skript/variables.csv}) es <b>solo cabecera</b> (verificado 2026-10-18),
 * así que no hay datos que puedan interpretarse mal; el parser se prueba con fixture.</p>
 */
public final class SkriptVariableParser {

    /** Una variable serializada de Skript. {@code valor} es {@code null} si el tipo no se decodificó. */
    public record Variable(String nombre, String tipo, String valor) {
    }

    private SkriptVariableParser() {
    }

    /**
     * Parsea el archivo completo.
     * @throws IOException si no se puede leer el archivo.
     */
    public static List<Variable> parse(Path archivo) throws IOException {
        List<Variable> variables = new ArrayList<>();
        for (String lineaCruda : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
            String linea = lineaCruda.trim();
            if (linea.isEmpty() || linea.startsWith("#")) {
                continue;   // cabecera y comentarios (formato de Skript)
            }
            String[] campos = dividirCsv(linea);
            if (campos.length != 3) {
                continue;   // línea inválida: se ignora, no se inventa
            }
            String nombre = campos[0];
            String tipo = campos[1];
            if ("null".equalsIgnoreCase(tipo)) {
                continue;   // el tipo "null" significa variable borrada
            }
            variables.add(new Variable(nombre, tipo, decodificar(tipo, campos[2])));
        }
        return variables;
    }

    /**
     * Divide una línea CSV de Skript en 3 campos (coma como separador, {@code "..."} para escapar).
     * Réplica del comportamiento de {@code FlatFileStorage.splitCSV} sin usar su regex.
     */
    static String[] dividirCsv(String linea) {
        List<String> campos = new ArrayList<>(3);
        StringBuilder actual = new StringBuilder();
        boolean enComillas = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (enComillas) {
                if (c == '"') {
                    if (i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                        actual.append('"');
                        i++;
                    } else {
                        enComillas = false;
                    }
                } else {
                    actual.append(c);
                }
            } else if (c == '"') {
                enComillas = true;
            } else if (c == ',') {
                campos.add(actual.toString().trim());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        campos.add(actual.toString().trim());
        return campos.toArray(new String[0]);
    }

    /**
     * Decodifica el valor: hexadecimal → bytes → texto.
     * @return el texto, o {@code null} si el hex es inválido o el tipo no es interpretable
     *         (en cuyo caso el llamador lo reporta, nunca lo adivina).
     */
    static String decodificar(String tipo, String valorHex) {
        String t = tipo == null ? "" : tipo.toLowerCase(Locale.ROOT);
        boolean interpretable = switch (t) {
            case "string", "text", "number", "integer", "long", "double", "boolean", "date" -> true;
            default -> false;
        };
        if (!interpretable) {
            return null;
        }
        byte[] bytes = deHex(valorHex);
        return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
    }

    private static byte[] deHex(String hex) {
        if (hex == null || hex.isEmpty() || hex.length() % 2 != 0) {
            return null;
        }
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < out.length; i++) {
            int alto = Character.digit(hex.charAt(2 * i), 16);
            int bajo = Character.digit(hex.charAt(2 * i + 1), 16);
            if (alto < 0 || bajo < 0) {
                return null;
            }
            out[i] = (byte) ((alto << 4) + bajo);
        }
        return out;
    }
}
