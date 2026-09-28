package co.edu.ue.uebank.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Crea una única instancia de Retrofit para toda la app (singleton).
 *
 * BASE_URL: 10.0.2.2 es la dirección con la que el EMULADOR de Android ve el
 * "localhost" del computador donde corre la API. Si pruebas con un celular
 * físico, cámbiala por la IP de tu computador en la red WiFi (ej. 192.168.1.10)
 * y agrégala también en res/xml/network_security_config.xml.
 */
public final class ApiClient {

    public static final String BASE_URL = "http://10.0.2.2:3000/api/";

    private static UebankApi instancia;

    private ApiClient() {
    }

    public static synchronized UebankApi get() {
        if (instancia == null) {
            instancia = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(UebankApi.class);
        }
        return instancia;
    }
}
