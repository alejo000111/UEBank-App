package co.edu.ue.uebank.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utilidad para nunca guardar contraseñas en texto plano.
 *
 * Idea general: en vez de guardar la contraseña, guardamos un "hash" (una huella
 * digital) generada a partir de ella. Como dos contraseñas iguales generarían
 * siempre el mismo hash (fácil de atacar con tablas precalculadas), a cada
 * usuario se le agrega una "sal" (salt) aleatoria antes de calcular el hash.
 * Así, el resultado guardado en la base de datos es único aunque dos personas
 * usen la misma contraseña.
 *
 * Algoritmo usado: PBKDF2WithHmacSHA256 con 120.000 iteraciones, el mismo
 * estándar que usan la mayoría de apps bancarias reales para dificultar los
 * ataques de fuerza bruta.
 */
public final class PasswordUtils {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 120_000;
    private static final int LONGITUD_LLAVE_BITS = 256;
    private static final int LONGITUD_SALT_BYTES = 16;

    private PasswordUtils() {
        // Clase de solo utilidades: no se instancia.
    }

    /**
     * Genera una "sal" aleatoria distinta para cada usuario.
     */
    private static byte[] generarSalt() {
        byte[] salt = new byte[LONGITUD_SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * Deriva la contraseña con PBKDF2 usando la sal indicada.
     */
    private static byte[] derivarHash(char[] password, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(password, salt, ITERACIONES, LONGITUD_LLAVE_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITMO);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // Esto no debería pasar: PBKDF2WithHmacSHA256 está disponible en todo Android moderno.
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        }
    }

    /**
     * Genera el valor que se debe guardar en la base de datos al registrar un
     * usuario nuevo: la sal y el hash, separados por ":", en Base64.
     */
    public static String crearHashAlmacenable(String password) {
        byte[] salt = generarSalt();
        byte[] hash = derivarHash(password.toCharArray(), salt);
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hashBase64 = Base64.getEncoder().encodeToString(hash);
        return saltBase64 + ":" + hashBase64;
    }

    /**
     * Verifica una contraseña ingresada en el login contra el valor "salt:hash"
     * guardado en la base de datos.
     *
     * La comparación se hace con MessageDigest.isEqual, que compara en tiempo
     * constante: así el tiempo de respuesta no delata cuántos caracteres
     * acertó un atacante (ataque de temporización).
     */
    public static boolean verificarPassword(String password, String almacenado) {
        if (almacenado == null || !almacenado.contains(":")) {
            return false;
        }
        String[] partes = almacenado.split(":", 2);
        byte[] salt = Base64.getDecoder().decode(partes[0]);
        byte[] hashEsperado = Base64.getDecoder().decode(partes[1]);

        byte[] hashCalculado = derivarHash(password.toCharArray(), salt);
        return MessageDigest.isEqual(hashCalculado, hashEsperado);
    }
}
