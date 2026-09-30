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

//Clase
public class ListaAdapter<T> extends RecyclerView.Adapter<ListaAdapter.ViewHolder> {

    //Atributos
    private final List<T> items = new ArrayList<>();
    private final Function<T, String> titulo;
    private final Function<T, String> subtitulo;
    private final Consumer<T> alTocar;

    //Constructor
    public ListaAdapter(Function<T, String> titulo, Function<T, String> subtitulo, Consumer<T> alTocar) {
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.alTocar = alTocar;
    }

    //Actualizar lista
    public void actualizar(List<T> nuevosItems) {
        items.clear();
        if (nuevosItems != null) {
            items.addAll(nuevosItems);
        }
        notifyDataSetChanged();
    }

    //Lista vacía
    public boolean estaVacia() {
        return items.isEmpty();
    }

    //Crear fila
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View fila = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lista, parent, false);
        return new ViewHolder(fila);
    }

    //Llenar fila
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T item = items.get(position);
        holder.tvTitulo.setText(titulo.apply(item));
        holder.tvSubtitulo.setText(subtitulo.apply(item));
        holder.itemView.setOnClickListener(v -> alTocar.accept(item));
    }

    //Cantidad de filas
    @Override
    public int getItemCount() {
        return items.size();
    }

    //ViewHolder
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
