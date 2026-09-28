package co.edu.ue.uebank.data;

import android.provider.BaseColumns;

/**
 * "Contrato" de la tabla de beneficiarios (mismo patrón que UsuarioContract).
 */
public final class BeneficiarioContract {

    private BeneficiarioContract() {
        // Solo contiene constantes.
    }

    public static final class BeneficiarioEntry implements BaseColumns {
        public static final String TABLE_NAME = "beneficiarios";

        public static final String COLUMN_ID_USUARIO = "id_usuario";
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_NUMERO_CUENTA = "numero_cuenta";
        public static final String COLUMN_TELEFONO = "telefono";
    }
}
