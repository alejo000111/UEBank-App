package co.edu.ue.uebank;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.NumberFormat;
import java.util.Locale;

import co.edu.ue.uebank.data.UsuarioRepository;
import co.edu.ue.uebank.managers.SessionManager;
import co.edu.ue.uebank.model.Usuario;

/**
 * Panel principal: lo primero que ve el cliente luego de iniciar sesión.
 * Por ahora muestra su saludo y su saldo; los botones de Beneficiarios y
 * Movimientos quedan como punto de partida para las próximas entregas
 * (Room para el CRUD de beneficiarios, y la API REST para los movimientos).
 */
public class PanelPrincipalActivity extends AppCompatActivity {

    private UsuarioRepository usuarioRepository;
    private SessionManager sessionManager;

    private TextView tvSaludo;
    private TextView tvSaldo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_panel_principal);

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

    private void initViews() {
        this.tvSaludo = findViewById(R.id.tvSaludo);
        this.tvSaldo = findViewById(R.id.tvSaldo);
    }

    private void setupEventListeners() {
        Button btnMiPerfil = findViewById(R.id.btnMiPerfil);
        btnMiPerfil.setOnClickListener(v ->
                startActivity(new Intent(this, PerfilActivity.class)));

        Button btnBeneficiarios = findViewById(R.id.btnBeneficiarios);
        btnBeneficiarios.setOnClickListener(v ->
                Toast.makeText(this, R.string.funcion_proximamente, Toast.LENGTH_SHORT).show());

        Button btnMovimientos = findViewById(R.id.btnMovimientos);
        btnMovimientos.setOnClickListener(v ->
                Toast.makeText(this, R.string.funcion_proximamente, Toast.LENGTH_SHORT).show());

        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Se recarga cada vez que la pantalla vuelve a primer plano (por
        // ejemplo, al volver desde Perfil) por si el saldo llegara a cambiar.
        mostrarDatosUsuario();
    }

    private void mostrarDatosUsuario() {
        long idUsuario = sessionManager.getIdUsuario();
        Usuario usuario = usuarioRepository.obtenerPorId(idUsuario);

        if (usuario == null) {
            // La sesión quedó "huérfana" (no debería pasar en uso normal).
            cerrarSesion();
            return;
        }

        tvSaludo.setText(getString(R.string.saludo_usuario, usuario.getNombre()));

        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        tvSaldo.setText(formatoMoneda.format(usuario.getSaldo()));
    }

    private void cerrarSesion() {
        sessionManager.cerrarSesion();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
