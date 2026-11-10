package com.auto.app.easycar.security;

import com.auto.app.easycar.entitis.Usuario;
import com.auto.app.easycar.repositories.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Carga el usuario desde la tabla "usuario" usando el email como username.
 * La contrasena almacenada ya esta cifrada con BCrypt; Spring Security compara con PasswordEncoder.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontro el usuario con email " + email));

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                // El modelo de datos no tiene roles: todo usuario autenticado puede consumir la API.
                .authorities(List.of())
                .build();
    }
}
