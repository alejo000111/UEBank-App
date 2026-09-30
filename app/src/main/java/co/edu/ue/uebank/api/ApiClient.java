package co.edu.ue.uebank.api;

import co.edu.ue.uebank.BuildConfig;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//Clase
public final class ApiClient {

    //Atributos
    public static final String BASE_URL = BuildConfig.API_BASE_URL;

    private static UebankApi instancia;

    //Constructor
    private ApiClient() {
    }

    //Obtener instancia
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
