#include "zaku_servo.h"

#define DIRECTION_LEFT  1
#define DIRECTION_RIGHT 2

ZakuServo zakuServo = ZakuServo(SERVO_PIN);

ZakuServo::ZakuServo(uint8_t servo_pin) : servo_pin(servo_pin), servo1Pos(90), direction(DIRECTION_LEFT) {
  servoN1 = new Servo();
  servoN2 = new Servo();
  servo1 = new Servo();
}

void ZakuServo::setup() {
  servo1->setPeriodHertz(50);    // standard 50 hz servo
  servoN1->attach(2, 1000, 2000);
  servoN2->attach(16, 1000, 2000);
  
  servo1->attach(servo_pin, 1000, 2000);
  
  servo1->write(servo1Pos);
}

int ZakuServo::angle() {
  return servo1Pos;
}

int ZakuServo::left(int step) {
  direction = DIRECTION_LEFT;
  servo1Pos = min(servo1Pos + step, MONO_EYE_MAX_ANGLE);
  servo1->write(servo1Pos);
  return servo1Pos;
}

int ZakuServo::right(int step) {
  direction = DIRECTION_RIGHT;
  servo1Pos = max(servo1Pos - step, MONO_EYE_MIN_ANGLE);
  servo1->write(servo1Pos);
  return servo1Pos;
}

int ZakuServo::set(int angle) {
  int newPos = min(max(angle, MONO_EYE_MIN_ANGLE), MONO_EYE_MAX_ANGLE);
  direction = servo1Pos - newPos > 0 ? DIRECTION_LEFT :  DIRECTION_RIGHT;
  Serial.println(direction ? "LEFT" : "RIGHT");
  servo1Pos = newPos;
  servo1->write(servo1Pos);
  return servo1Pos;
}

int ZakuServo::hang_around() {
  int step = random(15, 60);
  if (direction == DIRECTION_LEFT) {
    left(step);
    if (servo1Pos == MONO_EYE_MAX_ANGLE) {
      direction = DIRECTION_RIGHT;
    }
  } else {
    right(step);
    if (servo1Pos == MONO_EYE_MIN_ANGLE) {
      direction = DIRECTION_LEFT;
    }
  }
  return servo1Pos;
}
