package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.UserVehiculoRequest;
import com.auto.app.easycar.dto.UserVehiculoResponse;
import com.auto.app.easycar.entitis.UserVehiculo;
import com.auto.app.easycar.entitis.Usuario;
import com.auto.app.easycar.entitis.Vehiculo;
import com.auto.app.easycar.exceptions.DuplicateResourceException;
import com.auto.app.easycar.exceptions.ResourceNotFoundException;
import com.auto.app.easycar.repositories.UserVehiculoRepository;
import com.auto.app.easycar.repositories.UsuarioRepository;
import com.auto.app.easycar.repositories.VehiculoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserVehiculoServiceImplTest {

    @Mock
    private UserVehiculoRepository repository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @InjectMocks
    private UserVehiculoServiceImpl service;

    @Test
    void asociaUsuarioYVehiculo() {
        UUID usuarioId = UUID.randomUUID();
        UUID vehiculoId = UUID.randomUUID();

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(vehiculoId);

        when(repository.existsByFkUserVehiculo_IdAndFkVehiculoUser_Id(usuarioId, vehiculoId)).thenReturn(false);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(vehiculoRepository.findById(vehiculoId)).thenReturn(Optional.of(vehiculo));
        when(repository.save(any(UserVehiculo.class))).thenAnswer(invocacion -> {
            UserVehiculo guardado = invocacion.getArgument(0);
            guardado.setId(1L);
            return guardado;
        });

        UserVehiculoResponse response = service.create(new UserVehiculoRequest(usuarioId, vehiculoId));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.usuarioId()).isEqualTo(usuarioId);
        assertThat(response.vehiculoId()).isEqualTo(vehiculoId);
    }

    @Test
    void rechazaAsociacionDuplicada() {
        UUID usuarioId = UUID.randomUUID();
        UUID vehiculoId = UUID.randomUUID();
        when(repository.existsByFkUserVehiculo_IdAndFkVehiculoUser_Id(usuarioId, vehiculoId)).thenReturn(true);

        assertThatThrownBy(() -> service.create(new UserVehiculoRequest(usuarioId, vehiculoId)))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void fallaSiElUsuarioNoExiste() {
        UUID usuarioId = UUID.randomUUID();
        UUID vehiculoId = UUID.randomUUID();

        when(repository.existsByFkUserVehiculo_IdAndFkVehiculoUser_Id(usuarioId, vehiculoId)).thenReturn(false);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new UserVehiculoRequest(usuarioId, vehiculoId)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(usuarioId.toString());
    }

    @Test
    void buscarPorIdInexistenteLanzaNoEncontrado() {
        when(repository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(3L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
