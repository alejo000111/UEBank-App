package co.edu.ue.uebank;

import android.text.InputType;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

import co.edu.ue.uebank.api.ApiCallback;
import co.edu.ue.uebank.api.ApiClient;
import co.edu.ue.uebank.api.Cuenta;
import co.edu.ue.uebank.api.Movimiento;
import co.edu.ue.uebank.ui.Formato;
import co.edu.ue.uebank.ui.FormDialog;
import co.edu.ue.uebank.ui.ListaBaseActivity;

/**
 * CRUD de movimientos consumiendo la API REST.
 *
 * Al crear un movimiento, el SERVIDOR (no la app) actualiza el saldo de la
 * cuenta dentro de una transacción de PostgreSQL y rechaza los retiros que
 * superen el saldo. Anular un movimiento revierte su efecto.
 *
 * Si se abre desde "Ver movimientos" de una cuenta, recibe EXTRA_CUENTA_ID y
 * muestra solo los movimientos de esa cuenta.
 */
public class MovimientosActivity extends ListaBaseActivity<Movimiento> {

    public static final String EXTRA_CUENTA_ID = "cuenta_id";

    private static final String DEPOSITO = "DEPOSITO";
    private static final String RETIRO = "RETIRO";

    private long cuentaFiltro = -1;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.cuentaFiltro = getIntent().getLongExtra(EXTRA_CUENTA_ID, -1);
    }

    @Override
    protected int getTituloRes() {
        return R.string.movimientos_titulo;
    }

    @Override
    protected int getTextoAgregarRes() {
        return R.string.movimiento_agregar;
    }

    @Override
    protected String titulo(Movimiento m) {
        String signo = DEPOSITO.equals(m.tipo) ? "+ " : "- ";
        return signo + Formato.moneda(m.monto) + "  ·  " + m.tipo;
    }

    @Override
    protected String subtitulo(Movimiento m) {
        String fecha = m.fecha == null ? "" : m.fecha.substring(0, Math.min(10, m.fecha.length()));
        String descripcion = m.descripcion == null || m.descripcion.isEmpty() ? "" : "  ·  " + m.descripcion;
        return getString(R.string.cuenta_etiqueta, m.cuentaNumero) + "  ·  " + fecha + descripcion;
    }

    // ---------- READ ----------
    @Override
    protected void cargarDatos() {
        Long filtro = cuentaFiltro == -1 ? null : cuentaFiltro;
        ApiClient.get().listarMovimientos(getUsuario(), filtro)
                .enqueue(new ApiCallback<>(this, this::mostrar));
    }

    // ---------- CREATE: elegir cuenta -> elegir tipo -> llenar monto ----------
    @Override
    protected void onAgregar() {
        if (cuentaFiltro != -1) {
            elegirTipo(cuentaFiltro);
            return;
        }
        ApiClient.get().listarCuentas(getUsuario()).enqueue(new ApiCallback<>(this, this::elegirCuenta));
    }

    private void elegirCuenta(List<Cuenta> cuentas) {
        if (cuentas == null || cuentas.isEmpty()) {
            toast(R.string.movimiento_sin_cuentas);
            return;
        }
        String[] etiquetas = new String[cuentas.size()];
        for (int i = 0; i < cuentas.size(); i++) {
            Cuenta c = cuentas.get(i);
            etiquetas[i] = c.numero + "  (" + Formato.moneda(c.saldo) + ")";
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.movimiento_elegir_cuenta)
                .setItems(etiquetas, (d, indice) -> elegirTipo(cuentas.get(indice).id))
                .show();
    }

    private void elegirTipo(long cuentaId) {
        String[] tipos = {getString(R.string.movimiento_deposito), getString(R.string.movimiento_retiro)};
        new AlertDialog.Builder(this)
                .setTitle(R.string.movimiento_elegir_tipo)
                .setItems(tipos, (d, indice) ->
                        formularioNuevo(cuentaId, indice == 0 ? DEPOSITO : RETIRO))
                .show();
    }

    private void formularioNuevo(long cuentaId, String tipo) {
        String[] hints = {getString(R.string.hint_monto), getString(R.string.hint_descripcion)};
        int[] entradas = {
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL,
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        };

        FormDialog.mostrar(this, R.string.movimiento_nuevo, hints, entradas, null, valores -> {
            Double monto = Formato.aNumero(valores[0]);
            if (monto == null || monto <= 0) {
                toast(R.string.error_dato_invalido);
                return false;
            }
            Movimiento nuevo = new Movimiento(cuentaId, tipo, monto, valores[1]);
            ApiClient.get().crearMovimiento(nuevo).enqueue(new ApiCallback<>(this, creado -> {
                toast(R.string.exito_guardado);
                cargarDatos();
            }));
            return true;
        });
    }

    // ---------- UPDATE (solo la descripción) / DELETE (anular) ----------
    @Override
    protected void onItemSeleccionado(Movimiento m) {
        mostrarOpciones(
                new String[]{getString(R.string.opcion_editar), getString(R.string.opcion_eliminar)},
                new Runnable[]{
                        () -> editarDescripcion(m),
                        () -> confirmar(R.string.confirmar_anular, () ->
                                ApiClient.get().eliminarMovimiento(m.id)
                                        .enqueue(new ApiCallback<>(this, vacio -> {
                                            toast(R.string.exito_eliminado);
                                            cargarDatos();
                                        })))
                });
    }

    private void editarDescripcion(Movimiento m) {
        FormDialog.mostrar(this, R.string.movimiento_editar,
                new String[]{getString(R.string.hint_descripcion)},
                new int[]{InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES},
                new String[]{m.descripcion}, valores -> {
                    Movimiento cambios = new Movimiento(m.cuentaId, m.tipo, m.monto, valores[0]);
                    ApiClient.get().actualizarMovimiento(m.id, cambios)
                            .enqueue(new ApiCallback<>(this, actualizado -> {
                                toast(R.string.exito_guardado);
                                cargarDatos();
                            }));
                    return true;
                });
    }
}
