package co.edu.ue.uebank;

import android.content.Intent;
import android.text.InputType;

import java.util.Locale;

import co.edu.ue.uebank.api.ApiCallback;
import co.edu.ue.uebank.api.ApiClient;
import co.edu.ue.uebank.api.Cuenta;
import co.edu.ue.uebank.ui.Formato;
import co.edu.ue.uebank.ui.FormDialog;
import co.edu.ue.uebank.ui.ListaBaseActivity;

/**
 * CRUD de cuentas consumiendo la API REST (Node.js + PostgreSQL).
 * El saldo solo se puede modificar registrando movimientos, por eso al editar
 * únicamente se cambian el número y el tipo.
 */
public class CuentasActivity extends ListaBaseActivity<Cuenta> {

    @Override
    protected int getTituloRes() {
        return R.string.cuentas_titulo;
    }

    @Override
    protected int getTextoAgregarRes() {
        return R.string.cuenta_agregar;
    }

    @Override
    protected String titulo(Cuenta c) {
        return getString(R.string.cuenta_etiqueta, c.numero);
    }

    @Override
    protected String subtitulo(Cuenta c) {
        return c.tipo + "  ·  " + Formato.moneda(c.saldo);
    }

    // ---------- READ ----------
    @Override
    protected void cargarDatos() {
        ApiClient.get().listarCuentas(getUsuario())
                .enqueue(new ApiCallback<>(this, this::mostrar));
    }

    // ---------- CREATE ----------
    @Override
    protected void onAgregar() {
        String[] hints = {
                getString(R.string.hint_numero_cuenta),
                getString(R.string.hint_tipo_cuenta),
                getString(R.string.hint_saldo_inicial_cuenta)
        };
        int[] tipos = {
                InputType.TYPE_CLASS_NUMBER,
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS,
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
        };

        FormDialog.mostrar(this, R.string.cuenta_nueva, hints, tipos,
                new String[]{"", "AHORROS", "0"}, valores -> {
                    Double saldo = Formato.aNumero(valores[2]);
                    if (valores[1].isEmpty() || saldo == null) {
                        toast(R.string.error_dato_invalido);
                        return false;
                    }
                    if (!Formato.numeroCuentaValido(valores[0])) {
                        toast(R.string.error_numero_cuenta_invalido);
                        return false;
                    }
                    Cuenta nueva = new Cuenta(getUsuario(), valores[0], valores[1], saldo);
                    ApiClient.get().crearCuenta(nueva).enqueue(new ApiCallback<>(this, creada -> {
                        toast(R.string.exito_guardado);
                        cargarDatos();
                    }));
                    return true;
                });
    }

    // ---------- UPDATE / DELETE ----------
    @Override
    protected void onItemSeleccionado(Cuenta cuenta) {
        mostrarOpciones(
                new String[]{
                        getString(R.string.opcion_ver_movimientos),
                        getString(R.string.opcion_editar),
                        getString(R.string.opcion_eliminar)},
                new Runnable[]{
                        () -> startActivity(new Intent(this, MovimientosActivity.class)
                                .putExtra(MovimientosActivity.EXTRA_CUENTA_ID, cuenta.id)),
                        () -> editar(cuenta),
                        () -> confirmar(R.string.confirmar_eliminar, () ->
                                ApiClient.get().eliminarCuenta(cuenta.id)
                                        .enqueue(new ApiCallback<>(this, vacio -> {
                                            toast(R.string.exito_eliminado);
                                            cargarDatos();
                                        })))
                });
    }

    private void editar(Cuenta cuenta) {
        String[] hints = {
                getString(R.string.hint_numero_cuenta),
                getString(R.string.hint_tipo_cuenta)
        };
        int[] tipos = {
                InputType.TYPE_CLASS_NUMBER,
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        };

        FormDialog.mostrar(this, R.string.cuenta_editar, hints, tipos,
                new String[]{cuenta.numero, cuenta.tipo}, valores -> {
                    if (valores[1].isEmpty()) {
                        toast(R.string.error_campos_vacios);
                        return false;
                    }
                    if (!Formato.numeroCuentaValido(valores[0])) {
                        toast(R.string.error_numero_cuenta_invalido);
                        return false;
                    }
                    Cuenta cambios = new Cuenta(getUsuario(), valores[0],
                            valores[1].toUpperCase(Locale.ROOT), cuenta.saldo);
                    ApiClient.get().actualizarCuenta(cuenta.id, cambios)
                            .enqueue(new ApiCallback<>(this, actualizada -> {
                                toast(R.string.exito_guardado);
                                cargarDatos();
                            }));
                    return true;
                });
    }
}
