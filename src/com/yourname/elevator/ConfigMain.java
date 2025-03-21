package com.yourname.elevator;

import javax.swing.*;

public class ConfigMain {
    private static int floors;
    private static int numberOfElevators;
    public static void main(String[] args) {
        while (true) {
            String input = JOptionPane.showInputDialog("Enter the number of floors:");
            try {
                int number = Integer.parseInt(input);
                if (number > 0) {
                    floors = number;
                    break;
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter a positive integer.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please enter a valid integer.");
            }
        }
        while (true) {
            String input = JOptionPane.showInputDialog("Enter the number of elevators:");
            try {
                int number = Integer.parseInt(input);
                if (number > 0) {
                    numberOfElevators = number;
                    break;
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter a positive integer.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please enter a valid integer.");
            }
        }

        //FloorMain.main(new String[]{});

        Main.main(new String[]{String.valueOf(floors), String.valueOf(numberOfElevators)});
        ElevatorMain.main(new String[]{String.valueOf(numberOfElevators)});
        //FloorMain.main(new String[]{});
    }
}
