package controller;

import model.Drone;

import java.util.List;
import java.util.Random;

/**
 * Simulates realistic drone telemetry updates including:
 * - smooth movement
 * - altitude drift
 * - battery consumption
 *
 * This version avoids unrealistic random jumps and instead
 * models gradual system behavior.
 */
public class TelemetryGenerator {

    private Random rand = new Random();

    // Controls movement smoothness
    private static final double POSITION_DRIFT = 0.0008;
    private static final double ALTITUDE_DRIFT = 0.4;
    private static final double BATTERY_DRAIN_MIN = 0.15;
    private static final double BATTERY_DRAIN_MAX = 0.35;

    public void updateDrones(List<Drone> drones) {

        for (Drone d : drones) {

            // -------------------------
            // 1. SMOOTH POSITION UPDATE
            // -------------------------
            double latChange = rand.nextGaussian() * POSITION_DRIFT;
            double lonChange = rand.nextGaussian() * POSITION_DRIFT;

            double newLat = clampLatitude(d.getLatitude() + latChange);
            double newLon = clampLongitude(d.getLongitude() + lonChange);

            // -------------------------
            // 2. ALTITUDE SIMULATION
            // -------------------------
            double altChange = rand.nextGaussian() * ALTITUDE_DRIFT;
            double newAlt = Math.max(0, d.getAltitude() + altChange);

            // -------------------------
            // 3. BATTERY DRAIN MODEL
            // -------------------------
            double drain = BATTERY_DRAIN_MIN
                    + (rand.nextDouble() * (BATTERY_DRAIN_MAX - BATTERY_DRAIN_MIN));

            double newBattery = Math.max(0, d.getBattery() - drain);

            // -------------------------
            // 4. APPLY UPDATE
            // -------------------------
            d.update(newLat, newLon, newAlt, newBattery);
        }
    }

    // -------------------------
    // HELPER METHODS
    // -------------------------

    /**
     * Ensures latitude stays within valid GPS bounds.
     */
    private double clampLatitude(double lat) {
        return Math.max(-90, Math.min(90, lat));
    }

    /**
     * Ensures longitude stays within valid GPS bounds.
     */
    private double clampLongitude(double lon) {
        return Math.max(-180, Math.min(180, lon));
    }
}
