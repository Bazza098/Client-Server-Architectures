package com.westminster.smartcampus.w1887550.storage;

import com.westminster.smartcampus.w1887550.model.Room;
import com.westminster.smartcampus.w1887550.model.Sensor;
import com.westminster.smartcampus.w1887550.model.SensorReading;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe singleton that keeps every Room, Sensor and SensorReading
 * in memory. We use {@link ConcurrentHashMap} so that the JAX-RS runtime,
 * which creates one resource instance per request by default, can safely
 * read and write from multiple worker threads without races.
 *
 * No database is used – the coursework brief forbids it.
 *
 * @author Abdul (w1887550)
 */
public final class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room>    rooms    = new ConcurrentHashMap<>();
    private final Map<String, Sensor>  sensors  = new ConcurrentHashMap<>();

    /** sensorId -> list of its readings.  The list itself is synchronized. */
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private DataStore() {
        seed();
    }

    public static DataStore getInstance() { return INSTANCE; }

    // ---------- Rooms ----------
    public Collection<Room> allRooms()                 { return rooms.values(); }
    public Room  getRoom(String id)                    { return rooms.get(id); }
    public void  putRoom(Room r)                       { rooms.put(r.getId(), r); }
    public Room  removeRoom(String id)                 { return rooms.remove(id); }

    // ---------- Sensors ----------
    public Collection<Sensor> allSensors()             { return sensors.values(); }
    public Sensor getSensor(String id)                 { return sensors.get(id); }
    public void   putSensor(Sensor s)                  { sensors.put(s.getId(), s); }
    public Sensor removeSensor(String id)              { return sensors.remove(id); }

    // ---------- Readings ----------
    public List<SensorReading> getReadings(String sensorId) {
        return readings.computeIfAbsent(sensorId,
                k -> Collections.synchronizedList(new ArrayList<>()));
    }

    public void addReading(String sensorId, SensorReading r) {
        getReadings(sensorId).add(r);
    }

    public void removeReadingsFor(String sensorId)     { readings.remove(sensorId); }

    // ---------- Seed data so the demo isn't empty ----------
    private void seed() {
        Room r1 = new Room("LIB-301", "Library Quiet Study",   40);
        Room r2 = new Room("LAB-101", "Computer Science Lab",  60);
        putRoom(r1);
        putRoom(r2);

        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE",      21.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-002",  "CO2",         "ACTIVE",      420.0, "LIB-301");
        Sensor s3 = new Sensor("OCC-003",  "Occupancy",   "MAINTENANCE",  0.0, "LAB-101");
        putSensor(s1);
        putSensor(s2);
        putSensor(s3);

        r1.getSensorIds().add(s1.getId());
        r1.getSensorIds().add(s2.getId());
        r2.getSensorIds().add(s3.getId());
    }
}
