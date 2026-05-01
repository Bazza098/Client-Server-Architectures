package com.westminster.smartcampus.w1887550.model;

/**
 * Represents a physical sensor deployed in a room.
 * @author Abdul (w1887550)
 */
public class Sensor {

    /** Valid statuses: "ACTIVE", "MAINTENANCE", "OFFLINE". */
    private String id;
    private String type;            // "Temperature", "Occupancy", "CO2", etc.
    private String status;          // ACTIVE | MAINTENANCE | OFFLINE
    private double currentValue;    // Most recent measurement
    private String roomId;          // FK to Room.id

    public Sensor() { }

    public Sensor(String id, String type, String status,
                  double currentValue, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.currentValue = currentValue;
        this.roomId = roomId;
    }

    public String getId()                           { return id; }
    public void   setId(String id)                  { this.id = id; }

    public String getType()                         { return type; }
    public void   setType(String type)              { this.type = type; }

    public String getStatus()                       { return status; }
    public void   setStatus(String status)          { this.status = status; }

    public double getCurrentValue()                 { return currentValue; }
    public void   setCurrentValue(double value)     { this.currentValue = value; }

    public String getRoomId()                       { return roomId; }
    public void   setRoomId(String roomId)          { this.roomId = roomId; }
}
