package co.edu.ue.uebank.api;

//Clase
public class AuthRequest {
    public String usuario;
    public String hash;

    public AuthRequest(String usuario, String hash) {
        this.usuario = usuario;
        this.hash = hash;
    }
}
