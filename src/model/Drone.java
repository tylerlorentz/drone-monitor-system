package model;

import java.util.ArrayList;
import java.util.List;

public class Drone {

    private String id;
    private double latitude;
    private double longitude;
    private double altitude;
    private double battery;
    private double velocity;

    // -------------------------
    // TELEMETRY HISTORY
    // -------------------------
    private List<String> telemetryHistory =
            new ArrayList<>();

    public Drone(String id,
                 double lat,
                 double lon,
                 double alt,
                 double battery,
                 double velocity) {

        this.id = id;
        this.latitude = lat;
        this.longitude = lon;
        this.altitude = alt;
        this.battery = battery;
        this.velocity = velocity;
    }

    public String getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getBattery() {
        return battery;
    }

    public double getVelocity() {
        return velocity;
    }

    // -------------------------
    // TELEMETRY HISTORY ACCESS
    // -------------------------
    public List<String> getTelemetryHistory() {
        return telemetryHistory;
    }

    public void addTelemetryHistory(String entry) {

        telemetryHistory.add(entry);

        // Keep only recent 10 entries
        if (telemetryHistory.size() > 10) {
            telemetryHistory.remove(0);
        }
    }

    public void update(double lat,
                       double lon,
                       double alt,
                       double battery,
                       double velocity) {

        this.latitude = lat;
        this.longitude = lon;
        this.altitude = alt;
        this.battery = battery;
        this.velocity = velocity;

        // -------------------------
        // STORE TELEMETRY SNAPSHOT
        // -------------------------
        addTelemetryHistory(
                String.format(
                        "LAT=%.5f LON=%.5f ALT=%.2f BAT=%.1f%% VEL=%.3f",
                        latitude,
                        longitude,
                        altitude,
                        battery,
                        velocity
                )
        );
    }

    @Override
    public String toString() {

        return id
                + " | lat=" + latitude
                + " lon=" + longitude
                + " alt=" + altitude
                + " battery=" + battery
                + " velocity=" + velocity;
    }
}