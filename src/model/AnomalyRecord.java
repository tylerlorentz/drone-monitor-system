package model;

import java.time.LocalDateTime;

public class AnomalyRecord {
    private String droneId;
    private String type;
    private String details;
    private LocalDateTime timestamp;

    public AnomalyRecord(String droneId, String type, String details) {
        this.droneId = droneId;
        this.type = type;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public String getDroneId() { return droneId; }
    public String getType() { return type; }
    public String getDetails() { return details; }
    public LocalDateTime getTimestamp() { return timestamp; }

}