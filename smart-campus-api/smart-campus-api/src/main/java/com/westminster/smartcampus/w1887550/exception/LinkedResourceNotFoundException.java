package com.westminster.smartcampus.w1887550.exception;

/**
 * Thrown when a client submits a payload whose foreign-key reference
 * (e.g. {@code roomId}) points at a resource that does not exist.
 * Mapped to HTTP 422 Unprocessable Entity.
 *
 * @author Abdul (w1887550)
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    private final String referenceField;
    private final String referenceValue;

    public LinkedResourceNotFoundException(String referenceField, String referenceValue) {
        super("The referenced " + referenceField + " '" + referenceValue
              + "' does not exist in the system.");
        this.referenceField = referenceField;
        this.referenceValue = referenceValue;
    }

    public String getReferenceField() { return referenceField; }
    public String getReferenceValue() { return referenceValue; }
}
