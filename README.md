# Lunar Rover State Machine

A finite state machine implementation modeling the control system of the Apollo 15 Lunar Roving Vehicle (LRV), the first crewed vehicle driven on the Moon (July 30, 1971).

## Overview

This project simulates the Lunar Rover's control system using a state machine approach. The original vehicle used only **two pedals** and a **switch** to control both movement and equipment (cameras and drill), requiring creative engineering solutions to pack all functionality into minimal controls.

## Features

- **Movement Control**: Forward/backward acceleration, constant speed cruise, deceleration
- **Camera Control**: Color television camera and 16mm film camera with picture-taking and temporizer (self-timer)
- **Drill Control**: On/off toggle with auto-disable safety feature
- **Mode Switching**: Toggle between Movement and Camera/Drill modes (only when safe)

## State Machine Diagram

The system uses nested/composite states:

```
Lunar Rover Control System
├── Movement Control Mode
│   ├── Rest
│   ├── Accelerating Forward
│   ├── Constant Speed Forward
│   ├── Decelerating Forward
│   ├── Accelerating Backward
│   ├── Constant Speed Backward
│   └── Decelerating Backward
│
└── Camera/Drill Control Mode
    ├── Idle
    ├── Color Camera Mode
    │   ├── Ready
    │   ├── Taking Picture
    │   └── Temporizer Active
    ├── 16mm Camera Mode
    │   ├── Ready
    │   ├── Taking Picture
    │   └── Temporizer Active
    └── Drill Mode
        ├── Off
        └── On
```

Full UML diagram is included in the root folder

## Project Structure

```
lunar-rover-state-machine/
├── README.md
└── src/
    ├── ControlMode.java                   # Movement/Camera-Drill mode enum
    ├── MovementState.java                 # Movement states enum
    ├── CameraDrillState.java              # Camera/Drill states enum
    ├── CameraSubState.java                # Camera sub-states enum
    ├── DrillSubState.java                 # Drill sub-states enum
    ├── PedalEvent.java                    # Input events enum
    ├── LunarRoverStateMachine.java        # Main state machine logic
    └── LunarRover.java                    # Main class with test harness
```

## Requirements

- Java 21 or higher
- NetBeans IDE (recommended) or any Java IDE

## How to Run

```bash
# Navigate to src folder
cd src

# Compile all files
javac *.java

# Run with assertions enabled
java -ea LunarRover
```

The program automatically runs all test scenarios and outputs results to the console.

## Control Mapping

### Movement Mode

| Input | Action |
|-------|--------|
| Left pedal press | Accelerate forward |
| Left pedal hold (>3s) | Accelerate backward (from rest) |
| Right pedal press | Decelerate |
| Right pedal hold (>3s) | Engage constant speed |

### Camera/Drill Mode

| Input | Action |
|-------|--------|
| Left pedal hold (5s) | Enter Color Camera mode |
| Left pedal hold (10s) | Enter 16mm Camera mode |
| Left pedal double press | Enter Drill mode |
| Right pedal press | Return to Idle |

### In Camera Modes

| Input | Action |
|-------|--------|
| Left pedal press | Take picture |
| Left pedal hold (5s) | Activate temporizer (self-timer) |

### In Drill Mode

| Input | Action |
|-------|--------|
| Left pedal press | Toggle drill on/off |

## Assumptions

The original assignment description was incomplete. The following assumptions were made:

1. Switch position determines active mode
2. Switching modes stops any current action
3. "Double press" = two presses within 1 second
4. Deceleration applies to both directions
5. Speed has max limits (forward & reverse)
6. Temporizer has configurable delay
7. Constant backward speed added for symmetry
8. Vehicle must be at rest to switch modes
9. Hold duration detected at release (10s vs 5s)
10. Decel + left pedal always accelerates forward
11. Exiting device mode auto-disables device
12. Temporizer auto-takes picture after delay

## Test Coverage

| Test Scenario | Coverage |
|---------------|----------|
| Movement Control | Forward accel, cruise, decel, backward accel, backward cruise, return to rest |
| Mode Switching | REST↔Camera/Drill, blocked while moving, blocked while in device mode |
| Camera Control | Color camera, 16mm camera, take picture, temporizer |
| Drill Control | Enter drill, on/off toggle, auto-disable on exit |
| Edge Cases | Ignored events, max speed, decel→re-accel, direction changes |

## Technologies

- **Java 21** - Implementation language
- **NetBeans IDE** - Development environment
- **Mermaid** - State machine diagrams

## Historical Context

The Lunar Roving Vehicle (LRV) was developed by Boeing and used on Apollo missions 15, 16, and 17. It could travel at speeds up to 12 km/h and allowed astronauts to explore much greater distances from the lunar module than on foot.

## License

MIT License - Feel free to use for educational purposes.
