package com.packetinspector.dpi.exception;

public class InvalidPcapException extends RuntimeException {
    public InvalidPcapException(String message, Throwable cause) {
        super(message, cause);
    }
}
