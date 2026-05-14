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

    /**
     * Creates an anomaly record with auto-assigned severity and timestamp.
     *
     * Severity is derived from the anomaly type so AnomalyDetector
     * doesn't have to set it manually.
     *
     * @param droneId  ID of the drone that triggered the anomaly
     * @param type     anomaly type string (e.g. "LOW_BATTERY", "CRASH_RISK")
     * @param details  human-readable description of the anomaly
     */
    public AnomalyRecord(String droneId, String type, String details) {
        this.droneId   = droneId;
        this.type      = type;
        this.details   = details;
        this.severity  = deriveSeverity(type);
        this.timestamp = LocalDateTime.now();
    }


    /**
     * Maps anomaly type to a severity string.
     * Keeps severity logic in one place so adding a new type
     * only requires updating this method.
     *
     * @param type the anomaly type
     * @return "CRITICAL", "WARNING", or "INFO"
     */
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

    /**
     * Returns the human-readable message for display in the dashboard.
     * Alias for getDetails() — satisfies the call in AnomalyDetector.
     *
     * @return the details string
     */
    public String getMessage() { return details; }

    /**
     * Returns the timestamp as a formatted string.
     *
     * @return timestamp in "yyyy-MM-dd HH:mm:ss" format
     */
    public String getFormattedTimestamp() {
        return timestamp.format(FORMATTER);
    }


    /**
     * Serializes this record to a single CSV row.
     * Column order matches the header written by CSVExporter:
     * DroneID, Type, Severity, Details, Timestamp
     *
     * Details are wrapped in quotes to handle any commas in the message.
     *
     * @return a comma-separated string representing this record
     */
    public String toCSVRow() {
        return String.format("%s,%s,%s,\"%s\",%s",
            droneId,
            type,
            severity,
            details.replace("\"", "\"\""), // escape any quotes in details
            getFormattedTimestamp()
        );
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | %s: %s",
            getFormattedTimestamp(), droneId, severity, type, details);
    }
}
