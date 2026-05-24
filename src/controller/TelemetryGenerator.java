package controller;

import model.Drone;

import java.util.List;
import java.util.Random;

/**
 * Simulates realistic drone telemetry updates including:
 * - smooth movement with velocity/orientation tracking
 * - altitude drift
 * - battery consumption
 * - occasional scripted anomaly scenarios for testing
 *
 * FIX: Now calls the 6-argument update() so velocity and orientation
 * are always kept current (required by AnomalyDetector's SHARP_TURN check).
 */
public class TelemetryGenerator {

    private final Random rand = new Random();

    private static final double POSITION_DRIFT    = 0.0008;
    private static final double ALTITUDE_DRIFT    = 0.4;
    private static final double BATTERY_DRAIN_MIN = 0.15;
    private static final double BATTERY_DRAIN_MAX = 0.35;

    /** Chance (0–1) that an anomaly scenario fires for a drone this cycle. */
    private static final double ANOMALY_CHANCE = 0.04;

    public void updateDrones(List<Drone> drones) {
        for (Drone d : drones) {
            // -------------------------
            // 1. SMOOTH POSITION UPDATE
            // -------------------------
            double latChange = rand.nextGaussian() * POSITION_DRIFT;
            double lonChange = rand.nextGaussian() * POSITION_DRIFT;
            double newLat    = clampLatitude(d.getLatitude()   + latChange);
            double newLon    = clampLongitude(d.getLongitude() + lonChange);

            // Velocity = magnitude of movement this cycle (degrees)
            double newVelocity = Math.sqrt(latChange * latChange + lonChange * lonChange);

            // -------------------------
            // 2. ORIENTATION UPDATE
            // Heading derived from movement direction; small noise added for realism
            // -------------------------
            double newOrientation;
            if (newVelocity > 1e-9) {
                // atan2 gives radians; convert to compass degrees (0 = North)
                double headingRad = Math.atan2(lonChange, latChange);
                newOrientation = (Math.toDegrees(headingRad) + 360) % 360;
            } else {
                // No movement — keep previous heading with slight noise
                newOrientation = (d.getOrientation() + rand.nextGaussian() * 2 + 360) % 360;
            }

            // -------------------------
            // 3. ALTITUDE SIMULATION
            // -------------------------
            double altChange = rand.nextGaussian() * ALTITUDE_DRIFT;
            double newAlt    = Math.max(0, d.getAltitude() + altChange);

            // -------------------------
            // 4. BATTERY DRAIN MODEL
            // -------------------------
            double drain      = BATTERY_DRAIN_MIN
                + (rand.nextDouble() * (BATTERY_DRAIN_MAX - BATTERY_DRAIN_MIN));
            double newBattery = Math.max(0, d.getBattery() - drain);

            // -------------------------
            // 5. SCRIPTED ANOMALY INJECTION (for demonstration / testing)
            // -------------------------
            if (rand.nextDouble() < ANOMALY_CHANCE) {
                int scenario = rand.nextInt(3);
                switch (scenario) {
                    case 0: // sudden altitude drop
                        newAlt = Math.max(0, newAlt - 15.0);
                        break;
                    case 1: // sharp turn
                        newOrientation = (newOrientation + 120 + rand.nextInt(120)) % 360;
                        break;
                    case 2: // GPS jump (simulates spoofing)
                        newLat = clampLatitude(newLat   + (rand.nextBoolean() ? 0.08 : -0.08));
                        newLon = clampLongitude(newLon  + (rand.nextBoolean() ? 0.08 : -0.08));
                        break;
                }
            }

            // -------------------------
            // 6. APPLY FULL UPDATE (velocity + orientation now included)
            // -------------------------
            d.update(newLat, newLon, newAlt, newBattery, newVelocity, newOrientation);
        }
    }

    private double clampLatitude(double lat)  { return Math.max(-90,  Math.min(90,  lat)); }
    private double clampLongitude(double lon) { return Math.max(-180, Math.min(180, lon)); }
}
