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
import com.example.lab6_iot.adapter.IngresoAdapter;
import com.example.lab6_iot.model.Ingreso;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class IngresosFragment extends Fragment {
    private CollectionReference ref;
    private IngresoAdapter adapter;
    private List<Ingreso> lista = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup c,
                             @Nullable Bundle b) {
        View v = inf.inflate(R.layout.fragment_ingresos, c, false);
        RecyclerView rv = v.findViewById(R.id.rvIngresos);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new IngresoAdapter(lista, new IngresoAdapter.Listener() {
            @Override public void onEdit(Ingreso ingreso) { showDialog(ingreso); }
            @Override public void onDelete(Ingreso ingreso) {
                ref.document(ingreso.getId()).delete();
            }
        });
        rv.setAdapter(adapter);

        FloatingActionButton fab = v.findViewById(R.id.fabAddIngreso);
        fab.setOnClickListener(x -> showDialog(null));

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("ingresos");

        ref.addSnapshotListener((snap, e) -> {
            if (e != null) return;
            lista.clear();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                Ingreso ing = doc.toObject(Ingreso.class);
                ing.setId(doc.getId());
                lista.add(ing);
            }
            adapter.notifyDataSetChanged();
        });

        return v;
    }

    private void showDialog(Ingreso ingreso) {
        View form = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_ingreso, null);
        EditText etT = form.findViewById(R.id.etTitulo);
        EditText etM = form.findViewById(R.id.etMonto);
        EditText etD = form.findViewById(R.id.etDescripcion);
        if (ingreso != null) {
            etT.setText(ingreso.getTitulo());
            etM.setText(String.valueOf(ingreso.getMonto()));
            etD.setText(ingreso.getDescripcion());
        }
        new AlertDialog.Builder(getContext())
                .setTitle(ingreso == null ? "Nuevo ingreso" : "Editar ingreso")
                .setView(form)
                .setPositiveButton("Guardar", (dlg, x) -> {
                    String t = etT.getText().toString();
                    double m = Double.parseDouble(etM.getText().toString());
                    String d = etD.getText().toString();
                    Ingreso ing = new Ingreso(t, m, d, Timestamp.now());
                    if (ingreso == null) {
                        ref.add(ing);
                    } else {
                        ref.document(ingreso.getId()).set(ing);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
