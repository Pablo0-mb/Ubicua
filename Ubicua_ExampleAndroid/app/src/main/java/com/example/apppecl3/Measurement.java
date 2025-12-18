package com.example.apppecl3;

public class Measurement {
    private float temperature;
    private float humidity;
    private int light;
    private String timestamp;

    // Getters para que Android pueda leer los datos
    public float getTemperature() { return temperature; }
    public float getHumidity() { return humidity; }
    public int getLight() { return light; }
    public String getTimestamp() { return timestamp; }

    // Metodo para mostrarlo en una lista
    @Override
    public String toString() {
        return timestamp + " | T:" + temperature + " | H:" + humidity;
    }
}
