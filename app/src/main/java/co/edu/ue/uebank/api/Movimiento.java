package co.edu.ue.uebank.api;

import com.google.gson.annotations.SerializedName;

/**
 * Movimiento (depósito o retiro) de una cuenta, tal como lo maneja la API.
 */
public class Movimiento {

    public long id;

    @SerializedName("cuenta_id")
    public long cuentaId;

    @SerializedName("cuenta_numero")
    public String cuentaNumero; // solo viene en las respuestas (JOIN con cuentas)

    public String tipo;
    public double monto;
    public String descripcion;
    public String fecha; // ISO-8601, por ejemplo 2026-09-23T15:04:05.000Z

    public Movimiento(long cuentaId, String tipo, double monto, String descripcion) {
        this.cuentaId = cuentaId;
        this.tipo = tipo;
        this.monto = monto;
        this.descripcion = descripcion;
    }
}
