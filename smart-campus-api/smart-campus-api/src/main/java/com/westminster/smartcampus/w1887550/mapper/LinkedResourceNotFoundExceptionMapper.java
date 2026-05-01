package com.westminster.smartcampus.w1887550.mapper;

import com.westminster.smartcampus.w1887550.exception.LinkedResourceNotFoundException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps {@link LinkedResourceNotFoundException} to HTTP 422 Unprocessable
 * Entity.  This is more semantically accurate than 404 because the URL
 * itself is valid, but a reference inside the payload points at a
 * non-existent resource.
 *
 * @author Abdul (w1887550)
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    /** There is no constant for 422 in {@link Response.Status}, so we define it. */
    private static final int UNPROCESSABLE_ENTITY = 422;

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error",          "Unprocessable Entity");
        body.put("status",         UNPROCESSABLE_ENTITY);
        body.put("code",           "LINKED_RESOURCE_NOT_FOUND");
        body.put("message",        ex.getMessage());
        body.put("referenceField", ex.getReferenceField());
        body.put("referenceValue", ex.getReferenceValue());
        body.put("hint",           "Create the referenced resource first, or correct the id.");

        return Response.status(UNPROCESSABLE_ENTITY)
                       .type(MediaType.APPLICATION_JSON)
                       .entity(body)
                       .build();
    }
}
