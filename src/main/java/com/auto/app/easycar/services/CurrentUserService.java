package com.auto.app.easycar.services;

import com.auto.app.easycar.entitis.Usuario;
import com.auto.app.easycar.exceptions.AccesoDenegadoException;
import com.auto.app.easycar.repositories.UsuarioRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Resuelve el usuario autenticado de la peticion actual. El JWT solo lleva el
 * email como subject, asi que aqui se traduce a la entidad Usuario.
 */
@Service
public class CurrentUserService {

    private final UsuarioRepository usuarioRepository;

    public CurrentUserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario actual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new AccesoDenegadoException("No hay un usuario autenticado en la peticion");
        }
        return usuarioRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new AccesoDenegadoException("El usuario autenticado no existe"));
    }

    public UUID actualId() {
        return actual().getId();
    }
}
