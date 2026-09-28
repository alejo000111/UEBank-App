package co.edu.ue.uebank.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import co.edu.ue.uebank.data.BeneficiarioContract.BeneficiarioEntry;
import co.edu.ue.uebank.data.UsuarioContract.UsuarioEntry;

/**
 * Administra la creación y las versiones de la base de datos local SQLite.
 * Android llama automáticamente a onCreate() la primera vez que se abre la
 * base de datos, y a onUpgrade() si se sube el número de versión.
 *
 * Historial de versiones:
 *   1 -> tabla usuarios (con una columna "saldo")
 *   2 -> se agrega la tabla beneficiarios (CRUD local)
 *   3 -> se elimina "saldo" de usuarios: el saldo real ahora vive en la API
 *        (PostgreSQL) como la suma de las cuentas del cliente. Tener el mismo
 *        dato en dos bases distintas es lo que se conoce como "fuentes de
 *        verdad duplicadas"; se unifica dejando una sola fuente (la API).
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "uebank.db";
    private static final int DATABASE_VERSION = 3;

    private static final String SQL_CREATE_USUARIOS =
            "CREATE TABLE " + UsuarioEntry.TABLE_NAME + " (" +
                    UsuarioEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    UsuarioEntry.COLUMN_NOMBRE + " TEXT NOT NULL, " +
                    UsuarioEntry.COLUMN_USUARIO + " TEXT NOT NULL UNIQUE, " +
                    UsuarioEntry.COLUMN_PASSWORD_HASH + " TEXT NOT NULL, " +
                    UsuarioEntry.COLUMN_FOTO_PATH + " TEXT)";

    // Cada beneficiario pertenece a un usuario; si el usuario se borra, sus
    // beneficiarios también (ON DELETE CASCADE, requiere foreign_keys = ON).
    private static final String SQL_CREATE_BENEFICIARIOS =
            "CREATE TABLE IF NOT EXISTS " + BeneficiarioEntry.TABLE_NAME + " (" +
                    BeneficiarioEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    BeneficiarioEntry.COLUMN_ID_USUARIO + " INTEGER NOT NULL " +
                    "REFERENCES " + UsuarioEntry.TABLE_NAME + "(" + UsuarioEntry._ID + ") ON DELETE CASCADE, " +
                    BeneficiarioEntry.COLUMN_NOMBRE + " TEXT NOT NULL, " +
                    BeneficiarioEntry.COLUMN_NUMERO_CUENTA + " TEXT NOT NULL, " +
                    BeneficiarioEntry.COLUMN_TELEFONO + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // SQLite ignora las llaves foráneas a menos que se activen en cada conexión.
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USUARIOS);
        db.execSQL(SQL_CREATE_BENEFICIARIOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Migración 1 -> 2: solo se agrega la tabla nueva, sin tocar los
        // usuarios que ya estaban registrados (no se pierden datos).
        if (oldVersion < 2) {
            db.execSQL(SQL_CREATE_BENEFICIARIOS);
        }

        // Migración -> 3: quita la columna "saldo" de usuarios. SQLite no
        // soporta DROP COLUMN en todas las versiones de Android, así que se
        // recrea la tabla sin esa columna y se copian los demás datos.
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + UsuarioEntry.TABLE_NAME + " RENAME TO usuarios_temporal");
            db.execSQL(SQL_CREATE_USUARIOS);
            db.execSQL("INSERT INTO " + UsuarioEntry.TABLE_NAME + " (" +
                    UsuarioEntry._ID + ", " + UsuarioEntry.COLUMN_NOMBRE + ", " +
                    UsuarioEntry.COLUMN_USUARIO + ", " + UsuarioEntry.COLUMN_PASSWORD_HASH + ", " +
                    UsuarioEntry.COLUMN_FOTO_PATH + ") SELECT " +
                    UsuarioEntry._ID + ", " + UsuarioEntry.COLUMN_NOMBRE + ", " +
                    UsuarioEntry.COLUMN_USUARIO + ", " + UsuarioEntry.COLUMN_PASSWORD_HASH + ", " +
                    UsuarioEntry.COLUMN_FOTO_PATH + " FROM usuarios_temporal");
            db.execSQL("DROP TABLE usuarios_temporal");
        }
    }
}
