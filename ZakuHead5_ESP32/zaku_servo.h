#ifndef ZAKU_SERVO_h
#define ZAKU_SERVO_h

#include <ESP32Servo.h>

class ZakuServo {
public:
    ZakuServo(uint8_t servo_pin);
    void setup();
    int left(int step);
    int right(int step);
    int set(int angle);
    int hang_around();
    int angle();
private:
    Servo* servoN1; // For Camera
    Servo* servoN2; // For Camera
    Servo* servo1;
    uint8_t servo_pin;
    int servo1Pos;
    int direction;
};

#define MONO_EYE_MIN_ANGLE  10
#define MONO_EYE_MAX_ANGLE 170

#define SERVO_PIN      14
extern ZakuServo zakuServo;

#endif /* ZAKU_SERVO_h */
