package controller;

import model.Drone;
import model.AnomalyRecord;
import view.Dashboard;

import java.util.ArrayList;
import java.util.List;

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

        while (true) {
            generator.updateDrones(drones);

            List<AnomalyRecord> anomalies = detector.detect(drones);

            //view.display(drones, anomalies);

            Thread.sleep(2000);
        }
    }
}