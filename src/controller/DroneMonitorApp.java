package controller;

import model.AnomalyRecord;
import model.Drone;
import view.Dashboard;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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

    public static void main(String[] args) {

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
        view.setTelemetryGenerator(generator);

        // Graceful shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            database.close();
            audio.shutdown();
            System.out.println("[App] Shutdown complete.");
        }));

        // -------------------------
        // 4. MAIN MONITORING LOOP
        // -------------------------
        ScheduledExecutorService executor =
                Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(new Runnable() {

            int cycle = 0;

            @Override
            public void run() {
                cycle++;

                generator.updateDrones(drones);

                List<AnomalyRecord> anomalies = detector.detect(drones);

                for (AnomalyRecord a : anomalies) {
                    database.insert(a);
                }

                audio.processAnomalies(anomalies);

                final List<AnomalyRecord> anomalySnapshot =
                        new ArrayList<>(anomalies);

                final List<Drone> droneSnapshot =
                        new ArrayList<>(drones);

                javax.swing.SwingUtilities.invokeLater(() ->
                        view.display(droneSnapshot, anomalySnapshot)
                );

                System.out.println("Cycle: " + cycle +
                        " | DB records: " + database.countAll());
            }

        }, 0, CYCLE_INTERVAL_MS, TimeUnit.MILLISECONDS);

    }
}