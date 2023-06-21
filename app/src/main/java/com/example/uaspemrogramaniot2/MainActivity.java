package com.example.uaspemrogramaniot2;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MqttCallback {

    MqttClient client = null;

    LineChart SuhuChart;
    LineChart Kecepatanchart;
    private TextView DummyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectToMQTTBroker();

        Log.d("MQTT", "subscribed");

        SuhuChart = findViewById(R.id.SuhuChart);
        Kecepatanchart = findViewById(R.id.KecepatanChart);
        DummyTextView = findViewById(R.id.DummyTextView);

        LineData data = new LineData(getLabel(10));
        SuhuChart.setData(data);
        SuhuChart.animateXY(4000, 4000);
        SuhuChart.invalidate();

        LineData Tempdata = new LineData(getLabel(10));
        Kecepatanchart.setData(Tempdata);
        Kecepatanchart.animateXY(4000, 4000);
        Kecepatanchart.invalidate();

    }

    private void connectToMQTTBroker() {
        try {
            client = new MqttClient("tcp://192.168.41.73:1883", "randomrandom", new MemoryPersistence());
            client.setCallback(this);
            client.connect();

            client.subscribe("4173/dht");
            client.subscribe("4173/potensio");
            client.subscribe("4173/dummy");

            Log.d("MQTT", "Connected to MQTT broker");
        } catch (MqttException e) {
            e.printStackTrace();
            Log.d("MQTT", "Connection to MQTT broker failed");
        }
    }

    private ArrayList getLabel(int n) {
        ArrayList xLabel = new ArrayList();
        for(int i = 0; i < n; i++) {
            xLabel.add("n: " + Integer.toString(i + 1));
        }
        return xLabel;
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d("MQTT", "Connection lost: " + cause.getMessage());
        // Handle reconnection logic here
        connectToMQTTBroker();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d("MQTT", "topic: " + topic);
        Log.d("MQTT", "message: " + message);
        float value = Float.parseFloat(String.valueOf(message));

        if (topic.equals("4173/dht")) {
            updateDHTLineChart(value);
        } else if (topic.equals("4173/potensio")) {
            updateKecepatanLineChart(value);
        } else if (topic.equals("4173/dummy")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String dummyValue = message.toString();
                    DummyTextView.setText(dummyValue);
                }
            });
        }
    }

    private void updateKecepatanLineChart(float value) {
        LineData lineData = Kecepatanchart.getData();

        if (lineData != null) {
            ILineDataSet dataSet = lineData.getDataSetByIndex(0);

            if (dataSet == null) {
                dataSet = createDataSet();
                lineData.addDataSet(dataSet);
            }

            int entryCount = dataSet.getEntryCount();

            if (entryCount >= 10) {
                for (int i = 0; i < 5; i++) {
                    dataSet.removeEntry(i);
                }

                Entry newEntry = new Entry(value, 0);
                dataSet.addEntry(newEntry);
            } else {
                Entry newEntry = new Entry(value, entryCount);
                dataSet.addEntry(newEntry);
            }

            lineData.notifyDataChanged();
            Kecepatanchart.notifyDataSetChanged();
            Kecepatanchart.setVisibleXRangeMaximum(10);
            Kecepatanchart.moveViewToX(-1);
        }
    }
    private void updateDHTLineChart(float value) {
        LineData lineData = SuhuChart.getData();

        if (lineData != null) {
            ILineDataSet dataSet = lineData.getDataSetByIndex(0);

            if (dataSet == null) {
                dataSet = createDataSet2();
                lineData.addDataSet(dataSet);
            }

            int entryCount = dataSet.getEntryCount();

            if (entryCount >= 10) {
                for (int i = 0; i < 10; i++) {
                    dataSet.removeEntry(i);
                }

                Entry newEntry = new Entry(value, 0);
                dataSet.addEntry(newEntry);
            } else {
                Entry newEntry = new Entry(value, entryCount);
                dataSet.addEntry(newEntry);
            }

            lineData.notifyDataChanged();
            SuhuChart.notifyDataSetChanged();
            SuhuChart.setVisibleXRangeMaximum(10);
            SuhuChart.moveViewToX(-1);
        }
    }

    private LineDataSet createDataSet() {
        LineDataSet dataSet = new LineDataSet(null, "Kecepatan");
        dataSet.setColor(Color.MAGENTA);
        dataSet.setCircleColor(Color.MAGENTA);
        dataSet.setDrawValues(false);
        return dataSet;
    }
    private LineDataSet createDataSet2() {
        LineDataSet dataSet = new LineDataSet(null, "Suhu");
        dataSet.setColor(Color.MAGENTA);
        dataSet.setCircleColor(Color.MAGENTA);
        dataSet.setDrawValues(false);
        return dataSet;
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}