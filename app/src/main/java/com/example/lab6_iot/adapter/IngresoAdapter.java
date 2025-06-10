package com.example.lab6_iot.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6_iot.R;
import com.example.lab6_iot.model.Ingreso;

import java.text.DateFormat;
import java.util.List;

public class IngresoAdapter extends RecyclerView.Adapter<IngresoAdapter.VH> {

    public interface Listener {
        void onEdit(Ingreso ingreso);
        void onDelete(Ingreso ingreso);
    }

    private final List<Ingreso> items;
    private final Listener listener;

    public IngresoAdapter(List<Ingreso> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingreso, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Ingreso ingreso = items.get(position);

        holder.tvTitulo.setText(ingreso.getTitulo());
        holder.tvMonto.setText(String.format("%.2f", ingreso.getMonto()));
        holder.tvFecha.setText(DateFormat.getDateInstance().format(ingreso.getFecha().toDate()));
        holder.tvDescripcion.setText(
                ingreso.getDescripcion() != null ? ingreso.getDescripcion() : ""
        );

        holder.itemView.setOnClickListener(v -> listener.onEdit(ingreso));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(ingreso));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvMonto, tvFecha, tvDescripcion;
        ImageButton btnDelete;

        VH(View itemView) {
            super(itemView);
            tvTitulo       = itemView.findViewById(R.id.tvTitulo);
            tvMonto        = itemView.findViewById(R.id.tvMonto);
            tvFecha        = itemView.findViewById(R.id.tvFecha);
            tvDescripcion  = itemView.findViewById(R.id.tvDescripcion);
            btnDelete      = itemView.findViewById(R.id.btnDelete);
        }
    }
}
