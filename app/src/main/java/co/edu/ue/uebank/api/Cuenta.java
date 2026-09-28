package co.edu.ue.uebank.api;

/**
 * Cuenta bancaria tal como la entrega y recibe la API (JSON).
 * Gson rellena los campos por nombre; los que llevan @SerializedName usan
 * el nombre en snake_case del JSON.
 */
public class Cuenta {

    public long id;
    public String usuario;
    public String numero;
    public String tipo;
    public double saldo;

    public Cuenta(String usuario, String numero, String tipo, double saldo) {
        this.usuario = usuario;
        this.numero = numero;
        this.tipo = tipo;
        this.saldo = saldo;
    }
}
