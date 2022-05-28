/*********
  Rui Santos
  Complete instructions at https://RandomNerdTutorials.com/esp32-cam-projects-ebook/
  
  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files.
  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
*********/

#define USE_SERIAL_DEBUG

#include "Arduino.h"
#include "soc/soc.h"             // disable brownout problems
#include "soc/rtc_cntl_reg.h"    // disable brownout problems

#include "zaku_status.h"
#include "zaku_leds.h"
#include "zaku_servo.h"

void initCamera();
void initCameraServer();
void startCameraServer();

void breath();

time_t now;
time_t last_random_move;

void setup() {

  WRITE_PERI_REG(RTC_CNTL_BROWN_OUT_REG, 0); //disable brownout detector

  #ifdef USE_SERIAL_DEBUG
  Serial.begin(115200);
  Serial.setDebugOutput(false);
  #endif

  zakuServo.setup();

  initCamera();
  initCameraServer();
  startCameraServer();

  time(&last_catch);
  time(&last_random_move);
}

void loop() {
  time(&now);

  if (now - last_catch > 30) {
    // Reset
    mono_eye_leds.red(0);
    mono_eye_leds.orange(0);
    time(&last_catch);
  } else if (now - last_catch > 10) {
    // Sleeping
    breath();
  } else if (now - last_catch > 3) {
    if (now - last_random_move > 0.3) {
      time(&last_random_move);
      zakuServo.hang_around();
    }
  } else {
    // Found
  }
  delay(100);
}
