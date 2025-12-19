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

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

public class StreetMonitoring extends AppCompatActivity {

    private TextView txtTemp, txtHum, txtLuz;
    private SwitchMaterial switchLed;
    private MqttClient client;

    // CONFIGURACIÓN
    private static final String BROKER_URL = "tcp://10.0.2.2:1883";
    private static final String CLIENT_ID = MqttClient.generateClientId();

    // TOPICS (Asegúrate que coinciden con los de tu Arduino)
    private static final String TOPIC_TEMP = "ubicua_db/temperatura";
    private static final String TOPIC_HUM = "ubicua_db/humedad";
    private static final String TOPIC_LUZ = "ubicua_db/luz";
    private static final String TOPIC_LED = "ubicua_db/led/set";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_street_monitoring);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Vincular vistas
        txtTemp = findViewById(R.id.txtTemperatura);
        txtHum = findViewById(R.id.txtHumedad);
        txtLuz = findViewById(R.id.txtLuz);
        switchLed = findViewById(R.id.switchLed);

        // Listener del LED
        if (switchLed != null) {
            switchLed.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String mensaje = isChecked ? "ON" : "OFF";
                publicarMensajeMqtt(TOPIC_LED, mensaje);
            });
        }

        // Iniciar conexión
        conectarMqtt();
    }

    private void conectarMqtt() {
        try {
            client = new MqttClient(BROKER_URL, CLIENT_ID, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e("ubicua", "¡Conexión MQTT perdida!");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload = new String(message.getPayload());
                    Log.d("ubicua", "Recibido en [" + topic + "]: " + payload);

                    // Actualizamos la UI en el hilo principal
                    runOnUiThread(() -> procesarMensaje(topic, payload));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) { }
            });

            Log.i("ubicua", "Conectando a " + BROKER_URL + "...");
            client.connect(options);
            Log.i("ubicua", "Conectado. Suscribiendo...");

            client.subscribe(TOPIC_TEMP);
            client.subscribe(TOPIC_HUM);
            client.subscribe(TOPIC_LUZ);

        } catch (MqttException e) {
            Log.e("ubicua", "Error MQTT: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- AQUÍ ESTABA EL PROBLEMA, AHORA ESTÁ ADAPTADO A TU JSON ---
    private void procesarMensaje(String topic, String mensaje) {
        try {
            // 1. Convertimos el String a JSON
            JSONObject jsonPrincipal = new JSONObject(mensaje);

            // 2. Entramos al objeto "data" (IMPORTANTÍSIMO: Tu JSON lo tiene anidado)
            if (!jsonPrincipal.has("data")) {
                Log.w("ubicua", "El JSON no tiene campo 'data'. Saltando...");
                return;
            }
            JSONObject data = jsonPrincipal.getJSONObject("data");

            // 3. Extraemos el valor según el topic
            if (topic.equals(TOPIC_TEMP)) {
                // Buscamos "temperature_celsius"
                double temp = data.getDouble("temperature_celsius");
                txtTemp.setText(String.format("%.1f ºC", temp)); // Formato con 1 decimal
            }
            else if (topic.equals(TOPIC_HUM)) {
                // Buscamos "humidity_percent"
                double hum = data.getDouble("humidity_percent");
                txtHum.setText(String.format("%.0f %%", hum)); // Sin decimales
            }
            else if (topic.equals(TOPIC_LUZ)) {
                // Buscamos "light_intensity"
                int luz = data.getInt("light_intensity");
                txtLuz.setText(luz + " lux");
            }

        } catch (Exception e) {
            Log.e("ubicua", "Error parseando JSON: " + e.getMessage());
            // Si falla, no crashea, solo deja el valor anterior o "--"
        }
    }

    private void publicarMensajeMqtt(String topic, String message) {
        try {
            if (client != null && client.isConnected()) {
                MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                mqttMessage.setQos(1);
                client.publish(topic, mqttMessage);
            }
        } catch (MqttException e) {
            Log.e("ubicua", "Error publicando: " + e.getMessage());
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