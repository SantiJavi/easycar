package com.auto.app.easycar.exceptions;

/**
 * Se lanza cuando el recurso solicitado no existe. El handler global la traduce a HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
