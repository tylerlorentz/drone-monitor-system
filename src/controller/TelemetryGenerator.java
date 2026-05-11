package controller;

import model.Drone;

import java.util.List;
import java.util.Random;

/**
 * Simulates live telemetry updates for drones.
 */
public class TelemetryGenerator {

    private Random rand = new Random();

    public void updateDrones(List<Drone> drones) {

        for (Drone d : drones) {

            double lat = d.getLatitude()
                    + (rand.nextDouble() - 0.5) * 0.01;

            double lon = d.getLongitude()
                    + (rand.nextDouble() - 0.5) * 0.01;

            double alt = Math.max(
                    0,
                    d.getAltitude() + (rand.nextDouble() - 0.5) * 5
            );

            double battery = Math.max(
                    0,
                    d.getBattery() - rand.nextDouble() * 2
            );

            double velocity = Math.max(
                    0,
                    d.getVelocity() + (rand.nextDouble() - 0.5) * 0.2
            );

            // --------------------------------------
            // CONTROLLED DEMO ANOMALY SCENARIOS
            // --------------------------------------

            // D1 = LOW BATTERY
            if (d.getId().equals("D1")) {
                battery = 10;
            }

            // D2 = LOW ALTITUDE
            if (d.getId().equals("D2")) {
                alt = 2;
            }

            // D3 = GPS SPOOFING
            if (d.getId().equals("D3")) {
                lat = 200;
            }

            d.update(lat, lon, alt, battery, velocity);
        }
    }
}