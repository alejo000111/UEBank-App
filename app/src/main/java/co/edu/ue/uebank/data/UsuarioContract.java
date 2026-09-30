package co.edu.ue.uebank.data;

import android.provider.BaseColumns;

//Clase
public final class UsuarioContract {

    //Constructor
    private UsuarioContract() {
    }

    //Tabla usuarios
    public static final class UsuarioEntry implements BaseColumns {
        public static final String TABLE_NAME = "usuarios";

        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_USUARIO = "usuario";
        public static final String COLUMN_PASSWORD_HASH = "password_hash";
        public static final String COLUMN_FOTO_PATH = "foto_path";
    }
}
