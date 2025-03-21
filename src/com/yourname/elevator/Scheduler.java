package com.yourname.elevator;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class Scheduler implements Runnable {
    private DatagramSocket socket;
    private SchedulerState state;
    private List<TaskData> readyTasks;
    private int time;
    private List<TaskData> addedTasks;
    private List<TaskData> incomingTasks;
    private List<ElevatorTimingEvent> timingEvents;
    private List<ElevatorTimingEvent> inActiveTimingEvents;
    private final Map<Integer, ElevatorStatus> availableElevators = new HashMap<>();
    private byte[] buf = new byte[256];
    private final int floorPort = 6665;
    private final int startTime;
    private List<UpdateElevatorPositionListener> listeners = new ArrayList<>();

    public Scheduler() throws Exception {
        this.state = new SchedulerIdleState();
        this.socket = new DatagramSocket(4445);
        this.readyTasks = new ArrayList<TaskData>();
        this.incomingTasks = new ArrayList<TaskData>();
        this.addedTasks = new ArrayList<TaskData>();
        List normalList = new ArrayList<>();
        this.timingEvents = Collections.synchronizedList(normalList);
        this.inActiveTimingEvents = new ArrayList<>();
        this.startTime = (int) (new Date().getTime()/1000);
        byte[] buf = "Connected".getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("127.0.0.1"), floorPort);
        socket.send(packet);
        System.out.println("Connected To Floor");

    }

    @Override
    public void run() {
        System.out.println("Scheduler started.");
        /*
        for (TaskData task : tasks) {
            System.out.println(task);
        }
        /*
         */
        while (true) {
            try {
                state.handleEvent(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void makeTaskAvailable(){
        if(!incomingTasks.isEmpty()){ // if there exists an incoming task
            for (TaskData task : incomingTasks){ // for each task in incomingTasks list
                if((task.getTimeInt() <= ((int) (new Date().getTime()/1000)) - startTime) && !addedTasks.contains(task)){
                    System.out.println(task + " has been made available");
                    readyTasks.add(task);
                    addedTasks.add(task);
                }
            }
        }
    }

    protected void waitForEvents() throws Exception {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String received = new String(packet.getData(), 0, packet.getLength()).trim();
        System.out.println(received);

        if(received.contains("ERROR")){
            String[] parts = received.split(" ");
            handleError(parts[1], packet.getPort(), packet.getAddress());
        }

        if (received.contains("available")) {
            processElevatorAvailability(received, packet);
            String[] parts = received.split(" ");
            if (parts.length > 1){
                try {
                    int elevatorId = Integer.parseInt(parts[1]);
                    System.out.println("Elevator ID: " + elevatorId);


                } catch (NumberFormatException e) {
                    System.err.println("Error parsing Elevator ID from message: " + received);
                }
            }
        }
        if (received.contains("arrived")){                                  // this should make car/floor button unlit
            moveTimingEvent(packet.getPort());
            String[] parts = received.split(" ");
            if (parts.length > 1) {
                try {
                    int elevatorId = Integer.parseInt(parts[1]);

                    sendActionCommand(elevatorId, "OPEN_DOOR");
                    sendActionCommand(elevatorId, "LOADING");
                    sendActionCommand(elevatorId, "CLOSE_DOOR");
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing Elevator ID from message: " + received);
                }
            }
        }
        if (received.contains("Data")) {
            addTask(received);
        }
        if (received.contains("ElevatorFloorButton")){
            processElevatorFloorButtonCall(received, packet);
        }
        if(received.contains("ReadyToCloseDoors")){
            handleReadyToCloseDoors(received);
        }
        if(received.contains("Loading")){
            String[] parts = received.split(" ");
            int elevatorId = Integer.parseInt(parts[1]);
            ElevatorStatus elevatorToLoad = availableElevators.get(elevatorId);
            elevatorToLoad.addLoad();
            System.out.println("Current Load is " + elevatorToLoad.getLoad());
        }
        if(received.contains("Unloading")){
            String[] parts = received.split(" ");
            int elevatorId = Integer.parseInt(parts[1]);
            ElevatorStatus elevatorToUnload = availableElevators.get(elevatorId);
            elevatorToUnload.removeLoad();
            System.out.println("Current Load is " + elevatorToUnload.getLoad());
        }
        if(received.contains("FloorButtonPanel")){
            String[] parts = received.split(" ");
            String button = parts[1];
            int destinationFloor = Integer.parseInt(parts[2]);
            int time = (int) (new Date().getTime()/1000) - startTime;
            ElevatorStatus chosenElevator = findNearestAvailableElevator(destinationFloor);
            String receivedString = "FloorButtonPanel, " + time + ", " + chosenElevator.getFloor() + ", " + button + ", " + destinationFloor + ", " + "G";
            addTask(receivedString);
        }
    }

    private void handleError(String errorMessage, int port, InetAddress address) {
        switch (errorMessage){
            case "TimeMiss":
                try {
                    sendTask("ERROR-RESPONSE SHUTDOWN", address, port);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            case "DoorStuckOpen":
                try {
                    sendTask("ERROR-RESPONSE CLOSEDOOR", address, port);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            case "DoorStuckClose":
                try {
                    sendTask("ERROR-RESPONSE OPENDOOR", address, port);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
        }
    }

    private void handleReadyToCloseDoors(String received) {
        String[] parts = received.split(" ");
        int elevatorId = Integer.parseInt(parts[1]);
        try {
            sendActionCommand(elevatorId, received);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processElevatorFloorButtonCall(String received, DatagramPacket packet) {
        String[] parts = received.split(" ");
        int elevatorId = Integer.parseInt(parts[1]);
        int elevatorFloorButton = Integer.parseInt(parts[2]);
        availableElevators.put(elevatorId, new ElevatorStatus(elevatorId, packet.getAddress(), packet.getPort(), elevatorFloorButton));
    }

    private void processElevatorAvailability(String received, DatagramPacket packet) {
        String[] parts = received.split(" ");
        int elevatorId = Integer.parseInt(parts[1]);
        int elevatorFloor = Integer.parseInt(parts[5]);
        availableElevators.put(elevatorId, new ElevatorStatus(elevatorId, packet.getAddress(), packet.getPort(), elevatorFloor));
    }

    /** adds a new task to the tasks arraylist
     *
     * @param received, the new task to be added to the tasks arraylist
     */
    private void addTask(String received) {
        String[] taskString = received.split(",");

        int time = Integer.parseInt(taskString[1].trim());
        int initialFloor = Integer.parseInt(taskString[2].trim());
        String button = taskString[3].trim();
        int destinationFloor = Integer.parseInt(taskString[4].trim());
        String error = taskString[5].trim();

        TaskData newTask = new TaskData(time,initialFloor,button,destinationFloor, error);

        incomingTasks.add(newTask);
    }

    public void moveTimingEvent(int port) {
        for (int i = 0; i < timingEvents.size(); i++) {
            ElevatorTimingEvent timingEvent = timingEvents.get(i);
            if ((port - 5000) == timingEvent.getElevatorId()) {
                timingEvent.setCurrentTime(time);
                inActiveTimingEvents.add(timingEvent);
                timingEvents.remove(i);
                // Since we removed an element, decrement the index to avoid skipping the next element
                i--;
            }
        }
    }

    private void updateTimingEvent(int port, InetAddress address) {
        boolean newEventRequired = true;

        // Iterate over timingEvents
        for (int i = 0; i < timingEvents.size(); i++) {
            ElevatorTimingEvent timingEvent = timingEvents.get(i);
            if ((port - 5000) == timingEvent.getElevatorId()) {
                newEventRequired = false;
                // Update current time if elevator id matches
                timingEvent.setCurrentTime(time);
            }
        }

        // Iterate over inActiveTimingEvents
        for (int i = 0; i < inActiveTimingEvents.size(); i++) {
            ElevatorTimingEvent timingEvent = inActiveTimingEvents.get(i);
            if ((port - 5000) == timingEvent.getElevatorId()) {
                newEventRequired = false;
                // Update current time if elevator id matches
                timingEvent.setCurrentTime(time);
                // Move the event back to timingEvents list
                timingEvents.add(timingEvent);
                inActiveTimingEvents.remove(i);
                // Since we removed an element, decrement the index to avoid skipping the next element
                i--;
            }
        }

        if (newEventRequired) {
            timingEvents.add(new ElevatorTimingEvent(address, (port - 5000), time));
        }
    }


    private void sendTask(String task, InetAddress address, int port) throws Exception {
        byte[] buf = task.getBytes();
        System.out.println("SENDING " + task);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        updateTimingEvent(port, address);
        System.out.println(timingEvents);
        socket.send(packet);
    }

    protected void assignTasksIfPossible() {
        while (!readyTasks.isEmpty() && !availableElevators.isEmpty()) {
            TaskData task = readyTasks.remove(0);
            ElevatorStatus nearestElevator = findNearestAvailableElevator(task.getInitialFloor());
            if (nearestElevator != null && nearestElevator.getLoad() < nearestElevator.getCapacity()) {
                try {
                    System.out.println("___Assigning task " + task + " to elevator " + nearestElevator + "___");
                    sendTask(task.toString(), nearestElevator.getAddress(), nearestElevator.getPort());
                    notifyPositionUpdateListeners(nearestElevator.getId()-1, task.getDestinationFloor()); //update simulation window
                    sendActionCommand(nearestElevator.getId(), "MOVING");
                    if(nearestElevator.getLoad() >= nearestElevator.getCapacity()){
                        availableElevators.remove(nearestElevator.getId());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        this.setState(new SchedulerIdleState());
    }

    protected void sendActionCommand(int elevatorId, String newState) throws Exception {
        ElevatorStatus elevator = availableElevators.get(elevatorId);
        if (elevator != null) {
            String command = "ChangeState " + newState;
            byte[] buf = command.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, elevator.getAddress(), elevator.getPort());
            socket.send(packet);
            System.out.println("Sent state change command to Elevator " + elevatorId + ": " + newState);
        }
    }

    private ElevatorStatus findNearestAvailableElevator(int floor) {
        ElevatorStatus nearestElevator = null;
        int minDistance = Integer.MAX_VALUE;
        for (ElevatorStatus elevator : availableElevators.values()) {
            int distance = Math.abs(elevator.getFloor() - floor);
            if (distance < minDistance) {
                minDistance = distance;
                nearestElevator = elevator;
            }
        }
        return nearestElevator;
    }

    public SchedulerState getState() {
        return state;
    }

    public void clockUpdate(int time) {
        makeTaskAvailable();
        this.time = time;

        for (int i = 0; i < timingEvents.size(); i++) {
            ElevatorTimingEvent timingEvent = timingEvents.get(i);

            if (timingEvent.getCurrentTime() > (timingEvent.getTimeNeededToMeet() + time)) {
                handleError("TimeMiss", timingEvent.getElevatorId() + 5000, timingEvent.getElevatorAddress());
            }

            inActiveTimingEvents.add(timingEvent);
            timingEvents.remove(timingEvent);

        }

    }

    public void close() {
        socket.close();
    }

    public void addPositionUpdateListener(UpdateElevatorPositionListener listener) {
        listeners.add(listener);
    }

    private void notifyPositionUpdateListeners(int selectedCar, int floorNumber) {
        for (UpdateElevatorPositionListener listener : listeners) {
            listener.updateElevatorPosition(selectedCar, floorNumber);
        }
    }

    private static class ElevatorStatus {
        private final int id;
        private final InetAddress address;
        private final int port;
        private final int floor;
        private int load;
        private final int capacity = 5;

        public ElevatorStatus(int id, InetAddress address, int port, int floor) {
            this.id = id;
            this.address = address;
            this.port = port;
            this.floor = floor;
            load = 0;
        }

        public int getId() { return id; }
        public InetAddress getAddress() { return address; }
        public int getPort() { return port; }
        public int getFloor() { return floor; }
        public int getLoad() { return load; }
        public int getCapacity() { return capacity; }
        public void addLoad() { load += 1;
            System.out.println("Load increased by 1");}
        public void removeLoad() { load -= 1;
            System.out.println("Load decreased by 1");}
    }

    public void setState(SchedulerState schedulingState) {
        state = schedulingState;
    }
    public List<TaskData> getReadyTasks(){
        return readyTasks;
    }
    public Map<Integer, ElevatorStatus> getAvailableElevators(){
        return availableElevators;
    }
}