package co.edu.ue.uebank.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import co.edu.ue.uebank.R;

/**
 * Adaptador genérico de RecyclerView: pinta cada elemento con un título y un
 * subtítulo (layout item_lista.xml) y avisa cuando se toca una fila. Sirve
 * para cualquier modelo (Beneficiario, Cuenta, Movimiento, Meta): solo hay
 * que decirle cómo obtener el título y el subtítulo de cada uno.
 *
 * RecyclerView reutiliza las vistas de las filas que salen de la pantalla
 * (patrón ViewHolder) en vez de crear una vista nueva por cada elemento como
 * hacía el ListView original: por eso es más eficiente con listas largas.
 */
public class ListaAdapter<T> extends RecyclerView.Adapter<ListaAdapter.ViewHolder> {

    private final List<T> items = new ArrayList<>();
    private final Function<T, String> titulo;
    private final Function<T, String> subtitulo;
    private final Consumer<T> alTocar;

    public ListaAdapter(Function<T, String> titulo, Function<T, String> subtitulo, Consumer<T> alTocar) {
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.alTocar = alTocar;
    }

    /** Reemplaza todos los elementos mostrados (recarga completa de la lista). */
    public void actualizar(List<T> nuevosItems) {
        items.clear();
        if (nuevosItems != null) {
            items.addAll(nuevosItems);
        }
        notifyDataSetChanged();
    }

    public boolean estaVacia() {
        return items.isEmpty();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View fila = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lista, parent, false);
        return new ViewHolder(fila);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T item = items.get(position);
        holder.tvTitulo.setText(titulo.apply(item));
        holder.tvSubtitulo.setText(subtitulo.apply(item));
        holder.itemView.setOnClickListener(v -> alTocar.accept(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitulo;
        final TextView tvSubtitulo;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tvTitulo = itemView.findViewById(R.id.tvItemTitulo);
            this.tvSubtitulo = itemView.findViewById(R.id.tvItemSubtitulo);
        }
    }
}
