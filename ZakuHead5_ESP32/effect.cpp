#include "zaku_leds.h"

typedef struct effect
{
    uint16_t red;
    uint16_t orange;
} EFFECT;

int breathStep = 0;
EFFECT breathCmd[12] = {
    {1, 1},
    {2, 2},
    {3, 3},
    {4, 4},
    {5, 5},
    {6, 6},
    {5, 3},
    {4, 3},
    {3, 1},
    {2, 1},
    {1, 0},
    {0, 0}
};
int breathCmdSize = sizeof(breathCmd) / sizeof(breathCmd[0]);

#define EFFECT_RATIO 2
void breath() {
    int index = breathStep / EFFECT_RATIO;
    EFFECT cmd = breathCmd[index];
    mono_eye_leds.red(cmd.red);
    mono_eye_leds.orange(cmd.orange);
    breathStep = (breathStep + 1) % (EFFECT_RATIO * breathCmdSize);
}