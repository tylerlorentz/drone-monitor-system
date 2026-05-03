package model;

public class Drone {
    private String id;
    private double latitude;
    private double longitude;
    private double altitude;
    private double battery;
    private double velocity;

    public Drone(String id, double lat, double lon, double alt, double battery, double velocity) {
        this.id = id;
        this.latitude = lat;
        this.longitude = lon;
        this.altitude = alt;
        this.battery = battery;
        this.velocity = velocity;
    }

    public String getId() { return id; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getAltitude() { return altitude; }
    public double getBattery() { return battery; }
    public double getVelocity() { return velocity; }

    public void update(double lat, double lon, double alt, double battery) {
        this.latitude = lat;
        this.longitude = lon;
        this.altitude = alt;
        this.battery = battery;
    }

    @Override
    public String toString() {
        return id + " | lat=" + latitude + " lon=" + longitude +
                " alt=" + altitude + " battery=" + battery;
    }
}