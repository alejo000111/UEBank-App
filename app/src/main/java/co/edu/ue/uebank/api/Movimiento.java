package co.edu.ue.uebank.api;

import com.google.gson.annotations.SerializedName;

//Clase
public class Movimiento {

    //Atributos
    public long id;

    @SerializedName("cuenta_id")
    public long cuentaId;

    @SerializedName("cuenta_numero")
    public String cuentaNumero;

    public String tipo;
    public double monto;
    public String descripcion;
    public String fecha;

    //Constructor
    public Movimiento(long cuentaId, String tipo, double monto, String descripcion) {
        this.cuentaId = cuentaId;
        this.tipo = tipo;
        this.monto = monto;
        this.descripcion = descripcion;
    }
}
