package com.westminster.smartcampus.w1887550.app;

import com.westminster.smartcampus.w1887550.filter.LoggingFilter;
import com.westminster.smartcampus.w1887550.mapper.GenericExceptionMapper;
import com.westminster.smartcampus.w1887550.mapper.LinkedResourceNotFoundExceptionMapper;
import com.westminster.smartcampus.w1887550.mapper.RoomNotEmptyExceptionMapper;
import com.westminster.smartcampus.w1887550.mapper.SensorUnavailableExceptionMapper;
import com.westminster.smartcampus.w1887550.resource.DiscoveryResource;
import com.westminster.smartcampus.w1887550.resource.SensorReadingResource;
import com.westminster.smartcampus.w1887550.resource.SensorResource;
import com.westminster.smartcampus.w1887550.resource.SensorRoomResource;
import com.westminster.smartcampus.w1887550.storage.DataStore;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.logging.Logger;

/**
 * JAX-RS application entry point.
 *
 * Extends Jersey's {@link ResourceConfig} (which itself extends
 * jakarta.ws.rs.core.Application). This is the correct pattern for GlassFish 7:
 *
 *  - @ApplicationPath("/api/v1") sets the sub-path after the WAR context root.
 *  - All resources, mappers, filters and Jackson are explicitly registered so
 *    GlassFish does not need to do classpath scanning (more reliable).
 *  - Jersey and HK2 classes are available at compile time via provided scope
 *    in pom.xml and are present at runtime inside GlassFish 7.
 *
 * Full base URL: http://localhost:8080/smart-campus-api/api/v1/
 *
 * === JAX-RS Resource Lifecycle ===
 * By default JAX-RS creates a NEW resource class instance per HTTP request
 * (per-request scope). Instance fields on resource classes are therefore
 * NOT shared between requests. The shared state lives exclusively in
 * DataStore (a singleton), which uses ConcurrentHashMap so multiple
 * concurrent requests are safely handled without race conditions or data loss.
 *
 * @author Abdul (w1887550)
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {

    private static final Logger LOG = Logger.getLogger(SmartCampusApplication.class.getName());

    public SmartCampusApplication() {

        // ── REST resource classes ─────────────────────────────────────────────
        // SensorReadingResource is NOT registered here — it is instantiated
        // by the sub-resource locator in SensorResource (JAX-RS pattern).
        register(DiscoveryResource.class);
        register(SensorRoomResource.class);
        register(SensorResource.class);

        // ── Exception mappers ─────────────────────────────────────────────────
        register(RoomNotEmptyExceptionMapper.class);           // 409 Conflict
        register(LinkedResourceNotFoundExceptionMapper.class); // 422 Unprocessable
        register(SensorUnavailableExceptionMapper.class);      // 403 Forbidden
        register(GenericExceptionMapper.class);                // 500 catch-all

        // ── Filters ───────────────────────────────────────────────────────────
        register(LoggingFilter.class);

        // ── JSON serialisation via Jackson ────────────────────────────────────
        // JacksonFeature registers MessageBodyWriter/Reader for application/json.
        // The Jackson jars are bundled in WEB-INF/lib (compile scope in pom.xml)
        // because GlassFish 7 does not expose its internal Jackson to web apps.
        register(JacksonFeature.class);

        // Eagerly initialise DataStore so seed data is in place before the first
        // request arrives (defensive — getInstance() is also called lazily).
        DataStore.getInstance();

        LOG.info("================================================");
        LOG.info(" Smart Campus API - Abdul (w1887550)");
        LOG.info(" Context root : /smart-campus-api");
        LOG.info(" App path     : /api/v1");
        LOG.info(" Base URL     : http://localhost:8080/smart-campus-api/api/v1/");
        LOG.info("================================================");
    }
}
