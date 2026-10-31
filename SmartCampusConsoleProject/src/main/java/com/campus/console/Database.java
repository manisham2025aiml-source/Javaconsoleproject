package com.campus.console;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/smart_campus_db?createDatabaseIfNotExist=true";
    private static final String USER = "root";
    // Replace CHANGE_ME with your MySQL password.
    private static final String PASSWORD = "Manisha@123";

    private Database() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initialize() throws SQLException {
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS campus_resources (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(120) NOT NULL, " +
                    "type VARCHAR(60) NOT NULL, location VARCHAR(150), capacity INT NOT NULL, " +
                    "available BOOLEAN NOT NULL DEFAULT TRUE)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS bookings (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, requester_name VARCHAR(120) NOT NULL, " +
                    "requester_email VARCHAR(150) NOT NULL, resource_id BIGINT NOT NULL, " +
                    "booking_date DATE NOT NULL, start_time TIME NOT NULL, end_time TIME NOT NULL, " +
                    "purpose VARCHAR(255) NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'PENDING', " +
                    "CONSTRAINT fk_booking_resource FOREIGN KEY(resource_id) REFERENCES campus_resources(id))");
        }
    }
}
