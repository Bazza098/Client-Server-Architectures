package com.westminster.smartcampus.w1887550.mapper;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global safety-net mapper. Catches any unhandled Exception and returns
 * a clean HTTP 500 - never exposing a raw stack trace to the client.
 *
 * Uses Exception (not Throwable) for compatibility with GlassFish 7.
 * WebApplicationExceptions are re-emitted unchanged so Jersey's own
 * 404/405/415 responses are preserved.
 *
 * @author Abdul (w1887550)
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Exception ex) {

        if (ex instanceof WebApplicationException) {
            return ((WebApplicationException) ex).getResponse();
        }

        LOG.log(Level.SEVERE, "Unhandled exception reached global safety net", ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error",   "Internal Server Error");
        body.put("status",  500);
        body.put("code",    "UNEXPECTED_ERROR");
        body.put("message", "An unexpected error occurred. "
                          + "Please contact the API administrator if this persists.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .type(MediaType.APPLICATION_JSON)
                       .entity(body)
                       .build();
    }
}
