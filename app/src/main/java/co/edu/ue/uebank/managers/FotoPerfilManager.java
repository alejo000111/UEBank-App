package co.edu.ue.uebank.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

//Clase
public class FotoPerfilManager {

    //Atributos
    private static final String TAG = "FotoPerfilManager";
    private static final String CARPETA_FOTOS = "fotos_perfil";

    private final Context context;

    //Constructor
    public FotoPerfilManager(Context context) {
        this.context = context.getApplicationContext();
    }

    //Guardar foto
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

    //Cargar foto
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
