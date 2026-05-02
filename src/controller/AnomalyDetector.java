package controller;

import model.Drone;
import model.AnomalyRecord;
import java.util.ArrayList;
import java.util.List;

public class AnomalyDetector {

    public List<AnomalyRecord> detect(List<Drone> drones) {
        List<AnomalyRecord> anomalies = new ArrayList<>();

        for (Drone d : drones) {

            if (d.getBattery() < 15) {
                anomalies.add(new AnomalyRecord(d.getId(),
                        "LOW_BATTERY",
                        "Battery at " + d.getBattery()));
            }

            if (d.getAltitude() < 5) {
                anomalies.add(new AnomalyRecord(d.getId(),
                        "ALTITUDE_RISK",
                        "Low altitude: " + d.getAltitude()));
            }

            if (Math.abs(d.getLatitude()) > 90 || Math.abs(d.getLongitude()) > 180) {
                anomalies.add(new AnomalyRecord(d.getId(),
                        "GPS_SPOOFING",
                        "Invalid coordinates"));
            }
        }

        return anomalies;
    }
}