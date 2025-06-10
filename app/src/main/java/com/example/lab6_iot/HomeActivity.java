package com.example.lab6_iot;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ① Carga siempre el fragment de Ingresos la primera vez
        if (savedInstanceState == null) {
            loadFragment(new IngresosFragment());
        }

        // ② Ahora configura la BottomNavigation
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setOnItemSelectedListener(item -> {
            Fragment f = null;
            int id = item.getItemId();
            if (id == R.id.nav_ingresos) {
                f = new IngresosFragment();
            } else if (id == R.id.nav_egresos) {
                f = new EgresosFragment();
            } else if (id == R.id.nav_resumen) {
                f = new ResumenFragment();
            } else if (id == R.id.nav_logout) {
                // cerrar sesión...
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(t -> {
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        });
                return true;
            }
            if (f != null) {
                loadFragment(f);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
