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

import co.edu.ue.uebank.api.ApiClient;
import co.edu.ue.uebank.api.SaldoTotal;
import co.edu.ue.uebank.data.UsuarioRepository;
import co.edu.ue.uebank.managers.SessionManager;
import co.edu.ue.uebank.model.Usuario;
import co.edu.ue.uebank.ui.Formato;

//Clase
public class PanelPrincipalActivity extends AppCompatActivity {

    //Atributos
    private UsuarioRepository usuarioRepository;
    private SessionManager sessionManager;

    private TextView tvSaludo;
    private TextView tvSaldo;

    //Ciclo de vida
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

    //Vistas
    private void initViews() {
        this.tvSaludo = findViewById(R.id.tvSaludo);
        this.tvSaldo = findViewById(R.id.tvSaldo);
    }

    //Eventos
    private void setupEventListeners() {
        Button btnMiPerfil = findViewById(R.id.btnMiPerfil);
        btnMiPerfil.setOnClickListener(v ->
                startActivity(new Intent(this, PerfilActivity.class)));

        Button btnBeneficiarios = findViewById(R.id.btnBeneficiarios);
        btnBeneficiarios.setOnClickListener(v ->
                startActivity(new Intent(this, BeneficiariosActivity.class)));

        Button btnCuentas = findViewById(R.id.btnCuentas);
        btnCuentas.setOnClickListener(v ->
                startActivity(new Intent(this, CuentasActivity.class)));

        Button btnMovimientos = findViewById(R.id.btnMovimientos);
        btnMovimientos.setOnClickListener(v ->
                startActivity(new Intent(this, MovimientosActivity.class)));

        Button btnMetas = findViewById(R.id.btnMetas);
        btnMetas.setOnClickListener(v ->
                startActivity(new Intent(this, MetasActivity.class)));

        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    //Al volver a la pantalla
    @Override
    protected void onResume() {
        super.onResume();
        mostrarDatosUsuario();
    }

    //Mostrar datos
    private void mostrarDatosUsuario() {
        long idUsuario = sessionManager.getIdUsuario();
        Usuario usuario = usuarioRepository.obtenerPorId(idUsuario);

        if (usuario == null) {
            cerrarSesion();
            return;
        }

        tvSaludo.setText(getString(R.string.saludo_usuario, usuario.getNombre()));

        tvSaldo.setText(R.string.saldo_cargando);
        SaldoTotal.consultar(this, usuario.getUsuario(), total -> tvSaldo.setText(
                total == null ? getString(R.string.saldo_no_disponible) : Formato.moneda(total)));
    }

    //Cerrar sesión
    private void cerrarSesion() {
        sessionManager.cerrarSesion();
        ApiClient.setToken(null);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
