# Smart Campus Sensor & Room Management API

**Student:** Abdul | **ID:** w1887550 | **Module:** 5COSC022W Client-Server Architectures

A RESTful API built with JAX-RS (Jersey) and GlassFish 7 for managing campus rooms, sensors, and historical sensor readings.

---

## API Overview

The Smart Campus API provides a versioned REST interface at `/api/v1`. It manages three core resources:

- **Rooms** – Physical spaces on campus (e.g. `LIB-301`, `LAB-101`)
- **Sensors** – Devices deployed in rooms (temperature, CO2, occupancy, etc.)
- **Sensor Readings** – Historical measurement log per sensor (sub-resource)

All data is stored in-memory using `ConcurrentHashMap` and `Collections.synchronizedList`. No database is used.

---

## Technology Stack

| Component | Choice |
|-----------|--------|
| Language | Java 17 |
| JAX-RS Implementation | Jersey 3.x (Jakarta EE 9+) |
| Server | GlassFish 7 (embedded via WAR deployment) |
| JSON | Jackson via `JacksonFeature` |
| Build Tool | Maven |

---

## How to Build & Run

### Prerequisites

- Java 17+
- Maven 3.8+
- GlassFish 7 installed (or use the bundled WAR approach)

### Steps

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd smart-campus-api
   ```

2. **Build the WAR file**
   ```bash
   mvn clean package
   ```
   The WAR is generated at `target/smart-campus-api.war`.

3. **Deploy to GlassFish 7**
   ```bash
   # Copy WAR to GlassFish autodeploy folder
   cp target/smart-campus-api.war $GLASSFISH_HOME/domains/domain1/autodeploy/

   # Start GlassFish (if not already running)
   $GLASSFISH_HOME/bin/asadmin start-domain domain1
   ```

4. **Verify the server is up**
   ```bash
   curl http://localhost:8080/smart-campus-api/api/v1
   ```

The API base URL is: `http://localhost:8080/smart-campus-api/api/v1`

---

## Sample curl Commands

### 1. Discovery Endpoint
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1
```

### 2. List all Rooms
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms
```

### 3. Create a new Room
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"ENG-205","name":"Engineering Lab","capacity":30}'
```

### 4. Register a new Sensor (with valid roomId)
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-099","type":"Temperature","status":"ACTIVE","currentValue":20.0,"roomId":"LIB-301"}'
```

### 5. Filter Sensors by type
```bash
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2"
```

### 6. Post a Sensor Reading
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":23.7}'
```

### 7. Get Reading History for a Sensor
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings
```

### 8. Attempt to DELETE a Room with active sensors (triggers 409)
```bash
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301
```

### 9. Register a Sensor with non-existent roomId (triggers 422)
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"BAD-001","type":"Temperature","roomId":"DOES-NOT-EXIST"}'
```

### 10. Post reading to a MAINTENANCE sensor (triggers 403)
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/OCC-003/readings \
  -H "Content-Type: application/json" \
  -d '{"value":5.0}'
```

---

## Report: Answers to Coursework Questions

---

### Part 1 – Service Architecture & Setup

#### 1.1 – JAX-RS Resource Lifecycle

By default, JAX-RS creates a **new resource class instance for every incoming HTTP request** (per-request scope). This means that any instance variables declared directly on a resource class are not shared between requests — they live and die within a single request cycle.

This architectural decision has a direct and important impact on in-memory data management. Because each request gets its own resource object, storing data as instance fields on a resource class would cause all data to be lost the moment the request ends. To avoid this, all shared state in this project is held in the **`DataStore` singleton** — a class that is created once and lives for the lifetime of the application. The `DataStore` uses `ConcurrentHashMap` (for rooms and sensors) and `Collections.synchronizedList` (for reading lists) to allow multiple concurrent per-request resource instances to safely read and write shared data without race conditions or data loss. Without this synchronisation strategy, two concurrent POST requests could overwrite each other's data or produce inconsistent results.

#### 1.2 – HATEOAS and Hypermedia-Driven Design

HATEOAS (Hypermedia as the Engine of Application State) is considered a hallmark of advanced RESTful design because it makes an API **self-documenting and self-navigable at runtime**. Rather than requiring clients to have prior knowledge of all available URLs, a HATEOAS-compliant response embeds links directly in the JSON payload, pointing to related resources and available actions.

The Discovery endpoint in this project (`GET /api/v1`) demonstrates this: it returns a `_links` object containing the URLs for `rooms` and `sensors`, allowing a client to navigate the entire API starting from a single known entry point. This benefits client developers significantly compared to static documentation — if URLs change or new resources are added, the server-side response is updated and clients that follow links dynamically adapt automatically, without requiring documentation updates or client code changes.

---

### Part 2 – Room Management

#### 2.1 – Returning IDs vs Full Room Objects

When returning a list of rooms, there are two design options. Returning **only IDs** produces a very small payload and is efficient on the network, but it forces the client to issue a separate `GET /rooms/{id}` request for every room it wants details about — known as the N+1 problem. This multiplies HTTP round-trips and increases overall latency.

Returning **full room objects** (the approach used here) transfers everything in a single response. The trade-off is a larger payload, but for typical campus data volumes this is entirely acceptable. The client can process the full list immediately without further requests. For very large collections, pagination can be introduced to balance bandwidth and usability — but for this use case, returning full objects in one list is the better client experience.

#### 2.2 – Idempotency of DELETE

Yes, the DELETE operation is idempotent in this implementation, and this is consistent with the HTTP specification. **Idempotency** means that making the same request multiple times produces the same server state as making it once.

In this API, the first `DELETE /rooms/{roomId}` removes the room from the `DataStore` and returns `200 OK`. If a client then sends the exact same `DELETE` request a second time, the room no longer exists, and the server returns `404 Not Found`. Crucially, the server state is identical in both cases — the room is absent. The response code differs (200 vs 404), but the state of the data does not change after the first deletion. This confirms idempotency: no matter how many times the client repeats the request, it cannot delete the room twice or corrupt the data.

---

### Part 3 – Sensor Operations & Linking

#### 3.1 – @Consumes and Content-Type Mismatch

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS that the POST endpoint for sensors only accepts request bodies with a `Content-Type: application/json` header. If a client sends data with a different format — for example `text/plain` or `application/xml` — JAX-RS will reject the request **before the method is even invoked** and return an **HTTP 415 Unsupported Media Type** response automatically.

This is handled entirely by the JAX-RS framework. Jersey inspects the incoming `Content-Type` header, compares it against the declared `@Consumes` value, and if there is a mismatch it short-circuits the request with a 415 error. The resource method body never executes. This provides a clean, consistent boundary: the method is only called when the data format is guaranteed to be one the application can actually deserialise.

#### 3.2 – @QueryParam vs Path-Based Filtering

Using `@QueryParam` (e.g. `GET /api/v1/sensors?type=CO2`) is considered superior to embedding the filter in the path (e.g. `/api/vl/sensors/type/CO2`) for several reasons.

First, **semantic clarity**: a path segment identifies a specific resource, while a query parameter expresses optional search or filter criteria on a collection. `/sensors` identifies the sensor collection — the `type` query parameter is a modifier on that collection, not a distinct resource.

Second, **flexibility**: query parameters are easily combined (`?type=CO2&status=ACTIVE`), optional by nature, and extensible without changing the URL structure. Adding a second filter with path segments would require a new URL pattern for every combination.

Third, **client tooling**: HTTP clients, browsers, and caching proxies all understand that query parameters represent variable search conditions, while path segments represent resource identity. This distinction matters for cache key generation and API discoverability.

---

### Part 4 – Sub-Resources

#### 4.1 – Sub-Resource Locator Pattern

The Sub-Resource Locator pattern provides significant architectural benefits for large APIs. Instead of defining every nested path inside a single monolithic resource class (which would quickly become hundreds of lines with mixed responsibilities), the pattern allows a method annotated without an HTTP verb to **delegate** processing to a separate, purpose-built class.

In this project, `SensorResource` contains a locator method for `/{sensorId}/readings` that returns a new instance of `SensorReadingResource`. All reading logic — GET history, POST new reading, status constraint checking, side-effect updates — lives in `SensorReadingResource`. `SensorResource` remains focused solely on sensor-level concerns.

This separation mirrors the Single Responsibility Principle: each class has one reason to change. It also makes unit testing easier, since `SensorReadingResource` can be tested in isolation with a mock `sensorId`. In a large campus API with dozens of nested resource types, this pattern prevents any single controller class from becoming unmaintainable.

---

### Part 5 – Error Handling & Logging

#### 5.2 – HTTP 422 vs HTTP 404 for Missing References

HTTP 422 (Unprocessable Entity) is semantically more accurate than 404 (Not Found) when a client submits a well-formed JSON payload that contains a reference to a resource that does not exist.

A 404 means the **URL being requested** was not found — the endpoint itself does not exist. In the case of `POST /sensors` with a non-existent `roomId`, the URL `/sensors` is perfectly valid and resolves correctly. The problem is not the URL; it is the **content of the payload**. The `roomId` value inside the JSON body references a room that the system cannot locate.

HTTP 422 was designed precisely for this situation: the request is syntactically correct (valid JSON, correct Content-Type) but semantically invalid because it contains an unresolvable reference. Using 422 communicates a more precise diagnostic to the client — "your data is logically inconsistent" rather than "this page doesn't exist" — and allows client-side error handling to distinguish between navigation errors (404) and data integrity errors (422).

#### 5.4 – Cybersecurity Risks of Exposing Stack Traces

Exposing raw Java stack traces to external API consumers creates several serious security risks.

**Information disclosure**: A stack trace reveals the full internal package structure, class names, method names, and line numbers of the application. An attacker learns the technology stack (e.g. Jersey, GlassFish, Jackson), the application's internal architecture, and the exact source file layout — all of which greatly aid reverse engineering.

**Vulnerability mapping**: Exception messages often include database query fragments, file paths, configuration values, or internal IDs. Even without a database in this project, a `NullPointerException` trace pointing to a specific method reveals business logic that can be exploited.

**Targeted attacks**: Knowing which version of a library is in use (visible from JAR names in stack traces) lets an attacker look up known CVEs for that exact version and craft a targeted exploit.

The `GenericExceptionMapper` in this project prevents all of this by catching every unhandled `Exception`, logging the full trace **server-side only** (invisible to the caller), and returning a generic `500 Internal Server Error` JSON response with no internal details.

#### 5.5 – JAX-RS Filters vs Manual Logging

Using a JAX-RS filter (implementing `ContainerRequestFilter` and `ContainerResponseFilter`) for logging is far superior to inserting `Logger.info()` statements inside every resource method, for several reasons.

**Cross-cutting concern separation**: Logging is not part of any individual resource's business logic. Embedding logger calls in resource methods mixes two unrelated responsibilities and makes the resource classes harder to read, test, and maintain.

**Guaranteed coverage**: A filter registered with the JAX-RS runtime intercepts **every** request and response automatically, including requests rejected by JAX-RS itself before reaching a resource method (e.g. 404 for unknown paths, 415 for wrong content-type). Manual logging inside resource methods cannot capture these cases.

**Single point of change**: If the log format needs updating — for example, adding a correlation ID or response time — there is exactly one class to modify. With manual logging, every resource method across every class would need to be updated individually.

**Consistency**: Filters guarantee that every log entry follows the same format, making log analysis and monitoring tooling straightforward to configure.
