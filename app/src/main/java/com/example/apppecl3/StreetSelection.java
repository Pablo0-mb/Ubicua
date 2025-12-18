package com.example.apppecl3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StreetSelection extends AppCompatActivity {
    List<Measurement> listaMediciones;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_street_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        listView = findViewById(R.id.listView);
        obtenerDatosDelServidor();
    }
    private void obtenerDatosDelServidor() {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<Measurement>> call = apiService.getItems();
        call.enqueue(new Callback<List<Measurement>>() {
            @Override
            public void onResponse(Call<List<Measurement>> call, Response<List<Measurement>> response) {
                if (response.isSuccessful()) {
                    listaMediciones = response.body();
                    mostrarLista(listaMediciones);
                } else {
                    Log.e("ubicua","Error del servidor");
                }
            }

            @Override
            public void onFailure(Call<List<Measurement>> call, Throwable t) {
                Log.e("ubicua","Error: " + t.getMessage());
            }
        });
    }

    private void mostrarLista(List<Measurement> lista) {
        // 1. Guardamos la lista en la variable global
        listaMediciones = lista;

        // 2. Log para depuración
        StringBuilder builder = new StringBuilder();
        for (Measurement item : lista) {
            builder.append(item.getTimestamp())
                    .append(" -> T: ")
                    .append(item.getTemperature())
                    .append("\n");
        }
        Log.i("ubicua", "Datos recibidos: \n" + builder.toString());

        // 3. Configurar el Adaptador para el ListView
        // Usamos 'android.R.layout.simple_list_item_1' que es un diseño estándar de texto
        ArrayAdapter<Measurement> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                lista
        );

        // 4. Asignar al ListView
        listView.setAdapter(adapter);

    }
}