package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Encapsulates a single detected anomaly.
 *
 * Stores the drone ID, anomaly type, a human-readable message,
 * severity level, and the timestamp when it was detected.
 * Can serialize itself to a CSV row for export.
 */
public class AnomalyRecord {
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String droneId;
    private String type;
    private String details;
    private String severity;
    private LocalDateTime timestamp;

    public AnomalyRecord(String droneId, String type, String details) {
        this.droneId   = droneId;
        this.type      = type;
        this.details   = details;
        this.severity  = deriveSeverity(type);
        this.timestamp = LocalDateTime.now();
    }
    public String getSeverityIcon() {
    return switch (severity) {
        case "CRITICAL" -> "⛔";
        case "WARNING" -> "⚠";
        default -> "ℹ";
    };
}
    private String deriveSeverity(String type) {
        switch (type) {
            case "CRITICAL_BATTERY":
            case "CRASH_RISK":
            case "EMERGENCY_RISK":
                return "CRITICAL";
            case "LOW_BATTERY":
            case "ALTITUDE_RISK":
            case "HIGH_VELOCITY":
            case "GPS_SPOOFING":
            case "ALTITUDE_DROP":
            case "SHARP_TURN":
                return "WARNING";
            default:
                return "INFO";
        }
    }

    public String getDroneId()  { return droneId; }
    public String getType()     { return type; }
    public String getDetails()  { return details; }
    public String getSeverity() { return severity; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getMessage()  { return details; }

    public String getFormattedTimestamp() {
        return timestamp.format(FORMATTER);
    }

    /**
     * Serializes this record to a CSV row.
     * Column order: DroneID, Type, Severity, Details, Timestamp
     */
    public String toCSVRow() {
        return String.format("%s,%s,%s,\"%s\",%s",
            droneId,
            type,
            severity,
            details.replace("\"", "\"\""),
            getFormattedTimestamp()
        );
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | %s: %s",
            getFormattedTimestamp(), droneId, severity, type, details);
    }
}
