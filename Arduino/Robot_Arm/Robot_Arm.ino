/******************************************************************
 * author: Balázs Simon
 * e-mail: simon.balazs.1992@gmail.com
 * 
 * This code controls a robot arm. An Arduino 101 is used in this project. 
 * If you plan to use this code on another Arduino you might need to use Serial
 * instead of Serial1
 * More info:
 * https://www.hackster.io/Abysmal/robot-arm-controlled-through-ethernet-5ce9ce
 ******************************************************************/

#include <Servo.h>

// Used pins
#define GRIPPER_SERVO 9
#define UPPER_WRIST_SERVO 6
#define LOWER_WRIST_SERVO 5
#define BASE_ROTATOR_WRIST_SERVO 3

// Gripper states
#define GRIPPER_OPEN 0
#define GRIPPER_CLOSED_SOFT 1
#define GRIPPER_CLOSED_MEDIUM 2
#define GRIPPER_CLOSED_HARD 3

// Gripper configuration, specific to your robot
#define GRIPPER_OPEN_ANGLE 150            // The angle of the servo in different positions
#define GRIPPER_CLOSED_SOFT_ANGLE 75      // Closing with low force (less stressful on the servo)
#define GRIPPER_CLOSED_MEDIUM_ANGLE 65    // Closing with medium force (slightly stressful on the servo)
#define GRIPPER_CLOSED_HARD_ANGLE 0       // Closing with high force (quite stressful on the servo, it will also heat up)

// Limits, specific to your robot
#define SERVO_ANGLE_UPPER_LIMIT 170
#define SERVO_ANGLE_LOWER_LIMIT 0
#define SERVO_ANGLE_RESTING 90
#define MIN_MOVEMENT_DELAY 2

// Commands
#define MOVE_GRIPPER 'G'
#define MOVE_ARM 'A'

Servo gripper, upperWrist, lowerWrist, baseRotator;

unsigned int movementDelay = 5;

int gripperState;
int targetUpperWristAngle;
int targetLowerWristAngle;
int targetBaseRotatorAngle;

float currentUpperWristAngle, currentLowerWristAngle, currentBaseRotatorAngle;
float upperWristStep, lowerWristStep, baseRotatorStep;

unsigned long lastStepTime = 0;

void setup() {
  Serial1.begin(115200);

  gripper.attach(GRIPPER_SERVO);
  upperWrist.attach(UPPER_WRIST_SERVO);
  lowerWrist.attach(LOWER_WRIST_SERVO);
  baseRotator.attach(BASE_ROTATOR_WRIST_SERVO);

  // Setting default position on starting. Specific for your robot, so you 
  // might need to adjust it to yours
  gripperState = GRIPPER_CLOSED_SOFT;
  targetUpperWristAngle = currentUpperWristAngle = 140;
  targetLowerWristAngle = currentLowerWristAngle = 45;
  targetBaseRotatorAngle = currentBaseRotatorAngle = 85;

  moveGripper(gripperState);
  moveTo(targetUpperWristAngle, targetLowerWristAngle, targetBaseRotatorAngle);
}

void loop() {
  if (Serial1.available() > 0) {
    // The first character means the received command type.
    char command = Serial1.read();

    if (command == MOVE_GRIPPER) {
      updateGripperState();
      moveGripper(gripperState);
      delay(250);
    }
    else if (command == MOVE_ARM) {
      updateArmMovement();
    }
  }

  if (millis() - lastStepTime >= movementDelay) {
    if (targetUpperWristAngle != round(currentUpperWristAngle)) currentUpperWristAngle -= upperWristStep;
    if (targetLowerWristAngle != round(currentLowerWristAngle)) currentLowerWristAngle -= lowerWristStep;
    if (targetBaseRotatorAngle != round(currentBaseRotatorAngle)) currentBaseRotatorAngle -= baseRotatorStep;

    moveTo(currentUpperWristAngle, currentLowerWristAngle, currentBaseRotatorAngle);

    printPosition();
    lastStepTime = millis();
  }
}

void updateGripperState() {
  if (Serial1.available() > 0)
    gripperState = Serial1.parseInt();
  else
    return;

  clearSerial1();
}

void updateArmMovement() {
  int mDelay;
  if (Serial1.available() > 0)
    mDelay = Serial1.parseInt();
  else
    return;

  int upperWristAngle;
  if (Serial1.available() > 0)
    upperWristAngle = Serial1.parseInt();
  else
    return;

  int lowerWristAngle;
  if (Serial1.available() > 0)
    lowerWristAngle = Serial1.parseInt();
  else
    return;

  int baseRotatorAngle;
  if (Serial1.available() > 0)
    baseRotatorAngle = Serial1.parseInt();
  else
    return;

  clearSerial1();

  // This calculation is used to slow down a movement of the robot arm segments.
  // mDelay is used as speed. Instantious movement would be too fast and it could
  // cause hardware issues.
  movementDelay = mDelay >= MIN_MOVEMENT_DELAY ? mDelay : MIN_MOVEMENT_DELAY;
  targetUpperWristAngle = upperWristAngle;
  targetLowerWristAngle = lowerWristAngle;
  targetBaseRotatorAngle = baseRotatorAngle;
  upperWristStep = (currentUpperWristAngle - targetUpperWristAngle) / 100;
  lowerWristStep = (currentLowerWristAngle - targetLowerWristAngle) / 100;
  baseRotatorStep = (currentBaseRotatorAngle - targetBaseRotatorAngle) / 100;
}

void clearSerial1() {
  // Clearing the serial buffer. New line and/or carriage return characters
  // are expected to be in the buffer.
  while (Serial1.available() > 0)
    Serial1.read();
}

void moveGripper(int state) {
  switch (state) {
    case GRIPPER_OPEN:
      gripper.write(GRIPPER_OPEN_ANGLE);
      break;
    case GRIPPER_CLOSED_SOFT:
      gripper.write(GRIPPER_CLOSED_SOFT_ANGLE);
      break;
    case GRIPPER_CLOSED_MEDIUM:
      gripper.write(GRIPPER_CLOSED_MEDIUM_ANGLE);
      break;
    case GRIPPER_CLOSED_HARD:
      gripper.write(GRIPPER_CLOSED_HARD_ANGLE);
      break;
  }
}

void moveTo(int upperWristAngle, int lowerWristAngle, int baseRotatorAngle) {
  // Moving the robot arm segments with updating the servos
  upperWrist.write(upperWristAngle);
  lowerWrist.write(lowerWristAngle);
  baseRotator.write(baseRotatorAngle);
}

void printPosition() {
  // Sending back feedback
  Serial1.print(movementDelay);
  Serial1.print(" ");
  Serial1.print(gripperState);
  Serial1.print(" ");
  Serial1.print(round(currentUpperWristAngle));
  Serial1.print(" ");
  Serial1.print(round(currentLowerWristAngle));
  Serial1.print(" ");
  Serial1.println(round(currentBaseRotatorAngle));
}

