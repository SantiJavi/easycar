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
                "Cambio de aceite", fecha, "Usar 5W-30", 50000, 1, vehiculoId));

        assertThat(response.nombreMantenimiento()).isEqualTo("Cambio de aceite");
        assertThat(response.vehiculoId()).isEqualTo(vehiculoId);
        assertThat(response.kilometrajeObjetivo()).isEqualTo(50000);
    }

    @Test
    void fallaSiElVehiculoNoExiste() {
        UUID vehiculoId = UUID.randomUUID();
        when(vehiculoRepository.findById(vehiculoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new TipoMantenimientoRequest(
                "Cambio de aceite", new Date(), null, 1000, 1, vehiculoId)))
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
}
