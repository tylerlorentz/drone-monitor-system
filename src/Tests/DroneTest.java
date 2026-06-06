package Tests;

import model.Drone;
import model.DroneStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Drone class.
 *
 * Boundary values used:
 *   BATTERY_CRITICAL  = 5.0   BATTERY_WARNING  = 15.0
 *   ALTITUDE_CRITICAL = 2.0   ALTITUDE_WARNING = 5.0
 */
class DroneTest {

    private static final String ID  = "DRONE-01";
    private static final double LAT = 47.6062;
    private static final double LON = -122.3321;

    private Drone normalDrone;

    @BeforeEach
    void setUp() {
        // Battery 50 %, altitude 100 m — well within NORMAL
        normalDrone = new Drone(ID, LAT, LON, 100.0, 50.0, 10.0, 90.0);
    }

    // ── Constructor & getters ──────────────────────────────────────────────

    @Test
    void fullConstructorStoresAllFields() {
        assertEquals(ID,      normalDrone.getId());
        assertEquals(LAT,     normalDrone.getLatitude(),  1e-9);
        assertEquals(LON,     normalDrone.getLongitude(), 1e-9);
        assertEquals(100.0,   normalDrone.getAltitude(),  1e-9);
        assertEquals(50.0,    normalDrone.getBattery(),   1e-9);
        assertEquals(10.0,    normalDrone.getVelocity(),  1e-9);
        assertEquals(90.0,    normalDrone.getOrientation(), 1e-9);
    }

    @Test
    void shortConstructorDefaultsOrientationToZero() {
        Drone d = new Drone(ID, LAT, LON, 100.0, 50.0, 10.0);
        assertEquals(0.0, d.getOrientation(), 1e-9);
    }

    // ── getStatus – NORMAL ────────────────────────────────────────────────

    @Test
    void statusIsNormalWhenBatteryAndAltitudeAreAboveWarning() {
        // battery=50 > 15, altitude=100 > 5
        Assertions.assertEquals(DroneStatus.NORMAL, normalDrone.getStatus());
    }

    @Test
    void statusIsNormalAtExactlyBatteryWarningBoundary() {
        // battery == 15 is NOT < 15, so still NORMAL
        Drone d = new Drone(ID, LAT, LON, 100.0, 15.0, 5.0, 0.0);
        assertEquals(DroneStatus.NORMAL, d.getStatus());
    }

    // ── getStatus – WARNING ───────────────────────────────────────────────

    @Test
    void statusIsWarningWhenBatteryBelowWarningThreshold() {
        // battery=14 < 15 but > 5, altitude fine
        Drone d = new Drone(ID, LAT, LON, 100.0, 14.0, 5.0, 0.0);
        assertEquals(DroneStatus.WARNING, d.getStatus());
    }

    @Test
    void statusIsWarningWhenAltitudeBelowWarningThreshold() {
        // altitude=4 < 5 but > 2, battery fine
        Drone d = new Drone(ID, LAT, LON, 4.0, 50.0, 5.0, 0.0);
        assertEquals(DroneStatus.WARNING, d.getStatus());
    }

    @Test
    void statusIsWarningAtJustAboveCriticalBattery() {
        // battery just above CRITICAL (5.0) but below WARNING (15.0)
        Drone d = new Drone(ID, LAT, LON, 100.0, 5.01, 5.0, 0.0);
        assertEquals(DroneStatus.WARNING, d.getStatus());
    }

    // ── getStatus – CRITICAL ──────────────────────────────────────────────

    @Test
    void statusIsCriticalWhenBatteryAtOrBelowCriticalThreshold() {
        Drone d = new Drone(ID, LAT, LON, 100.0, 5.0, 5.0, 0.0);
        assertEquals(DroneStatus.CRITICAL, d.getStatus());
    }

    @Test
    void statusIsCriticalWhenBatteryBelowCriticalThreshold() {
        Drone d = new Drone(ID, LAT, LON, 100.0, 4.9, 5.0, 0.0);
        assertEquals(DroneStatus.CRITICAL, d.getStatus());
    }

    @Test
    void statusIsCriticalWhenAltitudeAtOrBelowCriticalThreshold() {
        Drone d = new Drone(ID, LAT, LON, 2.0, 50.0, 5.0, 0.0);
        assertEquals(DroneStatus.CRITICAL, d.getStatus());
    }

    @Test
    void statusIsCriticalWhenAltitudeBelowCriticalThreshold() {
        Drone d = new Drone(ID, LAT, LON, 1.5, 50.0, 5.0, 0.0);
        assertEquals(DroneStatus.CRITICAL, d.getStatus());
    }

    @Test
    void statusIsCriticalWhenBothBatteryAndAltitudeAreCritical() {
        Drone d = new Drone(ID, LAT, LON, 1.0, 3.0, 5.0, 0.0);
        assertEquals(DroneStatus.CRITICAL, d.getStatus());
    }

    // ── update (full) ─────────────────────────────────────────────────────

    @Test
    void fullUpdateChangesAllTelemetryFields() {
        normalDrone.update(48.0, -123.0, 50.0, 20.0, 8.0, 180.0);

        assertEquals(48.0,   normalDrone.getLatitude(),    1e-9);
        assertEquals(-123.0, normalDrone.getLongitude(),   1e-9);
        assertEquals(50.0,   normalDrone.getAltitude(),    1e-9);
        assertEquals(20.0,   normalDrone.getBattery(),     1e-9);
        assertEquals(8.0,    normalDrone.getVelocity(),    1e-9);
        assertEquals(180.0,  normalDrone.getOrientation(), 1e-9);
    }

    @Test
    void fullUpdateChangesStatus() {
        assertEquals(DroneStatus.NORMAL, normalDrone.getStatus());
        // Drop battery into CRITICAL range
        normalDrone.update(LAT, LON, 100.0, 3.0, 5.0, 0.0);
        assertEquals(DroneStatus.CRITICAL, normalDrone.getStatus());
    }

    // ── update (partial – 4-arg) ──────────────────────────────────────────

    @Test
    void partialUpdateChangesPositionBatteryOnly() {
        double origVelocity     = normalDrone.getVelocity();
        double origOrientation  = normalDrone.getOrientation();

        normalDrone.update(48.0, -123.0, 50.0, 20.0);

        assertEquals(48.0,           normalDrone.getLatitude(),    1e-9);
        assertEquals(-123.0,         normalDrone.getLongitude(),   1e-9);
        assertEquals(50.0,           normalDrone.getAltitude(),    1e-9);
        assertEquals(20.0,           normalDrone.getBattery(),     1e-9);
        assertEquals(origVelocity,   normalDrone.getVelocity(),    1e-9);
        assertEquals(origOrientation, normalDrone.getOrientation(), 1e-9);
    }

    // ── toString ─────────────────────────────────────────────────────────

    @Test
    void toStringContainsDroneId() {
        assertTrue(normalDrone.toString().contains(ID));
    }

    @Test
    void toStringContainsStatusLabel() {
        assertTrue(normalDrone.toString().contains("NORMAL"));
    }
}
