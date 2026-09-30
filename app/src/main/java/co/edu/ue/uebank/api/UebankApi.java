package co.edu.ue.uebank.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Contrato de la API REST. Retrofit genera en tiempo de ejecución la
 * implementación de esta interfaz: cada método = una petición HTTP.
 * Son 3 CRUD completos: cuentas, movimientos y metas.
 */
public interface UebankApi {

    // ----- Autenticación (JWT) -----
    // Públicos: la API no exige el encabezado Authorization en estos dos.
    // Ver ApiClient para cómo se agrega el token a los demás endpoints.
    @POST("auth/registro")
    Call<TokenResponse> registrarAuth(@Body AuthRequest body);

    @POST("auth/login")
    Call<TokenResponse> loginAuth(@Body AuthRequest body);

    // ----- Cuentas -----
    @GET("cuentas")
    Call<List<Cuenta>> listarCuentas(@Query("usuario") String usuario);

    @POST("cuentas")
    Call<Cuenta> crearCuenta(@Body Cuenta cuenta);

    @PUT("cuentas/{id}")
    Call<Cuenta> actualizarCuenta(@Path("id") long id, @Body Cuenta cuenta);

    @DELETE("cuentas/{id}")
    Call<Void> eliminarCuenta(@Path("id") long id);

    // ----- Movimientos -----
    // cuentaId es opcional: si es null, Retrofit no envía el parámetro.
    @GET("movimientos")
    Call<List<Movimiento>> listarMovimientos(@Query("usuario") String usuario,
                                             @Query("cuenta_id") Long cuentaId);

    @POST("movimientos")
    Call<Movimiento> crearMovimiento(@Body Movimiento movimiento);

    @PUT("movimientos/{id}")
    Call<Movimiento> actualizarMovimiento(@Path("id") long id, @Body Movimiento movimiento);

    @DELETE("movimientos/{id}")
    Call<Void> eliminarMovimiento(@Path("id") long id);

    // ----- Metas de ahorro -----
    @GET("metas")
    Call<List<Meta>> listarMetas(@Query("usuario") String usuario);

    @POST("metas")
    Call<Meta> crearMeta(@Body Meta meta);

    @PUT("metas/{id}")
    Call<Meta> actualizarMeta(@Path("id") long id, @Body Meta meta);

    @DELETE("metas/{id}")
    Call<Void> eliminarMeta(@Path("id") long id);
}
