package com.auto.app.easycar.exceptions;

/** El usuario autenticado no tiene permiso sobre el recurso (HTTP 403). */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String message) {
        super(message);
    }
}
