package co.edu.ue.uebank.api;

/**
 * Cuerpo de POST /api/auth/registro y /api/auth/login.
 *
 * "hash" es el mismo "salt:hash" en Base64 que PasswordUtils ya calculó en
 * el teléfono (PBKDF2WithHmacSHA256 + sal): la contraseña real del cliente
 * nunca viaja por la red ni llega a la API.
 */
public class AuthRequest {
    public String usuario;
    public String hash;

    public AuthRequest(String usuario, String hash) {
        this.usuario = usuario;
        this.hash = hash;
    }
}
