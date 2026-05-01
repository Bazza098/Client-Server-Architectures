package com.westminster.smartcampus.w1887550.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Cross-cutting logging filter.  By implementing both
 * {@link ContainerRequestFilter} and {@link ContainerResponseFilter}
 * in a single {@link Provider}, we centralise request/response logging
 * for every endpoint without polluting the resource classes.
 *
 * Each inbound HTTP request is logged with its method and URI, and each
 * outbound response is logged with the matching method, URI and final
 * HTTP status code.
 *
 * @author Abdul (w1887550)
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String uri    = requestContext.getUriInfo().getRequestUri().toString();
        LOG.info(">>> REQUEST  " + method + " " + uri);
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        String method = requestContext.getMethod();
        String uri    = requestContext.getUriInfo().getRequestUri().toString();
        int    status = responseContext.getStatus();
        LOG.info("<<< RESPONSE " + method + " " + uri + "  ->  HTTP " + status);
    }
}
