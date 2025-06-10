package com.example.lab6_iot.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6_iot.R;
import com.example.lab6_iot.model.Egreso;

import java.text.DateFormat;
import java.util.List;

public class EgresoAdapter extends RecyclerView.Adapter<EgresoAdapter.VH> {

    public interface Listener {
        void onEdit(Egreso egreso);
        void onDelete(Egreso egreso);
    }

    private final List<Egreso> items;
    private final Listener listener;

    public EgresoAdapter(List<Egreso> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_egreso, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Egreso egreso = items.get(position);

        holder.tvTitulo.setText(egreso.getTitulo());
        holder.tvMonto.setText(String.format("%.2f", egreso.getMonto()));
        holder.tvFecha.setText(DateFormat.getDateInstance().format(egreso.getFecha().toDate()));
        holder.tvDescripcion.setText(
                egreso.getDescripcion() != null ? egreso.getDescripcion() : ""
        );

        holder.itemView.setOnClickListener(v -> listener.onEdit(egreso));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(egreso));
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
