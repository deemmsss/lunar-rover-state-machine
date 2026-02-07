/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package lunarrover;

/**
 *
 * @author demi
 */
public class LunarRover {
    
    public static void main(String[] args) {
        LunarRoverStateMachine rover = new LunarRoverStateMachine();
        rover.addListener(System.out::println);
        
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║                                                        ║");
        System.out.println("║          LUNAR ROVER STATE MACHINE SIMULATION          ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        
        // Run all test scenarios
        testMovementControl(rover);
        testModeSwitching(rover);
        testCameraControl(rover);
        testDrillControl(rover);
        testEdgeCases(rover);
        
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║     ALL SIMULATIONS COMPLETE                           ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }
    
    // ========================================================
    // TEST SCENARIO 1: Movement Control
    // ========================================================
    static void testMovementControl(LunarRoverStateMachine rover) {
        System.out.println("\n\n══════════════════════════════════════════════════════");
        System.out.println("TEST 1: MOVEMENT CONTROL");
        System.out.println("══════════════════════════════════════════════════════");
        
        // Reset to initial state
        rover = new LunarRoverStateMachine();
        rover.addListener(System.out::println);
        
        System.out.println("\n-- Test 1.1: Forward acceleration --");
        rover.processEvent(PedalEvent.LEFT_PRESSED);  // Rest -> AccelFwd
        rover.processEvent(PedalEvent.LEFT_PRESSED);  // Continue accel
        rover.processEvent(PedalEvent.LEFT_PRESSED);  // Continue accel
        
        System.out.println("\n-- Test 1.2: Engage constant speed --");
        rover.processEvent(PedalEvent.RIGHT_HELD_3S); // AccelFwd -> ConstSpeed
        
        System.out.println("\n-- Test 1.3: Decelerate from cruise --");
        rover.processEvent(PedalEvent.RIGHT_PRESSED); // ConstSpeed -> DecelFwd
        rover.processEvent(PedalEvent.RIGHT_PRESSED); // Continue decel
        rover.processEvent(PedalEvent.RIGHT_PRESSED); // Continue decel
        rover.processEvent(PedalEvent.RIGHT_PRESSED); // Should reach REST
        
        System.out.println("\n-- Test 1.4: Backward acceleration --");
        rover.processEvent(PedalEvent.LEFT_HELD_3S);  // Rest -> AccelBack
        rover.processEvent(PedalEvent.LEFT_PRESSED);  // Continue reverse
        
        System.out.println("\n-- Test 1.5: Constant speed backward (Assumption #7 - symmetry) --");
        rover.processEvent(PedalEvent.RIGHT_HELD_3S); // AccelBack -> ConstSpeedBack
        assert rover.getMovementState() == MovementState.CONSTANT_SPEED_BACKWARD : "Should be at constant backward speed";
        
        System.out.println("\n-- Test 1.6: Decelerate from reverse to rest --");
        rover.processEvent(PedalEvent.RIGHT_PRESSED); // ConstSpeedBack -> DecelBack
        rover.processEvent(PedalEvent.RIGHT_PRESSED); // Continue
        rover.processEvent(PedalEvent.RIGHT_PRESSED); // Should reach REST
        
        assert rover.getMovementState() == MovementState.REST : "Should be at REST";
        System.out.println("\n✓ Movement Control tests passed!");
    }
    
    // ========================================================
    // TEST SCENARIO 2: Mode Switching
    // ========================================================
    static void testModeSwitching(LunarRoverStateMachine rover) {
        System.out.println("\n\n══════════════════════════════════════════════════════");
        System.out.println("TEST 2: MODE SWITCHING");
        System.out.println("══════════════════════════════════════════════════════");
        
        // Reset
        rover = new LunarRoverStateMachine();
        rover.addListener(System.out::println);
        
        System.out.println("\n-- Test 2.1: Switch from REST to Camera/Drill --");
        rover.processEvent(PedalEvent.SWITCH_FLIPPED);
        assert rover.getControlMode() == ControlMode.CAMERA_DRILL : "Should be in Camera/Drill mode";
        
        System.out.println("\n-- Test 2.2: Switch back from IDLE to Movement --");
        rover.processEvent(PedalEvent.SWITCH_FLIPPED);
        assert rover.getControlMode() == ControlMode.MOVEMENT : "Should be in Movement mode";
        
        System.out.println("\n-- Test 2.3: Try switching while moving - should fail (Assumption #8) --");
        rover.processEvent(PedalEvent.LEFT_PRESSED);  // Start moving
        rover.processEvent(PedalEvent.SWITCH_FLIPPED); // Should be rejected
        assert rover.getControlMode() == ControlMode.MOVEMENT : "Should still be in Movement mode (Assumption #8)";
        
        System.out.println("\n-- Test 2.4: Try switching from device mode - should fail (Assumption #8) --");
        // First get back to rest and switch to camera/drill
        rover.processEvent(PedalEvent.RIGHT_PRESSED); // Decel
        rover.processEvent(PedalEvent.RIGHT_PRESSED); // Should reach REST
        rover.processEvent(PedalEvent.SWITCH_FLIPPED); // Now in Camera/Drill
        rover.processEvent(PedalEvent.LEFT_HELD_5S);   // Enter Color Camera
        rover.processEvent(PedalEvent.SWITCH_FLIPPED); // Should be rejected
        assert rover.getControlMode() == ControlMode.CAMERA_DRILL : "Should still be in Camera/Drill";
        
        System.out.println("\n✓ Mode Switching tests passed!");
    }
    
    // ========================================================
    // TEST SCENARIO 3: Camera Control
    // ========================================================
    static void testCameraControl(LunarRoverStateMachine rover) {
        System.out.println("\n\n══════════════════════════════════════════════════════");
        System.out.println("TEST 3: CAMERA CONTROL");
        System.out.println("══════════════════════════════════════════════════════");
        
        // Reset and switch to Camera/Drill mode
        rover = new LunarRoverStateMachine();
        rover.addListener(System.out::println);
        rover.processEvent(PedalEvent.SWITCH_FLIPPED);
        
        System.out.println("\n-- Test 3.1: Enter Color Camera mode --");
        rover.processEvent(PedalEvent.LEFT_HELD_5S);
        assert rover.getCameraDrillState() == CameraDrillState.COLOR_CAMERA;
        
        System.out.println("\n-- Test 3.2: Take a picture --");
        rover.processEvent(PedalEvent.LEFT_PRESSED);
        
        System.out.println("\n-- Test 3.3: Activate temporizer --");
        rover.processEvent(PedalEvent.LEFT_HELD_5S);
        assert rover.getColorCameraSubState() == CameraSubState.TEMPORIZER_ACTIVE;
        
        System.out.println("\n-- Test 3.4: Timer expires, take selfie (Assumption #12) --");
        rover.processEvent(PedalEvent.TIMER_EXPIRED);
        assert rover.getColorCameraSubState() == CameraSubState.READY : "Should be back to READY after auto-picture";
        
        System.out.println("\n-- Test 3.5: Return to IDLE --");
        rover.processEvent(PedalEvent.RIGHT_PRESSED);
        assert rover.getCameraDrillState() == CameraDrillState.IDLE;
        
        System.out.println("\n-- Test 3.6: Enter 16mm Camera mode --");
        rover.processEvent(PedalEvent.LEFT_HELD_10S);
        assert rover.getCameraDrillState() == CameraDrillState.CAMERA_16MM;
        
        System.out.println("\n-- Test 3.7: Take picture with 16mm --");
        rover.processEvent(PedalEvent.LEFT_PRESSED);
        
        System.out.println("\n-- Test 3.8: Return to IDLE --");
        rover.processEvent(PedalEvent.RIGHT_PRESSED);
        
        System.out.println("\n✓ Camera Control tests passed!");
    }
    
    // ========================================================
    // TEST SCENARIO 4: Drill Control
    // ========================================================
    static void testDrillControl(LunarRoverStateMachine rover) {
        System.out.println("\n\n══════════════════════════════════════════════════════");
        System.out.println("TEST 4: DRILL CONTROL");
        System.out.println("══════════════════════════════════════════════════════");
        
        // Reset and switch to Camera/Drill mode
        rover = new LunarRoverStateMachine();
        rover.addListener(System.out::println);
        rover.processEvent(PedalEvent.SWITCH_FLIPPED);
        
        System.out.println("\n-- Test 4.1: Enter Drill mode --");
        rover.processEvent(PedalEvent.LEFT_DOUBLE_PRESS);
        assert rover.getCameraDrillState() == CameraDrillState.DRILL;
        assert rover.getDrillSubState() == DrillSubState.OFF;
        
        System.out.println("\n-- Test 4.2: Turn drill ON --");
        rover.processEvent(PedalEvent.LEFT_PRESSED);
        assert rover.getDrillSubState() == DrillSubState.ON;
        
        System.out.println("\n-- Test 4.3: Turn drill OFF --");
        rover.processEvent(PedalEvent.LEFT_PRESSED);
        assert rover.getDrillSubState() == DrillSubState.OFF;
        
        System.out.println("\n-- Test 4.4: Turn ON then exit (auto-disable - Assumption #11) --");
        rover.processEvent(PedalEvent.LEFT_PRESSED);  // Turn ON
        assert rover.getDrillSubState() == DrillSubState.ON : "Drill should be ON";
        rover.processEvent(PedalEvent.RIGHT_PRESSED); // Exit - should auto-disable
        assert rover.getCameraDrillState() == CameraDrillState.IDLE;
        assert rover.getDrillSubState() == DrillSubState.OFF : "Drill should auto-disable (Assumption #11)";
        
        System.out.println("\n✓ Drill Control tests passed!");
    }
    
    // ========================================================
    // TEST SCENARIO 5: Edge Cases
    // ========================================================
    static void testEdgeCases(LunarRoverStateMachine rover) {
        System.out.println("\n\n══════════════════════════════════════════════════════");
        System.out.println("TEST 5: EDGE CASES");
        System.out.println("══════════════════════════════════════════════════════");
        
        // Reset
        rover = new LunarRoverStateMachine();
        rover.addListener(System.out::println);
        
        System.out.println("\n-- Test 5.1: Ignored events in REST --");
        rover.processEvent(PedalEvent.RIGHT_PRESSED);  // Should be ignored
        rover.processEvent(PedalEvent.RIGHT_HELD_3S);  // Should be ignored
        
        System.out.println("\n-- Test 5.2: Accelerate to max speed --");
        for (int i = 0; i < 15; i++) {
            rover.processEvent(PedalEvent.LEFT_PRESSED);
        }
        
        System.out.println("\n-- Test 5.3: Decel then re-accelerate --");
        rover.processEvent(PedalEvent.RIGHT_PRESSED); // DecelFwd
        rover.processEvent(PedalEvent.LEFT_PRESSED);  // Re-accel
        
        System.out.println("\n-- Test 5.4: Decel and engage cruise mid-decel --");
        rover.processEvent(PedalEvent.RIGHT_PRESSED); // DecelFwd
        rover.processEvent(PedalEvent.RIGHT_HELD_3S); // Cruise from decel
        
        System.out.println("\n-- Test 5.5: Assumption #10 - Left pedal from backward decel goes forward --");
        // Get to backward deceleration
        rover.processEvent(PedalEvent.RIGHT_PRESSED); // Decel from cruise
        rover.processEvent(PedalEvent.RIGHT_PRESSED); // Continue to rest
        rover.processEvent(PedalEvent.RIGHT_PRESSED); // Ensure at rest
        rover.processEvent(PedalEvent.LEFT_HELD_3S);  // Accelerate backward
        rover.processEvent(PedalEvent.RIGHT_PRESSED); // Decel backward
        rover.processEvent(PedalEvent.LEFT_PRESSED);  // Should go FORWARD (Assumption #10)
        assert rover.getMovementState() == MovementState.ACCELERATING_FORWARD : "Should accelerate forward";
        
        System.out.println("\n✓ Edge Cases tests passed!");
    }
}
