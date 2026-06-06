package Tests;

import model.DroneStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the DroneStatus enum.
 */
class DroneStatusTest {

    @Test
    void allValuesExist() {
        DroneStatus[] values = DroneStatus.values();
        assertEquals(3, values.length);
    }

    @Test
    void normalValueExists() {
        assertEquals(DroneStatus.NORMAL, DroneStatus.valueOf("NORMAL"));
    }

    @Test
    void warningValueExists() {
        assertEquals(DroneStatus.WARNING, DroneStatus.valueOf("WARNING"));
    }

    @Test
    void criticalValueExists() {
        assertEquals(DroneStatus.CRITICAL, DroneStatus.valueOf("CRITICAL"));
    }

    @Test
    void valuesAreDistinct() {
        assertNotEquals(DroneStatus.NORMAL, DroneStatus.WARNING);
        assertNotEquals(DroneStatus.NORMAL, DroneStatus.CRITICAL);
        assertNotEquals(DroneStatus.WARNING, DroneStatus.CRITICAL);
    }

    @Test
    void ordinalOrderIsNormalWarningCritical() {
        assertTrue(DroneStatus.NORMAL.ordinal() < DroneStatus.WARNING.ordinal());
        assertTrue(DroneStatus.WARNING.ordinal() < DroneStatus.CRITICAL.ordinal());
    }

    @Test
    void valueOfInvalidNameThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> DroneStatus.valueOf("UNKNOWN"));
    }
}
