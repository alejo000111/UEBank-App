package co.edu.ue.uebank.api;

//Clase
public class Cuenta {

    //Atributos
    public long id;
    public String usuario;
    public String numero;
    public String tipo;
    public double saldo;

    //Constructor
    public Cuenta(String usuario, String numero, String tipo, double saldo) {
        this.usuario = usuario;
        this.numero = numero;
        this.tipo = tipo;
        this.saldo = saldo;
    }
}
