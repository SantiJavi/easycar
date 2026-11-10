package com.auto.app.easycar.exceptions;

/**
 * Se lanza cuando se intenta crear un recurso que ya existe (email o placa duplicados).
 * El handler global la traduce a HTTP 409.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
