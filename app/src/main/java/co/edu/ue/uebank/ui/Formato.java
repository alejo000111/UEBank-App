package co.edu.ue.uebank.ui;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;

//Clase
public final class Formato {

    //Atributos
    private static final NumberFormat MONEDA =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));

    private static final Pattern NUMERO_CUENTA = Pattern.compile("^[0-9]{4,20}$");
    private static final Pattern FECHA_ISO = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    //Constructor
    private Formato() {
    }

    //Formato moneda
    public static String moneda(double valor) {
        return MONEDA.format(valor);
    }

    //Texto a número
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

    //Validar número de cuenta
    public static boolean numeroCuentaValido(String texto) {
        return texto != null && NUMERO_CUENTA.matcher(texto.trim()).matches();
    }

    //Validar fecha
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
