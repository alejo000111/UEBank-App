package co.edu.ue.uebank.ui;

import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;

import co.edu.ue.uebank.R;

//Clase
public final class FormDialog {

    //Interfaz de guardado
    public interface OnSubmit {
        boolean onSubmit(String[] valores);
    }

    //Constructor
    private FormDialog() {
    }

    //Mostrar formulario
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
