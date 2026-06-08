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
 * <p>
 * Orchestrates:
 *  - Drone fleet initialisation
 *  - TelemetryGenerator for simulated data
 *  - AnomalyDetector for per-cycle anomaly checks
 *  - AnomalyDatabase for SQLite persistence
 *  - AudioAlertManager for audio alerts
 *  - Dashboard (View) refresh on each cycle
 * <p>
 * The main loop runs every 2 seconds on a background thread;
 * all UI updates are dispatched to the EDT via invokeLater.
 */
public class DroneMonitorApp {

    private static final int CYCLE_INTERVAL_MS = 2000;

    // -------------------------
    // PAUSE / MUTE FLAGS
    // volatile because the scheduler runs on a background thread
    // while the UI toggles these from the EDT
    // -------------------------
    private volatile boolean paused = false;
    private volatile boolean muted  = false;

    /**
     * Indicates whether telemetry processing is currently paused.
     *
     * @return true if processing is paused; otherwise false
     */
    public boolean isPaused() { return paused; }

    /**
     * Indicates whether audio alerts are currently muted.
     *
     * @return true if audio alerts are muted; otherwise false
     */
    public boolean isMuted()  { return muted;  }

    /**
     * Toggles the paused state of the application.
     */
    public void togglePause() { paused = !paused; }

    /**
    * Toggles the audio mute state of the application.
    */
    public void toggleMute()  { muted  = !muted;  }

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        new DroneMonitorApp().start();
    }

    /**
     * Initializes application components and begins
     * monitoring operations.
     */
    public void start() {

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
        view.setApp(this);           // gives the dashboard a reference to toggle pause/mute
        view.setTelemetryGenerator(generator);
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
        ScheduledExecutorService executor =
                Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(new Runnable() {

            int cycle = 0;

            /**
             * Executes the main monitoring loop.
             * <p>
             * This method continuously updates telemetry,
             * performs anomaly detection, refreshes displays,
             * and records anomalies until the application exits.
             */
            @Override
            public void run() {

                // Skip cycle if paused
                if (paused) return;

                cycle++;

                generator.updateDrones(drones);

                List<AnomalyRecord> anomalies = detector.detect(drones);

                for (AnomalyRecord a : anomalies) {
                    database.insert(a);
                }

                // Audio alerts — skipped when muted
                if (!muted) {
                    audio.processAnomalies(anomalies);
                }

                final List<AnomalyRecord> anomalySnapshot = new ArrayList<>(anomalies);
                final List<Drone>         droneSnapshot   = new ArrayList<>(drones);

                javax.swing.SwingUtilities.invokeLater(() ->
                        view.display(droneSnapshot, anomalySnapshot)
                );

                System.out.println("Cycle: " + cycle +
                        " | DB records: " + database.countAll() +
                        (muted  ? " [MUTED]"  : "") +
                        (paused ? " [PAUSED]" : ""));
            }

        }, 0, CYCLE_INTERVAL_MS, TimeUnit.MILLISECONDS);

    }
}
