package view;

import model.Drone;
import model.AnomalyRecord;

import java.util.List;

public class ConsoleDashboard {

    public void display(List<Drone> drones, List<AnomalyRecord> anomalies) {

        System.out.println("\n===== DRONE STATUS =====");

        for (Drone d : drones) {
            System.out.println(d);
        }

        System.out.println("\n===== ANOMALIES =====");

        if (anomalies.isEmpty()) {
            System.out.println("No anomalies detected.");
        } else {
            for (AnomalyRecord a : anomalies) {
                System.out.println(a.getDroneId() + " | " + a.getType()
                        + " | " + a.getDetails());
            }
        }
    }
}