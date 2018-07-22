package com.github.abysmalsb.robotarmcontroller;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

public class ConfigureActivity extends AppCompatActivity implements ConfigureMenuFragment.OnRobotStateUpdated, RunFragment.RobotScriptController {

    private final FragmentManager fragmentManager = getSupportFragmentManager();

    private String address;
    private int port;
    private TCPClient tcpClient;
    private RobotArmController robot;

    private List<String> initCommandsStash;
    private List<String> loopCommandsStash;

    private RunFragment runFragment;
    private Fragment current;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_init:
                    Fragment initFragment = fragmentManager.findFragmentByTag("init");
                    if (initFragment == null) {
                        initFragment = ConfigureMenuFragment.newInstance(true);
                        fragmentManager.beginTransaction().hide(current).add(R.id.content, initFragment, "init").commit();
                    } else {
                        fragmentManager.beginTransaction().hide(current).show(initFragment).commit();
                    }
                    current = initFragment;
                    return true;
                case R.id.navigation_loop:
                    Fragment loopFragment = fragmentManager.findFragmentByTag("loop");
                    if (loopFragment == null) {
                        loopFragment = ConfigureMenuFragment.newInstance(false);
                        fragmentManager.beginTransaction().hide(current).add(R.id.content, loopFragment, "loop").commit();
                    } else {
                        fragmentManager.beginTransaction().hide(current).show(loopFragment).commit();
                    }
                    current = loopFragment;
                    return true;
                case R.id.navigation_run:
                    RunFragment runFragment = (RunFragment) fragmentManager.findFragmentByTag("runFragment");
                    fragmentManager.beginTransaction().hide(current).show(runFragment).commit();
                    current = runFragment;
                    runFragment.getNewInitCommand(initCommandsStash);
                    runFragment.getNewLoopCommand(loopCommandsStash);
                    initCommandsStash = new LinkedList<>();
                    loopCommandsStash = new LinkedList<>();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);

        Intent intent = getIntent();
        address = intent.getStringExtra(ConnectActivity.TCP_ADDRESS);
        port = intent.getIntExtra(ConnectActivity.TCP_PORT, 0);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        initCommandsStash = new LinkedList<>();
        loopCommandsStash = new LinkedList<>();

        runFragment = new RunFragment();
        current = runFragment;
        fragmentManager.beginTransaction().add(R.id.content, current, "runFragment").commit();

        new TCPCommunicator().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                leave();
                finish();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        leave();
    }

    private void leave() {
        disconnect();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void disconnect() {
        tcpClient.stopClient();
    }

    @Override
    public void onPositionChanged(int speed, int upperWristAngle, int lowerWristAngle, int rotatorWristAngle) {
        String command = getRobotArmPositionCommand(speed, upperWristAngle, lowerWristAngle, rotatorWristAngle);
        tcpClient.sendMessage(command);
    }

    @Override
    public void onPositionSaved(boolean isInit, int speed, int upperWristAngle, int lowerWristAngle, int rotatorWristAngle) {
        String command = getRobotArmPositionCommand(speed, upperWristAngle, lowerWristAngle, rotatorWristAngle);
        saveCommand(isInit, command);
    }

    @Override
    public void onGripperStateChanged(boolean isReleased) {
        String command = getRobotGripperMoveCommand(isReleased);
        tcpClient.sendMessage(command);
    }

    @Override
    public void onGripperStateSaved(boolean isInit, boolean isReleased) {
        String command = getRobotGripperMoveCommand(isReleased);
        saveCommand(isInit, command);
    }

    private String getRobotArmPositionCommand(int speed, int upperWristAngle, int lowerWristAngle, int rotatorWristAngle) {
        return "A " + speed + " " + upperWristAngle + " " + lowerWristAngle + " " + rotatorWristAngle;
    }

    private String getRobotGripperMoveCommand(boolean released) {
        return "G " + (released ? 0 : 1);
    }

    @Override
    public void run(List<String> initCommands, List<String> loopCommands, TextView goalText) {
        robot = new RobotArmController(initCommands, loopCommands, tcpClient, goalText);
        robot.begin();
    }

    @Override
    public void stop() {
        robot = null;
    }

    private void saveCommand(boolean isInit, String command) {
        if (isInit)
            initCommandsStash.add(command);
        else
            loopCommandsStash.add(command);
    }

    private class TCPCommunicator extends AsyncTask<String, String, TCPClient> {

        @Override
        protected TCPClient doInBackground(String... strings) {
            //we create a TCPClient object and
            tcpClient = new TCPClient(address, port, new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            tcpClient.run();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... commands) {
            if (robot != null) {
                for (String command : commands) {
                    runFragment.updatePosition("R: " + command);
                    robot.positionUpdate(command);
                }
            }
        }
    }
}
