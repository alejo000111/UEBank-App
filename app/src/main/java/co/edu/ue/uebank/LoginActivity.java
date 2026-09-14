package co.edu.ue.uebank;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import co.edu.ue.uebank.data.UsuarioRepository;
import co.edu.ue.uebank.managers.SessionManager;
import co.edu.ue.uebank.model.Usuario;
import co.edu.ue.uebank.security.PasswordUtils;

/**
 * Pantalla de inicio de sesión y puerta de entrada de la app.
 *
 * Antes de mostrar el formulario revisa si ya hay una sesión "recordada"
 * (SharedPreferences) para saltar directo al panel principal, tal como lo
 * describe el módulo de autenticación del proyecto.
 */
public class LoginActivity extends AppCompatActivity {

    private UsuarioRepository usuarioRepository;
    private SessionManager sessionManager;

    private EditText etUsuario;
    private EditText etPassword;
    private CheckBox cbRecordar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.usuarioRepository = new UsuarioRepository(this);
        this.sessionManager = new SessionManager(this);

        // Si el usuario ya marcó "Recordar sesión" antes, no le mostramos el
        // login otra vez: lo mandamos directo al panel principal.
        if (sessionManager.haySesionRecordada()) {
            irAPanelPrincipal();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupEventListeners();
    }

    private void initViews() {
        this.etUsuario = findViewById(R.id.etUsuario);
        this.etPassword = findViewById(R.id.etPassword);
        this.cbRecordar = findViewById(R.id.cbRecordar);
    }

    private void setupEventListeners() {
        Button btnIngresar = findViewById(R.id.btnIngresar);
        btnIngresar.setOnClickListener(v -> intentarIniciarSesion());

        TextView tvIrRegistro = findViewById(R.id.tvIrRegistro);
        tvIrRegistro.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegistroActivity.class)));
    }

    private void intentarIniciarSesion() {
        String usuario = etUsuario.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(usuario) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.error_campos_vacios, Toast.LENGTH_SHORT).show();
            return;
        }

        Usuario usuarioEncontrado = usuarioRepository.obtenerPorUsuario(usuario);
        if (usuarioEncontrado == null) {
            Toast.makeText(this, R.string.error_usuario_no_existe, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean passwordCorrecta = PasswordUtils.verificarPassword(
                password, usuarioEncontrado.getPasswordHash());

        if (!passwordCorrecta) {
            Toast.makeText(this, R.string.error_password_incorrecta, Toast.LENGTH_SHORT).show();
            return;
        }

        sessionManager.iniciarSesion(usuarioEncontrado, cbRecordar.isChecked());
        irAPanelPrincipal();
    }

    private void irAPanelPrincipal() {
        Intent intent = new Intent(this, PanelPrincipalActivity.class);
        // Limpia el historial para que el botón "Atrás" no vuelva al login
        // una vez la sesión ya está iniciada.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
