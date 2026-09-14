package co.edu.ue.uebank.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import co.edu.ue.uebank.data.UsuarioContract.UsuarioEntry;

/**
 * Administra la creación y las versiones de la base de datos local SQLite.
 * Android llama automáticamente a onCreate() la primera vez que se abre la
 * base de datos, y a onUpgrade() si se sube el número de versión más adelante
 * (por ejemplo, cuando se agreguen las tablas de beneficiarios o movimientos).
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "uebank.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_USUARIOS =
            "CREATE TABLE " + UsuarioEntry.TABLE_NAME + " (" +
                    UsuarioEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    UsuarioEntry.COLUMN_NOMBRE + " TEXT NOT NULL, " +
                    UsuarioEntry.COLUMN_USUARIO + " TEXT NOT NULL UNIQUE, " +
                    UsuarioEntry.COLUMN_PASSWORD_HASH + " TEXT NOT NULL, " +
                    UsuarioEntry.COLUMN_SALDO + " REAL NOT NULL DEFAULT 0, " +
                    UsuarioEntry.COLUMN_FOTO_PATH + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USUARIOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Mientras el proyecto siga en la versión 1 no hay nada que migrar.
        // Cuando se agreguen más tablas (beneficiarios, movimientos) se hará
        // aquí un ALTER TABLE o un DROP + CREATE según el caso.
    }
}
