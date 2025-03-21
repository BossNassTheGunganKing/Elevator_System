package com.yourname.elevator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

/**
 * GUI to simulate elevator movement between floors.
 *
 * @Author: Daniel Godfrey
 */
class ElevatorSimulationWindow extends JPanel implements UpdateElevatorPositionListener{
    private final int carWidth = 40;
    private final int carHeight = 40;
    private int[][] carPositions; // Positions of multiple elevators
    private int[] moveY; // Y coordinate for vertical movement of multiple elevators
    private final int velocity = 10; // Pixels moved per timer tick
    private int[] stoppingPoints; // Positions of stopping points (floors)
    private Timer timer;
    private int numberOfElevators; // Number of elevators

    public ElevatorSimulationWindow(int numberOfStoppingPoints, int numberOfElevators) {
            this.numberOfElevators = numberOfElevators;
            this.carPositions = new int[numberOfElevators][2]; // Initialize positions for each elevator
            this.moveY = new int[numberOfElevators]; // Initialize destination Y for each elevator
            Arrays.fill(this.moveY, -1); // Initialize all destinations to -1

            this.setPreferredSize(new Dimension(200 * numberOfElevators, 800)); // Adjust width based on number of elevators
            this.setBackground(Color.WHITE);
            calculateStoppingPoints(numberOfStoppingPoints);

            for (int i = 0; i < numberOfElevators; i++) {
                carPositions[i][0] = 60 + 200 * i; // Set initial X position for each elevator shaft
                carPositions[i][1] = stoppingPoints[0] - carHeight / 2; // Set initial Y position
            }

            // Timer triggers every 30 ms, update to call moveCar for each elevator
            timer = new Timer(30, e -> {
                for (int i = 0; i < numberOfElevators; i++) {
                    moveCar(i, moveY[i]);
                }
            });
    }

    private void startCarMovement() {
        if (!timer.isRunning()) {
            timer.start();
        }
    }


    /**
     * Move the elevator car to a specified floor.
     *
     * @param destinationY the destination of the elevator car
     */
    private void moveCar(int elevatorIndex, int destinationY) {
        if (destinationY != -1) {
            int carMidPoint = carPositions[elevatorIndex][1] + carHeight / 2;
            if (Math.abs(carMidPoint - destinationY) <= velocity) {
                carPositions[elevatorIndex][1] = destinationY - carHeight / 2;
                moveY[elevatorIndex] = -1; // Reset destination Y after reaching
                if (Arrays.stream(moveY).allMatch(y -> y == -1)) {
                    timer.stop(); // Stop timer if all elevators have reached their destinations
                }
            } else if (carMidPoint < destinationY) {
                carPositions[elevatorIndex][1] += velocity;
            } else if (carMidPoint > destinationY) {
                carPositions[elevatorIndex][1] -= velocity;
            }
            repaint();
        }
    }

    /**
     * Calculate the positions of each floor in the shaft when floors are placed at an equal distance from each other.
     *
     * @param numberOfStoppingPoints the number of floors
     */
    private void calculateStoppingPoints(int numberOfStoppingPoints) {
        stoppingPoints = new int[numberOfStoppingPoints];
        int distance = this.getPreferredSize().height / (numberOfStoppingPoints + 1);
        for (int i = 0; i < numberOfStoppingPoints; i++) {
            stoppingPoints[i] = this.getPreferredSize().height - (i + 1) * distance;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int elevatorIndex = 0; elevatorIndex < numberOfElevators; elevatorIndex++) {
            drawElevatorShaft(g, elevatorIndex);
        }
    }
    private void drawElevatorShaft(Graphics g, int elevatorIndex) {
        g.setColor(Color.BLACK);
        int shaftX = 100 + 200 * elevatorIndex;
        int elevatorStartY = stoppingPoints[stoppingPoints.length - 1] - 5;
        int elevatorEndY = stoppingPoints[0] + 5;
        g.drawLine(shaftX, elevatorStartY, shaftX, elevatorEndY);

        for (int point : stoppingPoints) {
            g.fillOval(shaftX - 5, point - 5, 10, 10);
        }

        g.fillRect(carPositions[elevatorIndex][0], carPositions[elevatorIndex][1], carWidth, carHeight);
    }
    @Override
    public void updateElevatorPosition(int elevatorNumber, int floor) {
        if (elevatorNumber >= 0 && elevatorNumber < numberOfElevators && floor > 0 && floor <= stoppingPoints.length) {
            moveY[elevatorNumber] = stoppingPoints[floor - 1];
            if (!timer.isRunning()) {
                timer.start();
            }
        }
    }
}


