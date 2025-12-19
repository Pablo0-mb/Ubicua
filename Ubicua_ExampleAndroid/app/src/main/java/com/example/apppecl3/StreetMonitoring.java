package com.example.apppecl3;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.google.android.material.switchmaterial.SwitchMaterial;

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

    private TextView txtTemp, txtHum, txtLuz;
    private SwitchMaterial switchLed;
    private MqttClient client;

    // CONFIGURACIÓN
    // CAMBIO 1: Usar la IP real de la red local (donde está el broker), igual que en el Arduino
    private static final String BROKER_URL = "tcp://192.168.1.122:1883";
    private static final String CLIENT_ID = MqttClient.generateClientId();

    // TOPICS
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

    // CAMBIO 2: Ejecutar la conexión en un hilo separado (Thread)
    private void conectarMqtt() {
        new Thread(() -> {
            try {
                // Configuración inicial del cliente
                client = new MqttClient(BROKER_URL, CLIENT_ID, new MemoryPersistence());
                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);

                // Definimos el Callback (qué hacer cuando llegan mensajes)
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        Log.e("ubicua", "¡Conexión MQTT perdida!");
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        String payload = new String(message.getPayload());
                        Log.d("ubicua", "Recibido en [" + topic + "]: " + payload);

                        // IMPORTANTE: Volver al hilo principal para tocar la UI (TextViews)
                        runOnUiThread(() -> procesarMensaje(topic, payload));
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) { }
                });

                Log.i("ubicua", "Conectando a " + BROKER_URL + "...");

                // Esta línea es la que bloqueaba la app si no estaba en un hilo aparte
                client.connect(options);

                Log.i("ubicua", "Conectado. Suscribiendo...");

                // Nos suscribimos a los topics
                client.subscribe(TOPIC_TEMP);
                client.subscribe(TOPIC_HUM);
                client.subscribe(TOPIC_LUZ);

            } catch (MqttException e) {
                Log.e("ubicua", "Error MQTT: " + e.getMessage());
                e.printStackTrace();
            }
        }).start(); // .start() inicia el hilo
    }

    private void procesarMensaje(String topic, String mensaje) {
        try {
            // 1. Convertimos el String a JSON
            JSONObject jsonPrincipal = new JSONObject(mensaje);

            // 2. Entramos al objeto "data"
            if (!jsonPrincipal.has("data")) {
                Log.w("ubicua", "El JSON no tiene campo 'data'. Saltando...");
                return;
            }
            JSONObject data = jsonPrincipal.getJSONObject("data");

            // 3. Extraemos el valor según el topic
            if (topic.equals(TOPIC_TEMP)) {
                double temp = data.getDouble("temperature_celsius");
                txtTemp.setText(String.format("%.1f ºC", temp));
            }
            else if (topic.equals(TOPIC_HUM)) {
                double hum = data.getDouble("humidity_percent");
                txtHum.setText(String.format("%.0f %%", hum));
            }
            else if (topic.equals(TOPIC_LUZ)) {
                int luz = data.getInt("light_intensity");
                txtLuz.setText(luz + " lux");
            }

        } catch (Exception e) {
            Log.e("ubicua", "Error parseando JSON: " + e.getMessage());
        }
    }

    private void publicarMensajeMqtt(String topic, String message) {
        // Publicar también es una operación de red, idealmente debería ir en un hilo,
        // pero como es muy rápida y pequeña, a veces Android la permite.
        // Si te da problemas, envuélvela también en un new Thread(() -> { ... }).start();
        new Thread(() -> {
            try {
                if (client != null && client.isConnected()) {
                    MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                    mqttMessage.setQos(1);
                    client.publish(topic, mqttMessage);
                }
            } catch (MqttException e) {
                Log.e("ubicua", "Error publicando: " + e.getMessage());
            }
        }).start();
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