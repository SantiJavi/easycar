package com.auto.app.easycar.exceptions;

/** El codigo de invitacion existe pero ya no sirve: expirado o revocado (HTTP 410). */
public class InvitacionNoDisponibleException extends RuntimeException {

    public InvitacionNoDisponibleException(String message) {
        super(message);
    }
}
