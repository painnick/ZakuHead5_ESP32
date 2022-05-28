#include "zaku_leds.h"
#include "analogWrite.h"

ZakuLEDs mono_eye_leds = ZakuLEDs(EYE_RED_PIN, EYE_ORANGE_PIN);

ZakuLEDs::ZakuLEDs(uint8_t red_pin_no, uint8_t orange_pin_no)
: RED_PIN(red_pin_no), ORANGE_PIN(orange_pin_no)
{
    pinMode(RED_PIN, OUTPUT);
    pinMode(ORANGE_PIN, OUTPUT);
}

void ZakuLEDs::red(uint16_t bright) {
    analogWrite(RED_PIN, bright);
}

void ZakuLEDs::orange(uint16_t bright) {
    analogWrite(ORANGE_PIN, bright);
}
