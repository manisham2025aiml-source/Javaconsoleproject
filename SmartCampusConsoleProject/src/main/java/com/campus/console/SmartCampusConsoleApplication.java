package com.campus.console;

import java.sql.SQLException;
import java.util.Scanner;

public class SmartCampusConsoleApplication {
    public static void main(String[] args) {
        try {
            Database.initialize();
            System.out.println("Connected to MySQL. Database tables are ready.");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            System.out.println("Check MySQL is running and update the password in Database.java.");
            return;
        }

        try (Scanner sc = new Scanner(System.in)) {
            ResourceService resources = new ResourceService(sc);
            BookingService bookings = new BookingService(sc);
            while (true) {
                System.out.println("\n==============================================");
                System.out.println("   SMART CAMPUS RESOURCE BOOKING SYSTEM");
                System.out.println("==============================================");
                System.out.println("1. Add resource");
                System.out.println("2. View resources");
                System.out.println("3. Update resource");
                System.out.println("4. Delete resource");
                System.out.println("5. Create booking");
                System.out.println("6. View bookings");
                System.out.println("7. Search bookings by date");
                System.out.println("8. Update booking status");
                System.out.println("0. Exit");
                System.out.print("Enter choice: ");
                String choice = sc.nextLine().trim();
                try {
                    switch (choice) {
                        case "1" -> resources.add();
                        case "2" -> resources.view();
                        case "3" -> resources.update();
                        case "4" -> resources.delete();
                        case "5" -> bookings.create();
                        case "6" -> bookings.view(null);
                        case "7" -> bookings.view(bookings.readDate("Enter date (YYYY-MM-DD): "));
                        case "8" -> bookings.updateStatus();
                        case "0" -> { System.out.println("Thank you!"); return; }
                        default -> System.out.println("Invalid choice.");
                    }
                } catch (Exception e) {
                    System.out.println("Operation failed: " + e.getMessage());
                }
            }
        }
    }
}
