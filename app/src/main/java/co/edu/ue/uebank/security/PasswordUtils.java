package co.edu.ue.uebank.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

//Clase
public final class PasswordUtils {

    //Atributos
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 120_000;
    private static final int LONGITUD_LLAVE_BITS = 256;
    private static final int LONGITUD_SALT_BYTES = 16;

    //Constructor
    private PasswordUtils() {
    }

    //Generar sal
    private static byte[] generarSalt() {
        byte[] salt = new byte[LONGITUD_SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    //Calcular hash
    private static byte[] derivarHash(char[] password, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(password, salt, ITERACIONES, LONGITUD_LLAVE_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITMO);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        }
    }

    //Crear hash para guardar
    public static String crearHashAlmacenable(String password) {
        byte[] salt = generarSalt();
        byte[] hash = derivarHash(password.toCharArray(), salt);
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hashBase64 = Base64.getEncoder().encodeToString(hash);
        return saltBase64 + ":" + hashBase64;
    }

    //Verificar contraseña
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
