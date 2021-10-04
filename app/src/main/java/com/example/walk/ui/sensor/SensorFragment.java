package com.example.walk.ui.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.walk.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class SensorFragment extends Fragment implements SensorEventListener {
    private static final String TAG = "AccelSensorFragment";

    private TextView textview_accel_live_data_x, textview_accel_live_data_y, textview_accel_live_data_z,
            textview_accel_live_data_accuracy, textview_accel_live_data_delay;

    private TextView textview_accel_details_name, textview_accel_details_manufacturer,
            textview_accel_details_version, textview_accel_details_energy,
            textview_accel_details_resolution, textview_accel_details_max_range;

    private SensorManager sensorManager;
    private Sensor accelerationSensor;
    private boolean accelerationSensorRegistered;
    private int sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;

    private GraphView accelerationGraph;
    private LineGraphSeries<DataPoint> graphSeriesX;
    private LineGraphSeries<DataPoint> graphSeriesY;
    private LineGraphSeries<DataPoint> graphSeriesZ;


    private SensorViewModel mViewModel;

    public static SensorFragment newInstance() {
        return new SensorFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sensor_fragment, container, false);

        textview_accel_live_data_x = view.findViewById(R.id.textview_accel_live_data_x);
        textview_accel_live_data_y = view.findViewById(R.id.textview_accel_live_data_y);
        textview_accel_live_data_z = view.findViewById(R.id.textview_accel_live_data_z);
        textview_accel_live_data_accuracy = view.findViewById(R.id.textview_accel_live_data_accuracy);

        textview_accel_live_data_delay = view.findViewById(R.id.textview_accel_live_data_delay);
        textview_accel_live_data_delay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeDelay();
            }
        });

        accelerationGraph = (GraphView) view.findViewById(R.id.acceleration_graph);
        graphSeriesX = new LineGraphSeries<>(new DataPoint[]{});
        graphSeriesX.setTitle("x");
        graphSeriesX.setColor(getResources().getColor(R.color.green));
        graphSeriesY = new LineGraphSeries<>(new DataPoint[]{});
        graphSeriesY.setTitle("y");
        graphSeriesY.setColor(getResources().getColor(R.color.blue));
        graphSeriesZ = new LineGraphSeries<>(new DataPoint[]{});
        graphSeriesZ.setTitle("z");
        graphSeriesZ.setColor(getResources().getColor(R.color.red));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(SensorViewModel.class);


        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Override
    public void onStart() {
        registerListener();
        initGraphView();
        super.onStart();
    }

    @Override
    public void onStop() {
        unregisterListener();
        resetGraphView();
        super.onStop();
    }
    private void registerListener(){
        sensorManager.registerListener(this, accelerationSensor, sensorDelay);
        this.updateDelayText();
        accelerationSensorRegistered = true;
    }

    private void unregisterListener(){
        sensorManager.unregisterListener(this);
        accelerationSensorRegistered = false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            this.updateAccuracy(accuracy);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            this.updateAccelerationData(event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    private void updateAccelerationData(long timestamp, float x, float y, float z){


        String x_value = x + " " + getResources().getString(R.string.accel_unit);
        String y_value = y + " " + getResources().getString(R.string.accel_unit);
        String z_value = z + " " + getResources().getString(R.string.accel_unit);

        textview_accel_live_data_x.setText(x_value);
        textview_accel_live_data_y.setText(y_value);
        textview_accel_live_data_z.setText(z_value);

        graphSeriesX.appendData(new DataPoint(timestamp, x), true, Integer.MAX_VALUE);
        graphSeriesY.appendData(new DataPoint(timestamp, y), true, Integer.MAX_VALUE);
        graphSeriesZ.appendData(new DataPoint(timestamp, z), true, Integer.MAX_VALUE);


    }

    private void changeDelay(){
        switch (sensorDelay){
            case 0:
                sensorDelay = SensorManager.SENSOR_DELAY_GAME;
                break;
            case 1:
                sensorDelay = SensorManager.SENSOR_DELAY_UI;
                break;
            case 2:
                sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                break;
            case 3:
                sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
                break;
            default:
                sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                break;
        }
        unregisterListener();
        registerListener();
    }

    private void updateDelayText(){
        String delayMsg = "";
        switch (sensorDelay){
            case 0:
                delayMsg += "FASTEST (0 ms)";
                break;
            case 1:
                delayMsg += "GAME (20 ms)";
                break;
            case 2:
                delayMsg += "UI (67 ms)";
                break;
            case 3:
                delayMsg += "NORMAL (200 ms)";
                break;
            default:
                delayMsg += "CUSTOM (" + sensorDelay + ")";
                break;
        }
        textview_accel_live_data_delay.setText(delayMsg);
    }

    private void updateAccuracy(int accuracy){
        Log.d(TAG, "Accelerometer new accuracy : " + accuracy);
        textview_accel_live_data_accuracy.setText(String.valueOf(accuracy));
    }

    private void initGraphView(){
        accelerationGraph.getViewport().setScrollable(false);
        accelerationGraph.getViewport().setXAxisBoundsManual(true);
        accelerationGraph.getViewport().setMinX(0);
        accelerationGraph.getViewport().setMaxX(Integer.MAX_VALUE);
        accelerationGraph.getLegendRenderer().setVisible(true);
        accelerationGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        accelerationGraph.getLegendRenderer().setTextColor(getResources().getColor(R.color.colorWhite));
        accelerationGraph.getLegendRenderer().setTextSize(32);

        accelerationGraph.getViewport().setYAxisBoundsManual(true);
        accelerationGraph.getViewport().setMinY(-accelerationSensor.getMaximumRange());
        accelerationGraph.getViewport().setMaxY(accelerationSensor.getMaximumRange());

        accelerationGraph.addSeries(graphSeriesX);
        accelerationGraph.addSeries(graphSeriesY);
        accelerationGraph.addSeries(graphSeriesZ);

        accelerationGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
    }

    private void resetGraphView(){
        graphSeriesX.resetData(new DataPoint[]{});
        graphSeriesY.resetData(new DataPoint[]{});
        graphSeriesZ.resetData(new DataPoint[]{});
        accelerationGraph.removeAllSeries();
    }


}
