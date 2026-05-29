package controller;

import model.Drone;
import model.AnomalyRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detects anomalies in drone telemetry data.
 *
 * Compares each drone's current state against its previous state
 * to catch sudden changes that a single-snapshot check would miss:
 * - GPS spoofing (unexpected location jump between cycles)
 * - Sudden altitude drops (rapid descent in one cycle)
 * - Sharp turns (large heading change in one cycle)
 */
public class AnomalyDetector {

    private static final double GPS_JUMP_THRESHOLD      = 0.05;
    private static final double ALTITUDE_DROP_THRESHOLD = 10.0;
    private static final double SHARP_TURN_THRESHOLD    = 90.0;
    private static final double HIGH_VELOCITY_THRESHOLD = 0.6;
    private static final double BATTERY_CRITICAL        = 5.0;
    private static final double BATTERY_WARNING         = 15.0;
    private static final double ALTITUDE_CRITICAL       = 2.0;
    private static final double ALTITUDE_WARNING        = 5.0;

    private final Map<String, double[]> previousStates = new HashMap<>();
    private final Map<String, Integer> hoverCounter = new HashMap<>();
    
    public List<AnomalyRecord> detect(List<Drone> drones) {
        List<AnomalyRecord> anomalies = new ArrayList<>();

        for (Drone d : drones) {
            double battery     = d.getBattery();
            double altitude    = d.getAltitude();
            double velocity    = d.getVelocity();
            double lat         = d.getLatitude();
            double lon         = d.getLongitude();
            double orientation = d.getOrientation();

            // Battery checks
            if (battery <= BATTERY_CRITICAL) {
                anomalies.add(new AnomalyRecord(d.getId(), "CRITICAL_BATTERY",
                    "Battery critically low: " + String.format("%.1f", battery) + "%"));
            } else if (battery < BATTERY_WARNING) {
                anomalies.add(new AnomalyRecord(d.getId(), "LOW_BATTERY",
                    "Battery warning: " + String.format("%.1f", battery) + "%"));
            }

            // Altitude checks
            if (altitude <= ALTITUDE_CRITICAL) {
                anomalies.add(new AnomalyRecord(d.getId(), "CRASH_RISK",
                    "Dangerously low altitude: " + String.format("%.2f", altitude) + "m"));
            } else if (altitude < ALTITUDE_WARNING) {
                anomalies.add(new AnomalyRecord(d.getId(), "ALTITUDE_RISK",
                    "Low altitude warning: " + String.format("%.2f", altitude) + "m"));
            }

            // GPS validity
            if (Math.abs(lat) > 90 || Math.abs(lon) > 180) {
                anomalies.add(new AnomalyRecord(d.getId(), "GPS_ERROR",
                    "Invalid GPS coordinates: (" + String.format("%.5f", lat)
                        + ", " + String.format("%.5f", lon) + ")"));
            }

            // High velocity
            if (velocity > HIGH_VELOCITY_THRESHOLD) {
                anomalies.add(new AnomalyRecord(d.getId(), "HIGH_VELOCITY",
                    "Abnormal movement speed: " + String.format("%.5f", velocity)));
            }

            // Combined emergency
            if (battery < BATTERY_WARNING && altitude < ALTITUDE_WARNING) {
                anomalies.add(new AnomalyRecord(d.getId(), "EMERGENCY_RISK",
                    "Low battery AND low altitude — high crash risk"));
            }

            // Delta-based checks
            if (previousStates.containsKey(d.getId())) {
                double[] prev    = previousStates.get(d.getId());
                double prevLat   = prev[0];
                double prevLon   = prev[1];
                double prevAlt   = prev[2];
                double prevOri   = prev[3];
                double prevBattery = prev[4];

                double latDelta = Math.abs(lat - prevLat);
                double lonDelta = Math.abs(lon - prevLon);
                if (latDelta > GPS_JUMP_THRESHOLD || lonDelta > GPS_JUMP_THRESHOLD) {
                    anomalies.add(new AnomalyRecord(d.getId(), "GPS_SPOOFING",
                        "Sudden location jump detected — Δlat=" + String.format("%.5f", latDelta)
                            + " Δlon=" + String.format("%.5f", lonDelta)));
                }

                double altDrop = prevAlt - altitude;
                if (altDrop > ALTITUDE_DROP_THRESHOLD) {
                    anomalies.add(new AnomalyRecord(d.getId(), "ALTITUDE_DROP",
                        "Sudden altitude drop of " + String.format("%.2f", altDrop) + "m in one cycle"));
                }

                if ((prevBattery - battery) > 5.0) {
                    anomalies.add(new AnomalyRecord(
                        d.getId(),
                        "RAPID_BATTERY_DRAIN",
                        "Battery dropped rapidly in one cycle"
                    ));
                }

                double turnDelta = headingDelta(prevOri, orientation);
                if (turnDelta > SHARP_TURN_THRESHOLD) {
                    anomalies.add(new AnomalyRecord(d.getId(), "SHARP_TURN",
                        "Sharp turn detected: " + String.format("%.1f", turnDelta) + "° change in one cycle"));
                }

                if (velocity < 0.00005) {
                    hoverCounter.put(d.getId(), hoverCounter.getOrDefault(d.getId(), 0) + 1);

                    if (hoverCounter.get(d.getId()) >= 5) {
                        anomalies.add(new AnomalyRecord(
                            d.getId(),
                            "SUSPICIOUS_HOVERING",
                            "Drone has remained nearly stationary for multiple cycles"
                        ));
                    }
                } else {
                    hoverCounter.put(d.getId(), 0);
                }   

                if (turnDelta > 140 && velocity > 0.0004) {
                    anomalies.add(new AnomalyRecord(
                        d.getId(),
                        "ERRATIC_MOVEMENT",
                        "Extreme directional changes detected"
                    ));
                }
            }
            previousStates.put(d.getId(), new double[]{lat, lon, altitude, orientation, battery });
        }

        return anomalies;
    }

    private double headingDelta(double from, double to) {
        double delta = Math.abs(to - from) % 360;
        return delta > 180 ? 360 - delta : delta;
    }
}
