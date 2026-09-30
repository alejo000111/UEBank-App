package co.edu.ue.uebank.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import co.edu.ue.uebank.data.UsuarioContract.UsuarioEntry;
import co.edu.ue.uebank.model.Usuario;

//Clase
public class UsuarioRepository {

    //Atributos
    private final DatabaseHelper databaseHelper;

    //Constructor
    public UsuarioRepository(Context context) {
        this.databaseHelper = new DatabaseHelper(context);
    }

    //Insertar usuario
    public long insertarUsuario(Usuario usuario) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put(UsuarioEntry.COLUMN_NOMBRE, usuario.getNombre());
        valores.put(UsuarioEntry.COLUMN_USUARIO, usuario.getUsuario());
        valores.put(UsuarioEntry.COLUMN_PASSWORD_HASH, usuario.getPasswordHash());

        return db.insert(UsuarioEntry.TABLE_NAME, null, valores);
    }

    //Buscar por usuario
    public Usuario obtenerPorUsuario(String usuario) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String seleccion = UsuarioEntry.COLUMN_USUARIO + " = ?";
        String[] argumentos = {usuario};

        try (Cursor cursor = db.query(
                UsuarioEntry.TABLE_NAME,
                null,
                seleccion,
                argumentos,
                null,
                null,
                null)) {

            if (cursor.moveToFirst()) {
                return mapearCursorAUsuario(cursor);
            }
            return null;
        }
    }

    //Buscar por id
    public Usuario obtenerPorId(long id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String seleccion = UsuarioEntry._ID + " = ?";
        String[] argumentos = {String.valueOf(id)};

        try (Cursor cursor = db.query(
                UsuarioEntry.TABLE_NAME,
                null,
                seleccion,
                argumentos,
                null,
                null,
                null)) {

            if (cursor.moveToFirst()) {
                return mapearCursorAUsuario(cursor);
            }
            return null;
        }
    }

    //Validar existencia
    public boolean existeUsuario(String usuario) {
        return obtenerPorUsuario(usuario) != null;
    }

    //Actualizar foto
    public void actualizarFotoPerfil(long id, String fotoPath) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put(UsuarioEntry.COLUMN_FOTO_PATH, fotoPath);

        db.update(
                UsuarioEntry.TABLE_NAME,
                valores,
                UsuarioEntry._ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    //Cursor a objeto
    private Usuario mapearCursorAUsuario(Cursor cursor) {
        Usuario usuario = new Usuario();
        usuario.setId(cursor.getLong(cursor.getColumnIndexOrThrow(UsuarioEntry._ID)));
        usuario.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(UsuarioEntry.COLUMN_NOMBRE)));
        usuario.setUsuario(cursor.getString(cursor.getColumnIndexOrThrow(UsuarioEntry.COLUMN_USUARIO)));
        usuario.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(UsuarioEntry.COLUMN_PASSWORD_HASH)));
        usuario.setFotoPath(cursor.getString(cursor.getColumnIndexOrThrow(UsuarioEntry.COLUMN_FOTO_PATH)));
        return usuario;
    }
}
