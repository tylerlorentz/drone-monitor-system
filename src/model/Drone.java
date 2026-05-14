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

    /**
     * Creates a drone with a full initial state.
     *
     * @param id          unique drone identifier (e.g. "D1")
     * @param lat         initial latitude
     * @param lon         initial longitude
     * @param alt         initial altitude in metres
     * @param battery     initial battery percentage (0–100)
     * @param velocity    initial velocity (degrees of lat/lon per update cycle)
     * @param orientation initial heading in degrees (0 = North, 90 = East)
     */
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

    /**
     * Convenience constructor — orientation defaults to 0 (North).
     * Keeps backward compatibility with the existing 6-argument call in DroneMonitorApp.
     */
    public Drone(String id, double lat, double lon, double alt,
                 double battery, double velocity) {
        this(id, lat, lon, alt, battery, velocity, 0.0);
    }

    /**
     * Applies a full telemetry update from TelemetryGenerator.
     *
     * @param lat         new latitude
     * @param lon         new longitude
     * @param alt         new altitude in metres
     * @param battery     new battery percentage
     * @param velocity    magnitude of movement this cycle
     * @param orientation new heading in degrees
     */
    public void update(double lat, double lon, double alt,
                       double battery, double velocity, double orientation) {
        this.latitude    = lat;
        this.longitude   = lon;
        this.altitude    = alt;
        this.battery     = battery;
        this.velocity    = velocity;
        this.orientation = orientation;
    }

    /**
     * Backward-compatible 4-argument update — velocity and orientation stay unchanged.
     * Matches the old signature used in TelemetryGenerator before this update.
     */
    
    public void update(double lat, double lon, double alt, double battery) {
        this.latitude = lat;
        this.longitude = lon;
        this.altitude = alt;
        this.battery = battery;
    }


    /**
     * Derives the drone's current status from its telemetry.
     * CRITICAL if battery <= 5% or altitude <= 2m.
     * WARNING  if battery < 15% or altitude < 5m.
     * NORMAL   otherwise.
     *
     * @return the computed DroneStatus
     */

    
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
