package com.westminster.smartcampus.w1887550.mapper;

import com.westminster.smartcampus.w1887550.exception.SensorUnavailableException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps {@link SensorUnavailableException} to HTTP 403 Forbidden.
 * The sensor exists, but its current state forbids the requested action.
 *
 * @author Abdul (w1887550)
 */
@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error",         "Forbidden");
        body.put("status",        403);
        body.put("code",          "SENSOR_UNAVAILABLE");
        body.put("message",       ex.getMessage());
        body.put("sensorId",      ex.getSensorId());
        body.put("currentStatus", ex.getCurrentStatus());
        body.put("hint",          "Only sensors in status ACTIVE can accept new readings.");

        return Response.status(Response.Status.FORBIDDEN)
                       .type(MediaType.APPLICATION_JSON)
                       .entity(body)
                       .build();
    }
}
