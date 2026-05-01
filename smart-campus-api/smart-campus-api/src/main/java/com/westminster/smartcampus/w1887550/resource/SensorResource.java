package com.westminster.smartcampus.w1887550.resource;

import com.westminster.smartcampus.w1887550.exception.LinkedResourceNotFoundException;
import com.westminster.smartcampus.w1887550.model.Room;
import com.westminster.smartcampus.w1887550.model.Sensor;
import com.westminster.smartcampus.w1887550.storage.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Resource class for managing Sensors.  Base path: /api/v1/sensors
 *
 *   GET    /sensors                    -> list sensors (optional ?type= filter)
 *   POST   /sensors                    -> register a new sensor (validates roomId)
 *   GET    /sensors/{id}               -> fetch one sensor
 *   DELETE /sensors/{id}               -> remove a sensor
 *   *      /sensors/{id}/readings      -> delegated to SensorReadingResource (sub-resource locator)
 *
 * @author Abdul (w1887550)
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    /** List all sensors, optionally filtered by ?type=... (case-insensitive). */
    @GET
    public Response listSensors(@QueryParam("type") String type) {
        Collection<Sensor> all = store.allSensors();

        if (type == null || type.trim().isEmpty()) {
            return Response.ok(all).build();
        }

        List<Sensor> filtered = all.stream()
            .filter(s -> s.getType() != null && s.getType().equalsIgnoreCase(type.trim()))
            .collect(Collectors.toList());
        return Response.ok(filtered).build();
    }

    /**
     * Register a new sensor.  Validates that the referenced roomId exists,
     * otherwise a LinkedResourceNotFoundException is thrown (HTTP 422).
     */
    @POST
    public Response createSensor(Sensor newSensor, @Context UriInfo uriInfo) {

        if (newSensor == null
                || newSensor.getId() == null || newSensor.getId().trim().isEmpty()
                || newSensor.getRoomId() == null || newSensor.getRoomId().trim().isEmpty()) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error",   "Bad Request");
            err.put("status",  400);
            err.put("message", "Fields 'id' and 'roomId' are required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        if (store.getSensor(newSensor.getId()) != null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error",   "Conflict");
            err.put("status",  409);
            err.put("message", "A sensor with id '" + newSensor.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(err).build();
        }

        // === Foreign-key validation ===
        Room parentRoom = store.getRoom(newSensor.getRoomId());
        if (parentRoom == null) {
            throw new LinkedResourceNotFoundException("roomId", newSensor.getRoomId());
        }

        // Default status if not supplied
        if (newSensor.getStatus() == null || newSensor.getStatus().trim().isEmpty()) {
            newSensor.setStatus("ACTIVE");
        }

        store.putSensor(newSensor);
        parentRoom.getSensorIds().add(newSensor.getId());

        URI location = uriInfo.getAbsolutePathBuilder()
                              .path(newSensor.getId())
                              .build();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Sensor registered successfully.");
        body.put("sensor",  newSensor);

        return Response.created(location).entity(body).build();
    }

    /** Fetch a single sensor. */
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor s = store.getSensor(sensorId);
        if (s == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error",   "Not Found");
            err.put("status",  404);
            err.put("message", "No sensor exists with id '" + sensorId + "'.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        return Response.ok(s).build();
    }

    /** Decommission a sensor and detach it from its parent room. */
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor s = store.getSensor(sensorId);
        if (s == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error",   "Not Found");
            err.put("status",  404);
            err.put("message", "No sensor exists with id '" + sensorId + "'.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }

        Room parent = store.getRoom(s.getRoomId());
        if (parent != null) {
            parent.getSensorIds().remove(sensorId);
        }

        store.removeSensor(sensorId);
        store.removeReadingsFor(sensorId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Sensor '" + sensorId + "' decommissioned successfully.");
        return Response.ok(body).build();
    }

    /**
     * === Sub-Resource Locator ===
     *
     * Any request to /sensors/{sensorId}/readings (or deeper) is delegated to
     * a new {@link SensorReadingResource} instance that is bound to the
     * specific sensor identified by sensorId.  This keeps the history logic
     * out of this class and enables clean nesting.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource readings(@PathParam("sensorId") String sensorId) {
        // Eagerly confirm the sensor exists so the client gets a clear error.
        if (store.getSensor(sensorId) == null) {
            throw new LinkedResourceNotFoundException("sensorId", sensorId);
        }
        return new SensorReadingResource(sensorId);
    }
}
