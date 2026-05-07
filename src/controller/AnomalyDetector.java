package controller;

import model.Drone;
import model.AnomalyRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects anomalies in drone telemetry data.
 *
 * Now includes improved safety rules and more structured detection logic.
 */
public class AnomalyDetector {

    public List<AnomalyRecord> detect(List<Drone> drones) {

        List<AnomalyRecord> anomalies = new ArrayList<>();

        for (Drone d : drones) {

            double battery = d.getBattery();
            double altitude = d.getAltitude();
            double velocity = d.getVelocity();
            double lat = d.getLatitude();
            double lon = d.getLongitude();

            // -------------------------
            // 1. BATTERY SAFETY RULES
            // -------------------------
            if (battery <= 5) {
                anomalies.add(new AnomalyRecord(
                        d.getId(),
                        "CRITICAL_BATTERY",
                        "Battery critically low: " + String.format("%.1f", battery) + "%"
                ));
            } else if (battery < 15) {
                anomalies.add(new AnomalyRecord(
                        d.getId(),
                        "LOW_BATTERY",
                        "Battery warning: " + String.format("%.1f", battery) + "%"
                ));
            }

            // -------------------------
            // 2. ALTITUDE SAFETY RULES
            // -------------------------
            if (altitude < 2) {
                anomalies.add(new AnomalyRecord(
                        d.getId(),
                        "CRASH_RISK",
                        "Dangerously low altitude: " + String.format("%.2f", altitude)
                ));
            } else if (altitude < 5) {
                anomalies.add(new AnomalyRecord(
                        d.getId(),
                        "ALTITUDE_RISK",
                        "Low altitude warning: " + String.format("%.2f", altitude)
                ));
            }

            // -------------------------
            // 3. GPS VALIDATION
            // -------------------------
            if (Math.abs(lat) > 90 || Math.abs(lon) > 180) {
                anomalies.add(new AnomalyRecord(
                        d.getId(),
                        "GPS_ERROR",
                        "Invalid GPS coordinates detected"
                ));
            }

            // -------------------------
            // 4. MOVEMENT ANOMALY (VELOCITY)
            // -------------------------
            if (velocity > 0.6) {
                anomalies.add(new AnomalyRecord(
                        d.getId(),
                        "HIGH_VELOCITY",
                        "Abnormal movement speed detected: " + String.format("%.5f", velocity)
                ));
            }

            // -------------------------
            // 5. COMBINED RISK CHECK
            // -------------------------
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
}
