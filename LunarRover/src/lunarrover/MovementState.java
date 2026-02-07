/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lunarrover;

/**
 *
 * @author demi
 */
public enum MovementState {
    REST,
    ACCELERATING_FORWARD,
    CONSTANT_SPEED_FORWARD,
    DECELERATING_FORWARD,
    DECELERATING_BACKWARD,
    ACCELERATING_BACKWARD,
    CONSTANT_SPEED_BACKWARD
}
