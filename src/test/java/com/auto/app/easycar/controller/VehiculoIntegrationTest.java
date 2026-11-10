package com.auto.app.easycar.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VehiculoIntegrationTest extends AbstractIntegrationTest {

    private static String placaUnica() {
        return "T" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String crearVehiculo(String token, String placa) throws Exception {
        String body = mockMvc.perform(post("/api/vehiculos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"placa":"%s","nombre":"Mi carro","marca":"Toyota","fechaMatricula":"2023-05-15"}
                                """.formatted(placa)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placa").value(placa))
                .andExpect(jsonPath("$.tiposMantenimiento").isEmpty())
                .andExpect(jsonPath("$.historial").isEmpty())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    @Test
    void creaVehiculoConKilometrajeYLoActualizaSinHistorial() throws Exception {
        String token = tokenNuevoUsuario();
        String placa = placaUnica();

        String body = mockMvc.perform(post("/api/vehiculos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"placa":"%s","nombre":"Nuevo","marca":"Kia","kilometrajeActual":1200}
                                """.formatted(placa)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.kilometrajeActual").value(1200))
                .andExpect(jsonPath("$.historial").isEmpty())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(body, "$.id");

        mockMvc.perform(patch("/api/vehiculos/" + id + "/kilometraje")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kilometrajeActual\":2500}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kilometrajeActual").value(2500))
                .andExpect(jsonPath("$.historial").isEmpty())
                .andExpect(jsonPath("$.tiposMantenimiento").isEmpty());

        mockMvc.perform(patch("/api/vehiculos/" + UUID.randomUUID() + "/kilometraje")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kilometrajeActual\":10}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/vehiculos/" + id + "/kilometraje")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kilometrajeActual\":-5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cicloCrudCompletoDeVehiculo() throws Exception {
        String token = tokenNuevoUsuario();
        String placa = placaUnica();
        String id = crearVehiculo(token, placa);

        mockMvc.perform(get("/api/vehiculos/" + id).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marca").value("Toyota"));

        mockMvc.perform(put("/api/vehiculos/" + id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"placa":"%s","nombre":"Editado","marca":"Mazda","fechaMatricula":"2023-05-15"}
                                """.formatted(placa)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Editado"))
                .andExpect(jsonPath("$.marca").value("Mazda"));

        mockMvc.perform(delete("/api/vehiculos/" + id).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/vehiculos/" + id).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void rechazaPlacaDuplicadaYDevuelveConflicto() throws Exception {
        String token = tokenNuevoUsuario();
        String placa = placaUnica();
        crearVehiculo(token, placa);

        mockMvc.perform(post("/api/vehiculos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"placa":"%s","nombre":"Otro","marca":"Kia"}
                                """.formatted(placa)))
                .andExpect(status().isConflict());
    }

    @Test
    void validaLosCamposObligatorios() throws Exception {
        String token = tokenNuevoUsuario();

        mockMvc.perform(post("/api/vehiculos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"\",\"marca\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.placa").exists())
                .andExpect(jsonPath("$.fieldErrors.marca").exists());
    }

    @Test
    void vehiculoExponeTiposEHistorialYLosHijosNoVuelvenAlPadre() throws Exception {
        Sesion sesion = sesion();
        String token = sesion.token();
        String vehiculoId = crearVehiculo(token, placaUnica());

        String tipoBody = mockMvc.perform(post("/api/tipos-mantenimiento")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombreMantenimiento":"Cambio de aceite","fechaSiguienteMantenimiento":"2026-12-01T08:00:00.000+00:00",
                                 "observacion":"Usar 5W-30","kilometrajeObjetivo":50000,"tipoVista":1,"vehiculoId":"%s"}
                                """.formatted(vehiculoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vehiculoId").value(vehiculoId))
                // TipoMantenimiento -> Vehiculo no se anida (el vehiculo ve al tipo, no viceversa)
                .andExpect(jsonPath("$.vehiculo").doesNotExist())
                .andExpect(jsonPath("$.historial").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        Integer tipoId = JsonPath.read(tipoBody, "$.id");

        String historialBody = mockMvc.perform(post("/api/historiales")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fechaMantenimiento":"2026-10-01T08:00:00.000+00:00","kilometrajeActual":48200,
                                 "observacion":"Aceite y filtro","tipoMantenimientoId":%d,"usuarioId":"%s","vehiculoId":"%s"}
                                """.formatted(tipoId, sesion.usuarioId(), vehiculoId)))
                .andExpect(status().isCreated())
                // Historial -> vehiculo / usuario / tipo no se anidan
                .andExpect(jsonPath("$.vehiculo").doesNotExist())
                .andExpect(jsonPath("$.usuario").doesNotExist())
                .andExpect(jsonPath("$.tipoMantenimiento").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        Integer historialId = JsonPath.read(historialBody, "$.id");

        // Vehiculo -> tipos + historial: visibles
        mockMvc.perform(get("/api/vehiculos/" + vehiculoId).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tiposMantenimiento[0].id").value(tipoId))
                .andExpect(jsonPath("$.tiposMantenimiento[0].nombreMantenimiento").value("Cambio de aceite"))
                .andExpect(jsonPath("$.historial[0].id").value(historialId))
                .andExpect(jsonPath("$.historial[0].kilometrajeActual").value(48200))
                // y los hijos no arrastran al vehiculo de vuelta
                .andExpect(jsonPath("$.tiposMantenimiento[0].fkTipoMantenimiento").doesNotExist())
                .andExpect(jsonPath("$.historial[0].fkVehiculoHistorial").doesNotExist());

        // Usuario -> historial: visible
        mockMvc.perform(get("/api/usuarios/" + sesion.usuarioId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historial[0].id").value(historialId))
                .andExpect(jsonPath("$.historial[0].usuario").doesNotExist());

        // El historial por su propio endpoint sigue devolviendo solo ids
        mockMvc.perform(get("/api/historiales/" + historialId).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehiculoId").value(vehiculoId))
                .andExpect(jsonPath("$.usuarioId").value(sesion.usuarioId()))
                .andExpect(jsonPath("$.tipoMantenimientoId").value(tipoId));
    }

    @Test
    void noSePuedeBorrarUnVehiculoConHistorialAsociado() throws Exception {
        Sesion sesion = sesion();
        String token = sesion.token();
        String vehiculoId = crearVehiculo(token, placaUnica());

        String tipoBody = mockMvc.perform(post("/api/tipos-mantenimiento")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombreMantenimiento":"Alineacion","tipoVista":2,"vehiculoId":"%s"}
                                """.formatted(vehiculoId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer tipoId = JsonPath.read(tipoBody, "$.id");

        mockMvc.perform(post("/api/historiales")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fechaMantenimiento":"2026-10-01T08:00:00.000+00:00","kilometrajeActual":1000,
                                 "tipoMantenimientoId":%d,"usuarioId":"%s","vehiculoId":"%s"}
                                """.formatted(tipoId, sesion.usuarioId(), vehiculoId)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/vehiculos/" + vehiculoId).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isConflict());
    }
}
