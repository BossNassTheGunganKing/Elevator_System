package com.yourname.elevator;

import javax.swing.*;
import java.awt.*;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main extends JFrame{
    private static int floors;
    private static int numberOfElevators;
    private static ElevatorSimulationWindow elevatorWindow;
    private static FloorButtonPanel floorPanel;
    private static CarButtonPanel carPanel;
    public static void main(String[] args) {
        floors = Integer.parseInt(args[0]);
        numberOfElevators = Integer.parseInt(args[1]);

        SwingUtilities.invokeLater(() -> {
            JFrame frame= new JFrame("Elevator Simulation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new FlowLayout());

            elevatorWindow = new ElevatorSimulationWindow(floors, numberOfElevators);
            elevatorWindow.setOpaque(true);
            frame.setContentPane(elevatorWindow);

            frame.pack();
            frame.setVisible(true);

            try {
                floorPanel = new FloorButtonPanel(floors);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
            floorPanel.setVisible(true);

            carPanel = new CarButtonPanel(floors, numberOfElevators);
            carPanel.setVisible(true);
        });



        try {
            Scheduler scheduler = new Scheduler();
            Thread schedulerThread = new Thread(scheduler);
            schedulerThread.start();

            Clock clock = new Clock();
            clock.addSubscriber(scheduler);
            Thread clockThread = new Thread(clock);
            clockThread.start();


            SwingUtilities.invokeLater(() -> {
                carPanel.addPositionUpdateListener(elevatorWindow);
                floorPanel.addPositionUpdateListener(elevatorWindow);
                scheduler.addPositionUpdateListener(elevatorWindow);
                //scheduler.addPositionUpdateListener(elevatorWindow);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
