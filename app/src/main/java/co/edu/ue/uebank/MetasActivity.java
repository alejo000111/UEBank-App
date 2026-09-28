package co.edu.ue.uebank;

import android.text.InputType;

import co.edu.ue.uebank.api.ApiCallback;
import co.edu.ue.uebank.api.ApiClient;
import co.edu.ue.uebank.api.Meta;
import co.edu.ue.uebank.ui.Formato;
import co.edu.ue.uebank.ui.FormDialog;
import co.edu.ue.uebank.ui.ListaBaseActivity;

/**
 * CRUD de metas de ahorro consumiendo la API REST.
 */
public class MetasActivity extends ListaBaseActivity<Meta> {

    private static final int[] TIPOS_ENTRADA = {
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES,
            InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL,
            InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL,
            InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE
    };

    @Override
    protected int getTituloRes() {
        return R.string.metas_titulo;
    }

    @Override
    protected int getTextoAgregarRes() {
        return R.string.meta_agregar;
    }

    @Override
    protected String titulo(Meta m) {
        return m.nombre;
    }

    @Override
    protected String subtitulo(Meta m) {
        int porcentaje = m.montoObjetivo > 0 ? (int) Math.round(m.montoAhorrado * 100 / m.montoObjetivo) : 0;
        String texto = Formato.moneda(m.montoAhorrado) + " de " + Formato.moneda(m.montoObjetivo)
                + " (" + porcentaje + "%)";
        return m.fechaLimite == null ? texto : texto + "  ·  " + m.fechaLimite;
    }

    // ---------- READ ----------
    @Override
    protected void cargarDatos() {
        ApiClient.get().listarMetas(getUsuario()).enqueue(new ApiCallback<>(this, this::mostrar));
    }

    // ---------- CREATE ----------
    @Override
    protected void onAgregar() {
        mostrarFormulario(null);
    }

    // ---------- UPDATE / DELETE ----------
    @Override
    protected void onItemSeleccionado(Meta meta) {
        mostrarOpciones(
                new String[]{getString(R.string.opcion_editar), getString(R.string.opcion_eliminar)},
                new Runnable[]{
                        () -> mostrarFormulario(meta),
                        () -> confirmar(R.string.confirmar_eliminar, () ->
                                ApiClient.get().eliminarMeta(meta.id)
                                        .enqueue(new ApiCallback<>(this, vacio -> {
                                            toast(R.string.exito_eliminado);
                                            cargarDatos();
                                        })))
                });
    }

    /** Formulario para crear (existente == null) o editar una meta. */
    private void mostrarFormulario(Meta existente) {
        String[] hints = {
                getString(R.string.hint_nombre_meta),
                getString(R.string.hint_monto_objetivo),
                getString(R.string.hint_monto_ahorrado),
                getString(R.string.hint_fecha_limite)
        };
        String[] iniciales = existente == null
                ? new String[]{"", "", "0", ""}
                : new String[]{
                existente.nombre,
                String.valueOf(existente.montoObjetivo),
                String.valueOf(existente.montoAhorrado),
                existente.fechaLimite == null ? "" : existente.fechaLimite};

        FormDialog.mostrar(this,
                existente == null ? R.string.meta_agregar : R.string.meta_editar,
                hints, TIPOS_ENTRADA, iniciales, valores -> {
                    Double objetivo = Formato.aNumero(valores[1]);
                    Double ahorrado = Formato.aNumero(valores[2]);
                    if (valores[0].isEmpty() || objetivo == null || objetivo <= 0 || ahorrado == null) {
                        toast(R.string.error_dato_invalido);
                        return false;
                    }
                    if (!Formato.fechaValida(valores[3])) {
                        toast(R.string.error_fecha_invalida);
                        return false;
                    }
                    String fecha = valores[3].isEmpty() ? null : valores[3];
                    Meta meta = new Meta(getUsuario(), valores[0], objetivo, ahorrado, fecha);

                    retrofit2.Call<Meta> llamada = existente == null
                            ? ApiClient.get().crearMeta(meta)
                            : ApiClient.get().actualizarMeta(existente.id, meta);
                    llamada.enqueue(new ApiCallback<>(this, guardada -> {
                        toast(R.string.exito_guardado);
                        cargarDatos();
                    }));
                    return true;
                });
    }
}
