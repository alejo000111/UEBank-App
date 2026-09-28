package co.edu.ue.uebank.api;

import android.app.Activity;
import android.content.Context;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Calcula el saldo total de un usuario: la suma del saldo de todas sus
 * cuentas en la API. Es la ÚNICA fuente de verdad del saldo en toda la app;
 * ni el panel principal ni el perfil guardan su propio número de saldo.
 */
public final class SaldoTotal {

    private SaldoTotal() {
    }

    public interface OnResultado {
        /** total es null si no se pudo consultar (sin conexión o error del servidor). */
        void onSaldo(Double total);
    }

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
