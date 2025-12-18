package com.example.apppecl3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

        // Ajuste de barras del sistema (diseño moderno)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // -------------------------------------------------------------
        // CONFIGURACIÓN DE LOS BOTONES DEL MENÚ
        // -------------------------------------------------------------

        // 1. Botón "Ver Histórico de Datos" -> Abre StreetSelection
        Button btnHistory = findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StreetSelection.class);
                startActivity(intent);
            }
        });

        // 2. Botón "Monitor Tiempo Real" -> Abre StreetMonitoring
        Button btnRealTime = findViewById(R.id.btnRealTime);
        btnRealTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StreetMonitoring.class);
                startActivity(intent);
            }
        });
    }
}