package co.edu.ue.uebank.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import co.edu.ue.uebank.data.UsuarioContract.UsuarioEntry;
import co.edu.ue.uebank.model.Usuario;

/**
 * Repositorio de usuarios: es la única clase que sabe escribir SQL. El resto
 * de la app (las Activities) solo llama a estos métodos con objetos Usuario,
 * sin tener que preocuparse por Cursor, ContentValues, etc.
 */
public class UsuarioRepository {

    private final DatabaseHelper databaseHelper;

    public UsuarioRepository(Context context) {
        this.databaseHelper = new DatabaseHelper(context);
    }

    /**
     * Inserta un usuario nuevo y devuelve su id generado, o -1 si el usuario
     * ya existía (columna UNIQUE) o si ocurrió cualquier otro error.
     */
    public long insertarUsuario(Usuario usuario) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put(UsuarioEntry.COLUMN_NOMBRE, usuario.getNombre());
        valores.put(UsuarioEntry.COLUMN_USUARIO, usuario.getUsuario());
        valores.put(UsuarioEntry.COLUMN_PASSWORD_HASH, usuario.getPasswordHash());
        valores.put(UsuarioEntry.COLUMN_SALDO, usuario.getSaldo());

        // insert() devuelve -1 automáticamente si se viola la restricción UNIQUE.
        return db.insert(UsuarioEntry.TABLE_NAME, null, valores);
    }

    /**
     * Busca un usuario por su nombre de usuario/correo. Devuelve null si no existe.
     */
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

    /**
     * Busca un usuario por su id, por ejemplo para recargar sus datos luego
     * de iniciar sesión.
     */
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

    public boolean existeUsuario(String usuario) {
        return obtenerPorUsuario(usuario) != null;
    }

    /**
     * Actualiza solamente la ruta de la foto de perfil de un usuario.
     */
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

    private Usuario mapearCursorAUsuario(Cursor cursor) {
        Usuario usuario = new Usuario();
        usuario.setId(cursor.getLong(cursor.getColumnIndexOrThrow(UsuarioEntry._ID)));
        usuario.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(UsuarioEntry.COLUMN_NOMBRE)));
        usuario.setUsuario(cursor.getString(cursor.getColumnIndexOrThrow(UsuarioEntry.COLUMN_USUARIO)));
        usuario.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(UsuarioEntry.COLUMN_PASSWORD_HASH)));
        usuario.setSaldo(cursor.getDouble(cursor.getColumnIndexOrThrow(UsuarioEntry.COLUMN_SALDO)));
        usuario.setFotoPath(cursor.getString(cursor.getColumnIndexOrThrow(UsuarioEntry.COLUMN_FOTO_PATH)));
        return usuario;
    }
}
