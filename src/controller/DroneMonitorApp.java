package controller;

import model.Drone;
import model.AnomalyRecord;
import view.Dashboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for the Drone Fleet Security Monitor.
 *
 * Orchestrates:
 *  - Drone fleet initialisation
 *  - TelemetryGenerator for simulated data
 *  - AnomalyDetector for per-cycle anomaly checks
 *  - AnomalyDatabase for SQLite persistence
 *  - AudioAlertManager for audio alerts
 *  - Dashboard (View) refresh on each cycle
 *
 * The main loop runs every 2 seconds on a background thread;
 * all UI updates are dispatched to the EDT via invokeLater.
 */
public class DroneMonitorApp {

    private static final int CYCLE_INTERVAL_MS = 2000;

    public static void main(String[] args) throws InterruptedException {

        // -------------------------
        // 1. INITIALISE FLEET
        // -------------------------
        List<Drone> drones = new ArrayList<>();
        drones.add(new Drone("D1", 47.6062, -122.3321, 50.0, 100.0, 0.0));
        drones.add(new Drone("D2", 47.6150, -122.3450, 60.0, 100.0, 0.0));
        drones.add(new Drone("D3", 47.6230, -122.3560, 55.0, 100.0, 0.0));
        drones.add(new Drone("D4", 47.5990, -122.3200, 45.0, 100.0, 0.0));

        // -------------------------
        // 2. INITIALISE SERVICES
        // -------------------------
        TelemetryGenerator generator = new TelemetryGenerator();
        AnomalyDetector    detector  = new AnomalyDetector();
        AnomalyDatabase    database  = new AnomalyDatabase();
        AudioAlertManager  audio     = new AudioAlertManager();

        // -------------------------
        // 3. LAUNCH DASHBOARD
        // -------------------------
        Dashboard view = new Dashboard();
        view.setDatabase(database);
        javax.swing.SwingUtilities.invokeLater(() -> view.setVisible(true));

        // Graceful shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            database.close();
            audio.shutdown();
            System.out.println("[App] Shutdown complete.");
        }));

        // -------------------------
        // 4. MAIN MONITORING LOOP
        // -------------------------
        int cycle = 0;
        while (true) {
            cycle++;

            // Update telemetry
            generator.updateDrones(drones);

            // Detect anomalies
            List<AnomalyRecord> anomalies = detector.detect(drones);

            // Persist anomalies to SQLite
            for (AnomalyRecord a : anomalies) {
                database.insert(a);
            }

            // Audio alerts
            audio.processAnomalies(anomalies);

            // Refresh dashboard (on EDT)
            final List<AnomalyRecord> anomalySnapshot = new ArrayList<>(anomalies);
            final List<Drone>         droneSnapshot   = new ArrayList<>(drones);
            javax.swing.SwingUtilities.invokeLater(() -> view.display(droneSnapshot, anomalySnapshot));

            // -------------------------
            // CONSOLE OUTPUT
            // -------------------------
            System.out.println("\n====================================");
            System.out.printf(" DRONE FLEET STATUS | CYCLE %d | DB records: %d%n",
                cycle, database.countAll());
            System.out.println("====================================");

            for (Drone d : drones) {
                System.out.printf(
                    "%s | LAT: %.5f | LON: %.5f | ALT: %.2f | BAT: %.1f%% | VEL: %.5f | ORI: %.1f° | [%s]%n",
                    d.getId(), d.getLatitude(), d.getLongitude(),
                    d.getAltitude(), d.getBattery(), d.getVelocity(),
                    d.getOrientation(), d.getStatus()
                );
            }

            if (!anomalies.isEmpty()) {
                System.out.println("\n--- ANOMALIES DETECTED ---");
                for (AnomalyRecord a : anomalies) {
                    System.out.printf("  %-4s [%-12s] %s%n",
                        a.getDroneId(), a.getType(), a.getMessage());
                }
            } else {
                System.out.println("  No anomalies this cycle.");
            }
            System.out.println("====================================");

            Thread.sleep(2000);
        }
    }
}