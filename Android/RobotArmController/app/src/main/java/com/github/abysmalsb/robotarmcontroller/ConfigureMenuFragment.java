package com.github.abysmalsb.robotarmcontroller;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.ToggleButton;

public class ConfigureMenuFragment extends Fragment {
    private static final String IS_INIT = "is_init";

    private static final int SPEED_CONVERT = 25;

    private boolean isInit;

    private boolean previousGripperState;
    private int previousUpperWristAngle;
    private int previousLowerWristAngle;
    private int previousRotatorWristAngle;

    private OnRobotStateUpdated mListener;

    private Button saveButton;
    private ToggleButton gripperToggle;
    private SeekBar speedSeekBar;
    private SeekBar upperWristAngleSeekBar;
    private SeekBar lowerWristAngleSeekBar;
    private SeekBar rotatorWristAngleSeekBar;

    public ConfigureMenuFragment() {
        // Required empty public constructor
    }

    public static ConfigureMenuFragment newInstance(boolean isInit) {
        ConfigureMenuFragment fragment = new ConfigureMenuFragment();
        Bundle args = new Bundle();
        args.putBoolean(IS_INIT, isInit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isInit = getArguments().getBoolean(IS_INIT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_configure_menu, container, false);
        saveButton = view.findViewById(R.id.save);
        gripperToggle = view.findViewById(R.id.gripper);
        speedSeekBar = view.findViewById(R.id.speed);
        upperWristAngleSeekBar = view.findViewById(R.id.upperWrist);
        lowerWristAngleSeekBar = view.findViewById(R.id.lowerWrist);
        rotatorWristAngleSeekBar = view.findViewById(R.id.rotatorWrist);

        saveButton.setText("Save " + (isInit ? "init" : "loop") + " position");
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean currentGripperState = gripperToggle.isChecked();
                if (previousGripperState != currentGripperState) {
                    mListener.onGripperStateSaved(isInit, currentGripperState);
                    previousGripperState = currentGripperState;
                }
                int speed = speedSeekBar.getProgress();
                int currentUpperWristAngle = upperWristAngleSeekBar.getProgress();
                int currentLowerWristAngle = lowerWristAngleSeekBar.getProgress();
                int currentRotatorWristAngle = rotatorWristAngleSeekBar.getProgress();

                if (previousUpperWristAngle != currentUpperWristAngle ||
                        previousLowerWristAngle != currentLowerWristAngle ||
                        previousRotatorWristAngle != currentRotatorWristAngle) {
                    mListener.onPositionSaved(isInit, SPEED_CONVERT - speed, currentUpperWristAngle, currentLowerWristAngle, currentRotatorWristAngle);
                    previousUpperWristAngle = currentUpperWristAngle;
                    previousLowerWristAngle = currentLowerWristAngle;
                    previousRotatorWristAngle = currentRotatorWristAngle;
                }
            }
        });

        gripperToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mListener.onGripperStateChanged(isChecked);
            }
        });

        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                int speed = speedSeekBar.getProgress();
                int upperWristAngle = upperWristAngleSeekBar.getProgress();
                int lowerWristAngle = lowerWristAngleSeekBar.getProgress();
                int rotatorWristAngle = rotatorWristAngleSeekBar.getProgress();
                mListener.onPositionChanged(SPEED_CONVERT - speed, upperWristAngle, lowerWristAngle, rotatorWristAngle);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };

        speedSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        upperWristAngleSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        lowerWristAngleSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        rotatorWristAngleSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        previousGripperState = gripperToggle.isChecked();
        previousUpperWristAngle = upperWristAngleSeekBar.getProgress();
        previousLowerWristAngle = lowerWristAngleSeekBar.getProgress();
        previousRotatorWristAngle = rotatorWristAngleSeekBar.getProgress();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRobotStateUpdated) {
            mListener = (OnRobotStateUpdated) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRobotStateUpdated");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnRobotStateUpdated {
        void onPositionChanged(int speed, int upperWristAngle, int lowerWristAngle, int rotatorWristAngle);

        void onPositionSaved(boolean isInit, int speed, int upperWristAngle, int lowerWristAngle, int rotatorWristAngle);

        void onGripperStateChanged(boolean released);

        void onGripperStateSaved(boolean isInit, boolean released);
    }
}
