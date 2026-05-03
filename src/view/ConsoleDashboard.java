package view;

import model.Drone;
import model.AnomalyRecord;

import java.util.List;

/**
 * Console based view for displaying drone fleet status and anomaly records.
 */
public class ConsoleDashboard {

    /**
     * Prints the current status of all drones and any detected anomalies to stdout.
     *
     * @param drones    the list of drones to display
     * @param anomalies the list of anomalies to display, or empty if none
     */
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