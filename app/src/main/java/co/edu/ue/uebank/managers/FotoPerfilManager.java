package co.edu.ue.uebank.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Guarda y recupera la foto de perfil como un archivo .jpg dentro del
 * almacenamiento interno y privado de la app (nadie más que UEBank puede
 * leer estos archivos, ni siquiera otras apps del teléfono).
 */
public class FotoPerfilManager {

    private static final String TAG = "FotoPerfilManager";
    private static final String CARPETA_FOTOS = "fotos_perfil";

    private final Context context;

    public FotoPerfilManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Comprime y guarda la foto tomada con la cámara en un archivo propio del
     * usuario. Devuelve la ruta absoluta del archivo, o null si algo falló.
     */
    public String guardarFotoPerfil(String nombreUsuario, Bitmap foto) {
        File carpeta = new File(context.getFilesDir(), CARPETA_FOTOS);
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            Log.e(TAG, "No se pudo crear la carpeta de fotos de perfil.");
            return null;
        }

        File archivo = new File(carpeta, nombreUsuario + ".jpg");
        try (FileOutputStream salida = new FileOutputStream(archivo)) {
            foto.compress(Bitmap.CompressFormat.JPEG, 90, salida);
            return archivo.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error al guardar la foto de perfil: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Carga la foto de perfil desde su ruta guardada. Devuelve null si la
     * ruta es nula o el archivo ya no existe.
     */
    public Bitmap cargarFotoPerfil(String rutaArchivo) {
        if (rutaArchivo == null) {
            return null;
        }
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            return null;
        }
        return BitmapFactory.decodeFile(archivo.getAbsolutePath());
    }
}
