package com.github.abysmalsb.robotarmcontroller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

public class RunFragment extends Fragment {

    public static final String INIT_SCRIPT = "deviceAddress";
    public static final String LOOP_SCRIPT = "deviceName";
    public static final String PREFS_NAME = "scriptPreferences";

    private TextView feedback;
    private TextView goal;
    private EditText initCommands;
    private EditText loopCommands;
    private Button start;
    private boolean running = false;

    private RobotScriptController mListener;
    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_run, container, false);
        initCommands = view.findViewById(R.id.initCommands);
        loopCommands = view.findViewById(R.id.loopCommands);
        feedback = view.findViewById(R.id.feedback);
        start = view.findViewById(R.id.run);
        goal = view.findViewById(R.id.goal);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!running) {
                    start.setText("Stop");
                    running = true;
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(INIT_SCRIPT, initCommands.getText().toString());
                    editor.putString(LOOP_SCRIPT, loopCommands.getText().toString());
                    editor.commit();
                    mListener.run(getCommands(initCommands), getCommands(loopCommands), goal);
                } else {
                    start.setText("Run");
                    running = false;
                    mListener.stop();
                }
            }
        });

        prefs = this.getActivity().getSharedPreferences(PREFS_NAME, 0);
        initCommands.setText(prefs.getString(INIT_SCRIPT, ""));
        loopCommands.setText(prefs.getString(LOOP_SCRIPT, ""));

        return view;
    }

    public List<String> getCommands(EditText field) {
        List<String> list = new LinkedList<>();
        for (String command : field.getText().toString().split("\n")) {
            if (!command.equals(""))
                list.add(command);
        }
        return list;
    }

    public void updatePosition(String position) {
        feedback.setText(position);
    }

    public void getNewInitCommand(List<String> newInitCommands) {
        for (String command : newInitCommands)
            initCommands.append(command + "\n");
    }

    public void getNewLoopCommand(List<String> newLoopCommands) {
        for (String command : newLoopCommands)
            loopCommands.append(command + "\n");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RobotScriptController) {
            mListener = (RobotScriptController) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement RobotScriptController");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface RobotScriptController {
        void run(List<String> initCommands, List<String> loopCommands, TextView goalText);

        void stop();
    }

}