package com.example.apppecl3;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog; // Para la alerta de borrar
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Para el botón flotante

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StreetSelection extends AppCompatActivity {

    private ListView listView;
    private LineChart chart;

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

        // 1. Vincular elementos con los NUEVOS IDs
        listView = findViewById(R.id.listView);
        chart = findViewById(R.id.chartSensores); // <--- OJO: Antes era chartTemperatura
        FloatingActionButton fabDelete = findViewById(R.id.fabDelete); // <--- Botón nuevo

        // 2. Configurar botón de borrar
        if (fabDelete != null) {
            fabDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("¿Borrar Histórico?")
                        .setMessage("Se eliminarán todos los datos. ¿Estás seguro?")
                        .setPositiveButton("Borrar", (dialog, which) -> borrarDatosDelServidor())
                        .setNegativeButton("Cancelar", null)
                        .show();
            });
        }

        // 3. Cargar datos
        obtenerDatosDelServidor();
    }

    private void borrarDatosDelServidor() {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<Void> call = apiService.deleteItems();

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(StreetSelection.this, "Datos borrados", Toast.LENGTH_SHORT).show();
                    obtenerDatosDelServidor(); // Recargar para ver que está vacío
                } else {
                    Toast.makeText(StreetSelection.this, "Error: ¿Tienes el Servlet DeleteData?", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(StreetSelection.this, "Error de red al borrar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void obtenerDatosDelServidor() {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<Measurement>> call = apiService.getItems();

        call.enqueue(new Callback<List<Measurement>>() {
            @Override
            public void onResponse(Call<List<Measurement>> call, Response<List<Measurement>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mostrarListaYGrafico(response.body());
                } else {
                    Toast.makeText(StreetSelection.this, "Sin datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Measurement>> call, Throwable t) {
                Toast.makeText(StreetSelection.this, "Error al conectar con servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarListaYGrafico(List<Measurement> lista) {
        // Lista
        ArrayAdapter<Measurement> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_history_row, // <--- Aquí usamos tu nuevo archivo XML
                lista
        );        listView.setAdapter(adapter);

        // Gráfico
        if (lista.isEmpty()) {
            chart.clear();
            return;
        }

        ArrayList<Entry> pTemp = new ArrayList<>();
        ArrayList<Entry> pHum = new ArrayList<>();
        ArrayList<Entry> pLuz = new ArrayList<>();

        for (int i = 0; i < lista.size(); i++) {
            Measurement m = lista.get(i);
            pTemp.add(new Entry(i, m.getTemperature()));
            pHum.add(new Entry(i, m.getHumidity()));
            pLuz.add(new Entry(i, m.getLight()));
        }

        // Configuración visual DARK
        LineDataSet setTemp = crearDataSet(pTemp, "Temp", Color.RED);
        LineDataSet setHum = crearDataSet(pHum, "Hum", Color.CYAN); // Cian se ve mejor en negro
        LineDataSet setLuz = crearDataSet(pLuz, "Luz", Color.YELLOW);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(setTemp);
        dataSets.add(setHum);
        dataSets.add(setLuz);

        LineData data = new LineData(dataSets);
        chart.setData(data);

        // Estilos del Gráfico Oscuro
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setGridBackgroundColor(Color.TRANSPARENT);
        chart.getXAxis().setTextColor(Color.WHITE);
        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setTextColor(Color.WHITE);

        Description d = new Description();
        d.setText("");
        chart.setDescription(d);
        chart.animateX(1000);
        chart.invalidate();
    }

    private LineDataSet crearDataSet(ArrayList<Entry> puntos, String nombre, int color) {
        LineDataSet set = new LineDataSet(puntos, nombre);
        set.setColor(color);
        set.setCircleColor(color);
        set.setLineWidth(2f);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Líneas curvas suaves (queda más chulo)
        return set;
    }
}