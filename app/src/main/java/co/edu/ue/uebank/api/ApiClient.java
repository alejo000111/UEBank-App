package co.edu.ue.uebank.api;

import co.edu.ue.uebank.BuildConfig;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Crea una única instancia de Retrofit para toda la app (singleton).
 *
 * BASE_URL sale de BuildConfig.API_BASE_URL, que a su vez sale de la
 * propiedad API_BASE_URL en local.properties (ver app/build.gradle.kts). NO
 * está escrita aquí a propósito: local.properties es distinto en cada
 * computador y nunca se sube al repositorio, así que cada integrante del
 * equipo puede apuntar a su propio backend sin tocar código ni generar
 * conflictos de Git. Si nadie define nada, por defecto usa 10.0.2.2, la
 * dirección con la que el EMULADOR ve el "localhost" del mismo computador
 * donde corre la API (no sirve para un celular físico ni para el emulador
 * de otro compañero). Instrucciones completas en backend/README.md.
 */
public final class ApiClient {

    public static final String BASE_URL = BuildConfig.API_BASE_URL;

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
