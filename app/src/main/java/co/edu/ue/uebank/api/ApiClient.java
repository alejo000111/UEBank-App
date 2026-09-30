package co.edu.ue.uebank.api;

import java.io.IOException;

import co.edu.ue.uebank.BuildConfig;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
 *
 * Desde que la API exige JWT (ver backend/src/auth.js), un OkHttp
 * Interceptor le agrega el encabezado "Authorization: Bearer <token>" a
 * TODA petición saliente, sin tener que tocar cada Activity una por una.
 * setToken(...) lo guarda en memoria; quien inicia sesión (LoginActivity o
 * RegistroActivity) lo llama apenas consigue el token, y también lo
 * restaura ahí mismo desde SessionManager cuando "Recordar sesión" salta
 * directo al panel principal (el campo estático no sobrevive a que el
 * proceso de la app se reinicie).
 */
public final class ApiClient {

    public static final String BASE_URL = BuildConfig.API_BASE_URL;

    private static UebankApi instancia;
    private static volatile String token;

    private ApiClient() {
    }

    /** Guarda (o borra, con null) el token que se agrega a cada petición. */
    public static void setToken(String nuevoToken) {
        token = nuevoToken;
    }

    private static final class AuthInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            String actual = token;
            if (actual == null || actual.isEmpty()) {
                return chain.proceed(original);
            }
            Request conToken = original.newBuilder()
                    .header("Authorization", "Bearer " + actual)
                    .build();
            return chain.proceed(conToken);
        }
    }

    public static synchronized UebankApi get() {
        if (instancia == null) {
            OkHttpClient cliente = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor())
                    .build();
            instancia = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(cliente)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(UebankApi.class);
        }
        return instancia;
    }
}
