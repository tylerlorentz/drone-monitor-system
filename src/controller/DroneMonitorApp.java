package controller;

import model.Drone;
import model.AnomalyRecord;
import view.Dashboard;

import java.util.*;
import java.sql.*;
import java.io.PrintWriter;

public class DroneMonitorApp {

    private static Connection conn;

    public static void main(String[] args) throws InterruptedException {

        initDatabase();

        List<Drone> drones = new ArrayList<>();
        drones.add(new Drone("D1", 47.60, -122.33, 50, 100, 10));
        drones.add(new Drone("D2", 47.61, -122.34, 60, 100, 10));
        drones.add(new Drone("D3", 47.62, -122.35, 55, 100, 10));

        TelemetryGenerator generator = new TelemetryGenerator();
        AnomalyDetector detector = new AnomalyDetector();
        Dashboard view = new Dashboard();

        javax.swing.SwingUtilities.invokeLater(() -> view.setVisible(true));

        int cycle = 0;

        while (true) {
            cycle++;

            generator.updateDrones(drones);

            List<AnomalyRecord> anomalies = detector.detect(drones);

            for (AnomalyRecord a : anomalies) {
                System.out.println(a);
                saveAnomaly(a);
            }

            javax.swing.SwingUtilities.invokeLater(() -> {
                view.display(drones, anomalies);
                view.updateTelemetry(drones.get(0)); // simple default focus
            });

            System.out.println("\n====================================");
            System.out.println(" DRONE FLEET STATUS | CYCLE " + cycle);
            System.out.println("====================================");

            for (Drone d : drones) {
                System.out.printf(
                        "%s | LAT: %.5f | LON: %.5f | ALT: %.2f | BAT: %.1f%% | VEL: %.5f%n",
                        d.getId(),
                        d.getLatitude(),
                        d.getLongitude(),
                        d.getAltitude(),
                        d.getBattery(),
                        d.getVelocity()
                );
            }

            Thread.sleep(2000);
        }
    }

    private static void initDatabase() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:anomalies.db");

            String sql = """
                CREATE TABLE IF NOT EXISTS anomalies (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    droneId TEXT,
                    type TEXT,
                    severity TEXT,
                    details TEXT,
                    timestamp TEXT
                )
            """;

            conn.createStatement().execute(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void saveAnomaly(AnomalyRecord a) {
        String sql = """
            INSERT INTO anomalies (droneId, type, severity, details, timestamp)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, a.getDroneId());
            stmt.setString(2, a.getType());
            stmt.setString(3, a.getSeverity());
            stmt.setString(4, a.getDetails());
            stmt.setString(5, a.getFormattedTimestamp());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void queryAnomaliesByDrone(String droneId) {
        String sql = "SELECT * FROM anomalies WHERE droneId = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, droneId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                System.out.println(
                        rs.getString("timestamp") + " | " +
                        rs.getString("droneId") + " | " +
                        rs.getString("type") + " | " +
                        rs.getString("details")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void exportCSV() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM anomalies");
             PrintWriter pw = new PrintWriter("anomalies.csv")) {

            pw.println("DroneID,Type,Severity,Details,Timestamp");

            while (rs.next()) {
                pw.printf("%s,%s,%s,\"%s\",%s%n",
                        rs.getString("droneId"),
                        rs.getString("type"),
                        rs.getString("severity"),
                        rs.getString("details"),
                        rs.getString("timestamp"));
            }

            System.out.println("CSV exported.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
