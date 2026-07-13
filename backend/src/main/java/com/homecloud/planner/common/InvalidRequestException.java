package com.homecloud.planner.common;

/**
 * Signals a business-rule validation failure that bean validation cannot
 * express (e.g. a dependency cycle, a self-referencing dependency).
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
