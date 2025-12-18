package com.example.apppecl3;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

public class StreetMonitoring extends AppCompatActivity {

    // Variables para la interfaz gráfica
    private TextView txtTemp, txtHum, txtLuz;
    private MqttClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_street_monitoring);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Vincular los textos de la pantalla (Asegúrate de tener estos IDs en tu XML)
        txtTemp = findViewById(R.id.txtTemperatura);
        txtHum = findViewById(R.id.txtHumedad);
        txtLuz = findViewById(R.id.txtLuz);

        // 2. Iniciar conexión MQTT
        conectarMqtt();
    }

    private void conectarMqtt() {
        try {
            // Conexión al emulador (10.0.2.2 es el localhost de tu PC)
            client = new MqttClient(
                    "tcp://10.0.2.2:1883",
                    MqttClient.generateClientId(),
                    new MemoryPersistence()
            );

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e("ubicua", "Conexión perdida!");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload = new String(message.getPayload());
                    Log.i("ubicua", "Recibido en " + topic + ": " + payload);

                    // Importante: La UI solo se puede tocar desde el hilo principal
                    runOnUiThread(() -> procesarMensaje(topic, payload));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) { }
            });

            Log.i("ubicua", "Conectando al broker...");
            client.connect(options);
            Log.i("ubicua", "Conectado ✔");

            // 3. Suscribirse a TUS topics específicos
            client.subscribe("ubicua_db/temperatura");
            client.subscribe("ubicua_db/humedad");
            client.subscribe("ubicua_db/luz");

        } catch (MqttException e) {
            Log.e("ubicua", "Error MQTT: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para extraer los datos del JSON y ponerlos en pantalla
    private void procesarMensaje(String topic, String jsonString) {
        try {
            // El formato que envía tu Arduino/SetData es:
            // { "timestamp": "...", "data": { "temperature_celsius": 25.0 } }

            JSONObject json = new JSONObject(jsonString);

            // Verificamos si tiene el campo "data" (tu estructura)
            if (json.has("data")) {
                JSONObject data = json.getJSONObject("data");

                if (topic.equals("ubicua_db/temperatura")) {
                    double val = data.optDouble("temperature_celsius", 0.0);
                    txtTemp.setText(String.format("%.1f ºC", val));
                }
                else if (topic.equals("ubicua_db/humedad")) {
                    double val = data.optDouble("humidity_percent", 0.0);
                    txtHum.setText(String.format("%.1f %%", val));
                }
                else if (topic.equals("ubicua_db/luz")) {
                    // La luz a veces viene como 'light_intensity' o 'light'
                    int val = data.optInt("light_intensity", 0);
                    if(val == 0 && data.has("light")) val = data.getInt("light");

                    txtLuz.setText(String.valueOf(val));
                }
            } else {
                // Fallback por si llega un mensaje plano sin JSON
                if (topic.contains("temperatura")) txtTemp.setText(jsonString);
                else if (topic.contains("humedad")) txtHum.setText(jsonString);
                else if (topic.contains("luz")) txtLuz.setText(jsonString);
            }

        } catch (Exception e) {
            Log.e("ubicua", "Error parseando JSON: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}