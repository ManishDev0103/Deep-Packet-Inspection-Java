package com.packetinspector.dpi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPcapException extends RuntimeException {
    public InvalidPcapException(String message) {
        super(message);
    }

    public InvalidPcapException(String message, Throwable cause) {
        super(message, cause);
    }
}
