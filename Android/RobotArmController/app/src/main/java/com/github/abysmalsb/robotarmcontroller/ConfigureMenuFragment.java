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
    private int previousUpperJointAngle;
    private int previousLowerJointAngle;
    private int previousRotatorJointAngle;

    private OnRobotStateUpdated mListener;

    private Button saveButton;
    private ToggleButton gripperToggle;
    private SeekBar speedSeekBar;
    private SeekBar upperJointAngleSeekBar;
    private SeekBar lowerJointAngleSeekBar;
    private SeekBar rotatorJointAngleSeekBar;

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
        upperJointAngleSeekBar = view.findViewById(R.id.upperJoint);
        lowerJointAngleSeekBar = view.findViewById(R.id.lowerJoint);
        rotatorJointAngleSeekBar = view.findViewById(R.id.rotatorJoint);

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
                int currentUpperJointAngle = upperJointAngleSeekBar.getProgress();
                int currentLowerJointAngle = lowerJointAngleSeekBar.getProgress();
                int currentRotatorJointAngle = rotatorJointAngleSeekBar.getProgress();

                if (previousUpperJointAngle != currentUpperJointAngle ||
                        previousLowerJointAngle != currentLowerJointAngle ||
                        previousRotatorJointAngle != currentRotatorJointAngle) {
                    mListener.onPositionSaved(isInit, SPEED_CONVERT - speed, currentUpperJointAngle, currentLowerJointAngle, currentRotatorJointAngle);
                    previousUpperJointAngle = currentUpperJointAngle;
                    previousLowerJointAngle = currentLowerJointAngle;
                    previousRotatorJointAngle = currentRotatorJointAngle;
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
                int upperJointAngle = upperJointAngleSeekBar.getProgress();
                int lowerJointAngle = lowerJointAngleSeekBar.getProgress();
                int rotatorJointAngle = rotatorJointAngleSeekBar.getProgress();
                mListener.onPositionChanged(SPEED_CONVERT - speed, upperJointAngle, lowerJointAngle, rotatorJointAngle);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };

        speedSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        upperJointAngleSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        lowerJointAngleSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        rotatorJointAngleSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        previousGripperState = gripperToggle.isChecked();
        previousUpperJointAngle = upperJointAngleSeekBar.getProgress();
        previousLowerJointAngle = lowerJointAngleSeekBar.getProgress();
        previousRotatorJointAngle = rotatorJointAngleSeekBar.getProgress();

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
        void onPositionChanged(int speed, int upperJointAngle, int lowerJointAngle, int rotatorJointAngle);

        void onPositionSaved(boolean isInit, int speed, int upperJointAngle, int lowerJointAngle, int rotatorJointAngle);

        void onGripperStateChanged(boolean released);

        void onGripperStateSaved(boolean isInit, boolean released);
    }
}
