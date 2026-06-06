package Tests;

import model.AnomalyRecord;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the AnomalyRecord class.
 */
class AnomalyRecordTest {

    private static final String DRONE_ID = "DRONE-42";
    private static final String DETAILS  = "Battery critically low";

    // ── Three-arg constructor ─────────────────────────────────────────────

    @Test
    void threeArgConstructorStoresFields() {
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "CRITICAL_BATTERY", DETAILS);
        assertEquals(DRONE_ID, r.getDroneId());
        assertEquals("CRITICAL_BATTERY", r.getType());
        assertEquals(DETAILS, r.getDetails());
    }

    @Test
    void threeArgConstructorSetsTimestampToNow() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "CRITICAL_BATTERY", DETAILS);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertTrue(!r.getTimestamp().isBefore(before) && !r.getTimestamp().isAfter(after));
    }

    // ── Five-arg constructor ──────────────────────────────────────────────

    @Test
    void fiveArgConstructorStoresAllFields() {
        LocalDateTime ts = LocalDateTime.of(2024, 6, 1, 12, 0, 0);
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "LOW_BATTERY", DETAILS, "WARNING", ts);

        assertEquals(DRONE_ID,   r.getDroneId());
        assertEquals("LOW_BATTERY", r.getType());
        assertEquals(DETAILS,    r.getDetails());
        assertEquals("WARNING",  r.getSeverity());
        assertEquals(ts,         r.getTimestamp());
    }

    // ── deriveSeverity ────────────────────────────────────────────────────

    @Test
    void criticalBatteryTypeYieldsCriticalSeverity() {
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "CRITICAL_BATTERY", DETAILS);
        assertEquals("CRITICAL", r.getSeverity());
    }

    @Test
    void crashRiskTypeYieldsCriticalSeverity() {
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "CRASH_RISK", DETAILS);
        assertEquals("CRITICAL", r.getSeverity());
    }

    @Test
    void emergencyRiskTypeYieldsCriticalSeverity() {
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "EMERGENCY_RISK", DETAILS);
        assertEquals("CRITICAL", r.getSeverity());
    }

    @Test
    void lowBatteryTypeYieldsWarningSeverity() {
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "LOW_BATTERY", DETAILS);
        assertEquals("WARNING", r.getSeverity());
    }

    @Test
    void altitudeRiskTypeYieldsWarningSeverity() {
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "ALTITUDE_RISK", DETAILS);
        assertEquals("WARNING", r.getSeverity());
    }

    @Test
    void highVelocityTypeYieldsWarningSeverity() {
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "HIGH_VELOCITY", DETAILS);
        assertEquals("WARNING", r.getSeverity());
    }

    @Test
    void gpsSpoofingTypeYieldsWarningSeverity() {
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "GPS_SPOOFING", DETAILS);
        assertEquals("WARNING", r.getSeverity());
    }

    @Test
    void altitudeDropTypeYieldsWarningSeverity() {
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "ALTITUDE_DROP", DETAILS);
        assertEquals("WARNING", r.getSeverity());
    }

    @Test
    void sharpTurnTypeYieldsWarningSeverity() {
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "SHARP_TURN", DETAILS);
        assertEquals("WARNING", r.getSeverity());
    }

    @Test
    void unknownTypeYieldsInfoSeverity() {
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "SOME_UNKNOWN_TYPE", DETAILS);
        assertEquals("INFO", r.getSeverity());
    }

    // ── getSeverityIcon ───────────────────────────────────────────────────

    @Test
    void criticalSeverityIconIsStopSign() {
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "CRITICAL_BATTERY", DETAILS);
        assertEquals("⛔", r.getSeverityIcon());
    }

    @Test
    void warningSeverityIconIsWarningSign() {
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "LOW_BATTERY", DETAILS);
        assertEquals("⚠", r.getSeverityIcon());
    }

    @Test
    void infoSeverityIconIsInfoSymbol() {
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "SOME_UNKNOWN_TYPE", DETAILS);
        assertEquals("ℹ", r.getSeverityIcon());
    }

    // ── getMessage (alias for getDetails) ────────────────────────────────

    @Test
    void getMessageReturnsSameValueAsGetDetails() {
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "LOW_BATTERY", DETAILS);
        assertEquals(r.getDetails(), r.getMessage());
    }

    // ── getFormattedTimestamp ─────────────────────────────────────────────

    @Test
    void formattedTimestampMatchesExpectedPattern() {
        LocalDateTime ts = LocalDateTime.of(2024, 3, 15, 9, 5, 7);
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "LOW_BATTERY", DETAILS, "WARNING", ts);
        assertEquals("2024-03-15 09:05:07", r.getFormattedTimestamp());
    }

    // ── toCSVRow ──────────────────────────────────────────────────────────

    @Test
    void csvRowContainsAllFiveColumns() {
        LocalDateTime ts = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "LOW_BATTERY", DETAILS, "WARNING", ts);

        String csv = r.toCSVRow();
        String[] cols = csv.split(",", 5);   // split into at most 5 parts

        assertEquals(5, cols.length, "Expected 5 CSV columns");
        assertEquals(DRONE_ID,   cols[0]);
        assertEquals("LOW_BATTERY", cols[1]);
        assertEquals("WARNING",  cols[2]);
        assertTrue(cols[3].contains(DETAILS), "Details column should contain the detail text");
        assertEquals("2024-01-01 00:00:00", cols[4]);
    }

    @Test
    void csvRowEscapesDoubleQuotesInDetails() {
        String detailsWithQuotes = "Battery is \"low\"";
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "LOW_BATTERY", detailsWithQuotes,
                "WARNING", LocalDateTime.now());

        String csv = r.toCSVRow();
        // The details field should be wrapped in quotes with internal quotes doubled
        assertTrue(csv.contains("\"Battery is \"\"low\"\"\""),
                "Double quotes inside details should be escaped as \"\"");
    }

    @Test
    void csvRowHasNoLeadingOrTrailingWhitespace() {
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "LOW_BATTERY", DETAILS,
                "WARNING", LocalDateTime.now());
        String csv = r.toCSVRow();
        assertEquals(csv.trim(), csv);
    }

    // ── toString ─────────────────────────────────────────────────────────

    @Test
    void toStringContainsDroneId() {
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "LOW_BATTERY", DETAILS);
        assertTrue(r.toString().contains(DRONE_ID));
    }

    @Test
    void toStringContainsTypeAndSeverityAndDetails() {
        AnomalyRecord r = new AnomalyRecord(DRONE_ID, "LOW_BATTERY", DETAILS);
        String s = r.toString();
        assertTrue(s.contains("LOW_BATTERY"));
        assertTrue(s.contains("WARNING"));
        assertTrue(s.contains(DETAILS));
    }
}
