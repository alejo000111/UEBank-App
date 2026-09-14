package co.edu.ue.uebank.managers;

import android.content.Context;
import android.content.SharedPreferences;

import co.edu.ue.uebank.model.Usuario;

/**
 * Guarda la sesión activa usando SharedPreferences: un almacén simple de
 * pares clave-valor que Android persiste en un archivo XML propio de la app.
 *
 * Se usa para dos cosas:
 * 1. Saber, mientras la app está abierta, quién es el usuario que inició sesión.
 * 2. Si el usuario marcó "Recordar sesión", saltarse la pantalla de login la
 *    próxima vez que abra la app (hasta que cierre sesión manualmente).
 */
public class SessionManager {

    private static final String PREFS_NAME = "uebank_session";
    private static final String KEY_ID = "id_usuario";
    private static final String KEY_USUARIO = "usuario";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_RECORDAR = "recordar_sesion";

    private final SharedPreferences preferencias;

    public SessionManager(Context context) {
        this.preferencias = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Guarda los datos de la sesión activa. Si recordar es true, estos datos
     * seguirán ahí la próxima vez que se abra la app, incluso si el usuario
     * cierra la app por completo.
     */
    public void iniciarSesion(Usuario usuario, boolean recordar) {
        preferencias.edit()
                .putLong(KEY_ID, usuario.getId())
                .putString(KEY_USUARIO, usuario.getUsuario())
                .putString(KEY_NOMBRE, usuario.getNombre())
                .putBoolean(KEY_RECORDAR, recordar)
                .apply();
    }

    /**
     * true si hay una sesión guardada Y el usuario pidió que se recordara,
     * es decir, si LoginActivity debe saltar directo al panel principal.
     */
    public boolean haySesionRecordada() {
        return preferencias.getBoolean(KEY_RECORDAR, false) && getIdUsuario() != -1;
    }

    public long getIdUsuario() {
        return preferencias.getLong(KEY_ID, -1);
    }

    public String getNombreUsuario() {
        return preferencias.getString(KEY_NOMBRE, "");
    }

    /**
     * Borra la sesión activa. Se llama al presionar "Cerrar sesión".
     */
    public void cerrarSesion() {
        preferencias.edit().clear().apply();
    }
}
