package com.westminster.smartcampus.w1887550.resource;

import com.westminster.smartcampus.w1887550.exception.RoomNotEmptyException;
import com.westminster.smartcampus.w1887550.model.Room;
import com.westminster.smartcampus.w1887550.storage.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Resource class for managing Rooms.  Base path: /api/v1/rooms
 *
 *   GET    /rooms         -> list every room
 *   POST   /rooms         -> create a new room
 *   GET    /rooms/{id}    -> fetch one room
 *   DELETE /rooms/{id}    -> remove a room (blocked if it has sensors)
 *
 * @author Abdul (w1887550)
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    private final DataStore store = DataStore.getInstance();

    /** List all rooms. */
    @GET
    public Response listRooms() {
        Collection<Room> rooms = store.allRooms();
        return Response.ok(rooms).build();
    }

    /** Create a new room. Returns 201 + Location header. */
    @POST
    public Response createRoom(Room newRoom, @Context UriInfo uriInfo) {

        if (newRoom == null || newRoom.getId() == null || newRoom.getId().trim().isEmpty()) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error",   "Bad Request");
            err.put("status",  400);
            err.put("message", "Field 'id' is required to create a room.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        if (store.getRoom(newRoom.getId()) != null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error",   "Conflict");
            err.put("status",  409);
            err.put("message", "A room with id '" + newRoom.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(err).build();
        }

        store.putRoom(newRoom);

        // Build Location header pointing at the new room
        URI location = uriInfo.getAbsolutePathBuilder()
                              .path(newRoom.getId())
                              .build();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Room created successfully.");
        body.put("room",    newRoom);

        return Response.created(location).entity(body).build();
    }

    /** Fetch a single room by id. */
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error",   "Not Found");
            err.put("status",  404);
            err.put("message", "No room exists with id '" + roomId + "'.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        return Response.ok(room).build();
    }

    /**
     * Delete a room.  Business rule: if the room still has sensors
     * assigned to it, the deletion is blocked and we raise
     * {@link RoomNotEmptyException} (mapped to 409 Conflict).
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error",   "Not Found");
            err.put("status",  404);
            err.put("message", "No room exists with id '" + roomId + "'.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }

        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId, room.getSensorIds().size());
        }

        store.removeRoom(roomId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Room '" + roomId + "' deleted successfully.");
        return Response.ok(body).build();
    }
}
