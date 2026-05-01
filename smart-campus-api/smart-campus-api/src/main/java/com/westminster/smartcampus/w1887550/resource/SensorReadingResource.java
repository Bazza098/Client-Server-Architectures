package com.westminster.smartcampus.w1887550.resource;

import com.westminster.smartcampus.w1887550.exception.SensorUnavailableException;
import com.westminster.smartcampus.w1887550.model.Sensor;
import com.westminster.smartcampus.w1887550.model.SensorReading;
import com.westminster.smartcampus.w1887550.storage.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.*;

/**
 * Sub-resource bound to one specific sensor.  This class is **not** annotated
 * with a class-level @Path because it is always entered via the
 * sub-resource locator method in {@link SensorResource}:
 *
 *     /api/v1/sensors/{sensorId}/readings
 *
 *   GET  /  -> historical readings for the bound sensor
 *   POST /  -> append a new reading and update parent Sensor.currentValue
 *
 * @author Abdul (w1887550)
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String    sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /** Return the full reading history for this sensor. */
    @GET
    public Response getReadings() {
        List<SensorReading> history = store.getReadings(sensorId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sensorId", sensorId);
        body.put("count",    history.size());
        body.put("readings", history);
        return Response.ok(body).build();
    }

    /**
     * Append a new reading.  Side-effects:
     *  1. Assigns a UUID if the client didn't supply an id.
     *  2. Stamps the current time if timestamp == 0.
     *  3. Updates the parent Sensor's currentValue.
     *  4. Rejects the write with 403 Forbidden if the sensor is
     *     not in status ACTIVE.
     */
    @POST
    public Response addReading(SensorReading reading) {

        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            // Defensive - the locator should already have caught this.
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error",   "Not Found");
            err.put("status",  404);
            err.put("message", "No sensor exists with id '" + sensorId + "'.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }

        // Status constraint: only ACTIVE sensors may record data.
        if (!"ACTIVE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }

        if (reading == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error",   "Bad Request");
            err.put("status",  400);
            err.put("message", "Reading payload must contain at least a 'value'.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0L) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        store.addReading(sensorId, reading);

        // ===== Side-effect: update the parent sensor's currentValue =====
        sensor.setCurrentValue(reading.getValue());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message",        "Reading recorded successfully.");
        body.put("sensorId",       sensorId);
        body.put("reading",        reading);
        body.put("sensorCurrent",  sensor.getCurrentValue());

        return Response.status(Response.Status.CREATED).entity(body).build();
    }
}
