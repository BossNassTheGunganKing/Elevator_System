package com.yourname.elevator;

public class SchedulerIdleState implements SchedulerState{

    @Override
    public void handleEvent(Scheduler context) throws Exception {
        context.waitForEvents();
        if (!context.getReadyTasks().isEmpty() && !context.getAvailableElevators().isEmpty()) {
            context.setState(new SchedulerSchedulingState());
        }
    }
}
