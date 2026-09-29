package co.edu.ue.uebank.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import co.edu.ue.uebank.R;
import co.edu.ue.uebank.managers.SessionManager;

/**
 * Esqueleto común de las pantallas "lista + CRUD" (Beneficiarios, Cuentas,
 * Movimientos y Metas). Se encarga de lo repetitivo: layout, márgenes del
 * sistema, título, botón Agregar, RecyclerView, indicador de carga, mensaje
 * de "vacío" y botón Volver.
 *
 * Cada pantalla concreta solo dice QUÉ mostrar y QUÉ hacer al agregar o al
 * tocar un elemento (patrón "Template Method").
 */
public abstract class ListaBaseActivity<T> extends AppCompatActivity {

    protected SessionManager sessionManager;

    private ListaAdapter<T> adapter;
    private TextView tvVacio;
    private SwipeRefreshLayout swipeRefresh;

    protected abstract int getTituloRes();

    protected abstract int getTextoAgregarRes();

    protected abstract String titulo(T item);

    protected abstract String subtitulo(T item);

    /** Debe obtener los datos y terminar llamando a mostrar(lista). */
    protected abstract void cargarDatos();

    protected abstract void onAgregar();

    protected abstract void onItemSeleccionado(T item);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.sessionManager = new SessionManager(this);
        this.tvVacio = findViewById(R.id.tvVacio);

        ((TextView) findViewById(R.id.tvTitulo)).setText(getTituloRes());

        Button btnAgregar = findViewById(R.id.btnAgregar);
        btnAgregar.setText(getTextoAgregarRes());
        btnAgregar.setOnClickListener(v -> onAgregar());

        this.adapter = new ListaAdapter<>(this::titulo, this::subtitulo, this::onItemSeleccionado);
        RecyclerView rvItems = findViewById(R.id.rvItems);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);

        this.swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(this::cargarDatos);

        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Se recarga al volver a la pantalla para que siempre refleje lo último guardado.
        mostrarCargando(true);
        cargarDatos();
    }

    /** Pinta la lista, muestra el mensaje de "vacío" si no hay datos y apaga el indicador de carga. */
    protected void mostrar(List<T> datos) {
        adapter.actualizar(datos);
        tvVacio.setVisibility(adapter.estaVacia() ? View.VISIBLE : View.GONE);
        mostrarCargando(false);
    }

    /**
     * Prende o apaga el círculo de "cargando" de SwipeRefreshLayout.
     *
     * OJO: SwipeRefreshLayout tiene un bug conocido cuando se llama a
     * setRefreshing() ANTES de que la vista termine su primer layout (por
     * ejemplo, en Beneficiarios: como lee de SQLite, termina tan rápido que
     * "true" y "false" llegan casi al mismo tiempo). El resultado es que el
     * círculo se queda girando para siempre, aunque los datos ya cargaron.
     * Encolarlo con post() lo difiere hasta después del layout y evita el bug.
     */
    protected void mostrarCargando(boolean cargando) {
        swipeRefresh.post(() -> swipeRefresh.setRefreshing(cargando));
    }

    protected String getUsuario() {
        return sessionManager.getUsuario();
    }

    protected void toast(int mensajeRes) {
        Toast.makeText(this, mensajeRes, Toast.LENGTH_SHORT).show();
    }

    /** Menú de opciones (Editar / Eliminar / ...) para el elemento tocado. */
    protected void mostrarOpciones(String[] opciones, Runnable[] acciones) {
        new AlertDialog.Builder(this)
                .setItems(opciones, (dialogo, indice) -> acciones[indice].run())
                .show();
    }

    protected void confirmar(int mensajeRes, Runnable alConfirmar) {
        new AlertDialog.Builder(this)
                .setMessage(mensajeRes)
                .setPositiveButton(R.string.opcion_eliminar, (d, w) -> alConfirmar.run())
                .setNegativeButton(R.string.btn_cancelar, null)
                .show();
    }
}
