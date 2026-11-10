package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.auth.LoginRequest;
import com.auto.app.easycar.dto.auth.LoginResponse;

public interface AuthService {

    /**
     * Valida las credenciales y genera el token JWT.
     */
    LoginResponse login(LoginRequest request);
}
