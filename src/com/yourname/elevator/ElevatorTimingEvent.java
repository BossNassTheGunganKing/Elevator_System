package com.yourname.elevator;

import java.net.InetAddress;
import java.util.List;

public class ElevatorTimingEvent {

    private final InetAddress elevatorAddress;
    private final int elevatorId;
    private int currentTime;
    private int TimeNeededToMeet = 5;
    private List<Integer> previousTimings;

    public ElevatorTimingEvent(InetAddress elevatorAddress, int elevatorId, int currentTime){
        this.elevatorAddress = elevatorAddress;
        this.elevatorId = elevatorId;
        this.currentTime = currentTime;
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
    }

    public InetAddress getElevatorAddress() {
        return elevatorAddress;
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public int getTimeNeededToMeet() {
        return TimeNeededToMeet;
    }

    public void setTimeNeededToMeet(int timeNeededToMeet) {
        TimeNeededToMeet = timeNeededToMeet;
    }

    @Override
    public String toString() {
        return "ElevatorTimingEvent{" +
                "elevatorAddress=" + elevatorAddress +
                ", elevatorId=" + elevatorId +
                ", currentTime=" + currentTime +
                ", TimeNeededToMeet=" + TimeNeededToMeet +
                ", previousTimings=" + previousTimings +
                '}';
    }
}
