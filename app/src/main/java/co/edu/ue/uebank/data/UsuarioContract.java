package co.edu.ue.uebank.data;

import android.provider.BaseColumns;

/**
 * "Contrato" de la tabla de usuarios: aquí se centralizan los nombres de la
 * tabla y de las columnas para no repetir Strings sueltos por todo el código
 * (y evitar errores de tipeo al escribir consultas SQL).
 */
public final class UsuarioContract {

    private UsuarioContract() {
        // Solo contiene constantes.
    }

    public static final class UsuarioEntry implements BaseColumns {
        public static final String TABLE_NAME = "usuarios";

        // _ID lo aporta BaseColumns automáticamente
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_USUARIO = "usuario";
        public static final String COLUMN_PASSWORD_HASH = "password_hash";
        public static final String COLUMN_SALDO = "saldo";
        public static final String COLUMN_FOTO_PATH = "foto_path";
    }
}
