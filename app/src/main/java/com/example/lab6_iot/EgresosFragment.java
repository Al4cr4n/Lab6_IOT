package com.example.lab6_iot;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6_iot.adapter.EgresoAdapter;
import com.example.lab6_iot.model.Egreso;
import com.example.lab6_iot.storage.ServicioAlmacenamiento;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EgresosFragment extends Fragment {

    private CollectionReference ref;
    private EgresoAdapter adapter;
    private List<Egreso> lista = new ArrayList<>();

    private ServicioAlmacenamiento servicioStorage;

    private ActivityResultLauncher<String> pickImageLauncher;
    private Uri selectedImageUri;
    private ImageView dialogImgPreview;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && dialogImgPreview != null) {
                        selectedImageUri = uri;
                        dialogImgPreview.setImageURI(uri);
                        dialogImgPreview.setVisibility(View.VISIBLE);
                    }
                }
        );
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_egresos, container, false);

        servicioStorage = new ServicioAlmacenamiento();
        servicioStorage.conectar();

        RecyclerView rv = v.findViewById(R.id.rvEgresos);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EgresoAdapter(lista, new EgresoAdapter.Listener() {
            @Override public void onEdit(Egreso egreso) {
                showDialog(egreso);
            }
            @Override public void onDelete(Egreso egreso) {
                ref.document(egreso.getId()).delete();
            }
            @Override public void onDownload(Egreso egreso) {
                String ruta = egreso.getRutaImagen();
                if (ruta == null || ruta.isEmpty()) {
                    Toast.makeText(getContext(),
                            "No hay imagen asociada",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                File dir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if (dir != null) {
                    File destino = new File(dir, egreso.getId() + ".jpg");
                    servicioStorage.obtenerArchivo(ruta, destino)
                            .addOnSuccessListener(snap -> {
                                Toast.makeText(getContext(),
                                        "Imagen guardada en:\n" + destino.getAbsolutePath(),
                                        Toast.LENGTH_LONG).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(),
                                        "Error descargando imagen",
                                        Toast.LENGTH_SHORT).show();
                            });
                }
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

    private void showDialog(@Nullable Egreso egreso) {
        selectedImageUri = null;
        dialogImgPreview = null;

        View form = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_egreso, null);
        EditText etT = form.findViewById(R.id.etTitulo);
        EditText etM = form.findViewById(R.id.etMonto);
        EditText etD = form.findViewById(R.id.etDescripcion);
        MaterialButton btnSelect = form.findViewById(R.id.btnSelectImage);
        ImageView imgPrev    = form.findViewById(R.id.imgPreview);

        dialogImgPreview = imgPrev;

        final String rutaAntigua;
        if (egreso != null) {
            etT.setText(egreso.getTitulo());
            etM.setText(String.valueOf(egreso.getMonto()));
            etD.setText(egreso.getDescripcion());
            rutaAntigua = egreso.getRutaImagen();
            if (rutaAntigua != null) {
                imgPrev.setVisibility(View.VISIBLE);
                // Glide.with(this).load(rutaAntigua).into(imgPrev);
            }
        } else {
            rutaAntigua = null;
        }

        btnSelect.setOnClickListener(btn -> pickImageLauncher.launch("image/*"));

        new AlertDialog.Builder(requireContext())
                .setTitle(egreso == null ? "Nuevo egreso" : "Editar egreso")
                .setView(form)
                .setPositiveButton("Guardar", (dlg, x) -> {
                    if (egreso == null && selectedImageUri == null) {
                        Toast.makeText(getContext(),
                                "Debes seleccionar un comprobante",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String t = etT.getText().toString();
                    double m = Double.parseDouble(etM.getText().toString());
                    String d = etD.getText().toString();
                    saveEgreso(egreso, t, m, d, rutaAntigua);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void saveEgreso(@Nullable Egreso egreso,
                            String titulo,
                            double monto,
                            String descripcion,
                            String rutaAntigua) {
        if (selectedImageUri != null) {
            String rutaRemota = "comprobantes/egresos/" + System.currentTimeMillis() + ".jpg";
            servicioStorage.guardarArchivo(selectedImageUri, rutaRemota)
                    .addOnSuccessListener(uri -> {
                        persistEgreso(egreso, titulo, monto, descripcion, uri.toString());
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(),
                                "Error subiendo comprobante",
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            persistEgreso(egreso, titulo, monto, descripcion, rutaAntigua);
        }
    }

    private void persistEgreso(@Nullable Egreso egreso,
                               String titulo,
                               double monto,
                               String descripcion,
                               String rutaImagen) {
        Egreso e = new Egreso(titulo, monto, descripcion, Timestamp.now(), rutaImagen);
        if (egreso == null) {
            ref.add(e);
        } else {
            ref.document(egreso.getId()).set(e);
        }
    }
}
