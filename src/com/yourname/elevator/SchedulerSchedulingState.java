package com.yourname.elevator;

public class SchedulerSchedulingState implements SchedulerState{

    @Override
    public void handleEvent(Scheduler context) throws Exception {
        System.out.println("Assigning tasks _____");
        context.assignTasksIfPossible();
        context.setState(new SchedulerIdleState());
    }
}
