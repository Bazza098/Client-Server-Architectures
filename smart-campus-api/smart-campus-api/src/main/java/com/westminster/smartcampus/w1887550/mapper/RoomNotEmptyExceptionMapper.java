package com.westminster.smartcampus.w1887550.mapper;

import com.westminster.smartcampus.w1887550.exception.RoomNotEmptyException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps {@link RoomNotEmptyException} to HTTP 409 Conflict with a JSON body
 * explaining why the deletion was rejected.
 *
 * @author Abdul (w1887550)
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error",       "Conflict");
        body.put("status",      409);
        body.put("code",        "ROOM_NOT_EMPTY");
        body.put("message",     ex.getMessage());
        body.put("roomId",      ex.getRoomId());
        body.put("sensorCount", ex.getSensorCount());
        body.put("hint",        "Reassign or delete the sensors before deleting the room.");

        return Response.status(Response.Status.CONFLICT)
                       .type(MediaType.APPLICATION_JSON)
                       .entity(body)
                       .build();
    }
}
