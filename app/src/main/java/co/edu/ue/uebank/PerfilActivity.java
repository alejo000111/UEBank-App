package co.edu.ue.uebank;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.NumberFormat;
import java.util.Locale;

import co.edu.ue.uebank.data.UsuarioRepository;
import co.edu.ue.uebank.managers.FotoPerfilManager;
import co.edu.ue.uebank.managers.SessionManager;
import co.edu.ue.uebank.model.Usuario;

/**
 * Pantalla de perfil: muestra los datos del cliente y permite tomarle una
 * foto con la cámara del dispositivo.
 *
 * Aquí se aplica el flujo de permisos peligrosos que pide Android desde la
 * API 23: primero se revisa si el permiso ya fue concedido
 * (ContextCompat.checkSelfPermission) y, si no, se solicita en tiempo de
 * ejecución con un ActivityResultLauncher en vez del método antiguo
 * requestPermissions()/onRequestPermissionsResult().
 */
public class PerfilActivity extends AppCompatActivity {

    private UsuarioRepository usuarioRepository;
    private FotoPerfilManager fotoPerfilManager;
    private Usuario usuarioActual;

    private ImageView ivFotoPerfil;

    // Abre la cámara del sistema y devuelve una miniatura como Bitmap.
    private ActivityResultLauncher<Void> cameraLauncher;

    // Muestra el diálogo de "Permitir/Denegar" del sistema para CAMERA.
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.usuarioRepository = new UsuarioRepository(this);
        this.fotoPerfilManager = new FotoPerfilManager(this);

        SessionManager sessionManager = new SessionManager(this);
        this.usuarioActual = usuarioRepository.obtenerPorId(sessionManager.getIdUsuario());

        registrarLaunchers();
        initViews();
        setupEventListeners();
        mostrarDatosUsuario();
    }

    /**
     * Los ActivityResultLauncher deben registrarse antes de que la Activity
     * quede en estado STARTED, por eso se hace en onCreate.
     */
    private void registrarLaunchers() {
        this.cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        guardarNuevaFoto(bitmap);
                    }
                });

        this.permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                concedido -> {
                    if (concedido) {
                        cameraLauncher.launch(null);
                    } else {
                        Toast.makeText(this, R.string.permiso_camara_denegado, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void initViews() {
        this.ivFotoPerfil = findViewById(R.id.ivFotoPerfil);
    }

    private void setupEventListeners() {
        Button btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnTomarFoto.setOnClickListener(v -> abrirCamaraConPermiso());

        Button btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> finish());
    }

    private void abrirCamaraConPermiso() {
        boolean permisoConcedido = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        if (permisoConcedido) {
            cameraLauncher.launch(null);
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void guardarNuevaFoto(Bitmap foto) {
        ivFotoPerfil.setImageBitmap(foto);

        String ruta = fotoPerfilManager.guardarFotoPerfil(usuarioActual.getUsuario(), foto);
        if (ruta != null) {
            usuarioRepository.actualizarFotoPerfil(usuarioActual.getId(), ruta);
            usuarioActual.setFotoPath(ruta);
            Toast.makeText(this, R.string.foto_guardada, Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDatosUsuario() {
        if (usuarioActual == null) {
            finish();
            return;
        }

        TextView tvNombrePerfil = findViewById(R.id.tvNombrePerfil);
        TextView tvUsuarioPerfil = findViewById(R.id.tvUsuarioPerfil);
        TextView tvSaldoPerfil = findViewById(R.id.tvSaldoPerfil);

        tvNombrePerfil.setText(usuarioActual.getNombre());
        tvUsuarioPerfil.setText(usuarioActual.getUsuario());

        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        tvSaldoPerfil.setText(formatoMoneda.format(usuarioActual.getSaldo()));

        Bitmap fotoGuardada = fotoPerfilManager.cargarFotoPerfil(usuarioActual.getFotoPath());
        if (fotoGuardada != null) {
            ivFotoPerfil.setImageBitmap(fotoGuardada);
        }
        // Si no hay foto guardada, se deja el ícono de silueta que ya trae el layout.
    }
}
