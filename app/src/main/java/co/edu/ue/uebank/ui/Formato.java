package co.edu.ue.uebank.ui;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utilidades de formato y validación compartidas por las pantallas.
 */
public final class Formato {

    private static final NumberFormat MONEDA =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));

    private static final Pattern NUMERO_CUENTA = Pattern.compile("^[0-9]{4,20}$");
    private static final Pattern FECHA_ISO = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private Formato() {
    }

    public static String moneda(double valor) {
        return MONEDA.format(valor);
    }

    /**
     * Convierte el texto de un campo a número. Devuelve null si está vacío o no es válido.
     */
    public static Double aNumero(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * true si el texto son solo dígitos (entre 4 y 20), el formato que la API
     * exige para el número de cuenta. Validarlo también en el cliente evita
     * un viaje de red innecesario cuando el error es obvio.
     */
    public static boolean numeroCuentaValido(String texto) {
        return texto != null && NUMERO_CUENTA.matcher(texto.trim()).matches();
    }

    /**
     * true si el texto está vacío (la fecha es opcional) o si es una fecha
     * real en formato AAAA-MM-DD, por ejemplo rechaza "2026-02-30".
     */
    public static boolean fechaValida(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return true;
        }
        String limpio = texto.trim();
        if (!FECHA_ISO.matcher(limpio).matches()) {
            return false;
        }
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        formato.setLenient(false);
        try {
            formato.parse(limpio);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
