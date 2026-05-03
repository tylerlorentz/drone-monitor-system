package controller;

import model.Drone;
import model.AnomalyRecord;
import java.util.ArrayList;
import java.util.List;

public class AnomalyDetector {

    private static final double BATTERY_THRESHOLD = 15.0;
    private static final double ALTITUDE_THRESHOLD = 5.0;

    public List<AnomalyRecord> detect(List<Drone> drones) {
        List<AnomalyRecord> anomalies = new ArrayList<>();

        for (Drone d : drones) {

            if (isLowBattery(d)) {
                anomalies.add(new AnomalyRecord(
                        d.getId(),
                        AnomalyRecord.LOW_BATTERY,
                        "Battery at " + d.getBattery()
                ));
            }

            if (isLowAltitude(d)) {
                anomalies.add(new AnomalyRecord(
                        d.getId(),
                        AnomalyRecord.LOW_ALTITUDE,
                        "Low altitude: " + d.getAltitude()
                ));
            }

            if (isGPSSpoofing(d)) {
                anomalies.add(new AnomalyRecord(
                        d.getId(),
                        AnomalyRecord.GPS_SPOOFING,
                        "Invalid coordinates"
                ));
            }
        }

        return anomalies;
    }

    private boolean isLowBattery(Drone d) {
        return d.getBattery() < BATTERY_THRESHOLD;
    }

    private boolean isLowAltitude(Drone d) {
        return d.getAltitude() < ALTITUDE_THRESHOLD;
    }

    private boolean isGPSSpoofing(Drone d) {
        return Math.abs(d.getLatitude()) > 90 ||
                Math.abs(d.getLongitude()) > 180;
    }
}