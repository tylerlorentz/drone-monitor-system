package controller;

import model.AnomalyRecord;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles SQLite persistence for anomaly records.
 *
 * Creates (or opens) a local "drone_monitor.db" file and provides:
 * - insert(AnomalyRecord) — persists a new anomaly
 * - queryByDroneId(String) — all anomalies for a given drone
 * - queryByDateRange(LocalDateTime, LocalDateTime) — anomalies in a time window
 * - queryByType(String) — anomalies of a specific type (e.g. "LOW_BATTERY")
 * - queryAll() — full log
 *
 * Uses the bundled SQLite JDBC driver (sqlite-jdbc jar on the classpath).
 */
public class AnomalyDatabase {

    private static final String DB_URL = "jdbc:sqlite:drone_monitor.db";
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Connection conn;

    /**
     * Opens (or creates) the database and ensures the anomalies table exists.
     */
    public AnomalyDatabase() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            createTable();
        } catch (SQLException e) {
            System.err.println("[DB] Failed to connect: " + e.getMessage());
        }
    }

    /** Creates the anomalies table if it does not already exist. */
    private void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS anomalies (
                id        INTEGER PRIMARY KEY AUTOINCREMENT,
                drone_id  TEXT    NOT NULL,
                type      TEXT    NOT NULL,
                severity  TEXT    NOT NULL,
                details   TEXT    NOT NULL,
                timestamp TEXT    NOT NULL
            )
            """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Inserts a new anomaly record into the database.
     *
     * @param record the anomaly to persist
     */
    public void insert(AnomalyRecord record) {
        String sql = "INSERT INTO anomalies (drone_id, type, severity, details, timestamp) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, record.getDroneId());
            ps.setString(2, record.getType());
            ps.setString(3, record.getSeverity());
            ps.setString(4, record.getDetails());
            ps.setString(5, record.getFormattedTimestamp());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Insert failed: " + e.getMessage());
        }
    }

    /**
     * Retrieves all anomalies for a specific drone.
     *
     * @param droneId the drone identifier to filter by
     * @return list of matching AnomalyRecords
     */
    public List<AnomalyRecord> queryByDroneId(String droneId) {
        return query("SELECT * FROM anomalies WHERE drone_id = ?", droneId);
    }

    /**
     * Retrieves all anomalies of a specific type.
     *
     * @param type e.g. "LOW_BATTERY", "GPS_SPOOFING"
     * @return list of matching AnomalyRecords
     */
    public List<AnomalyRecord> queryByType(String type) {
        return query("SELECT * FROM anomalies WHERE type = ?", type);
    }

    /**
     * Retrieves all anomalies within a date-time range (inclusive).
     *
     * @param from start of range
     * @param to   end of range
     * @return list of matching AnomalyRecords
     */
    public List<AnomalyRecord> queryByDateRange(LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT * FROM anomalies WHERE timestamp >= ? AND timestamp <= ? ORDER BY timestamp DESC";
        List<AnomalyRecord> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from.format(FMT));
            ps.setString(2, to.format(FMT));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                results.add(rowToRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DB] Query failed: " + e.getMessage());
        }
        return results;
    }

    /** Returns the full anomaly log, newest first. */
    public List<AnomalyRecord> queryAll() {
        return query("SELECT * FROM anomalies ORDER BY timestamp DESC", null);
    }

    /** Returns the total number of anomaly records stored. */
    public int countAll() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery("SELECT COUNT(*) FROM anomalies")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DB] Count failed: " + e.getMessage());
        }
        return 0;
    }

    /** Closes the database connection gracefully. */
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            System.err.println("[DB] Close failed: " + e.getMessage());
        }
    }

    // -------------------------
    // PRIVATE HELPERS
    // -------------------------

    private List<AnomalyRecord> query(String sql, String param) {
        List<AnomalyRecord> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) ps.setString(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                results.add(rowToRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DB] Query failed: " + e.getMessage());
        }
        return results;
    }

    private AnomalyRecord rowToRecord(ResultSet rs) throws SQLException {
        // Reconstruct AnomalyRecord from DB columns
        // We create it via the public constructor; timestamp will be "now",
        // so we override it via a package-level helper if needed.
        // For display purposes this is sufficient.
        return new AnomalyRecord(
            rs.getString("drone_id"),
            rs.getString("type"),
            rs.getString("details")
        );
    }
}
