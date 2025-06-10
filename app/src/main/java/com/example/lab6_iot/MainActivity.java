package com.example.lab6_iot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "msg-test";

    // 1) Launcher para recibir el resultado de FirebaseUI Auth
    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(
                    new FirebaseAuthUIActivityResultContract(),
                    this::onSignInResult
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializa Firebase
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // No hay usuario: lanza el login
            startFirebaseUISignIn();
        } else {
            // Ya autenticado: validamos email
            validateEmailVerification(currentUser);
        }
    }

    /** Método que lanza FirebaseUI con layout personalizado */
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
                //  Facebook:
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


    /** Callback tras el flujo de FirebaseUI */
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                validateEmailVerification(user);
            }
        } else {
            Log.d(TAG, "Login cancelado o error");
            Toast.makeText(this, "Autenticación cancelada", Toast.LENGTH_SHORT).show();
        }
    }

    /** Refresca al usuario y navega a HomeActivity si está verificado */
    private void validateEmailVerification(FirebaseUser user) {
        user.reload().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error recargando usuario", task.getException());
                return;
            }
            if (user.isEmailVerified()) {
                Log.d(TAG, "Correo verificado");
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            } else {
                FirebaseAuth.getInstance().setLanguageCode("es-419");
                user.sendEmailVerification()
                        .addOnCompleteListener(t2 -> {
                            if (t2.isSuccessful()) {
                                Toast.makeText(
                                        this,
                                        "Se ha enviado un correo de verificación. Revisa tu bandeja.",
                                        Toast.LENGTH_LONG
                                ).show();
                            } else {
                                Toast.makeText(
                                        this,
                                        "Error al enviar correo de verificación.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
            }
        });
    }
}
