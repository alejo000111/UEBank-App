package co.edu.ue.uebank.model;

/**
 * Modelo de datos que representa a un cliente de UEBank.
 * Es una clase POJO simple: solo guarda datos, sin lógica de negocio.
 *
 * Nota de diseño: el saldo NO vive aquí. El saldo real del cliente es la suma
 * de sus cuentas, que están en la API (PostgreSQL); ver co.edu.ue.uebank.api.SaldoTotal.
 * Guardarlo también en SQLite crearía dos fuentes de verdad que podrían
 * desincronizarse (por ejemplo, si un movimiento se hace desde otro celular).
 */
public class Usuario {

    private long id;
    private String nombre;
    private String usuario;
    private String passwordHash; // Guarda "salt:hash", nunca la contraseña real
    private String fotoPath; // Ruta del archivo de la foto de perfil, o null si no tiene

    public Usuario() {
    }

    public Usuario(String nombre, String usuario, String passwordHash) {
        this.nombre = nombre;
        this.usuario = usuario;
        this.passwordHash = passwordHash;
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

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }
}
