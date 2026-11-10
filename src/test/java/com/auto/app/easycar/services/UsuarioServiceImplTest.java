package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.UsuarioRequest;
import com.auto.app.easycar.dto.UsuarioResponse;
import com.auto.app.easycar.dto.UsuarioUpdateRequest;
import com.auto.app.easycar.entitis.Usuario;
import com.auto.app.easycar.exceptions.DuplicateResourceException;
import com.auto.app.easycar.exceptions.ResourceNotFoundException;
import com.auto.app.easycar.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl service;

    @Test
    void creaUsuarioCifrandoLaContrasenaYNormalizandoElEmail() {
        when(repository.existsByEmailIgnoreCase("santiago@easycar.com")).thenReturn(false);
        when(passwordEncoder.encode("Easycar123")).thenReturn("$2a$10$hashCifrado");
        when(repository.save(any(Usuario.class))).thenAnswer(invocacion -> {
            Usuario guardado = invocacion.getArgument(0);
            guardado.setId(UUID.randomUUID());
            return guardado;
        });

        UsuarioResponse response = service.create(
                new UsuarioRequest("santiago", "  Santiago@EasyCar.com  ", "Easycar123"));

        assertThat(response.email()).isEqualTo("santiago@easycar.com");
        assertThat(response.usuario()).isEqualTo("santiago");
        assertThat(response.id()).isNotNull();
        verify(passwordEncoder).encode("Easycar123");
        verify(repository).save(org.mockito.ArgumentMatchers.argThat(
                usuario -> "$2a$10$hashCifrado".equals(usuario.getPassword())));
    }

    @Test
    void rechazaEmailDuplicadoConExcepcionDeConflicto() {
        when(repository.existsByEmailIgnoreCase("santiago@easycar.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(
                new UsuarioRequest("santiago", "santiago@easycar.com", "Easycar123")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("santiago@easycar.com");

        verify(passwordEncoder, never()).encode(any());
        verify(repository, never()).save(any());
    }

    @Test
    void actualizarSinContrasenaConservaElHashActual() {
        UUID id = UUID.randomUUID();
        Usuario existente = new Usuario();
        existente.setId(id);
        existente.setUsuario("viejo");
        existente.setEmail("viejo@easycar.com");
        existente.setPassword("$2a$10$hashOriginal");

        when(repository.findById(id)).thenReturn(Optional.of(existente));
        when(repository.findByEmailIgnoreCase("nuevo@easycar.com")).thenReturn(Optional.empty());
        when(repository.save(existente)).thenReturn(existente);

        UsuarioResponse response = service.update(id,
                new UsuarioUpdateRequest("nuevo", "Nuevo@EasyCar.com", null));

        assertThat(response.usuario()).isEqualTo("nuevo");
        assertThat(response.email()).isEqualTo("nuevo@easycar.com");
        assertThat(existente.getPassword()).isEqualTo("$2a$10$hashOriginal");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void actualizarConContrasenaLaVuelveACifrar() {
        UUID id = UUID.randomUUID();
        Usuario existente = new Usuario();
        existente.setId(id);
        existente.setEmail("viejo@easycar.com");
        existente.setPassword("$2a$10$hashOriginal");

        when(repository.findById(id)).thenReturn(Optional.of(existente));
        when(repository.findByEmailIgnoreCase("viejo@easycar.com")).thenReturn(Optional.of(existente));
        when(passwordEncoder.encode("NuevaClave1")).thenReturn("$2a$10$hashNuevo");
        when(repository.save(existente)).thenReturn(existente);

        service.update(id, new UsuarioUpdateRequest("viejo", "viejo@easycar.com", "NuevaClave1"));

        assertThat(existente.getPassword()).isEqualTo("$2a$10$hashNuevo");
    }

    @Test
    void actualizarAEmaillDeOtroUsuarioLanzaConflicto() {
        UUID id = UUID.randomUUID();
        Usuario existente = new Usuario();
        existente.setId(id);
        existente.setEmail("actual@easycar.com");

        Usuario otro = new Usuario();
        otro.setId(UUID.randomUUID());
        otro.setEmail("ocupado@easycar.com");

        when(repository.findById(id)).thenReturn(Optional.of(existente));
        when(repository.findByEmailIgnoreCase("ocupado@easycar.com")).thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> service.update(id,
                new UsuarioUpdateRequest("actual", "ocupado@easycar.com", null)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void buscarPorIdInexistenteLanzaNoEncontrado() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void eliminarInexistenteLanzaNoEncontrado() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).delete(any());
    }
}
