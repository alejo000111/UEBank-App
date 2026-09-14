package co.edu.ue.uebank.model;

/**
 * Modelo de datos que representa a un cliente de UEBank.
 * Es una clase POJO simple: solo guarda datos, sin lógica de negocio.
 *
 * Nota de diseño: para esta primera entrega el saldo vive directamente en el
 * usuario, en lugar de en una entidad "Cuenta" separada. Es una simplificación
 * a propósito, pensada para aprender primero SQLite con un modelo pequeño;
 * más adelante se puede dividir en Cliente/Cuenta como plantea el proyecto completo.
 */
public class Usuario {

    private long id;
    private String nombre;
    private String usuario;
    private String passwordHash; // Guarda "salt:hash", nunca la contraseña real
    private double saldo;
    private String fotoPath; // Ruta del archivo de la foto de perfil, o null si no tiene

    public Usuario() {
    }

    public Usuario(String nombre, String usuario, String passwordHash, double saldo) {
        this.nombre = nombre;
        this.usuario = usuario;
        this.passwordHash = passwordHash;
        this.saldo = saldo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }
}
