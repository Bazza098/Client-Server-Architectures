package com.westminster.smartcampus.w1887550.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root discovery endpoint. Returns API metadata and HATEOAS links.
 *
 * GET /api/v1/
 *
 * @author Abdul (w1887550)
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover(@Context UriInfo uriInfo) {

        String base = uriInfo.getBaseUri().toString();

        Map<String, String> links = new LinkedHashMap<>();
        links.put("self",    base);
        links.put("rooms",   base + "rooms");
        links.put("sensors", base + "sensors");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service",       "Smart Campus Sensor & Room Management API");
        body.put("version",       "1.0.0");
        body.put("apiVersion",    "v1");
        body.put("author",        "Abdul");
        body.put("studentId",     "w1887550");
        body.put("module",        "5COSC022W Client-Server Architectures");
        body.put("contact",       "w1887550@my.westminster.ac.uk");
        body.put("_links",        links);

        return Response.ok(body).build();
    }
}
