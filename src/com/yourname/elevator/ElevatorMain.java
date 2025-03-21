package com.yourname.elevator;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Scanner;

public class ElevatorMain {
    private static int floorButtonPressed;
    private static int calledElevator;
    private static ArrayList<Elevator> elevatorList;
    private static int numberOfElevators;
    public static void main(String[] args) {
        numberOfElevators = Integer.parseInt(args[0]);
        elevatorList = new ArrayList<>();

        for (int i = 1; i <= numberOfElevators; i++) {
            int elevatorId = i;
            int port = 5000 + elevatorId; // Assign a unique port for each elevator

            try {
                Elevator elevator = new Elevator(elevatorId, port);
                elevatorList.add(elevator);
                Thread elevatorThread = new Thread(elevator);
                elevatorThread.start();
                System.out.println("Elevator " + elevatorId + " started on port " + port);
            } catch (Exception e) {
                System.err.println("Error starting elevator " + elevatorId);
                e.printStackTrace();
            }
        }

        /*

        Scanner scanner = new Scanner(System.in);
        String elevatorFloorCommand;
        System.out.println("You can send floor command to any elevator by sending command elevator,floor");

        while (true){
            elevatorFloorCommand = scanner.next();
            if(isElevatorFloorCommandValid(elevatorFloorCommand)){

                for (Elevator e: elevatorList) {
                    if (e.getElevatorId() == calledElevator){
                        try {
                            e.buttonPressed(floorButtonPressed);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }

            }else {
                System.out.println("Invalid floor command entered");
            }

        }

         */


    }

    private static boolean isElevatorFloorCommandValid(String command){
        String[] parsedCommand = command.split(",");
        if ((Integer.valueOf(parsedCommand[0]) <= numberOfElevators)){
            calledElevator = Integer.valueOf(parsedCommand[0]);
        }else{
            return false;
        }
        if((Integer.valueOf(parsedCommand[1]) instanceof Integer)){
            floorButtonPressed = (Integer.valueOf(parsedCommand[1]));
        }else {
            return false;
        }

        return true;
    }

}
