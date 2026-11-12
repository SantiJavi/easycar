package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.CanjeInvitacionResponse;
import com.auto.app.easycar.dto.InvitacionRequest;
import com.auto.app.easycar.dto.InvitacionResponse;
import com.auto.app.easycar.dto.RenovarInvitacionRequest;
import com.auto.app.easycar.entitis.InvitacionVehiculo;
import com.auto.app.easycar.entitis.RolVehiculo;
import com.auto.app.easycar.entitis.UserVehiculo;
import com.auto.app.easycar.entitis.Usuario;
import com.auto.app.easycar.entitis.Vehiculo;
import com.auto.app.easycar.exceptions.AccesoDenegadoException;
import com.auto.app.easycar.exceptions.DuplicateResourceException;
import com.auto.app.easycar.exceptions.InvitacionNoDisponibleException;
import com.auto.app.easycar.exceptions.ResourceNotFoundException;
import com.auto.app.easycar.repositories.InvitacionVehiculoRepository;
import com.auto.app.easycar.repositories.UserVehiculoRepository;
import com.auto.app.easycar.repositories.VehiculoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitacionVehiculoServiceImplTest {

    private static final long MS_DIA = 86_400_000L;

    @Mock
    private InvitacionVehiculoRepository invitacionRepository;

    @Mock
    private UserVehiculoRepository userVehiculoRepository;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private AccesoVehiculoService acceso;

    @InjectMocks
    private InvitacionVehiculoServiceImpl service;

    private static Vehiculo vehiculo(UUID id) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(id);
        vehiculo.setPlaca("ABC123");
        vehiculo.setNombre("Mi carro");
        vehiculo.setMarca("Toyota");
        return vehiculo;
    }

    private static Usuario usuario(UUID id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsuario("tester");
        usuario.setEmail("tester@easycar.com");
        return usuario;
    }

    private static InvitacionVehiculo invitacion(Vehiculo vehiculo, Date expiraEn, boolean revocada) {
        InvitacionVehiculo invitacion = new InvitacionVehiculo();
        invitacion.setId(1L);
        invitacion.setCodigo("K7M2PQX9");
        invitacion.setFkVehiculoInvitacion(vehiculo);
        invitacion.setFkUsuarioInvitacion(usuario(UUID.randomUUID()));
        invitacion.setFechaCreacion(new Date());
        invitacion.setExpiraEn(expiraEn);
        invitacion.setRevocada(revocada);
        return invitacion;
    }

    @Test
    void creaCodigoConLaVigenciaElegidaPorElPropietario() {
        UUID vehiculoId = UUID.randomUUID();
        Vehiculo vehiculo = vehiculo(vehiculoId);

        when(vehiculoRepository.findById(vehiculoId)).thenReturn(Optional.of(vehiculo));
        when(invitacionRepository.existsByCodigo(any())).thenReturn(false);
        when(invitacionRepository.save(any(InvitacionVehiculo.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        InvitacionResponse response = service.crear(new InvitacionRequest(vehiculoId, 15));

        assertThat(response.codigo()).hasSize(8);
        assertThat(response.estado()).isEqualTo("VIGENTE");
        assertThat(response.diasRestantes()).isEqualTo(15);
        assertThat(response.vehiculoId()).isEqualTo(vehiculoId);
    }

    @Test
    void noGeneraCodigoSiNoEsElPropietario() {
        UUID vehiculoId = UUID.randomUUID();
        when(vehiculoRepository.findById(vehiculoId)).thenReturn(Optional.of(vehiculo(vehiculoId)));
        doThrow(new AccesoDenegadoException("Solo el propietario puede realizar esta accion"))
                .when(acceso).exigirPropietario(any());

        assertThatThrownBy(() -> service.crear(new InvitacionRequest(vehiculoId, 7)))
                .isInstanceOf(AccesoDenegadoException.class);

        verify(invitacionRepository, never()).save(any());
    }

    @Test
    void canjeaCodigoVigenteYCreaAccesoDeColaborador() {
        UUID vehiculoId = UUID.randomUUID();
        UUID invitadoId = UUID.randomUUID();
        Vehiculo vehiculo = vehiculo(vehiculoId);

        when(invitacionRepository.findByCodigoIgnoreCase("K7M2PQX9"))
                .thenReturn(Optional.of(invitacion(vehiculo, new Date(System.currentTimeMillis() + MS_DIA), false)));
        when(acceso.usuarioActual()).thenReturn(usuario(invitadoId));
        when(userVehiculoRepository.findByFkUserVehiculo_IdAndFkVehiculoUser_Id(invitadoId, vehiculoId))
                .thenReturn(Optional.empty());

        CanjeInvitacionResponse response = service.canjear(" k7m2pqx9 ");

        assertThat(response.vehiculoId()).isEqualTo(vehiculoId);
        assertThat(response.rol()).isEqualTo("COLABORADOR");

        ArgumentCaptor<UserVehiculo> captor = ArgumentCaptor.forClass(UserVehiculo.class);
        verify(userVehiculoRepository).save(captor.capture());
        assertThat(captor.getValue().getRol()).isEqualTo(RolVehiculo.COLABORADOR);
        assertThat(captor.getValue().getFkUserVehiculo().getId()).isEqualTo(invitadoId);
    }

    @Test
    void rechazaCodigoInexistente() {
        when(invitacionRepository.findByCodigoIgnoreCase("NOEXISTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.canjear("NOEXISTE"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rechazaCodigoVencido() {
        Vehiculo vehiculo = vehiculo(UUID.randomUUID());
        when(invitacionRepository.findByCodigoIgnoreCase("K7M2PQX9"))
                .thenReturn(Optional.of(invitacion(vehiculo, new Date(System.currentTimeMillis() - MS_DIA), false)));

        assertThatThrownBy(() -> service.canjear("K7M2PQX9"))
                .isInstanceOf(InvitacionNoDisponibleException.class)
                .hasMessageContaining("vencido");
    }

    @Test
    void rechazaCodigoRevocado() {
        Vehiculo vehiculo = vehiculo(UUID.randomUUID());
        when(invitacionRepository.findByCodigoIgnoreCase("K7M2PQX9"))
                .thenReturn(Optional.of(invitacion(vehiculo, new Date(System.currentTimeMillis() + MS_DIA), true)));

        assertThatThrownBy(() -> service.canjear("K7M2PQX9"))
                .isInstanceOf(InvitacionNoDisponibleException.class)
                .hasMessageContaining("revocado");
    }

    @Test
    void rechazaCanjeSiYaTieneAcceso() {
        UUID vehiculoId = UUID.randomUUID();
        UUID invitadoId = UUID.randomUUID();
        Vehiculo vehiculo = vehiculo(vehiculoId);

        UserVehiculo existente = new UserVehiculo();
        existente.setFkVehiculoUser(vehiculo);
        existente.setFkUserVehiculo(usuario(invitadoId));
        existente.setRol(RolVehiculo.COLABORADOR);

        when(invitacionRepository.findByCodigoIgnoreCase("K7M2PQX9"))
                .thenReturn(Optional.of(invitacion(vehiculo, new Date(System.currentTimeMillis() + MS_DIA), false)));
        when(acceso.usuarioActual()).thenReturn(usuario(invitadoId));
        when(userVehiculoRepository.findByFkUserVehiculo_IdAndFkVehiculoUser_Id(invitadoId, vehiculoId))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.canjear("K7M2PQX9"))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userVehiculoRepository, never()).save(any());
    }

    @Test
    void renovarExtiendeLaVigenciaDelCodigoVencido() {
        Vehiculo vehiculo = vehiculo(UUID.randomUUID());
        InvitacionVehiculo vencida = invitacion(
                vehiculo, new Date(System.currentTimeMillis() - MS_DIA), false);

        when(invitacionRepository.findById(1L)).thenReturn(Optional.of(vencida));
        when(invitacionRepository.save(any(InvitacionVehiculo.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        InvitacionResponse response = service.renovar(1L, new RenovarInvitacionRequest(30));

        assertThat(response.estado()).isEqualTo("VIGENTE");
        assertThat(response.diasRestantes()).isEqualTo(30);
        assertThat(vencida.getExpiraEn()).isAfter(new Date());
    }

    @Test
    void noRenuevaUnCodigoRevocado() {
        Vehiculo vehiculo = vehiculo(UUID.randomUUID());
        when(invitacionRepository.findById(1L))
                .thenReturn(Optional.of(invitacion(vehiculo, new Date(), true)));

        assertThatThrownBy(() -> service.renovar(1L, new RenovarInvitacionRequest(30)))
                .isInstanceOf(InvitacionNoDisponibleException.class);
    }

    @Test
    void revocarMarcaLaInvitacionYConservaElRegistro() {
        Vehiculo vehiculo = vehiculo(UUID.randomUUID());
        InvitacionVehiculo vigente = invitacion(
                vehiculo, new Date(System.currentTimeMillis() + MS_DIA), false);
        when(invitacionRepository.findById(1L)).thenReturn(Optional.of(vigente));
        when(invitacionRepository.save(any(InvitacionVehiculo.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        service.revocar(1L);

        assertThat(vigente.isRevocada()).isTrue();
    }
}
