package co.edu.ue.uebank.ui;

import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;

import co.edu.ue.uebank.R;

/**
 * Diálogo de formulario reutilizable: recibe los campos (hint, tipo de teclado
 * y valor inicial) y devuelve lo escrito. Se usa tanto para crear (valores
 * iniciales vacíos) como para editar (valores iniciales cargados).
 */
public final class FormDialog {

    /**
     * Se llama al pulsar "Guardar". Devuelve true si los datos fueron válidos
     * (el diálogo se cierra) o false para dejarlo abierto y que el usuario corrija.
     */
    public interface OnSubmit {
        boolean onSubmit(String[] valores);
    }

    private FormDialog() {
    }

    public static void mostrar(Context context, int tituloRes, String[] hints, int[] tiposEntrada,
                               String[] iniciales, OnSubmit alGuardar) {
        int margen = (int) (20 * context.getResources().getDisplayMetrics().density);

        LinearLayout contenedor = new LinearLayout(context);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setPadding(margen, margen / 2, margen, 0);

        EditText[] campos = new EditText[hints.length];
        for (int i = 0; i < hints.length; i++) {
            campos[i] = new EditText(context);
            campos[i].setHint(hints[i]);
            campos[i].setInputType(tiposEntrada[i]);
            campos[i].setSingleLine(true);
            if (iniciales != null && iniciales[i] != null) {
                campos[i].setText(iniciales[i]);
            }
            contenedor.addView(campos[i]);
        }

        AlertDialog dialogo = new AlertDialog.Builder(context)
                .setTitle(tituloRes)
                .setView(contenedor)
                .setPositiveButton(R.string.btn_guardar, null)
                .setNegativeButton(R.string.btn_cancelar, null)
                .create();

        dialogo.show();

        // El listener se pone después de show() para que un formulario inválido
        // NO cierre el diálogo (por defecto el botón positivo siempre lo cierra).
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String[] valores = new String[campos.length];
            for (int i = 0; i < campos.length; i++) {
                valores[i] = campos[i].getText().toString().trim();
            }
            if (alGuardar.onSubmit(valores)) {
                dialogo.dismiss();
            }
        });
    }
}
