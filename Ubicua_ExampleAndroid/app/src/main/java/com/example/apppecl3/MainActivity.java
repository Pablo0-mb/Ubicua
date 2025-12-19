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

        // Ajuste de márgenes para las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- CORRECCIÓN AQUÍ ---
        // Ya no buscamos "Button", sino una "View" (que sirve para las tarjetas)
        // Y usamos los IDs nuevos del diseño Dark Neon: btnHistoric y btnRealTime

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
    }
}