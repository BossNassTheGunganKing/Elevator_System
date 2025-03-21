package com.yourname.elevator;

import com.yourname.elevator.states.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Objects;

import static java.lang.Thread.sleep;

public class Elevator implements Runnable{

    private ElevatorStates state;
    private final int id;
    private int currentFloor;
    private DatagramSocket socket;
    private InetAddress schedulerAddress;
    private int schedulerPort = 4445;
    private byte[] buf = new byte[256];
    private int initialFloor;
    private int destinationFloor;
    private String button;
    private int floorButton;
    private boolean elevatorRun;

    public Elevator(int id, int port) throws Exception {
        elevatorRun = true;
        this.id = id;
        this.currentFloor = 1;
        this.state = new ElevatorIdle();
        this.socket = new DatagramSocket(port);
        this.schedulerAddress = InetAddress.getByName("127.0.0.1");
    }

    /*
    private void sendUpdate(String message) throws Exception {
        byte[] buf = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, schedulerAddress, schedulerPort);
        socket.send(packet);
    }

     */

    private void notifyAvailability() throws Exception {
        notifyScheduler("Elevator " + id + " available at floor " + currentFloor);
    }

    public void arrived() throws Exception {
        notifyScheduler("Elevator " + id + " arrived at destination floor " + currentFloor);
    }

    @Override
    public void run() {
        try {
            notifyAvailability(); // send inital msg to scheduler
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (elevatorRun) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength()).trim();

                System.out.println("Elevator " + id + " received message: " + message);

                if(message.startsWith("ERROR-RESPONSE")){
                    handleErrorResponse(message);
                }
                if(message.startsWith("TaskData")){
                    parseTaskData(message);
                }
                else if(message.startsWith("ChangeState")){
                    handleAction(message);
                }

                //displayState();


                if(elevatorRun) {
                    notifyAvailability();
                }
                if(elevatorRun == false){
                    sleep(5000);
                    System.out.println("RESTARTING ELEVATOR");
                    elevatorRun = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleErrorResponse(String message) {
        message = message.split(" ")[1];

        switch (message) {
            case "SHUTDOWN":
                elevatorRun = false;
                System.out.println("ELEVATOR " + id + " Has Ran Into Fatal Error Has Shut Down");
                break;
            case "CLOSEDOOR":
                closeDoors();
                System.out.println("ELEVATOR " + id + " Has Fixed Error and Closed Door");
                break;
            case "OPENDOOR":
                openDoors();
                System.out.println("ELEVATOR " + id + " Has Fixed Error and Opened Door");
                break;
        }

    }

    public void notifyScheduler(String message) throws Exception {
        byte[] buf = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, schedulerAddress, schedulerPort);
        socket.send(packet);
        System.out.println("Elevator " + id + " sent update to Scheduler: " + message);
    }

    public void notifyLoading() throws Exception {
        notifyScheduler("Loading " + id);
    }

    public void notifyUnloading() throws Exception {
        notifyScheduler("Unloading " + id);
    }

    public void readyToCloseDoors() throws Exception {
        notifyScheduler("ReadyToCloseDoors " + id);
    }

    private void handleAction(String message) throws Exception {
        String[] parts = message.split(" ");
        String newState = parts[1];

        switch (newState) {
            case "OPEN_DOOR":
                openDoors();
                break;
            case "CLOSE_DOOR":
                closeDoors();
                break;
            case "LOADING":
                notifyLoading();
                setState(new ElevatorLoadingPassengers());
                break;
            case "MOVING":
                setState(new ElevatorMovingToDestination());
                moveElevator();
                break;
            case "UNLOADING":
                notifyUnloading();
                setState(new ElevatorUnloadingPassengers());
                break;
        }
    }

    private void parseTaskData(String taskDataStr) {
        taskDataStr = taskDataStr.replace("TaskData{", "").replace("}", "").replace("'", "");
        String[] dataParts = taskDataStr.split(", ");
        System.out.println(Arrays.toString(dataParts));
        for (String part : dataParts) {
            String[] keyValue = part.split("=");
            switch (keyValue[0]) {
                case "floor":
                    initialFloor = Integer.parseInt(keyValue[1]);
                    break;
                case "destinationFloor":
                    destinationFloor = Integer.parseInt(keyValue[1]);
                    break;
                case "button":
                    button = keyValue[1];
                    break;
                case "error":
                    handleError(keyValue[1]);
                    break;
            }
        }
    }

    private void handleError(String error) {
        if (error.equals("G")) {
            return;
        }
        try {
            notifyScheduler("ERROR " + error);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Represents event telling elevator to close doors
     */
    public void closeDoors() {
        state.closeDoors(this);
    }

    /**
     * Represents event telling elevator to open doors
     */
    public void openDoors() {
        state.openDoors(this);
    }

    /**
     * Represents event elevator button pressed
     */
    public void buttonPressed(int floor) throws Exception {
        floorButton = floor;
        System.out.println("Internal Elevator Floor Command: Elevator " + id + " will move to " + floor);
        notifyScheduler("ElevatorFloorButton " + id + " " +floorButton);
        state.buttonPressed(this);
    }

    /**
     * Displays current elevator state
     */
    public void displayState() {
        state.displayState(this);
    }

    /**
     * Event where elevator told to move to floor
     */
    public void moveElevator() {
        state.moveElevator(this);
    }

    public int getElevatorId() {
        return id;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public int getInitialFloor() {
        return initialFloor;
    }

    public void setState(ElevatorStates state) {
        this.state = state;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public ElevatorStates getState(){ return state; }
    public void close(){
        socket.close();
    }
}
