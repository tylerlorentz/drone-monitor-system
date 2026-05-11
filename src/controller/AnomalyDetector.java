package controller;

import model.Drone;
import model.AnomalyRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects anomalies in drone telemetry data.
 */
public class AnomalyDetector {

    private static final double BATTERY_THRESHOLD = 15.0;
    private static final double ALTITUDE_THRESHOLD = 5.0;

    public List<AnomalyRecord> detect(List<Drone> drones) {

        List<AnomalyRecord> anomalies = new ArrayList<>();

        for (Drone d : drones) {

            double battery = d.getBattery();
            double altitude = d.getAltitude();
            double velocity = d.getVelocity();
            double lat = d.getLatitude();
            double lon = d.getLongitude();

            // LOW BATTERY
            if (isLowBattery(d)) {

                if (battery <= 5) {
                    anomalies.add(new AnomalyRecord(
                            d.getId(),
                            "CRITICAL_BATTERY",
                            "Battery critically low: " + String.format("%.1f", battery) + "%"
                    ));
                } else {
                    anomalies.add(new AnomalyRecord(
                            d.getId(),
                            AnomalyRecord.LOW_BATTERY,
                            "Battery warning: " + String.format("%.1f", battery) + "%"
                    ));
                }
            }

            // LOW ALTITUDE
            if (isLowAltitude(d)) {

                if (altitude < 2) {
                    anomalies.add(new AnomalyRecord(
                            d.getId(),
                            "CRASH_RISK",
                            "Dangerously low altitude: " + String.format("%.2f", altitude)
                    ));
                } else {
                    anomalies.add(new AnomalyRecord(
                            d.getId(),
                            "ALTITUDE_RISK",
                            "Low altitude warning: " + String.format("%.2f", altitude)
                    ));
                }
            }

            // GPS SPOOFING
            if (isGPSSpoofing(d)) {
                anomalies.add(new AnomalyRecord(
                        d.getId(),
                        AnomalyRecord.GPS_SPOOFING,
                        "Invalid GPS coordinates detected"
                ));
            }

            // HIGH VELOCITY
            if (velocity > 0.6) {
                anomalies.add(new AnomalyRecord(
                        d.getId(),
                        "HIGH_VELOCITY",
                        "Abnormal movement speed detected: " + String.format("%.5f", velocity)
                ));
            }

            // COMBINED RISK
            if (battery < 15 && altitude < 5) {
                anomalies.add(new AnomalyRecord(
                        d.getId(),
                        "EMERGENCY_RISK",
                        "Low battery AND low altitude detected (high crash risk)"
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