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

import com.google.android.material.switchmaterial.SwitchMaterial; // Importante para el nuevo interruptor

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
    private SwitchMaterial switchLed; // Variable para el interruptor
    private MqttClient client;

    // CONFIGURACIÓN MQTT
    private static final String BROKER_URL = "tcp://10.0.2.2:1883";
    private static final String CLIENT_ID = MqttClient.generateClientId();

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

        // 1. Vinculamos TODOS los elementos del nuevo XML Dark
        txtTemp = findViewById(R.id.txtTemperatura);
        txtHum = findViewById(R.id.txtHumedad);
        txtLuz = findViewById(R.id.txtLuz);
        switchLed = findViewById(R.id.switchLed); // <--- ESTO ES LO QUE SEGURAMENTE FALTABA

        // 2. Configurar el Listener del LED (Evitar NullPointer)
        if (switchLed != null) {
            switchLed.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String mensaje = isChecked ? "ON" : "OFF";
                publicarMensajeMqtt("ubicua_db/led/set", mensaje);
            });
        }

        // 3. Conectar MQTT
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
                    Log.e("ubicua", "Conexión perdida");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload = new String(message.getPayload());
                    runOnUiThread(() -> procesarMensaje(topic, payload));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) { }
            });

            client.connect(options);
            client.subscribe("ubicua_db/temperatura");
            client.subscribe("ubicua_db/humedad");
            client.subscribe("ubicua_db/luz");
            // No nos suscribimos al LED para no hacer bucle, solo publicamos

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void publicarMensajeMqtt(String topic, String message) {
        try {
            if (client != null && client.isConnected()) {
                MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                mqttMessage.setQos(1);
                client.publish(topic, mqttMessage);
                Log.i("ubicua", "LED enviado: " + message);
            } else {
                Toast.makeText(this, "Conectando MQTT...", Toast.LENGTH_SHORT).show();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void procesarMensaje(String topic, String mensaje) {
        String valorFinal = mensaje;
        try {
            JSONObject json = new JSONObject(mensaje);
            if (json.has("data")) {
                JSONObject data = json.getJSONObject("data");
                if (topic.contains("temperatura")) valorFinal = String.valueOf(data.optDouble("temperature_celsius"));
                else if (topic.contains("humedad")) valorFinal = String.valueOf(data.optDouble("humidity_percent"));
                else if (topic.contains("luz")) valorFinal = String.valueOf(data.optInt("light_intensity"));
            }
        } catch (Exception e) {
            valorFinal = mensaje;
        }

        if (topic.contains("temperatura")) txtTemp.setText(valorFinal + " ºC");
        else if (topic.contains("humedad")) txtHum.setText(valorFinal + " %");
        else if (topic.contains("luz")) txtLuz.setText(valorFinal + " lux");
    }
}