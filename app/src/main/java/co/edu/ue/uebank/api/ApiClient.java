package co.edu.ue.uebank.api;

import java.io.IOException;

import co.edu.ue.uebank.BuildConfig;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//Clase — singleton de Retrofit; agrega el JWT a cada petición via AuthInterceptor
public final class ApiClient {

    //Atributos
    public static final String BASE_URL = BuildConfig.API_BASE_URL;

    private static UebankApi instancia;
    private static volatile String token;

    //Constructor
    private ApiClient() {
    }

    //Guarda (o borra, con null) el token que se agrega a cada petición
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

    //Obtener instancia
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
