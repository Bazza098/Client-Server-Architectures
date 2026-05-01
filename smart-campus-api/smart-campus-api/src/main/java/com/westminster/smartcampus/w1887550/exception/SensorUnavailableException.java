package com.westminster.smartcampus.w1887550.exception;

/**
 * Thrown when a caller tries to POST a reading to a sensor whose
 * status is not ACTIVE (e.g. MAINTENANCE or OFFLINE).
 * Mapped to HTTP 403 Forbidden.
 *
 * @author Abdul (w1887550)
 */
public class SensorUnavailableException extends RuntimeException {

    private final String sensorId;
    private final String currentStatus;

    public SensorUnavailableException(String sensorId, String currentStatus) {
        super("Sensor '" + sensorId + "' is currently in status '"
              + currentStatus + "' and cannot accept new readings.");
        this.sensorId      = sensorId;
        this.currentStatus = currentStatus;
    }

    public String getSensorId()      { return sensorId; }
    public String getCurrentStatus() { return currentStatus; }
}
