package controller;

import model.Drone;
import model.AnomalyRecord;
import view.ConsoleDashboard;
import view.Dashboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for the Drone Fleet Monitor application.
 * Initialises the drone fleet, runs the telemetry loop, and updates the chosen view.
 *
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

        // Console Dashboard displays data in console instead of GUI.
        //ConsoleDashboard consoleDashboard = new ConsoleDashboard();

        while (true) {
            generator.updateDrones(drones);

            List<AnomalyRecord> anomalies = detector.detect(drones);

            // displays data in the ConsoleDashboard class, Dashboard class currently has no display()
            //view.display(drones, anomalies);

            Thread.sleep(2000);
        }
    }
}