package model;

/**
 * Represents a single drone's current state.
 *
 * Tracks position, altitude, battery, velocity, and orientation.
 * Computes its own DroneStatus based on current telemetry values.
 */
public class Drone {

    private static final double BATTERY_CRITICAL  = 5.0;
    private static final double BATTERY_WARNING   = 15.0;
    private static final double ALTITUDE_CRITICAL = 2.0;
    private static final double ALTITUDE_WARNING  = 5.0;

    private String id;
    private double latitude;
    private double longitude;
    private double altitude;
    private double battery;
    private double velocity;
    private double orientation; // degrees 0–359, where 0 = North

    public Drone(String id, double lat, double lon, double alt,
                 double battery, double velocity, double orientation) {
        this.id          = id;
        this.latitude    = lat;
        this.longitude   = lon;
        this.altitude    = alt;
        this.battery     = battery;
        this.velocity    = velocity;
        this.orientation = orientation;
    }

    public Drone(String id, double lat, double lon, double alt,
                 double battery, double velocity) {
        this(id, lat, lon, alt, battery, velocity, 0.0);
    }

    public void update(double lat, double lon, double alt,
                       double battery, double velocity, double orientation) {
        this.latitude    = lat;
        this.longitude   = lon;
        this.altitude    = alt;
        this.battery     = battery;
        this.velocity    = velocity;
        this.orientation = orientation;
    }

    public void update(double lat, double lon, double alt, double battery) {
        this.latitude = lat;
        this.longitude = lon;
        this.altitude = alt;
        this.battery = battery;
    }

    public DroneStatus getStatus() {
        if (battery <= BATTERY_CRITICAL || altitude <= ALTITUDE_CRITICAL) {
            return DroneStatus.CRITICAL;
        }
        if (battery < BATTERY_WARNING || altitude < ALTITUDE_WARNING) {
            return DroneStatus.WARNING;
        }
        return DroneStatus.NORMAL;
    }

    public String getId()          { return id; }
    public double getLatitude()    { return latitude; }
    public double getLongitude()   { return longitude; }
    public double getAltitude()    { return altitude; }
    public double getBattery()     { return battery; }
    public double getVelocity()    { return velocity; }
    public double getOrientation() { return orientation; }

    @Override
    public String toString() {
        return String.format(
                "%s | lat=%.5f lon=%.5f alt=%.2f battery=%.1f%% vel=%.5f ori=%.1f° [%s]",
                id, latitude, longitude, altitude, battery, velocity, orientation, getStatus()
        );
    }
}