package com.example.lab6_iot.storage;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class ServicioAlmacenamiento {

    private FirebaseStorage storage;
    private StorageReference storageRef;

    /** ① Inicializa Firebase Storage */
    public void conectar() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    /**
     * ② Sube un archivo al bucket.
     * @param archivoLocal  URI del archivo en el dispositivo (p.ej. Uri.fromFile(...))
     * @param rutaRemota    Ruta dentro del bucket, p.ej. "backups/registro1.json"
     * @return Task que al completarse entrega la URL de descarga pública (getDownloadUrl)
     */
    public Task<Uri> guardarArchivo(Uri archivoLocal, String rutaRemota) {
        // Referencia al archivo remoto
        StorageReference ref = storageRef.child(rutaRemota);
        // Primera task: subir
        UploadTask uploadTask = ref.putFile(archivoLocal);
        // Cuando termine la subida, pedimos la URL de descarga
        return uploadTask
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                });
    }

    /**
     * ③ Descarga un archivo remoto a un Fichero local.
     * @param rutaRemota    Ruta dentro del bucket, p.ej. "backups/registro1.json"
     * @param destinoLocal  File donde guardar los bytes
     * @return Task de FileDownloadTask.TaskSnapshot para manejar progreso/éxito/fracaso
     */
    public Task<FileDownloadTask.TaskSnapshot> obtenerArchivo(String rutaRemota, File destinoLocal) {
        StorageReference ref = storageRef.child(rutaRemota);
        return ref.getFile(destinoLocal);
    }
}