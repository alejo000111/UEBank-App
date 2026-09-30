package co.edu.ue.uebank.model;

//Clase
public class Usuario {

    //Atributos
    private long id;
    private String nombre;
    private String usuario;
    private String passwordHash;
    private String fotoPath;

    //Constructores
    public Usuario() {
    }

    public Usuario(String nombre, String usuario, String passwordHash) {
        this.nombre = nombre;
        this.usuario = usuario;
        this.passwordHash = passwordHash;
    }

    //Getters y setters
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
