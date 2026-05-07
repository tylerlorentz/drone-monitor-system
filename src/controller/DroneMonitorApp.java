package controller;

import model.Drone;
import model.AnomalyRecord;
import view.Dashboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Main application loop for drone fleet monitoring system.
 *
 * Now includes:
 * - runtime cycle tracking
 * - anomaly summary statistics
 * - improved console output formatting
 * - safer UI update handling
 */
public class DroneMonitorApp {

    public static void main(String[] args) throws InterruptedException {

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

            // -------------------------
            // SAFE UI UPDATE
            // -------------------------
            javax.swing.SwingUtilities.invokeLater(() -> view.display(drones, anomalies));

            // -------------------------
            // CONSOLE OUTPUT
            // -------------------------
            System.out.println("\n====================================");
            System.out.println(" DRONE FLEET STATUS | CYCLE " + cycle);
            System.out.println("====================================");

            int totalAnomalies = 0;
            int lowBatteryCount = 0;
            int gpsErrors = 0;
            int altitudeWarnings = 0;

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

            if (!anomalies.isEmpty()) {

                System.out.println("\n--- ANOMALIES DETECTED ---");

                for (AnomalyRecord a : anomalies) {

                    System.out.println(a.getDroneId()
                            + " -> "
                            + a.getType()
                            + ": "
                            + a.getMessage());

                    totalAnomalies++;

                    switch (a.getType()) {
                        case "LOW_BATTERY":
                            lowBatteryCount++;
                            break;
                        case "GPS_ERROR":
                        case "GPS_SPOOFING":
                            gpsErrors++;
                            break;
                        case "ALTITUDE_RISK":
                            altitudeWarnings++;
                            break;
                    }
                }

                System.out.println("\n--- ANOMALY SUMMARY ---");
                System.out.println("Total anomalies: " + totalAnomalies);
                System.out.println("Low battery: " + lowBatteryCount);
                System.out.println("GPS issues: " + gpsErrors);
                System.out.println("Altitude warnings: " + altitudeWarnings);

            } else {
                System.out.println("\nNo anomalies detected this cycle.");
            }

            System.out.println("====================================");

            Thread.sleep(2000);
        }
    }
}
