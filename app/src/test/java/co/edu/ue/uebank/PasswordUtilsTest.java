package co.edu.ue.uebank;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import co.edu.ue.uebank.security.PasswordUtils;

/**
 * Prueba unitaria de PasswordUtils. Corre en la máquina de desarrollo (no en
 * un emulador) porque, a partir de minSdk 26, PasswordUtils solo usa
 * java.security y java.util.Base64, sin depender de clases de Android.
 */
public class PasswordUtilsTest {

    @Test
    public void unaContrasenaCorrectaSeVerificaComoValida() {
        String hashAlmacenado = PasswordUtils.crearHashAlmacenable("miClaveSegura123");

        assertTrue(PasswordUtils.verificarPassword("miClaveSegura123", hashAlmacenado));
    }

    @Test
    public void unaContrasenaIncorrectaSeRechaza() {
        String hashAlmacenado = PasswordUtils.crearHashAlmacenable("miClaveSegura123");

        assertFalse(PasswordUtils.verificarPassword("otraClave", hashAlmacenado));
    }

    @Test
    public void dosUsuariosConLaMismaContrasenaGeneranHashesDistintos() {
        String hash1 = PasswordUtils.crearHashAlmacenable("claveRepetida");
        String hash2 = PasswordUtils.crearHashAlmacenable("claveRepetida");

        // Deben ser distintos porque cada uno usa una sal aleatoria diferente.
        assertNotEquals(hash1, hash2);
    }
}
