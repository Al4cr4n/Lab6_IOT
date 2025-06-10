package com.example.lab6_iot;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lab6_iot.adapter.EgresoAdapter;
import com.example.lab6_iot.model.Egreso;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class EgresosFragment extends Fragment {
    private CollectionReference ref;
    private EgresoAdapter adapter;
    private List<Egreso> lista = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup c,
                             @Nullable Bundle b) {
        View v = inf.inflate(R.layout.fragment_egresos, c, false);
        RecyclerView rv = v.findViewById(R.id.rvEgresos);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EgresoAdapter(lista, new EgresoAdapter.Listener() {
            @Override public void onEdit(Egreso egreso) { showDialog(egreso); }
            @Override public void onDelete(Egreso egreso) {
                ref.document(egreso.getId()).delete();
            }
        });
        rv.setAdapter(adapter);

        FloatingActionButton fab = v.findViewById(R.id.fabAddEgreso);
        fab.setOnClickListener(x -> showDialog(null));

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("egresos");

        ref.addSnapshotListener((snap, e) -> {
            if (e != null) return;
            lista.clear();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                Egreso eg = doc.toObject(Egreso.class);
                eg.setId(doc.getId());
                lista.add(eg);
            }
            adapter.notifyDataSetChanged();
        });

        return v;
    }

    private void showDialog(Egreso egreso) {
        View form = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_egreso, null);
        EditText etT = form.findViewById(R.id.etTitulo);
        EditText etM = form.findViewById(R.id.etMonto);
        EditText etD = form.findViewById(R.id.etDescripcion);
        if (egreso != null) {
            etT.setText(egreso.getTitulo());
            etM.setText(String.valueOf(egreso.getMonto()));
            etD.setText(egreso.getDescripcion());
        }
        new AlertDialog.Builder(getContext())
                .setTitle(egreso == null ? "Nuevo egreso" : "Editar egreso")
                .setView(form)
                .setPositiveButton("Guardar", (dlg, x) -> {
                    String t = etT.getText().toString();
                    double m = Double.parseDouble(etM.getText().toString());
                    String d = etD.getText().toString();
                    Egreso eg = new Egreso(t, m, d, Timestamp.now());
                    if (egreso == null) {
                        ref.add(eg);
                    } else {
                        ref.document(egreso.getId()).set(eg);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
