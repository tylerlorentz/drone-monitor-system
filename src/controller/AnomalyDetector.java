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
 *
 * Single-snapshot checks (battery, absolute altitude, velocity,
 * invalid coordinates) are also retained from the original.
 */
public class AnomalyDetector {

    /** Max realistic GPS change per cycle before flagging as spoofing (degrees). */
    private static final double GPS_JUMP_THRESHOLD     = 0.05;

    /** Max realistic altitude drop per cycle in metres before flagging. */
    private static final double ALTITUDE_DROP_THRESHOLD = 10.0;

    /** Max realistic heading change per cycle in degrees before flagging. */
    private static final double SHARP_TURN_THRESHOLD   = 90.0;

    /** Velocity above this value is flagged as abnormal movement. */
    private static final double HIGH_VELOCITY_THRESHOLD = 0.6;

    /** Battery at or below this value is a critical alert. */
    private static final double BATTERY_CRITICAL        = 5.0;

    /** Battery below this value is a warning. */
    private static final double BATTERY_WARNING         = 15.0;

    /** Altitude at or below this value is a crash risk. */
    private static final double ALTITUDE_CRITICAL       = 2.0;

    /** Altitude below this value is a low altitude warning. */
    private static final double ALTITUDE_WARNING        = 5.0;

    /**
     * Stores each drone's telemetry from the previous cycle.
     * Keyed by drone ID. Used for delta-based anomaly checks.
     */
    private Map<String, double[]> previousStates = new HashMap<>();

    /**
     * Runs all anomaly checks on the current drone list.
     *
     * Each drone is checked against both its current values and
     * its previous cycle values (if available). Results are
     * accumulated and returned as a list of AnomalyRecords.
     *
     * @param drones the current list of drones to inspect
     * @return a list of all anomalies detected this cycle (may be empty)
     */
    public List<AnomalyRecord> detect(List<Drone> drones) {
        List<AnomalyRecord> anomalies = new ArrayList<>();

        for (Drone d : drones) {
            double battery     = d.getBattery();
            double altitude    = d.getAltitude();
            double velocity    = d.getVelocity();
            double lat         = d.getLatitude();
            double lon         = d.getLongitude();
            double orientation = d.getOrientation();

            if (battery <= BATTERY_CRITICAL) {
                anomalies.add(new AnomalyRecord(
                    d.getId(),
                    "CRITICAL_BATTERY",
                    "Battery critically low: " + String.format("%.1f", battery) + "%"
                ));
            } else if (battery < BATTERY_WARNING) {
                anomalies.add(new AnomalyRecord(
                    d.getId(),
                    "LOW_BATTERY",
                    "Battery warning: " + String.format("%.1f", battery) + "%"
                ));
            }

            if (altitude <= ALTITUDE_CRITICAL) {
                anomalies.add(new AnomalyRecord(
                    d.getId(),
                    "CRASH_RISK",
                    "Dangerously low altitude: " + String.format("%.2f", altitude) + "m"
                ));
            } else if (altitude < ALTITUDE_WARNING) {
                anomalies.add(new AnomalyRecord(
                    d.getId(),
                    "ALTITUDE_RISK",
                    "Low altitude warning: " + String.format("%.2f", altitude) + "m"
                ));
            }

            if (Math.abs(lat) > 90 || Math.abs(lon) > 180) {
                anomalies.add(new AnomalyRecord(
                    d.getId(),
                    "GPS_ERROR",
                    "Invalid GPS coordinates: (" + String.format("%.5f", lat)
                        + ", " + String.format("%.5f", lon) + ")"
                ));
            }

            if (velocity > HIGH_VELOCITY_THRESHOLD) {
                anomalies.add(new AnomalyRecord(
                    d.getId(),
                    "HIGH_VELOCITY",
                    "Abnormal movement speed: " + String.format("%.5f", velocity)
                ));
            }

            if (battery < BATTERY_WARNING && altitude < ALTITUDE_WARNING) {
                anomalies.add(new AnomalyRecord(
                    d.getId(),
                    "EMERGENCY_RISK",
                    "Low battery AND low altitude — high crash risk"
                ));
            }


            if (previousStates.containsKey(d.getId())) {
                double[] prev = previousStates.get(d.getId());
                double prevLat  = prev[0];
                double prevLon  = prev[1];
                double prevAlt  = prev[2];
                double prevOri  = prev[3];

                double latDelta = Math.abs(lat - prevLat);
                double lonDelta = Math.abs(lon - prevLon);
                if (latDelta > GPS_JUMP_THRESHOLD || lonDelta > GPS_JUMP_THRESHOLD) {
                    anomalies.add(new AnomalyRecord(
                        d.getId(),
                        "GPS_SPOOFING",
                        "Sudden location jump detected — Δlat=" + String.format("%.5f", latDelta)
                            + " Δlon=" + String.format("%.5f", lonDelta)
                    ));
                }

                double altDrop = prevAlt - altitude;
                if (altDrop > ALTITUDE_DROP_THRESHOLD) {
                    anomalies.add(new AnomalyRecord(
                        d.getId(),
                        "ALTITUDE_DROP",
                        "Sudden altitude drop of " + String.format("%.2f", altDrop) + "m in one cycle"
                    ));
                }

                double turnDelta = headingDelta(prevOri, orientation);
                if (turnDelta > SHARP_TURN_THRESHOLD) {
                    anomalies.add(new AnomalyRecord(
                        d.getId(),
                        "SHARP_TURN",
                        "Sharp turn detected: " + String.format("%.1f", turnDelta) + "° change in one cycle"
                    ));
                }
            }

            previousStates.put(d.getId(), new double[]{lat, lon, altitude, orientation});
        }

        return anomalies;
    }


    /**
     * Computes the shortest angular difference between two headings.
     * Handles wrap-around (e.g. 350° → 10° = 20°, not 340°).
     *
     * @param from previous heading in degrees
     * @param to   current heading in degrees
     * @return the absolute shortest delta in degrees (0–180)
     */
    private double headingDelta(double from, double to) {
        double delta = Math.abs(to - from) % 360;
        return delta > 180 ? 360 - delta : delta;
    }
}
