package model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database manager, interacts with DroneMonitorApp and AnomalyRecord
 */
public class DatabaseManager {
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:anomalies.db";

    /**
     * Constructor
     */
    public DatabaseManager() {
        connect();
        createTableIfNotExists();
    }

    /**
     * Establish connection to SQLite database
     */
    public void connect() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Connected to SQLite database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create anomalies table if it doesn't exist
     */
    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS anomalies (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                drone_id TEXT NOT NULL,
                type TEXT NOT NULL,
                details TEXT,
                timestamp TEXT NOT NULL
            );
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save an anomaly record to the database
     */
    public void saveAnomaly(AnomalyRecord record) {
        String sql = "INSERT INTO anomalies (drone_id, timestamp, type, details) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, record.getDroneId());
            pstmt.setString(2, record.getType());
            pstmt.setString(3, record.getDetails());
            pstmt.setString(4, record.getTimestamp().toString());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all anomalies
     */
    public List<AnomalyRecord> getAllAnomalies() {
        String sql = "SELECT * FROM anomalies";
        List<AnomalyRecord> list = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(extractRecord(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Get anomalies by drone ID
     */
    public List<AnomalyRecord> getAnomaliesByDrone(String droneId) {
        String sql = "SELECT * FROM anomalies WHERE drone_id = ?";
        List<AnomalyRecord> list = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, droneId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(extractRecord(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Get anomalies by type
     */
    public List<AnomalyRecord> getAnomaliesByType(String type) {
        String sql = "SELECT * FROM anomalies WHERE type = ?";
        List<AnomalyRecord> list = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, type);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(extractRecord(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Get anomalies within a date range
     */
    public List<AnomalyRecord> getAnomaliesByDateRange(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT * FROM anomalies WHERE timestamp BETWEEN ? AND ?";
        List<AnomalyRecord> list = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, start.toString());
            pstmt.setString(2, end.toString());

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(extractRecord(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Helper method to convert ResultSet row into AnomalyRecord
     */
    private AnomalyRecord extractRecord(ResultSet rs) throws SQLException {
        String droneId = rs.getString("drone_id");
        String type = rs.getString("type");
        String details = rs.getString("details");
        LocalDateTime timestamp = LocalDateTime.parse(rs.getString("timestamp"));

        return new AnomalyRecord(droneId, type, details);
    }
}
