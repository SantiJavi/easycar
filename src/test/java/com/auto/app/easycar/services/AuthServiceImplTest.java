package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.auth.LoginRequest;
import com.auto.app.easycar.dto.auth.LoginResponse;
import com.auto.app.easycar.entitis.Usuario;
import com.auto.app.easycar.repositories.UsuarioRepository;
import com.auto.app.easycar.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AuthServiceImpl service;

    @Test
    void devuelveTokenCuandoLasCredencialesSonValidas() {
        UUID id = UUID.randomUUID();
        UserDetails userDetails = User.builder()
                .username("santiago@easycar.com")
                .password("$2a$10$hash")
                .authorities(List.of())
                .build();

        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsuario("santiago");
        usuario.setEmail("santiago@easycar.com");

        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(userDetails, null, List.of()));
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-de-prueba");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);
        when(usuarioRepository.findByEmailIgnoreCase("santiago@easycar.com")).thenReturn(Optional.of(usuario));

        LoginResponse response = service.login(new LoginRequest("  Santiago@EasyCar.com ", "Easycar123"));

        assertThat(response.token()).isEqualTo("jwt-de-prueba");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
        assertThat(response.usuarioId()).isEqualTo(id);
        assertThat(response.email()).isEqualTo("santiago@easycar.com");
    }

    @Test
    void normalizaElEmailAntesDeAutenticar() {
        UserDetails userDetails = User.builder()
                .username("santiago@easycar.com")
                .password("$2a$10$hash")
                .authorities(List.of())
                .build();

        when(authenticationManager.authenticate(argThat(auth ->
                "santiago@easycar.com".equals(((UsernamePasswordAuthenticationToken) auth).getPrincipal()))))
                .thenReturn(new UsernamePasswordAuthenticationToken(userDetails, null, List.of()));
        when(jwtService.generateToken(userDetails)).thenReturn("jwt");
        when(usuarioRepository.findByEmailIgnoreCase("santiago@easycar.com")).thenReturn(Optional.of(new Usuario()));

        service.login(new LoginRequest("SANTIAGO@easycar.com", "Easycar123"));

        // La verificacion esta en el matcher de authenticate(); llegar aqui sin excepcion es el assert.
        assertThat(true).isTrue();
    }

    @Test
    void propagaCredencialesInvalidas() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> service.login(new LoginRequest("santiago@easycar.com", "mala")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
