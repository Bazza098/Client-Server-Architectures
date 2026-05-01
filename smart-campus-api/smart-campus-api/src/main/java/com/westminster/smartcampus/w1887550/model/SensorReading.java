package com.westminster.smartcampus.w1887550.model;

/**
 * Represents a single historical measurement recorded by a sensor.
 * @author Abdul (w1887550)
 */
public class SensorReading {

    private String id;          // UUID for this reading event
    private long   timestamp;   // Epoch time in milliseconds
    private double value;       // Recorded metric

    public SensorReading() { }

    public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    public String getId()                 { return id; }
    public void   setId(String id)        { this.id = id; }

    public long   getTimestamp()              { return timestamp; }
    public void   setTimestamp(long ts)       { this.timestamp = ts; }

    public double getValue()                  { return value; }
    public void   setValue(double value)      { this.value = value; }
}
