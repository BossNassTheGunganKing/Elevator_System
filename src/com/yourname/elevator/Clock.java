package com.yourname.elevator;

import java.io.IOException;
import java.net.*;
import java.util.*;

import static java.lang.Thread.sleep;

public class Clock implements Runnable{
    private int time;
    private List<Scheduler> subscribers;
    public Clock(){
        time = 0;
        List regList = new ArrayList();
        subscribers = Collections.synchronizedList(regList);
    }

    public void addSubscriber(Scheduler sub){
        subscribers.add(sub);
    }

    private void sendClockUpdate(int time){
        for (Scheduler scheduler: subscribers){
            scheduler.clockUpdate(time);
        }
    }

    @Override
    public void run() {
        while (true){
            try {
                sleep(1000);
                time++;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            sendClockUpdate(time);
        }


    }
}
