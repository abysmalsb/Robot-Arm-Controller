package com.github.abysmalsb.robotarmcontroller;

import android.util.Log;
import android.widget.TextView;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RobotArmController {

    private final List<String> loop;
    private final TCPClient robot;
    private Iterator<String> commands;
    private TextView goalText;

    private boolean initialized = false;
    private int goalUpperWristAngle;
    private int goalLowerWristAngle;
    private int goalRotatorWristAngle;

    public RobotArmController(List<String> initCommands, List<String> loopCommands, TCPClient client, TextView goalText) {
        List<String> mergedList = new LinkedList<String>();
        mergedList.addAll(initCommands);
        mergedList.addAll(loopCommands);
        this.commands = mergedList.iterator();
        this.loop = loopCommands;
        this.robot = client;
        this.goalText = goalText;
    }

    public void begin() {
        if (!initialized && commands.hasNext()) {
            String command = commands.next();
            robot.sendMessage(command);
            goalText.setText("S: " + command);
            String[] angles = command.split(" ");
            updateGoals(command);
        } else
            throw new IllegalStateException("Communication already started");

        initialized = true;
    }

    public void positionUpdate(String currentPosition) {
        String[] angles = currentPosition.split(" ");
        int upperWristAngle = Integer.parseInt(angles[2]);
        int lowerWristAngle = Integer.parseInt(angles[3]);
        int rotatorWristAngle = Integer.parseInt(angles[4]);
        if (goalUpperWristAngle == upperWristAngle
                && goalLowerWristAngle == lowerWristAngle
                && goalRotatorWristAngle == rotatorWristAngle) {
            if (commands.hasNext()) {
                String command = commands.next();
                robot.sendMessage(command);
                goalText.setText("S: " + command);
                updateGoals(command);
            } else if (loop.size() > 0) {
                commands = loop.iterator();
                String command = commands.next();
                robot.sendMessage(command);
                goalText.setText("S: " + command);
                updateGoals(command);
            }
        }
    }

    private void updateGoals(String command) {
        String[] angles = command.split(" ");
        if (angles[0].equals("A")) {
            goalUpperWristAngle = Integer.parseInt(angles[2]);
            goalLowerWristAngle = Integer.parseInt(angles[3]);
            goalRotatorWristAngle = Integer.parseInt(angles[4]);
        }
    }
}
