package com.homecloud.planner.common;

/**
 * Signals that a request is well-formed but cannot be applied because it
 * conflicts with the current state of the resource (e.g. deleting a phase
 * that still has tasks).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
