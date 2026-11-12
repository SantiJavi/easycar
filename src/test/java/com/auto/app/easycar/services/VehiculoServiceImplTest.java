package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.VehiculoRequest;
import com.auto.app.easycar.dto.VehiculoResponse;
import com.auto.app.easycar.entitis.HistorialMantenimientos;
import com.auto.app.easycar.entitis.TipoMantenimiento;
import com.auto.app.easycar.entitis.Usuario;
import com.auto.app.easycar.entitis.Vehiculo;
import com.auto.app.easycar.exceptions.DuplicateResourceException;
import com.auto.app.easycar.exceptions.ResourceNotFoundException;
import com.auto.app.easycar.repositories.InvitacionVehiculoRepository;
import com.auto.app.easycar.repositories.UserVehiculoRepository;
import com.auto.app.easycar.repositories.VehiculoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceImplTest {

    @Mock
    private VehiculoRepository repository;

    @Mock
    private UserVehiculoRepository userVehiculoRepository;

    @Mock
    private InvitacionVehiculoRepository invitacionRepository;

    @Mock
    private AccesoVehiculoService acceso;

    @InjectMocks
    private VehiculoServiceImpl service;

    private static Vehiculo vehiculo(String placa) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(UUID.randomUUID());
        vehiculo.setPlaca(placa);
        vehiculo.setMarca("Toyota");
        vehiculo.setNombre("Mi carro");
        vehiculo.setFechaMatricula(LocalDate.of(2023, 5, 15));
        return vehiculo;
    }

    @Test
    void creaVehiculoCuandoLaPlacaNoExiste() {
        when(repository.existsByPlacaIgnoreCase("ABC123")).thenReturn(false);
        when(repository.save(any(Vehiculo.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        VehiculoResponse response = service.create(
                new VehiculoRequest("  ABC123 ", "Mi carro", "Toyota", LocalDate.of(2023, 5, 15), 125000));

        assertThat(response.placa()).isEqualTo("ABC123");
        assertThat(response.marca()).isEqualTo("Toyota");
        assertThat(response.kilometrajeActual()).isEqualTo(125000);
        assertThat(response.tiposMantenimiento()).isEmpty();
        assertThat(response.historial()).isEmpty();
    }

    @Test
    void rechazaPlacaDuplicada() {
        when(repository.existsByPlacaIgnoreCase("ABC123")).thenReturn(true);

        assertThatThrownBy(() -> service.create(
                new VehiculoRequest("ABC123", "Mi carro", "Toyota", null, null)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ABC123");

        verify(repository, never()).save(any());
    }

    @Test
    void actualizarAPlacaDeOtroVehiculoLanzaConflicto() {
        Vehiculo actual = vehiculo("ABC123");
        Vehiculo otro = vehiculo("XYZ999");

        when(repository.findById(actual.getId())).thenReturn(Optional.of(actual));
        when(repository.findByPlacaIgnoreCase("XYZ999")).thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> service.update(actual.getId(),
                new VehiculoRequest("XYZ999", "Mi carro", "Toyota", null, 1000)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void actualizarKilometrajeNoTocaLosMantenimientos() {
        Vehiculo vehiculo = vehiculo("ABC123");
        vehiculo.setKilometrajeActual(125000);

        when(repository.findById(vehiculo.getId())).thenReturn(Optional.of(vehiculo));
        when(repository.save(vehiculo)).thenReturn(vehiculo);

        VehiculoResponse response = service.actualizarKilometraje(vehiculo.getId(), 130500);

        assertThat(response.kilometrajeActual()).isEqualTo(130500);
        assertThat(response.tiposMantenimiento()).isEmpty();
    }

    @Test
    void actualizarKilometrajeDeVehiculoInexistenteLanzaNoEncontrado() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarKilometraje(id, 1000))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void buscarPorPlacaInexistenteLanzaNoEncontrado() {
        when(repository.findByPlacaIgnoreCase("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByPlaca("NOPE"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("NOPE");
    }

    @Test
    void devuelveTiposDeMantenimientoEHistorialAnidados() {
        Vehiculo vehiculo = vehiculo("ABC123");

        TipoMantenimiento tipo = new TipoMantenimiento();
        tipo.setId(10L);
        tipo.setNombreMantenimiento("Cambio de aceite");
        tipo.setKilometrajeObjetivo(50000);
        tipo.setTipoVista(1);
        tipo.setFkTipoMantenimiento(vehiculo);

        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());

        HistorialMantenimientos historial = new HistorialMantenimientos();
        historial.setId(20L);
        historial.setKilometrajeActual(48200);
        historial.setObservacion("Aceite y filtro");
        historial.setFkTipoHistorial(tipo);
        historial.setFkUserHistorial(usuario);
        historial.setFkVehiculoHistorial(vehiculo);

        vehiculo.setFkTipoMantenimiento(List.of(tipo));
        vehiculo.setFkVehiculoHistorial(List.of(historial));

        when(repository.findById(vehiculo.getId())).thenReturn(Optional.of(vehiculo));

        VehiculoResponse response = service.findById(vehiculo.getId());

        assertThat(response.tiposMantenimiento()).hasSize(1);
        assertThat(response.tiposMantenimiento().get(0).nombreMantenimiento()).isEqualTo("Cambio de aceite");
        assertThat(response.historial()).hasSize(1);
        assertThat(response.historial().get(0).kilometrajeActual()).isEqualTo(48200);
        assertThat(response.historial().get(0).usuarioId()).isEqualTo(usuario.getId());
    }
}
