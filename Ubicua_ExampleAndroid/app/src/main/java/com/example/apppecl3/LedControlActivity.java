package com.example.apppecl3;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class LedControlActivity extends AppCompatActivity {

    private MqttClient client;
    private static final String BROKER_URL = "tcp://192.168.1.122:1883"; // MISMA IP QUE ARDUINO
    private static final String CLIENT_ID = MqttClient.generateClientId();
    private static final String TOPIC_LED = "ubicua_db/led/set";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_control);

        // Conectar a MQTT al iniciar la pantalla
        conectarMqtt();

        // Configurar botones
        findViewById(R.id.btnOff).setOnClickListener(v -> enviarComando("off"));
        findViewById(R.id.btnAuto).setOnClickListener(v -> enviarComando("auto"));
        findViewById(R.id.btnRed).setOnClickListener(v -> enviarComando("red"));
        findViewById(R.id.btnGreen).setOnClickListener(v -> enviarComando("green"));
        findViewById(R.id.btnBlue).setOnClickListener(v -> enviarComando("blue"));
    }

    private void conectarMqtt() {
        new Thread(() -> {
            try {
                client = new MqttClient(BROKER_URL, CLIENT_ID, new MemoryPersistence());
                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);
                client.connect(options);
                Log.i("ubicua", "Conectado al Broker para Control LED");
            } catch (MqttException e) {
                Log.e("ubicua", "Error conectando MQTT LedControl: " + e.getMessage());
            }
        }).start();
    }

    private void enviarComando(String comando) {
        new Thread(() -> {
            try {
                if (client != null && client.isConnected()) {
                    MqttMessage message = new MqttMessage(comando.getBytes());
                    message.setQos(1);
                    client.publish(TOPIC_LED, message);
                    Log.d("ubicua", "Enviado comando LED: " + comando);

                    // Feedback visual (opcional)
                    runOnUiThread(() ->
                            Toast.makeText(this, "Enviado: " + comando, Toast.LENGTH_SHORT).show()
                    );
                } else {
                    Log.e("ubicua", "Cliente no conectado, intentando reconectar...");
                    conectarMqtt(); // Reintentar conexión simple
                }
            } catch (MqttException e) {
                Log.e("ubicua", "Error publicando comando: " + e.getMessage());
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