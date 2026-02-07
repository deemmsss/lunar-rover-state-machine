/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lunarrover;

/**
 *
 * @author demi
 */
import java.util.*;
import java.util.function.Consumer;

public class LunarRoverStateMachine {
    // Current states
    private ControlMode controlMode = ControlMode.MOVEMENT;
    private MovementState movementState = MovementState.REST;
    private CameraDrillState cameraDrillState = CameraDrillState.IDLE;
    private CameraSubState colorCameraSubState = CameraSubState.READY;
    private CameraSubState camera16mmSubState = CameraSubState.READY;
    private DrillSubState drillSubState = DrillSubState.OFF;
    
    // Speed simulation (0-100, negative for reverse)
    private int speed = 0;
    private static final int MAX_SPEED = 100;
    private static final int MAX_REVERSE_SPEED = -50;
    private static final int ACCEL_STEP = 10;
    private static final int DECEL_STEP = 10;
    
    // Event listeners for simulation output
    private List<Consumer<String>> listeners = new ArrayList<>();
    
    public void addListener(Consumer<String> listener) {
        listeners.add(listener);
    }
    
    private void log(String message) {
        listeners.forEach(l -> l.accept(message));
    }
    
    // ========================================================
    // PUBLIC API
    // ========================================================
    
    public void processEvent(PedalEvent event) {
        log("\n>>> Event: " + event);
        
        if (event == PedalEvent.SWITCH_FLIPPED) {
            handleSwitchFlipped();
            return;
        }
        
        switch (controlMode) {
            case MOVEMENT -> handleMovementEvent(event);
            case CAMERA_DRILL -> handleCameraDrillEvent(event);
        }
        
        logCurrentState();
    }
    
    // ========================================================
    // MODE SWITCHING (Must be at Rest/Idle)
    // ========================================================
    
    private void handleSwitchFlipped() {
        if (controlMode == ControlMode.MOVEMENT) {
            if (movementState == MovementState.REST) {
                controlMode = ControlMode.CAMERA_DRILL;
                cameraDrillState = CameraDrillState.IDLE;
                resetCameraAndDrillSubStates();
                log("==> Switched to CAMERA/DRILL mode");
            } else {
                log("!!! Cannot switch modes - vehicle must be at REST");
            }
        } else {
            if (cameraDrillState == CameraDrillState.IDLE) {
                controlMode = ControlMode.MOVEMENT;
                movementState = MovementState.REST;
                log("==> Switched to MOVEMENT mode");
            } else {
                log("!!! Cannot switch modes - must return to IDLE first");
            }
        }
        logCurrentState();
    }
    
    // ========================================================
    // MOVEMENT CONTROL HANDLING
    // ========================================================
    
    private void handleMovementEvent(PedalEvent event) {
        switch (movementState) {
            case REST -> handleRestState(event);
            case ACCELERATING_FORWARD -> handleAccelForwardState(event);
            case CONSTANT_SPEED_FORWARD -> handleConstSpeedForwardState(event);
            case DECELERATING_FORWARD -> handleDeceleratingForwardState(event);
            case DECELERATING_BACKWARD -> handleDeceleratingBackwardState(event);
            case ACCELERATING_BACKWARD -> handleAccelBackwardState(event);
            case CONSTANT_SPEED_BACKWARD -> handleConstSpeedBackwardState(event);
        }
    }
    
    private void handleRestState(PedalEvent event) {
        switch (event) {
            case LEFT_PRESSED -> {
                movementState = MovementState.ACCELERATING_FORWARD;
                accelerate();
                log("==> Accelerating forward from rest");
            }
            case LEFT_HELD_3S -> {
                movementState = MovementState.ACCELERATING_BACKWARD;
                reverse();
                log("==> Accelerating backward from rest");
            }
            default -> log("--- Event ignored in REST state");
        }
    }
    
    private void handleAccelForwardState(PedalEvent event) {
        switch (event) {
            case LEFT_PRESSED -> {
                if (speed < MAX_SPEED) {
                    accelerate();
                    log("==> Continuing acceleration forward");
                } else {
                    log("--- Already at max speed");
                }
            }
            case RIGHT_PRESSED -> {
                movementState = MovementState.DECELERATING_FORWARD;
                decelerate();
                log("==> Decelerating (forward)");
            }
            case RIGHT_HELD_3S -> {
                movementState = MovementState.CONSTANT_SPEED_FORWARD;
                log("==> Engaged constant speed forward: " + speed);
            }
            default -> log("--- Event ignored in ACCELERATING_FORWARD state");
        }
    }
    
    private void handleConstSpeedForwardState(PedalEvent event) {
        switch (event) {
            case LEFT_PRESSED -> {
                if (speed < MAX_SPEED) {
                    movementState = MovementState.ACCELERATING_FORWARD;
                    accelerate();
                    log("==> Accelerating from cruise");
                } else {
                    log("--- Already at max speed");
                }
            }
            case RIGHT_PRESSED -> {
                movementState = MovementState.DECELERATING_FORWARD;
                decelerate();
                log("==> Decelerating from cruise");
            }
            default -> log("--- Event ignored in CONSTANT_SPEED_FORWARD state");
        }
    }
    
    private void handleDeceleratingForwardState(PedalEvent event) {
        switch (event) {
            case LEFT_PRESSED -> {
                movementState = MovementState.ACCELERATING_FORWARD;
                accelerate();
                log("==> Accelerating forward from decel");
            }
            case RIGHT_HELD_3S -> {
                if (speed > 0) {
                    movementState = MovementState.CONSTANT_SPEED_FORWARD;
                    log("==> Engaged constant speed at: " + speed);
                } else {
                    log("--- Cannot cruise at zero speed");
                }
            }
            case RIGHT_PRESSED -> {
                decelerate();
                if (speed == 0) {
                    movementState = MovementState.REST;
                    log("==> Came to rest");
                } else {
                    log("==> Continuing deceleration (forward)");
                }
            }
            default -> log("--- Event ignored in DECELERATING_FORWARD state");
        }
    }
    
    private void handleDeceleratingBackwardState(PedalEvent event) {
        switch (event) {
            case LEFT_PRESSED -> {
                movementState = MovementState.ACCELERATING_FORWARD;
                accelerate();
                log("==> Accelerating forward from reverse decel (Assumption #10)");
            }
            case RIGHT_HELD_3S -> {
                if (speed < 0) {
                    movementState = MovementState.CONSTANT_SPEED_BACKWARD;
                    log("==> Engaged constant speed backward at: " + speed);
                } else {
                    log("--- Cannot cruise at zero speed");
                }
            }
            case RIGHT_PRESSED -> {
                decelerateReverse();
                if (speed == 0) {
                    movementState = MovementState.REST;
                    log("==> Came to rest");
                } else {
                    log("==> Continuing deceleration (backward)");
                }
            }
            default -> log("--- Event ignored in DECELERATING_BACKWARD state");
        }
    }
    
    private void handleAccelBackwardState(PedalEvent event) {
        switch (event) {
            case LEFT_PRESSED -> {
                if (speed > MAX_REVERSE_SPEED) {
                    reverse();
                    log("==> Continuing acceleration backward");
                } else {
                    log("--- Already at max reverse speed");
                }
            }
            case RIGHT_PRESSED -> {
                movementState = MovementState.DECELERATING_BACKWARD;
                decelerateReverse();
                log("==> Decelerating (from reverse)");
            }
            case RIGHT_HELD_3S -> {
                movementState = MovementState.CONSTANT_SPEED_BACKWARD;
                log("==> Engaged constant speed backward: " + speed);
            }
            default -> log("--- Event ignored in ACCELERATING_BACKWARD state");
        }
    }
    
    private void handleConstSpeedBackwardState(PedalEvent event) {
        switch (event) {
            case LEFT_PRESSED -> {
                movementState = MovementState.ACCELERATING_BACKWARD;
                reverse();
                log("==> Accelerating backward from cruise");
            }
            case RIGHT_PRESSED -> {
                movementState = MovementState.DECELERATING_BACKWARD;
                decelerateReverse();
                log("==> Decelerating from reverse cruise");
            }
            default -> log("--- Event ignored in CONSTANT_SPEED_BACKWARD state");
        }
    }
    
    // Speed helpers
    private void accelerate() { speed = Math.min(speed + ACCEL_STEP, MAX_SPEED); }
    private void decelerate() { speed = Math.max(speed - DECEL_STEP, 0); }
    private void reverse() { speed = Math.max(speed - ACCEL_STEP, MAX_REVERSE_SPEED); }
    private void decelerateReverse() { speed = Math.min(speed + DECEL_STEP, 0); }
    
    // ========================================================
    // CAMERA/DRILL CONTROL HANDLING
    // ========================================================
    
    private void handleCameraDrillEvent(PedalEvent event) {
        switch (cameraDrillState) {
            case IDLE -> handleIdleState(event);
            case COLOR_CAMERA -> handleColorCameraState(event);
            case CAMERA_16MM -> handle16mmCameraState(event);
            case DRILL -> handleDrillState(event);
        }
    }
    
    private void handleIdleState(PedalEvent event) {
        switch (event) {
            case LEFT_HELD_5S -> {
                cameraDrillState = CameraDrillState.COLOR_CAMERA;
                colorCameraSubState = CameraSubState.READY;
                log("==> Entered COLOR CAMERA mode");
            }
            case LEFT_HELD_10S -> {
                cameraDrillState = CameraDrillState.CAMERA_16MM;
                camera16mmSubState = CameraSubState.READY;
                log("==> Entered 16MM CAMERA mode");
            }
            case LEFT_DOUBLE_PRESS -> {
                cameraDrillState = CameraDrillState.DRILL;
                drillSubState = DrillSubState.OFF;
                log("==> Entered DRILL mode");
            }
            default -> log("--- Event ignored in IDLE state");
        }
    }
    
    private void handleColorCameraState(PedalEvent event) {
        if (event == PedalEvent.RIGHT_PRESSED) {
            cameraDrillState = CameraDrillState.IDLE;
            colorCameraSubState = CameraSubState.READY;
            log("==> Returned to IDLE from Color Camera");
            return;
        }
        
        switch (colorCameraSubState) {
            case READY -> {
                switch (event) {
                    case LEFT_PRESSED -> {
                        colorCameraSubState = CameraSubState.TAKING_PICTURE;
                        log("==> Color Camera: Taking picture...");
                        // Auto-transition back (simulated)
                        colorCameraSubState = CameraSubState.READY;
                        log("==> Color Camera: Picture taken, ready");
                    }
                    case LEFT_HELD_5S -> {
                        colorCameraSubState = CameraSubState.TEMPORIZER_ACTIVE;
                        log("==> Color Camera: Temporizer activated (selfie mode)");
                    }
                    default -> log("--- Event ignored in Color Camera READY");
                }
            }
            case TEMPORIZER_ACTIVE -> {
                if (event == PedalEvent.TIMER_EXPIRED) {
                    colorCameraSubState = CameraSubState.TAKING_PICTURE;
                    log("==> Color Camera: Timer expired, taking picture...");
                    colorCameraSubState = CameraSubState.READY;
                    log("==> Color Camera: Picture taken, ready");
                } else {
                    log("--- Waiting for timer in Color Camera");
                }
            }
            case TAKING_PICTURE -> log("--- Color Camera busy taking picture");
        }
    }
    
    private void handle16mmCameraState(PedalEvent event) {
        if (event == PedalEvent.RIGHT_PRESSED) {
            cameraDrillState = CameraDrillState.IDLE;
            camera16mmSubState = CameraSubState.READY;
            log("==> Returned to IDLE from 16mm Camera");
            return;
        }
        
        switch (camera16mmSubState) {
            case READY -> {
                switch (event) {
                    case LEFT_PRESSED -> {
                        camera16mmSubState = CameraSubState.TAKING_PICTURE;
                        log("==> 16mm Camera: Taking picture...");
                        camera16mmSubState = CameraSubState.READY;
                        log("==> 16mm Camera: Picture taken, ready");
                    }
                    case LEFT_HELD_5S -> {
                        camera16mmSubState = CameraSubState.TEMPORIZER_ACTIVE;
                        log("==> 16mm Camera: Temporizer activated (selfie mode)");
                    }
                    default -> log("--- Event ignored in 16mm Camera READY");
                }
            }
            case TEMPORIZER_ACTIVE -> {
                if (event == PedalEvent.TIMER_EXPIRED) {
                    camera16mmSubState = CameraSubState.TAKING_PICTURE;
                    log("==> 16mm Camera: Timer expired, taking picture...");
                    camera16mmSubState = CameraSubState.READY;
                    log("==> 16mm Camera: Picture taken, ready");
                } else {
                    log("--- Waiting for timer in 16mm Camera");
                }
            }
            case TAKING_PICTURE -> log("--- 16mm Camera busy taking picture");
        }
    }
    
    private void handleDrillState(PedalEvent event) {
        switch (event) {
            case RIGHT_PRESSED -> {
                if (drillSubState == DrillSubState.ON) {
                    drillSubState = DrillSubState.OFF;
                    log("==> Drill auto-disabled on exit");
                }
                cameraDrillState = CameraDrillState.IDLE;
                log("==> Returned to IDLE from Drill");
            }
            case LEFT_PRESSED -> {
                if (drillSubState == DrillSubState.OFF) {
                    drillSubState = DrillSubState.ON;
                    log("==> Drill: Turned ON");
                } else {
                    drillSubState = DrillSubState.OFF;
                    log("==> Drill: Turned OFF");
                }
            }
            default -> log("--- Event ignored in DRILL state");
        }
    }
    
    private void resetCameraAndDrillSubStates() {
        colorCameraSubState = CameraSubState.READY;
        camera16mmSubState = CameraSubState.READY;
        drillSubState = DrillSubState.OFF;
    }
    
    // ========================================================
    // STATE REPORTING
    // ========================================================
    
    public void logCurrentState() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- Current State ---\n");
        sb.append("Control Mode: ").append(controlMode).append("\n");
        sb.append("Speed: ").append(speed).append("\n");
        
        if (controlMode == ControlMode.MOVEMENT) {
            sb.append("Movement State: ").append(movementState).append("\n");
        } else {
            sb.append("Camera/Drill State: ").append(cameraDrillState).append("\n");
            switch (cameraDrillState) {
                case COLOR_CAMERA -> sb.append("  Color Camera Sub: ").append(colorCameraSubState).append("\n");
                case CAMERA_16MM -> sb.append("  16mm Camera Sub: ").append(camera16mmSubState).append("\n");
                case DRILL -> sb.append("  Drill Sub: ").append(drillSubState).append("\n");
                default -> {}
            }
        }
        sb.append("---------------------");
        log(sb.toString());
    }
    
    // Getters for testing
    public ControlMode getControlMode() { return controlMode; }
    public MovementState getMovementState() { return movementState; }
    public CameraDrillState getCameraDrillState() { return cameraDrillState; }
    public CameraSubState getColorCameraSubState() { return colorCameraSubState; }
    public CameraSubState getCamera16mmSubState() { return camera16mmSubState; }
    public DrillSubState getDrillSubState() { return drillSubState; }
    public int getSpeed() { return speed; }
}
