package controller;

import model.Drone;
import java.util.List;
import java.util.Random;

public class TelemetryGenerator {
    private Random rand = new Random();

    public void updateDrones(List<Drone> drones) {
        for (Drone d : drones) {

            double lat = d.getLatitude() + (rand.nextDouble() - 0.5) * 0.01;
            double lon = d.getLongitude() + (rand.nextDouble() - 0.5) * 0.01;
            double alt = d.getAltitude() + (rand.nextDouble() - 0.5) * 5;
            double battery = d.getBattery() - rand.nextDouble() * 2;

            d.update(lat, lon, alt, battery);
        }
    }
}