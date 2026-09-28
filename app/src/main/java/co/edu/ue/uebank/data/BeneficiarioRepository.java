package co.edu.ue.uebank.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import co.edu.ue.uebank.data.BeneficiarioContract.BeneficiarioEntry;
import co.edu.ue.uebank.model.Beneficiario;

/**
 * CRUD completo de beneficiarios sobre SQLite: Create, Read, Update y Delete.
 * Igual que UsuarioRepository, es la única clase que conoce el SQL de esta tabla.
 */
public class BeneficiarioRepository {

    private final DatabaseHelper databaseHelper;

    public BeneficiarioRepository(Context context) {
        this.databaseHelper = new DatabaseHelper(context);
    }

    // ---------- CREATE ----------
    public long insertar(Beneficiario beneficiario) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.insert(BeneficiarioEntry.TABLE_NAME, null, aValores(beneficiario));
    }

    // ---------- READ ----------
    public List<Beneficiario> listarPorUsuario(long idUsuario) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        List<Beneficiario> lista = new ArrayList<>();

        try (Cursor cursor = db.query(
                BeneficiarioEntry.TABLE_NAME,
                null,
                BeneficiarioEntry.COLUMN_ID_USUARIO + " = ?",
                new String[]{String.valueOf(idUsuario)},
                null,
                null,
                BeneficiarioEntry.COLUMN_NOMBRE + " COLLATE NOCASE ASC")) {

            while (cursor.moveToNext()) {
                lista.add(mapearCursor(cursor));
            }
        }
        return lista;
    }

    // ---------- UPDATE ----------
    public boolean actualizar(Beneficiario beneficiario) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int filas = db.update(
                BeneficiarioEntry.TABLE_NAME,
                aValores(beneficiario),
                BeneficiarioEntry._ID + " = ?",
                new String[]{String.valueOf(beneficiario.getId())});
        return filas > 0;
    }

    // ---------- DELETE ----------
    public boolean eliminar(long id) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int filas = db.delete(
                BeneficiarioEntry.TABLE_NAME,
                BeneficiarioEntry._ID + " = ?",
                new String[]{String.valueOf(id)});
        return filas > 0;
    }

    private ContentValues aValores(Beneficiario beneficiario) {
        ContentValues valores = new ContentValues();
        valores.put(BeneficiarioEntry.COLUMN_ID_USUARIO, beneficiario.getIdUsuario());
        valores.put(BeneficiarioEntry.COLUMN_NOMBRE, beneficiario.getNombre());
        valores.put(BeneficiarioEntry.COLUMN_NUMERO_CUENTA, beneficiario.getNumeroCuenta());
        valores.put(BeneficiarioEntry.COLUMN_TELEFONO, beneficiario.getTelefono());
        return valores;
    }

    private Beneficiario mapearCursor(Cursor cursor) {
        Beneficiario b = new Beneficiario();
        b.setId(cursor.getLong(cursor.getColumnIndexOrThrow(BeneficiarioEntry._ID)));
        b.setIdUsuario(cursor.getLong(cursor.getColumnIndexOrThrow(BeneficiarioEntry.COLUMN_ID_USUARIO)));
        b.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(BeneficiarioEntry.COLUMN_NOMBRE)));
        b.setNumeroCuenta(cursor.getString(cursor.getColumnIndexOrThrow(BeneficiarioEntry.COLUMN_NUMERO_CUENTA)));
        b.setTelefono(cursor.getString(cursor.getColumnIndexOrThrow(BeneficiarioEntry.COLUMN_TELEFONO)));
        return b;
    }
}
