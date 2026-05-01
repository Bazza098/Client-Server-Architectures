package com.westminster.smartcampus.w1887550.exception;

/**
 * Thrown when a caller tries to DELETE a Room that still has
 * sensors registered against it.  Mapped to HTTP 409 Conflict.
 *
 * @author Abdul (w1887550)
 */
public class RoomNotEmptyException extends RuntimeException {

    private final String roomId;
    private final int    sensorCount;

    public RoomNotEmptyException(String roomId, int sensorCount) {
        super("Room '" + roomId + "' cannot be deleted because it still has "
              + sensorCount + " sensor(s) assigned to it.");
        this.roomId      = roomId;
        this.sensorCount = sensorCount;
    }

    public String getRoomId()      { return roomId; }
    public int    getSensorCount() { return sensorCount; }
}
