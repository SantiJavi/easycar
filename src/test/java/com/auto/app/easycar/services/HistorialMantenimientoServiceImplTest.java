package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.HistorialMantenimientoRequest;
import com.auto.app.easycar.dto.HistorialMantenimientoResponse;
import com.auto.app.easycar.entitis.HistorialMantenimientos;
import com.auto.app.easycar.entitis.TipoMantenimiento;
import com.auto.app.easycar.entitis.Usuario;
import com.auto.app.easycar.entitis.Vehiculo;
import com.auto.app.easycar.exceptions.AccesoDenegadoException;
import com.auto.app.easycar.exceptions.ResourceNotFoundException;
import com.auto.app.easycar.repositories.HistorialMantenimientosRepository;
import com.auto.app.easycar.repositories.TipoMantenimientoRepository;
import com.auto.app.easycar.repositories.UsuarioRepository;
import com.auto.app.easycar.repositories.VehiculoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistorialMantenimientoServiceImplTest {

    @Mock
    private HistorialMantenimientosRepository repository;

    @Mock
    private TipoMantenimientoRepository tipoMantenimientoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private AccesoVehiculoService acceso;

    @InjectMocks
    private HistorialMantenimientoServiceImpl service;

    @Test
    void creaHistorialResolviendoLasTresReferencias() {
        UUID usuarioId = UUID.randomUUID();
        UUID vehiculoId = UUID.randomUUID();

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(vehiculoId);
        TipoMantenimiento tipo = new TipoMantenimiento();
        tipo.setId(7L);

        when(tipoMantenimientoRepository.findById(7L)).thenReturn(Optional.of(tipo));
        when(vehiculoRepository.findById(vehiculoId)).thenReturn(Optional.of(vehiculo));
        when(acceso.usuarioActual()).thenReturn(usuario);
        when(repository.save(any(HistorialMantenimientos.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        HistorialMantenimientoResponse response = service.create(new HistorialMantenimientoRequest(
                new Date(), 48000, "Aceite", 7L, usuarioId, vehiculoId));

        assertThat(response.usuarioId()).isEqualTo(usuarioId);
        assertThat(response.vehiculoId()).isEqualTo(vehiculoId);
        assertThat(response.tipoMantenimientoId()).isEqualTo(7L);
        assertThat(response.kilometrajeActual()).isEqualTo(48000);
    }

    @Test
    void fallaSiElVehiculoNoExiste() {
        UUID usuarioId = UUID.randomUUID();
        UUID vehiculoId = UUID.randomUUID();

        when(vehiculoRepository.findById(vehiculoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new HistorialMantenimientoRequest(
                new Date(), 1000, null, 7L, usuarioId, vehiculoId)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(vehiculoId.toString());

        verify(repository, never()).save(any());
    }

    @Test
    void rechazaRegistrarHistorialANombreDeOtroUsuario() {
        UUID usuarioId = UUID.randomUUID();
        UUID otroUsuarioId = UUID.randomUUID();
        UUID vehiculoId = UUID.randomUUID();

        Usuario yo = new Usuario();
        yo.setId(usuarioId);
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(vehiculoId);

        when(vehiculoRepository.findById(vehiculoId)).thenReturn(Optional.of(vehiculo));
        when(acceso.usuarioActual()).thenReturn(yo);

        assertThatThrownBy(() -> service.create(new HistorialMantenimientoRequest(
                new Date(), 1000, null, 7L, otroUsuarioId, vehiculoId)))
                .isInstanceOf(AccesoDenegadoException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void rechazaFechaFutura() {
        UUID usuarioId = UUID.randomUUID();
        UUID vehiculoId = UUID.randomUUID();

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(vehiculoId);
        TipoMantenimiento tipo = new TipoMantenimiento();
        tipo.setId(7L);

        when(vehiculoRepository.findById(vehiculoId)).thenReturn(Optional.of(vehiculo));
        when(acceso.usuarioActual()).thenReturn(usuario);
        when(tipoMantenimientoRepository.findById(7L)).thenReturn(Optional.of(tipo));

        Date manana = new Date(System.currentTimeMillis() + 86_400_000L);

        assertThatThrownBy(() -> service.create(new HistorialMantenimientoRequest(
                manana, 1000, null, 7L, usuarioId, vehiculoId)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("futura");

        verify(repository, never()).save(any());
    }

    @Test
    void buscarPorIdInexistenteLanzaNoEncontrado() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
