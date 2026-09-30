package co.edu.ue.uebank;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import co.edu.ue.uebank.security.PasswordUtils;

//Clase
public class PasswordUtilsTest {

    //Prueba contraseña correcta
    @Test
    public void unaContrasenaCorrectaSeVerificaComoValida() {
        String hashAlmacenado = PasswordUtils.crearHashAlmacenable("miClaveSegura123");

        assertTrue(PasswordUtils.verificarPassword("miClaveSegura123", hashAlmacenado));
    }

    //Prueba contraseña incorrecta
    @Test
    public void unaContrasenaIncorrectaSeRechaza() {
        String hashAlmacenado = PasswordUtils.crearHashAlmacenable("miClaveSegura123");

        assertFalse(PasswordUtils.verificarPassword("otraClave", hashAlmacenado));
    }

    //Prueba sal distinta
    @Test
    public void dosUsuariosConLaMismaContrasenaGeneranHashesDistintos() {
        String hash1 = PasswordUtils.crearHashAlmacenable("claveRepetida");
        String hash2 = PasswordUtils.crearHashAlmacenable("claveRepetida");

        assertNotEquals(hash1, hash2);
    }
}
