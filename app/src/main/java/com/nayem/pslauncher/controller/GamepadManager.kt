package com.nayem.pslauncher.controller

import android.content.Context
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent

/**
 * Physical controller support — Android GameController API.
 * Works with DualShock 4, DualSense, Xbox, 8BitDo, generic BT/USB pads.
 * No root, no custom driver: Android already pairs them over Bluetooth.
 * Pairing: Settings > Bluetooth > pair the pad (PS button + Share for DS4,
 * PS + Create for DualSense) — then it shows up here.
 */
class GamepadManager(
    private val context: Context,
    private val onEvent: (String) -> Unit
) {
    fun refresh() { /* device list is read live in summary() */ }

    fun summary(): String {
        val pads = InputDevice.getDeviceIds()
            .map { InputDevice.getDevice(it) }
            .filter { it != null && (it.sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD }
        if (pads.isEmpty()) return "No controller connected — pair a PS/Xbox pad over Bluetooth, or turn on Virtual Controller"
        return "Controller: " + pads.joinToString { "${it!!.name} (${it.vendorId}:${it.productId})" }
    }

    /** True if this was a gamepad button — consume it here, show it in status. */
    fun handleKeyEvent(event: KeyEvent): Boolean {
        if ((event.source and InputDevice.SOURCE_GAMEPAD) != InputDevice.SOURCE_GAMEPAD &&
            (event.source and InputDevice.SOURCE_JOYSTICK) != InputDevice.SOURCE_JOYSTICK
        ) return false
        if (event.action != KeyEvent.ACTION_DOWN) return false
        val name = when (event.keyCode) {
            KeyEvent.KEYCODE_BUTTON_A -> "✕ / A"
            KeyEvent.KEYCODE_BUTTON_B -> "○ / B"
            KeyEvent.KEYCODE_BUTTON_X -> "□ / X"
            KeyEvent.KEYCODE_BUTTON_Y -> "△ / Y"
            KeyEvent.KEYCODE_BUTTON_L1 -> "L1"
            KeyEvent.KEYCODE_BUTTON_R1 -> "R1"
            KeyEvent.KEYCODE_BUTTON_L2 -> "L2"
            KeyEvent.KEYCODE_BUTTON_R2 -> "R2"
            KeyEvent.KEYCODE_BUTTON_START -> "Options / Start"
            KeyEvent.KEYCODE_BUTTON_SELECT -> "Share / Select"
            KeyEvent.KEYCODE_DPAD_UP -> "D-Up"
            KeyEvent.KEYCODE_DPAD_DOWN -> "D-Down"
            KeyEvent.KEYCODE_DPAD_LEFT -> "D-Left"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "D-Right"
            else -> "key ${event.keyCode}"
        }
        onEvent("Pad: $name pressed")
        return false // don't consume — let the game / Remote Play see it too
    }

    fun handleMotionEvent(event: MotionEvent): Boolean {
        if ((event.source and InputDevice.SOURCE_JOYSTICK) != InputDevice.SOURCE_JOYSTICK) return false
        val lx = event.getAxisValue(MotionEvent.AXIS_X)
        val ly = event.getAxisValue(MotionEvent.AXIS_Y)
        if (kotlin.math.abs(lx) > 0.2f || kotlin.math.abs(ly) > 0.2f) {
            // Only report big moves; the game reads the stick directly.
            // onEvent("Left stick: %.2f, %.2f".format(lx, ly))
        }
        return false
    }
}
