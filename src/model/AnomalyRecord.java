package model;

import java.time.LocalDateTime;

public class AnomalyRecord {

    private String droneId;
    private String type;
    private String details;
    private LocalDateTime timestamp;

    // Constants for anomaly types
    public static final String LOW_BATTERY = "LOW_BATTERY";
    public static final String LOW_ALTITUDE = "LOW_ALTITUDE";
    public static final String GPS_SPOOFING = "GPS_SPOOFING";

    public AnomalyRecord(String droneId, String type, String details) {
        this.droneId = droneId;
        this.type = type;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public String getDroneId() {
        return droneId;
    }

    public String getType() {
        return type;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getSummary() {
        return "Drone ID: " + droneId
                + " | Type: " + type
                + " | Time: " + timestamp
                + " | Details: " + details;
    }

    @Override
    public String toString() {
        return "AnomalyRecord{" +
                "droneId='" + droneId + '\'' +
                ", type='" + type + '\'' +
                ", details='" + details + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}