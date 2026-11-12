package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.TipoMantenimientoRequest;
import com.auto.app.easycar.dto.TipoMantenimientoResponse;
import com.auto.app.easycar.entitis.TipoMantenimiento;
import com.auto.app.easycar.entitis.Vehiculo;
import com.auto.app.easycar.exceptions.ResourceNotFoundException;
import com.auto.app.easycar.repositories.TipoMantenimientoRepository;
import com.auto.app.easycar.repositories.VehiculoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
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
class TipoMantenimientoServiceImplTest {

    @Mock
    private TipoMantenimientoRepository repository;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private AccesoVehiculoService acceso;

    @InjectMocks
    private TipoMantenimientoServiceImpl service;

    private static Vehiculo vehiculo(UUID id) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(id);
        vehiculo.setPlaca("ABC123");
        vehiculo.setMarca("Toyota");
        return vehiculo;
    }

    @Test
    void creaTipoDeMantenimientoAsociadoAlVehiculo() {
        UUID vehiculoId = UUID.randomUUID();
        Vehiculo vehiculo = vehiculo(vehiculoId);
        Date fecha = new Date();

        when(vehiculoRepository.findById(vehiculoId)).thenReturn(Optional.of(vehiculo));
        when(repository.save(any(TipoMantenimiento.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        TipoMantenimientoResponse response = service.create(new TipoMantenimientoRequest(
                "Cambio de aceite", fecha, "Usar 5W-30", 50000, 5000, null, 1, vehiculoId));

        assertThat(response.nombreMantenimiento()).isEqualTo("Cambio de aceite");
        assertThat(response.vehiculoId()).isEqualTo(vehiculoId);
        assertThat(response.kilometrajeObjetivo()).isEqualTo(50000);
        assertThat(response.frecuenciaKm()).isEqualTo(5000);
        assertThat(response.frecuenciaMeses()).isNull();
    }

    @Test
    void fallaSiElVehiculoNoExiste() {
        UUID vehiculoId = UUID.randomUUID();
        when(vehiculoRepository.findById(vehiculoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new TipoMantenimientoRequest(
                "Cambio de aceite", new Date(), null, 1000, null, null, 1, vehiculoId)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(vehiculoId.toString());

        verify(repository, never()).save(any());
    }

    @Test
    void listarPorVehiculoDelegaEnElRepositorio() {
        UUID vehiculoId = UUID.randomUUID();
        TipoMantenimiento tipo = new TipoMantenimiento();
        tipo.setId(5L);
        tipo.setNombreMantenimiento("Alineacion");
        tipo.setTipoVista(2);
        tipo.setFkTipoMantenimiento(vehiculo(vehiculoId));

        when(vehiculoRepository.findById(vehiculoId)).thenReturn(Optional.of(vehiculo(vehiculoId)));
        when(repository.findByFkTipoMantenimiento_Id(vehiculoId)).thenReturn(List.of(tipo));

        List<TipoMantenimientoResponse> resultado = service.findByVehiculo(vehiculoId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nombreMantenimiento()).isEqualTo("Alineacion");
    }

    @Test
    void buscarPorIdInexistenteLanzaNoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void actualizarCambiaDeKilometrajeAFechaYDescartaElObjetivoEnKm() {
        UUID vehiculoId = UUID.randomUUID();
        TipoMantenimiento existente = new TipoMantenimiento();
        existente.setId(7L);
        existente.setNombreMantenimiento("Cambio de aceite");
        existente.setKilometrajeObjetivo(50000);
        existente.setTipoVista(1);
        existente.setFkTipoMantenimiento(vehiculo(vehiculoId));
        Date nuevaFecha = new Date();

        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(vehiculoRepository.findById(vehiculoId)).thenReturn(Optional.of(vehiculo(vehiculoId)));
        when(repository.save(any(TipoMantenimiento.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        TipoMantenimientoResponse response = service.update(7L, new TipoMantenimientoRequest(
                "Cambio de aceite", nuevaFecha, null, null, null, 6, 2, vehiculoId));

        assertThat(response.tipoVista()).isEqualTo(2);
        assertThat(response.fechaSiguienteMantenimiento()).isEqualTo(nuevaFecha);
        assertThat(response.kilometrajeObjetivo()).isNull();
        assertThat(response.frecuenciaMeses()).isEqualTo(6);
        assertThat(response.frecuenciaKm()).isNull();
    }

    @Test
    void actualizarCambiaDeFechaAKilometrajeYDescartaLaFecha() {
        UUID vehiculoId = UUID.randomUUID();
        TipoMantenimiento existente = new TipoMantenimiento();
        existente.setId(8L);
        existente.setNombreMantenimiento("Alineacion");
        existente.setFechaSiguienteMantenimiento(new Date());
        existente.setTipoVista(2);
        existente.setFkTipoMantenimiento(vehiculo(vehiculoId));

        when(repository.findById(8L)).thenReturn(Optional.of(existente));
        when(vehiculoRepository.findById(vehiculoId)).thenReturn(Optional.of(vehiculo(vehiculoId)));
        when(repository.save(any(TipoMantenimiento.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        TipoMantenimientoResponse response = service.update(8L, new TipoMantenimientoRequest(
                "Alineacion", new Date(), null, 42000, 7000, null, 1, vehiculoId));

        assertThat(response.tipoVista()).isEqualTo(1);
        assertThat(response.kilometrajeObjetivo()).isEqualTo(42000);
        assertThat(response.frecuenciaKm()).isEqualTo(7000);
        assertThat(response.frecuenciaMeses()).isNull();
        assertThat(response.fechaSiguienteMantenimiento()).isNull();
    }
}
