package co.edu.ue.uebank;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import co.edu.ue.uebank.api.ApiCallback;
import co.edu.ue.uebank.api.ApiClient;
import co.edu.ue.uebank.api.AuthRequest;
import co.edu.ue.uebank.data.UsuarioRepository;
import co.edu.ue.uebank.managers.SessionManager;
import co.edu.ue.uebank.model.Usuario;
import co.edu.ue.uebank.security.PasswordUtils;

//Clase
public class RegistroActivity extends AppCompatActivity {

    //Atributos
    private static final int LONGITUD_MINIMA_PASSWORD = 6;

    private UsuarioRepository usuarioRepository;
    private SessionManager sessionManager;

    private EditText etNombre;
    private EditText etUsuario;
    private EditText etPassword;
    private EditText etConfirmarPassword;

    //Ciclo de vida
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.usuarioRepository = new UsuarioRepository(this);
        this.sessionManager = new SessionManager(this);

        initViews();
        setupEventListeners();
    }

    //Vistas
    private void initViews() {
        this.etNombre = findViewById(R.id.etNombre);
        this.etUsuario = findViewById(R.id.etUsuario);
        this.etPassword = findViewById(R.id.etPassword);
        this.etConfirmarPassword = findViewById(R.id.etConfirmarPassword);
    }

    //Eventos
    private void setupEventListeners() {
        Button btnRegistrarme = findViewById(R.id.btnRegistrarme);
        btnRegistrarme.setOnClickListener(v -> intentarRegistrar());

        TextView tvIrLogin = findViewById(R.id.tvIrLogin);
        tvIrLogin.setOnClickListener(v -> finish());
    }

    //Registrar usuario
    private void intentarRegistrar() {
        String nombre = etNombre.getText().toString().trim();
        String usuario = etUsuario.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmarPassword = etConfirmarPassword.getText().toString();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(usuario)
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmarPassword)) {
            Toast.makeText(this, R.string.error_campos_vacios, Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < LONGITUD_MINIMA_PASSWORD) {
            Toast.makeText(this, R.string.error_password_corta, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmarPassword)) {
            Toast.makeText(this, R.string.error_password_no_coincide, Toast.LENGTH_SHORT).show();
            return;
        }

        if (usuarioRepository.existeUsuario(usuario)) {
            Toast.makeText(this, R.string.error_usuario_existente, Toast.LENGTH_SHORT).show();
            return;
        }

        String passwordHash = PasswordUtils.crearHashAlmacenable(password);
        Usuario nuevoUsuario = new Usuario(nombre, usuario, passwordHash);

        long idGenerado = usuarioRepository.insertarUsuario(nuevoUsuario);
        if (idGenerado == -1) {
            Toast.makeText(this, R.string.error_usuario_existente, Toast.LENGTH_SHORT).show();
            return;
        }

        nuevoUsuario.setId(idGenerado);
        Toast.makeText(this, R.string.exito_registro, Toast.LENGTH_LONG).show();

        sessionManager.iniciarSesion(nuevoUsuario, false);

        // Da de alta el mismo "salt:hash" en la API para conseguir el JWT
        // que exigen Cuentas/Movimientos/Metas. Si no hay conexión, el
        // registro local ya quedó guardado igual: la próxima vez que este
        // usuario inicie sesión con internet, /api/auth/login lo registra
        // ahí sin que el usuario note nada (ver backend/src/routes/auth.js).
        pedirTokenApi(nuevoUsuario);

        Intent intent = new Intent(this, PanelPrincipalActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void pedirTokenApi(Usuario usuario) {
        // Contexto de aplicación: esta Activity se cierra (finish()) justo
        // después de lanzar el panel principal.
        AuthRequest cuerpo = new AuthRequest(usuario.getUsuario(), usuario.getPasswordHash());
        ApiClient.get().registrarAuth(cuerpo).enqueue(new ApiCallback<>(getApplicationContext(), respuesta -> {
            if (respuesta != null) {
                sessionManager.guardarToken(respuesta.token);
                ApiClient.setToken(respuesta.token);
            }
        }));
    }
}
