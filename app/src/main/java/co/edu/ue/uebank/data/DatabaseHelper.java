package co.edu.ue.uebank.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import co.edu.ue.uebank.data.BeneficiarioContract.BeneficiarioEntry;
import co.edu.ue.uebank.data.UsuarioContract.UsuarioEntry;

//Clase
public class DatabaseHelper extends SQLiteOpenHelper {

    //Atributos
    private static final String DATABASE_NAME = "uebank.db";
    private static final int DATABASE_VERSION = 3;

    private static final String SQL_CREATE_USUARIOS =
            "CREATE TABLE " + UsuarioEntry.TABLE_NAME + " (" +
                    UsuarioEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    UsuarioEntry.COLUMN_NOMBRE + " TEXT NOT NULL, " +
                    UsuarioEntry.COLUMN_USUARIO + " TEXT NOT NULL UNIQUE, " +
                    UsuarioEntry.COLUMN_PASSWORD_HASH + " TEXT NOT NULL, " +
                    UsuarioEntry.COLUMN_FOTO_PATH + " TEXT)";

    private static final String SQL_CREATE_BENEFICIARIOS =
            "CREATE TABLE IF NOT EXISTS " + BeneficiarioEntry.TABLE_NAME + " (" +
                    BeneficiarioEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    BeneficiarioEntry.COLUMN_ID_USUARIO + " INTEGER NOT NULL " +
                    "REFERENCES " + UsuarioEntry.TABLE_NAME + "(" + UsuarioEntry._ID + ") ON DELETE CASCADE, " +
                    BeneficiarioEntry.COLUMN_NOMBRE + " TEXT NOT NULL, " +
                    BeneficiarioEntry.COLUMN_NUMERO_CUENTA + " TEXT NOT NULL, " +
                    BeneficiarioEntry.COLUMN_TELEFONO + " TEXT)";

    //Constructor
    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Activar llaves foráneas
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    //Crear tablas
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USUARIOS);
        db.execSQL(SQL_CREATE_BENEFICIARIOS);
    }

    //Migraciones
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(SQL_CREATE_BENEFICIARIOS);
        }

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
