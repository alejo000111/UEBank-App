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
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import co.edu.ue.uebank.api.ApiClient;
import co.edu.ue.uebank.api.AuthRequest;
import co.edu.ue.uebank.api.TokenResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import co.edu.ue.uebank.data.UsuarioRepository;
import co.edu.ue.uebank.managers.SessionManager;
import co.edu.ue.uebank.model.Usuario;
import co.edu.ue.uebank.security.PasswordUtils;

//Clase
public class LoginActivity extends AppCompatActivity {

    //Atributos
    private UsuarioRepository usuarioRepository;
    private SessionManager sessionManager;

    private EditText etUsuario;
    private EditText etPassword;
    private CheckBox cbRecordar;

    //Ciclo de vida
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        this.usuarioRepository = new UsuarioRepository(this);
        this.sessionManager = new SessionManager(this);

        //Restaurar token
        ApiClient.setToken(sessionManager.getToken());

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

    //Vistas
    private void initViews() {
        this.etUsuario = findViewById(R.id.etUsuario);
        this.etPassword = findViewById(R.id.etPassword);
        this.cbRecordar = findViewById(R.id.cbRecordar);
    }

    //Eventos
    private void setupEventListeners() {
        Button btnIngresar = findViewById(R.id.btnIngresar);
        btnIngresar.setOnClickListener(v -> intentarIniciarSesion());

        TextView tvIrRegistro = findViewById(R.id.tvIrRegistro);
        tvIrRegistro.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegistroActivity.class)));
    }

    //Iniciar sesión
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

        pedirTokenApi(usuarioEncontrado);
    }

    //Pedir token
    private void pedirTokenApi(Usuario usuario) {
        AuthRequest cuerpo = new AuthRequest(usuario.getUsuario(), usuario.getPasswordHash());
        ApiClient.get().loginAuth(cuerpo).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> respuesta) {
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    sessionManager.guardarToken(respuesta.body().token);
                    ApiClient.setToken(respuesta.body().token);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.error_generico, Toast.LENGTH_LONG).show();
                }
                irAPanelPrincipal();
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable error) {
                Toast.makeText(getApplicationContext(), R.string.error_conexion, Toast.LENGTH_LONG).show();
                irAPanelPrincipal();
            }
        });
    }

    //Ir al panel principal
    private void irAPanelPrincipal() {
        Intent intent = new Intent(this, PanelPrincipalActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
