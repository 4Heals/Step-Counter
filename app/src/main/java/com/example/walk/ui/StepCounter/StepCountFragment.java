package com.example.walk.ui.StepCounter;

import static android.content.Context.SENSOR_SERVICE;

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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.walk.AccelerationData;
import com.example.walk.R;
import com.example.walk.StepDetector;
import com.example.walk.StepListener;
import com.example.walk.StepType;

import java.util.ArrayList;
import java.util.Locale;

public class StepCountFragment extends Fragment implements SensorEventListener, StepListener {

    private static final String TAG = "PedometerFragment";

    private CardView cardViewToggleStepCounting;
    private TextView textView_amount_steps, textView_type_of_step,
            textView_pedometer_is_running, textView_pedometer_toggle_text;

    private TextView textview_results_total_steps, textview_results_walking_steps, textview_results_jogging_steps, textview_results_running_steps,
            textview_results_total_distance, textview_results_average_speed, textview_results_average_frequency, textview_results_burned_calories, textview_results_total_moving_time;

    private StepCountViewModel mViewModel;

    public static StepCountFragment newInstance() {
        return new StepCountFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stepcounter_fragment, container, false);

        cardViewToggleStepCounting = view.findViewById(R.id.btn_pedometer_toggle_tracking);
        cardViewToggleStepCounting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mViewModel.isCountingSteps()) stopCounting();
                else startCounting();
            }
        });
        textView_pedometer_toggle_text = view.findViewById(R.id.textview_pedometer_toggle_text);

        textView_amount_steps = view.findViewById(R.id.textview_amount_steps);
        textView_type_of_step = view.findViewById(R.id.textview_pedometer_type_of_step);
        textView_pedometer_is_running = view.findViewById(R.id.textview_pedometer_isRunning);

        textview_results_total_steps = view.findViewById(R.id.textview_results_total_steps);
        textview_results_walking_steps = view.findViewById(R.id.textview_results_walking_steps);
        textview_results_jogging_steps = view.findViewById(R.id.textview_results_jogging_steps);
        textview_results_running_steps = view.findViewById(R.id.textview_results_running_steps);
        textview_results_total_distance = view.findViewById(R.id.textview_results_total_distance);
        textview_results_average_speed = view.findViewById(R.id.textview_results_average_speed);
        textview_results_average_frequency = view.findViewById(R.id.textview_results_average_frequency);
        textview_results_burned_calories = view.findViewById(R.id.textview_results_burned_calories);
        textview_results_total_moving_time = view.findViewById(R.id.textview_results_total_moving_time);

        if(mViewModel.getSensorManager() == null) {
            mViewModel.setSensorManager((SensorManager) getActivity().getSystemService(SENSOR_SERVICE));
        }
        if(mViewModel.getAccelerationSensor() == null){
            if(mViewModel.getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                mViewModel.setAccelerationSensor(mViewModel.getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            }
        }
        if(mViewModel.getStepDetector() == null){
            mViewModel.setStepDetector(new StepDetector());
        }
        mViewModel.getStepDetector().registerStepListener(this);

        if(mViewModel.getAccelerationDataArrayList() == null){
            mViewModel.setAccelerationDataArrayList(new ArrayList<AccelerationData>());
        }

        if(mViewModel.isCountingSteps()){
            textView_pedometer_toggle_text.setText(getResources().getText(R.string.disable_pedometer));
            textView_pedometer_is_running.setText(getResources().getText(R.string.pedometer_running));
            textView_pedometer_is_running.setTextColor(getResources().getColor(R.color.green));
            textView_amount_steps.setText(String.valueOf(mViewModel.getAmountOfSteps()));
            mViewModel.getSensorManager().registerListener(this, mViewModel.getAccelerationSensor(), SensorManager.SENSOR_DELAY_NORMAL);
        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(StepCountViewModel.class);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDetach() {
        mViewModel.getSensorManager().unregisterListener(this);
        super.onDetach();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        AccelerationData newAccelerationData = new AccelerationData();
        newAccelerationData.setX(sensorEvent.values[0]);
        newAccelerationData.setY(sensorEvent.values[1]);
        newAccelerationData.setZ(sensorEvent.values[2]);
        newAccelerationData.setTime(sensorEvent.timestamp);

        mViewModel.getAccelerationDataArrayList().add(newAccelerationData);
        mViewModel.getStepDetector().addAccelerationData(newAccelerationData);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void step(AccelerationData accelerationData, StepType stepType) {
        // Step event coming back from StepDetector
        mViewModel.setAmountOfSteps(mViewModel.getAmountOfSteps() + 1);
        textView_amount_steps.setText(String.valueOf(mViewModel.getAmountOfSteps()));
        if(stepType == StepType.WALKING) {
            mViewModel.setWalkingSteps(mViewModel.getWalkingSteps() + 1);
            textView_type_of_step.setText(getResources().getText(R.string.walking));
        }
        else if(stepType == StepType.JOGGING) {
            mViewModel.setJoggingSteps(mViewModel.getJoggingSteps() + 1);
            textView_type_of_step.setText(getResources().getText(R.string.jogging));
        }
        else {
            mViewModel.setRunningSteps(mViewModel.getRunningSteps() + 1);
            textView_type_of_step.setText(getResources().getText(R.string.running));
        }
    }

    private void calculateResults(){
        int totalSteps = mViewModel.getAmountOfSteps();
        textview_results_total_steps.setText(String.valueOf(totalSteps));

        int walkingSteps = mViewModel.getWalkingSteps();
        int joggingSteps = mViewModel.getJoggingSteps();
        int runningSteps = mViewModel.getRunningSteps();

        textview_results_walking_steps.setText(String.valueOf(walkingSteps));
        textview_results_jogging_steps.setText(String.valueOf(joggingSteps));
        textview_results_running_steps.setText(String.valueOf(runningSteps));

        float totalDistance = walkingSteps * 0.5f + joggingSteps * 1.0f + runningSteps * 1.5f;
        String distance = totalDistance + " m";
        textview_results_total_distance.setText(distance);

        float totalDuration = walkingSteps * 1.0f + joggingSteps * 0.75f + runningSteps * 0.5f;
        float hours = totalDuration / 3600;
        float minutes = (totalDuration % 3600) / 60;
        float seconds = totalDuration % 60;
        String duration = String.format(Locale.ENGLISH,"%.0f", hours) + "h " +
                String.format(Locale.ENGLISH, "%.0f", minutes) + "min " +
                String.format(Locale.ENGLISH, "%.0f", seconds) + "s";
        textview_results_total_moving_time.setText(duration);

        // Average speed:
        String averageSpeed = String.format(Locale.ENGLISH, "%.2f", totalDistance / totalDuration) + " m/s";
        textview_results_average_speed.setText(averageSpeed);

        // Average step frequency
        String averageStepFrequency = String.format(Locale.ENGLISH, "%.0f", totalSteps / minutes) + " Steps/min";
        textview_results_average_frequency.setText(averageStepFrequency);

        // Calories
        float totalCaloriesBurned = walkingSteps + 0.05f + joggingSteps * 0.1f + runningSteps * 0.2f;
        String totalCalories = String.format(Locale.ENGLISH, "%.0f", totalCaloriesBurned) + " calories";
        textview_results_burned_calories.setText(totalCalories);
    }

    private void resetUI(){
        mViewModel.setAmountOfSteps(0);
        mViewModel.setWalkingSteps(0);
        mViewModel.setJoggingSteps(0);
        mViewModel.setRunningSteps(0);
        textView_amount_steps.setText(String.valueOf(mViewModel.getWalkingSteps()));
    }

    private void startCounting(){
        if(!mViewModel.isCountingSteps()){
            try {
                resetUI();
                mViewModel.getSensorManager().registerListener(this, mViewModel.getAccelerationSensor(), SensorManager.SENSOR_DELAY_NORMAL);
                mViewModel.setCountingSteps(true);
                textView_pedometer_toggle_text.setText(getResources().getText(R.string.disable_pedometer));
                textView_pedometer_is_running.setText(getResources().getText(R.string.pedometer_running));
                textView_pedometer_is_running.setTextColor(getResources().getColor(R.color.green));
            } catch (Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void stopCounting(){
        if(mViewModel.isCountingSteps()){
            try {

                mViewModel.getSensorManager().unregisterListener(this);
                mViewModel.setCountingSteps(false);
                calculateResults();
                textView_pedometer_toggle_text.setText(getResources().getText(R.string.acitvate_pedometer));
                textView_pedometer_is_running.setText(getResources().getText(R.string.pedometer_not_running));
                textView_pedometer_is_running.setTextColor(getResources().getColor(R.color.red));
            } catch (Exception e){
                Log.d(TAG, e.getMessage());
            }
        }
    }

}

