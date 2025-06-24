package com.example.lab6_iot;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_iot.storage.ServicioAlmacenamiento;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ServicioAlmacenamiento servicioAlmacenamiento;

    // 1) Launcher para FirebaseUI Auth
    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(
                    new FirebaseAuthUIActivityResultContract(),
                    this::onSignInResult
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializa Firebase Core
        FirebaseApp.initializeApp(this);

        // Inicializa y conecta el servicio de Storage
        servicioAlmacenamiento = new ServicioAlmacenamiento();
        servicioAlmacenamiento.conectar();

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            startFirebaseUISignIn();
        } else {
            validateEmail(usuario);
        }
    }

    private void startFirebaseUISignIn() {
        AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout
                .Builder(R.layout.mipagina_login)
                .setEmailButtonId(R.id.btnEmail)
                .setGoogleButtonId(R.id.btnGoogle)
                .setFacebookButtonId(R.id.btnFacebook)
                .build();

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder()
                        .setPermissions(Arrays.asList("email", "public_profile"))
                        .build()
        );

        Intent intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setAuthMethodPickerLayout(customLayout)
                .setIsSmartLockEnabled(false)
                .build();

        signInLauncher.launch(intent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) validateEmail(user);
        } else {
            Log.d(TAG, "Login cancelado o error");
            Toast.makeText(this, "Autenticación cancelada", Toast.LENGTH_SHORT).show();
        }
    }

    private void validateEmail(FirebaseUser user) {
        user.reload().addOnCompleteListener(t -> {
            if (user.isEmailVerified()) {
                // Ya verificado: vamos al Home
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            } else {
                user.sendEmailVerification()
                        .addOnCompleteListener(v -> {
                            Toast.makeText(
                                    this,
                                    "Correo de verificación enviado.",
                                    Toast.LENGTH_LONG
                            ).show();
                        });
            }
        });
    }

    // === EJEMPLO DE USO DE STORAGE ===

    private void ejemploSubida() {
        // 1) Crea un Uri local (por ejemplo, un archivo JSON en cache)
        File f = new File(getCacheDir(), "respaldos.json");
        Uri uriLocal = Uri.fromFile(f);

        // 2) Define la ruta remota
        String rutaRemota = "respaldos/" + System.currentTimeMillis() + ".json";

        // 3) Lanza la subida
        servicioAlmacenamiento
                .guardarArchivo(uriLocal, rutaRemota)
                .addOnSuccessListener(downloadUrl -> {
                    Log.d(TAG, "Archivo subido a: " + downloadUrl);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error subiendo", e);
                });
    }

    private void ejemploDescarga() {
        String rutaRemota = "respaldos/1654321234.json";
        File destino = new File(getFilesDir(), "descarga.json");

        servicioAlmacenamiento
                .obtenerArchivo(rutaRemota, destino)
                .addOnSuccessListener(snapshot -> {
                    Log.d(TAG, "Descargado en: " + destino.getAbsolutePath());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error descargando", e);
                });
    }
}
