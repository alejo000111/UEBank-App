package co.edu.ue.uebank;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import co.edu.ue.uebank.data.BeneficiarioRepository;
import co.edu.ue.uebank.model.Beneficiario;
import co.edu.ue.uebank.ui.FormDialog;
import co.edu.ue.uebank.ui.ListaBaseActivity;

/**
 * CRUD de beneficiarios sobre SQLite (local) + segundo recurso del dispositivo:
 * la agenda de CONTACTOS.
 *
 * Al agregar se puede elegir un contacto del teléfono; la app solo copia el
 * nombre y el número, y el usuario completa el número de cuenta.
 *
 * Nota de seguridad: se abre el selector de contactos del sistema
 * (ACTION_PICK). Android le da a la app acceso temporal ÚNICAMENTE al contacto
 * que el usuario escoge, así que no hace falta el permiso READ_CONTACTS ni
 * tener acceso a toda la agenda (principio de mínimo privilegio).
 */
public class BeneficiariosActivity extends ListaBaseActivity<Beneficiario> {

    private static final int TIPO_TEXTO = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS;
    private static final int TIPO_NUMERO = InputType.TYPE_CLASS_NUMBER;
    private static final int TIPO_TELEFONO = InputType.TYPE_CLASS_PHONE;

    private BeneficiarioRepository repositorio;
    private ActivityResultLauncher<Intent> selectorContactos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.repositorio = new BeneficiarioRepository(this);

        // Se registra en onCreate (antes de STARTED), como exige ActivityResult.
        this.selectorContactos = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                resultado -> {
                    if (resultado.getResultCode() == Activity.RESULT_OK && resultado.getData() != null) {
                        leerContacto(resultado.getData().getData());
                    }
                });
    }

    @Override
    protected int getTituloRes() {
        return R.string.beneficiarios_titulo;
    }

    @Override
    protected int getTextoAgregarRes() {
        return R.string.beneficiario_agregar;
    }

    @Override
    protected String titulo(Beneficiario b) {
        return b.getNombre();
    }

    @Override
    protected String subtitulo(Beneficiario b) {
        String telefono = b.getTelefono() == null || b.getTelefono().isEmpty()
                ? getString(R.string.beneficiario_sin_telefono)
                : b.getTelefono();
        return b.getNumeroCuenta() + "  ·  " + telefono;
    }

    @Override
    protected void cargarDatos() {
        mostrar(repositorio.listarPorUsuario(sessionManager.getIdUsuario()));
    }

    // ---------- CREATE ----------
    @Override
    protected void onAgregar() {
        String[] opciones = {
                getString(R.string.beneficiario_origen_manual),
                getString(R.string.beneficiario_origen_contactos)
        };
        new AlertDialog.Builder(this)
                .setTitle(R.string.beneficiario_origen_titulo)
                .setItems(opciones, (dialogo, indice) -> {
                    if (indice == 0) {
                        mostrarFormulario(null, "", "");
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                        selectorContactos.launch(intent);
                    }
                })
                .show();
    }

    /** Lee nombre y teléfono del contacto elegido y abre el formulario prellenado. */
    private void leerContacto(Uri uriContacto) {
        String[] columnas = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };
        try (Cursor cursor = getContentResolver().query(uriContacto, columnas, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String nombre = cursor.getString(0);
                String telefono = cursor.getString(1);
                mostrarFormulario(null, nombre, telefono);
                return;
            }
        }
        toast(R.string.contacto_no_leido);
    }

    // ---------- READ / UPDATE / DELETE ----------
    @Override
    protected void onItemSeleccionado(Beneficiario b) {
        mostrarOpciones(
                new String[]{getString(R.string.opcion_editar), getString(R.string.opcion_eliminar)},
                new Runnable[]{
                        () -> mostrarFormulario(b, b.getNombre(), b.getTelefono()),
                        () -> confirmar(R.string.confirmar_eliminar, () -> {
                            repositorio.eliminar(b.getId());
                            toast(R.string.exito_eliminado);
                            cargarDatos();
                        })
                });
    }

    /**
     * Formulario para crear (existente == null) o editar un beneficiario.
     */
    private void mostrarFormulario(Beneficiario existente, String nombre, String telefono) {
        String[] hints = {
                getString(R.string.hint_nombre),
                getString(R.string.hint_cuenta_destino),
                getString(R.string.hint_telefono)
        };
        int[] tipos = {TIPO_TEXTO, TIPO_NUMERO, TIPO_TELEFONO};
        String[] iniciales = {
                nombre,
                existente == null ? "" : existente.getNumeroCuenta(),
                telefono
        };

        FormDialog.mostrar(this,
                existente == null ? R.string.beneficiario_nuevo : R.string.beneficiario_editar,
                hints, tipos, iniciales, valores -> {
                    if (valores[0].isEmpty() || valores[1].isEmpty()) {
                        toast(R.string.error_campos_vacios);
                        return false;
                    }
                    if (existente == null) {
                        repositorio.insertar(new Beneficiario(
                                sessionManager.getIdUsuario(), valores[0], valores[1], valores[2]));
                    } else {
                        existente.setNombre(valores[0]);
                        existente.setNumeroCuenta(valores[1]);
                        existente.setTelefono(valores[2]);
                        repositorio.actualizar(existente);
                    }
                    toast(R.string.exito_guardado);
                    cargarDatos();
                    return true;
                });
    }
}
