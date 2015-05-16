package com.jmedeisis.bugstick;

/**
 * Interface definition for joystick callbacks from user touch interactions.
 */
public interface JoystickListener {
    void onDown();

    /**
     * @param degrees -180 -> 180.
     * @param offset  normalized, 0 -> 1.
     */
    void onDrag(float degrees, float offset);

    void onUp();
}
