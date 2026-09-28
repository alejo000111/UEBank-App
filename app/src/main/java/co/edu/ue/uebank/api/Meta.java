package co.edu.ue.uebank.api;

import com.google.gson.annotations.SerializedName;

/**
 * Meta de ahorro de un usuario, tal como la maneja la API.
 */
public class Meta {

    public long id;
    public String usuario;
    public String nombre;

    @SerializedName("monto_objetivo")
    public double montoObjetivo;

    @SerializedName("monto_ahorrado")
    public double montoAhorrado;

    @SerializedName("fecha_limite")
    public String fechaLimite; // "AAAA-MM-DD" o null

    public Meta(String usuario, String nombre, double montoObjetivo, double montoAhorrado, String fechaLimite) {
        this.usuario = usuario;
        this.nombre = nombre;
        this.montoObjetivo = montoObjetivo;
        this.montoAhorrado = montoAhorrado;
        this.fechaLimite = fechaLimite;
    }
}
