package com.example.apppecl3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        View btnLedControl = findViewById(R.id.btnLedControl);

        // Ajuste de márgenes para las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View btnHistory = findViewById(R.id.btnHistoric); // <--- El ID nuevo es btnHistoric
        View btnRealTime = findViewById(R.id.btnRealTime);

        // Configurar el click para ir al Histórico
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StreetSelection.class);
            startActivity(intent);
        });

        // Configurar el click para ir al Tiempo Real
        btnRealTime.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StreetMonitoring.class);
            startActivity(intent);
        });

        if (btnLedControl != null) {
            btnLedControl.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, LedControlActivity.class);
                startActivity(intent);
            });
        }
    }
}