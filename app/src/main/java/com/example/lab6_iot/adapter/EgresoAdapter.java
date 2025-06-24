
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
        void onDownload(Egreso egreso);   // ← nuevo
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
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Egreso eg = items.get(pos);
        h.tvTitulo.setText(eg.getTitulo());
        h.tvMonto.setText(String.format("%.2f", eg.getMonto()));
        h.tvFecha.setText(DateFormat.getDateInstance().format(eg.getFecha().toDate()));
        h.tvDescripcion.setText(eg.getDescripcion() != null ? eg.getDescripcion() : "");

        h.itemView.setOnClickListener(v -> listener.onEdit(eg));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(eg));
        h.btnDownload.setOnClickListener(v -> listener.onDownload(eg));  // ← descarga
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvMonto, tvFecha, tvDescripcion;
        ImageButton btnDelete, btnDownload;

        VH(View itemView) {
            super(itemView);
            tvTitulo      = itemView.findViewById(R.id.tvTitulo);
            tvMonto       = itemView.findViewById(R.id.tvMonto);
            tvFecha       = itemView.findViewById(R.id.tvFecha);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            btnDelete     = itemView.findViewById(R.id.btnDelete);
            btnDownload   = itemView.findViewById(R.id.btnDownload);
        }
    }
}
