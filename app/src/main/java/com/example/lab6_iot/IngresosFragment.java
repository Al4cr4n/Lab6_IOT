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

import com.example.lab6_iot.adapter.IngresoAdapter;
import com.example.lab6_iot.model.Ingreso;
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

public class IngresosFragment extends Fragment {

    private CollectionReference ref;
    private IngresoAdapter adapter;
    private List<Ingreso> lista = new ArrayList<>();

    private ServicioAlmacenamiento servicioStorage;

    // Selector de imágenes
    private ActivityResultLauncher<String> pickImageLauncher;
    private Uri selectedImageUri;
    private ImageView dialogImgPreview;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Registramos el selector de contenido para imágenes
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
        View v = inflater.inflate(R.layout.fragment_ingresos, container, false);

        // Inicializar servicio de Storage
        servicioStorage = new ServicioAlmacenamiento();
        servicioStorage.conectar();

        // RecyclerView + Adapter
        RecyclerView rv = v.findViewById(R.id.rvIngresos);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new IngresoAdapter(lista, new IngresoAdapter.Listener() {
            @Override public void onEdit(Ingreso ingreso) {
                showDialog(ingreso);
            }
            @Override public void onDelete(Ingreso ingreso) {
                ref.document(ingreso.getId()).delete();
            }
            @Override public void onDownload(Ingreso ingreso) {
                String ruta = ingreso.getRutaImagen();
                if (ruta == null || ruta.isEmpty()) {
                    Toast.makeText(getContext(),
                            "No hay imagen asociada",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                File dir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if (dir != null) {
                    File destino = new File(dir, ingreso.getId() + ".jpg");
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

        // FAB para nuevo ingreso
        FloatingActionButton fab = v.findViewById(R.id.fabAddIngreso);
        fab.setOnClickListener(x -> showDialog(null));

        // Listener Firestore
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

    private void showDialog(@Nullable Ingreso ingreso) {
        selectedImageUri = null;
        dialogImgPreview = null;

        View form = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_ingreso, null);
        EditText etT = form.findViewById(R.id.etTitulo);
        EditText etM = form.findViewById(R.id.etMonto);
        EditText etD = form.findViewById(R.id.etDescripcion);
        MaterialButton btnSelect = form.findViewById(R.id.btnSelectImage);
        ImageView imgPrev    = form.findViewById(R.id.imgPreview);

        dialogImgPreview = imgPrev;

        final String rutaAntigua;
        if (ingreso != null) {
            etT.setText(ingreso.getTitulo());
            etM.setText(String.valueOf(ingreso.getMonto()));
            etD.setText(ingreso.getDescripcion());
            rutaAntigua = ingreso.getRutaImagen();
            if (rutaAntigua != null) {
                imgPrev.setVisibility(View.VISIBLE);
                // Glide.with(this).load(rutaAntigua).into(imgPrev);
            }
        } else {
            rutaAntigua = null;
        }

        btnSelect.setOnClickListener(btn -> pickImageLauncher.launch("image/*"));

        new AlertDialog.Builder(requireContext())
                .setTitle(ingreso == null ? "Nuevo ingreso" : "Editar ingreso")
                .setView(form)
                .setPositiveButton("Guardar", (dlg, x) -> {
                    if (ingreso == null && selectedImageUri == null) {
                        Toast.makeText(getContext(),
                                "Debes seleccionar un comprobante",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String t = etT.getText().toString();
                    double m = Double.parseDouble(etM.getText().toString());
                    String d = etD.getText().toString();
                    saveIngreso(ingreso, t, m, d, rutaAntigua);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void saveIngreso(@Nullable Ingreso ingreso,
                             String titulo,
                             double monto,
                             String descripcion,
                             String rutaAntigua) {
        if (selectedImageUri != null) {
            String rutaRemota = "comprobantes/ingresos/" + System.currentTimeMillis() + ".jpg";
            servicioStorage.guardarArchivo(selectedImageUri, rutaRemota)
                    .addOnSuccessListener(uri -> {
                        persistIngreso(ingreso, titulo, monto, descripcion, uri.toString());
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(),
                                "Error subiendo comprobante",
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            persistIngreso(ingreso, titulo, monto, descripcion, rutaAntigua);
        }
    }

    private void persistIngreso(@Nullable Ingreso ingreso,
                                String titulo,
                                double monto,
                                String descripcion,
                                String rutaImagen) {
        Ingreso ing = new Ingreso(titulo, monto, descripcion, Timestamp.now(), rutaImagen);
        if (ingreso == null) {
            ref.add(ing);
        } else {
            ref.document(ingreso.getId()).set(ing);
        }
    }
}
