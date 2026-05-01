package com.westminster.smartcampus.w1887550.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical room on campus.
 * @author Abdul (w1887550)
 */
public class Room {

    private String id;              // Unique identifier, e.g. "LIB-301"
    private String name;            // Human-readable name
    private int capacity;           // Maximum occupancy for safety regulations
    private List<String> sensorIds = new ArrayList<>(); // IDs of sensors deployed here

    public Room() { }

    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public String getId()                     { return id; }
    public void   setId(String id)            { this.id = id; }

    public String getName()                   { return name; }
    public void   setName(String name)        { this.name = name; }

    public int    getCapacity()               { return capacity; }
    public void   setCapacity(int capacity)   { this.capacity = capacity; }

    public List<String> getSensorIds()                 { return sensorIds; }
    public void setSensorIds(List<String> sensorIds)   {
        this.sensorIds = (sensorIds == null) ? new ArrayList<>() : sensorIds;
    }
}
