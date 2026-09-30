package co.edu.ue.uebank.managers;

import android.content.Context;
import android.content.SharedPreferences;

import co.edu.ue.uebank.model.Usuario;

//Clase
public class SessionManager {

    //Atributos
    private static final String PREFS_NAME = "uebank_session";
    private static final String KEY_ID = "id_usuario";
    private static final String KEY_USUARIO = "usuario";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_RECORDAR = "recordar_sesion";

    private final SharedPreferences preferencias;

    //Constructor
    public SessionManager(Context context) {
        this.preferencias = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    //Iniciar sesión
    public void iniciarSesion(Usuario usuario, boolean recordar) {
        preferencias.edit()
                .putLong(KEY_ID, usuario.getId())
                .putString(KEY_USUARIO, usuario.getUsuario())
                .putString(KEY_NOMBRE, usuario.getNombre())
                .putBoolean(KEY_RECORDAR, recordar)
                .apply();
    }

    //Sesión recordada
    public boolean haySesionRecordada() {
        return preferencias.getBoolean(KEY_RECORDAR, false) && getIdUsuario() != -1;
    }

    //Getters
    public long getIdUsuario() {
        return preferencias.getLong(KEY_ID, -1);
    }

    public String getUsuario() {
        return preferencias.getString(KEY_USUARIO, "");
    }

    public String getNombreUsuario() {
        return preferencias.getString(KEY_NOMBRE, "");
    }

    //Cerrar sesión
    public void cerrarSesion() {
        preferencias.edit().clear().apply();
    }
}
