package co.edu.ue.uebank.model;

/**
 * Persona a la que el cliente suele enviarle dinero. Se guarda en SQLite
 * (local) y pertenece a un usuario: cada cliente solo ve sus beneficiarios.
 */
public class Beneficiario {

    private long id;
    private long idUsuario;
    private String nombre;
    private String numeroCuenta;
    private String telefono; // opcional

    public Beneficiario() {
    }

    public Beneficiario(long idUsuario, String nombre, String numeroCuenta, String telefono) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.numeroCuenta = numeroCuenta;
        this.telefono = telefono;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
