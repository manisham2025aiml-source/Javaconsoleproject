package com.campus.console;

import java.sql.*;
import java.util.Scanner;

public class ResourceService {
    private final Scanner sc;
    public ResourceService(Scanner sc) { this.sc = sc; }

    public void add() throws SQLException {
        String name = required("Resource name: ");
        String type = required("Type (CLASSROOM/LAB/SEMINAR_HALL/PROJECTOR/OTHER): ").toUpperCase();
        String location = required("Location: ");
        int capacity = positiveInt("Capacity: ");
        String sql = "INSERT INTO campus_resources(name,type,location,capacity,available) VALUES(?,?,?,?,TRUE)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name); ps.setString(2, type); ps.setString(3, location); ps.setInt(4, capacity);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) System.out.println("Resource added. ID = " + rs.getLong(1));
            }
        }
    }

    public void view() throws SQLException {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM campus_resources ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\nID | NAME | TYPE | LOCATION | CAPACITY | AVAILABLE");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%d | %s | %s | %s | %d | %s%n",
                        rs.getLong("id"), rs.getString("name"), rs.getString("type"),
                        rs.getString("location"), rs.getInt("capacity"),
                        rs.getBoolean("available") ? "Yes" : "No");
            }
            if (!found) System.out.println("No resources found.");
        }
    }

    public void update() throws SQLException {
        view();
        long id = positiveLong("Resource ID to update: ");
        String name = required("New name: ");
        String type = required("New type: ").toUpperCase();
        String location = required("New location: ");
        int capacity = positiveInt("New capacity: ");
        System.out.print("Available? (Y/N): ");
        boolean available = sc.nextLine().trim().equalsIgnoreCase("Y");
        String sql = "UPDATE campus_resources SET name=?,type=?,location=?,capacity=?,available=? WHERE id=?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name); ps.setString(2, type); ps.setString(3, location);
            ps.setInt(4, capacity); ps.setBoolean(5, available); ps.setLong(6, id);
            System.out.println(ps.executeUpdate() == 1 ? "Resource updated." : "Resource ID not found.");
        }
    }

    public void delete() throws SQLException {
        view();
        long id = positiveLong("Resource ID to delete: ");
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM campus_resources WHERE id=?")) {
            ps.setLong(1, id);
            System.out.println(ps.executeUpdate() == 1 ? "Resource deleted." : "Resource not found.");
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) System.out.println("Cannot delete a resource that has bookings.");
            else throw e;
        }
    }

    private String required(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("This field is required.");
        }
    }
    private int positiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { int n = Integer.parseInt(sc.nextLine().trim()); if (n > 0) return n; }
            catch (NumberFormatException ignored) {}
            System.out.println("Enter a positive number.");
        }
    }
    private long positiveLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { long n = Long.parseLong(sc.nextLine().trim()); if (n > 0) return n; }
            catch (NumberFormatException ignored) {}
            System.out.println("Enter a valid positive ID.");
        }
    }
}
