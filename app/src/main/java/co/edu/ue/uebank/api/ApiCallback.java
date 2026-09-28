package co.edu.ue.uebank.api;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.function.Consumer;

import co.edu.ue.uebank.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Callback estándar para las peticiones: si sale bien, entrega el resultado;
 * si el servidor responde con error (400, 404, 409...), muestra el mensaje que
 * envió la API ({"error": "..."}); si no hay conexión, avisa al usuario.
 * Retrofit ejecuta la petición en un hilo secundario y llama a estos métodos
 * en el hilo principal, por eso aquí se puede tocar la interfaz directamente.
 */
public class ApiCallback<T> implements Callback<T> {

    private final Context context;
    private final Consumer<T> alExito;

    public ApiCallback(Context context, Consumer<T> alExito) {
        this.context = context;
        this.alExito = alExito;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> respuesta) {
        if (pantallaCerrada()) {
            return;
        }
        if (respuesta.isSuccessful()) {
            alExito.accept(respuesta.body());
        } else {
            Toast.makeText(context, leerMensajeError(respuesta), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable error) {
        if (pantallaCerrada()) {
            return;
        }
        Toast.makeText(context, R.string.error_conexion, Toast.LENGTH_LONG).show();
    }

    private boolean pantallaCerrada() {
        return context instanceof Activity && ((Activity) context).isFinishing();
    }

    private String leerMensajeError(Response<T> respuesta) {
        try {
            if (respuesta.errorBody() != null) {
                return new JSONObject(respuesta.errorBody().string()).getString("error");
            }
        } catch (Exception ignorado) {
            // Si el cuerpo no es el JSON esperado, se usa el mensaje genérico.
        }
        return context.getString(R.string.error_generico) + " (" + respuesta.code() + ")";
    }
}
