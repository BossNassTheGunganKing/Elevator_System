package com.yourname.elevator.states;

import com.yourname.elevator.Elevator;

public class ElevatorLoadingPassengers implements ElevatorStates{
    /**
     * Represents event telling elevator to close doors
     *
     * @param context represents the elevator
     */
    @Override
    public void closeDoors(Elevator context) {
        System.out.println("Elevator doors cannot be closed in Loading state");
    }

    /**
     * Represents event telling elevator to open doors
     *
     * @param context represents the elevator
     */
    @Override
    public void openDoors(Elevator context) {
        System.out.println("Elevator " + context.getElevatorId() + " is opening doors and loading passengers at floor " + context.getCurrentFloor());
        // 3 second load time
        try {
            Thread.sleep(3000);  //
            context.readyToCloseDoors();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        context.setState(new ElevatorMovingToDestination());
    }

    /**
     * Represents event elevator button pressed
     *
     * @param context represents the elevator
     */
    @Override
    public void buttonPressed(Elevator context) {

    }

    /**
     * Displays current elevator state
     *
     * @param context represents the elevator
     * @return
     */
    @Override
    public void displayState(Elevator context) {
        System.out.println("LOAD PASS");
    }

    /**
     * Event where elevator told to move to floor
     *
     * @param context represents elevator
     */
    @Override
    public void moveElevator(Elevator context) {
        System.out.println("Elevator cannot move while in Loading state.");
    }
}
