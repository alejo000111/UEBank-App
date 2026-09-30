package co.edu.ue.uebank.api;

import android.app.Activity;
import android.content.Context;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//Clase
public final class SaldoTotal {

    //Constructor
    private SaldoTotal() {
    }

    //Interfaz de resultado
    public interface OnResultado {
        void onSaldo(Double total);
    }

    //Consultar saldo total
    public static void consultar(Context context, String usuario, OnResultado callback) {
        ApiClient.get().listarCuentas(usuario).enqueue(new Callback<List<Cuenta>>() {
            @Override
            public void onResponse(Call<List<Cuenta>> call, Response<List<Cuenta>> respuesta) {
                if (pantallaCerrada()) {
                    return;
                }
                if (!respuesta.isSuccessful() || respuesta.body() == null) {
                    callback.onSaldo(null);
                    return;
                }
                double total = 0;
                for (Cuenta cuenta : respuesta.body()) {
                    total += cuenta.saldo;
                }
                callback.onSaldo(total);
            }

            @Override
            public void onFailure(Call<List<Cuenta>> call, Throwable error) {
                if (!pantallaCerrada()) {
                    callback.onSaldo(null);
                }
            }

            private boolean pantallaCerrada() {
                return context instanceof Activity && ((Activity) context).isFinishing();
            }
        });
    }
}
