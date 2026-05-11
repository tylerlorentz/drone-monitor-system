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
    private static final double VELOCITY_THRESHOLD = 15.0;

    public List<AnomalyRecord> detect(List<Drone> drones) {

        List<AnomalyRecord> anomalies = new ArrayList<>();

        for (Drone d : drones) {

            double battery = d.getBattery();
            double altitude = d.getAltitude();
            double velocity = d.getVelocity();
            double lat = d.getLatitude();
            double lon = d.getLongitude();

            // LOW BATTERY
            if (battery < BATTERY_THRESHOLD) {

                anomalies.add(new AnomalyRecord(
                        d.getId(),
                        "LOW_BATTERY",
                        "Battery warning: "
                                + String.format("%.1f", battery)
                                + "%"
                ));
            }

            // LOW ALTITUDE
            if (altitude < ALTITUDE_THRESHOLD) {

                anomalies.add(new AnomalyRecord(
                        d.getId(),
                        "ALTITUDE_RISK",
                        "Low altitude warning: "
                                + String.format("%.2f", altitude)
                ));
            }

            // GPS SPOOFING
            if (Math.abs(lat) > 90 || Math.abs(lon) > 180) {

                anomalies.add(new AnomalyRecord(
                        d.getId(),
                        "GPS_SPOOFING",
                        "Invalid GPS coordinates detected"
                ));
            }

            // HIGH VELOCITY
            if (velocity > VELOCITY_THRESHOLD) {

                anomalies.add(new AnomalyRecord(
                        d.getId(),
                        "HIGH_VELOCITY",
                        "Abnormal movement speed detected: "
                                + String.format("%.2f", velocity)
                ));
            }

            // EMERGENCY RISK
            if (battery < 15 && altitude < 5) {

                anomalies.add(new AnomalyRecord(
                        d.getId(),
                        "EMERGENCY_RISK",
                        "Low battery AND low altitude detected"
                ));
            }
        }

        return anomalies;
    }
}