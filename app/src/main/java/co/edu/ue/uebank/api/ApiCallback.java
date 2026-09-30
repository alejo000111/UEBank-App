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

//Clase
public class ApiCallback<T> implements Callback<T> {

    //Atributos
    private final Context context;
    private final Consumer<T> alExito;

    //Constructor
    public ApiCallback(Context context, Consumer<T> alExito) {
        this.context = context;
        this.alExito = alExito;
    }

    //Respuesta del servidor
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

    //Error de conexión
    @Override
    public void onFailure(Call<T> call, Throwable error) {
        if (pantallaCerrada()) {
            return;
        }
        Toast.makeText(context, R.string.error_conexion, Toast.LENGTH_LONG).show();
    }

    //Pantalla cerrada
    private boolean pantallaCerrada() {
        return context instanceof Activity && ((Activity) context).isFinishing();
    }

    //Mensaje de error
    private String leerMensajeError(Response<T> respuesta) {
        try {
            if (respuesta.errorBody() != null) {
                return new JSONObject(respuesta.errorBody().string()).getString("error");
            }
        } catch (Exception ignorado) {
        }
        return context.getString(R.string.error_generico) + " (" + respuesta.code() + ")";
    }
}
