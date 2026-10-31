package com.campus.console;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class BookingService {
    private final Scanner sc;
    public BookingService(Scanner sc) { this.sc = sc; }

    public void create() throws SQLException {
        long resourceId = positiveLong("Resource ID: ");
        String name = required("Student/staff name: ");
        String email = required("Email: ");
        LocalDate date = readDate("Booking date (YYYY-MM-DD): ");
        LocalTime start = readTime("Start time (HH:MM): ");
        LocalTime end = readTime("End time (HH:MM): ");
        if (!end.isAfter(start)) { System.out.println("End time must be after start time."); return; }
        if (date.isBefore(LocalDate.now())) { System.out.println("Date cannot be in the past."); return; }
        String purpose = required("Purpose: ");

        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);
            try {
                boolean available;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT available FROM campus_resources WHERE id=? FOR UPDATE")) {
                    ps.setLong(1, resourceId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { System.out.println("Resource not found."); con.rollback(); return; }
                        available = rs.getBoolean(1);
                    }
                }
                if (!available) { System.out.println("Resource is unavailable."); con.rollback(); return; }

                String conflict = "SELECT COUNT(*) FROM bookings WHERE resource_id=? AND booking_date=? " +
                        "AND status NOT IN ('REJECTED','CANCELLED') AND start_time < ? AND end_time > ?";
                try (PreparedStatement ps = con.prepareStatement(conflict)) {
                    ps.setLong(1, resourceId); ps.setDate(2, Date.valueOf(date));
                    ps.setTime(3, Time.valueOf(end)); ps.setTime(4, Time.valueOf(start));
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            System.out.println("Booking conflict: this resource has an overlapping booking request.");
                            con.rollback(); return;
                        }
                    }
                }

                String insert = "INSERT INTO bookings(requester_name,requester_email,resource_id," +
                        "booking_date,start_time,end_time,purpose,status) VALUES(?,?,?,?,?,?,?,'PENDING')";
                try (PreparedStatement ps = con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name); ps.setString(2, email); ps.setLong(3, resourceId);
                    ps.setDate(4, Date.valueOf(date)); ps.setTime(5, Time.valueOf(start));
                    ps.setTime(6, Time.valueOf(end)); ps.setString(7, purpose);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) System.out.println("Booking request saved. Booking ID = " + rs.getLong(1));
                    }
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback(); throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public void view(LocalDate date) throws SQLException {
        String sql = "SELECT b.*,r.name AS resource_name FROM bookings b " +
                "JOIN campus_resources r ON r.id=b.resource_id " +
                (date == null ? "" : "WHERE b.booking_date=? ") +
                "ORDER BY b.booking_date,b.start_time";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (date != null) ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\nID | REQUESTER | RESOURCE | DATE | START | END | STATUS | PURPOSE");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%d | %s | %s | %s | %s | %s | %s | %s%n",
                            rs.getLong("id"), rs.getString("requester_name"), rs.getString("resource_name"),
                            rs.getDate("booking_date"), rs.getTime("start_time"), rs.getTime("end_time"),
                            rs.getString("status"), rs.getString("purpose"));
                }
                if (!found) System.out.println("No bookings found.");
            }
        }
    }

    public void updateStatus() throws SQLException {
        view(null);
        long id = positiveLong("Booking ID: ");
        String status = required("New status (APPROVED/REJECTED/CANCELLED): ").toUpperCase();
        if (!status.equals("APPROVED") && !status.equals("REJECTED") && !status.equals("CANCELLED")) {
            System.out.println("Invalid status."); return;
        }
        try (Connection con = Database.getConnection()) {
            if (status.equals("APPROVED")) {
                String check = "SELECT COUNT(*) FROM bookings a JOIN bookings b " +
                        "ON a.resource_id=b.resource_id AND a.booking_date=b.booking_date " +
                        "AND a.id<>b.id AND a.status='APPROVED' " +
                        "AND a.start_time < b.end_time AND a.end_time > b.start_time WHERE b.id=?";
                try (PreparedStatement ps = con.prepareStatement(check)) {
                    ps.setLong(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) { System.out.println("Cannot approve: an approved booking overlaps."); return; }
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE bookings SET status=? WHERE id=?")) {
                ps.setString(1, status); ps.setLong(2, id);
                System.out.println(ps.executeUpdate() == 1 ? "Booking status updated." : "Booking not found.");
            }
        }
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return LocalDate.parse(sc.nextLine().trim()); }
            catch (DateTimeParseException e) { System.out.println("Use YYYY-MM-DD format."); }
        }
    }
    private LocalTime readTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return LocalTime.parse(sc.nextLine().trim()); }
            catch (DateTimeParseException e) { System.out.println("Use HH:MM format, e.g. 09:30."); }
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
    private long positiveLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { long n = Long.parseLong(sc.nextLine().trim()); if (n > 0) return n; }
            catch (NumberFormatException ignored) {}
            System.out.println("Enter a valid positive ID.");
        }
    }
}
