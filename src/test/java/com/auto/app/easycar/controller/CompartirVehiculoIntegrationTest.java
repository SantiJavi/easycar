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

/**
 * Flujo real de vehiculo compartido: el dueño genera un codigo con vigencia elegida,
 * otra persona lo canjea, edita el vehiculo y finalmente es expulsada sin perder el
 * historial que habia registrado.
 */
class CompartirVehiculoIntegrationTest extends AbstractIntegrationTest {

    private static String placaUnica() {
        return "C" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String crearVehiculo(String token, String placa) throws Exception {
        String body = mockMvc.perform(post("/api/vehiculos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"placa":"%s","nombre":"Compartido","marca":"Mazda","kilometrajeActual":1000}
                                """.formatted(placa)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("PROPIETARIO"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    @Test
    void duenioComparteConCodigoYElColaboradorEditaSinPoderBorrar() throws Exception {
        Sesion duenio = sesion();
        Sesion invitado = sesion();
        Sesion tercero = sesion();
        String vehiculoId = crearVehiculo(duenio.token(), placaUnica());

        // El tercero no tiene acceso todavia
        mockMvc.perform(get("/api/vehiculos/" + vehiculoId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tercero.token()))
                .andExpect(status().isForbidden());

        // El dueño genera un codigo con 7 dias de vigencia
        String invitacionBody = mockMvc.perform(post("/api/invitaciones")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + duenio.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehiculoId":"%s","diasValidez":7}
                                """.formatted(vehiculoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("VIGENTE"))
                .andExpect(jsonPath("$.diasRestantes").value(7))
                .andReturn().getResponse().getContentAsString();
        String codigo = JsonPath.read(invitacionBody, "$.codigo");
        Integer invitacionId = JsonPath.read(invitacionBody, "$.id");

        // Un tercero no puede generar codigos para un vehiculo ajeno
        mockMvc.perform(post("/api/invitaciones")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tercero.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehiculoId":"%s","diasValidez":7}
                                """.formatted(vehiculoId)))
                .andExpect(status().isForbidden());

        // El invitado canjea el codigo
        mockMvc.perform(post("/api/invitaciones/canjear/" + codigo)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invitado.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehiculoId").value(vehiculoId))
                .andExpect(jsonPath("$.rol").value("COLABORADOR"));

        // Ya aparece en su lista de vehiculos, con su rol
        mockMvc.perform(get("/api/vehiculos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invitado.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(vehiculoId))
                .andExpect(jsonPath("$[0].rol").value("COLABORADOR"));

        // Y para el tercero la lista sigue vacia
        mockMvc.perform(get("/api/vehiculos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tercero.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        // Canjear dos veces el mismo codigo no duplica el acceso
        mockMvc.perform(post("/api/invitaciones/canjear/" + codigo)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invitado.token()))
                .andExpect(status().isConflict());

        // El colaborador puede actualizar kilometraje y programar mantenimientos
        mockMvc.perform(patch("/api/vehiculos/" + vehiculoId + "/kilometraje")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invitado.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kilometrajeActual\":2500}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kilometrajeActual").value(2500))
                .andExpect(jsonPath("$.rol").value("COLABORADOR"));

        String tipoBody = mockMvc.perform(post("/api/tipos-mantenimiento")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invitado.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombreMantenimiento":"Aceite","kilometrajeObjetivo":7500,"tipoVista":1,"vehiculoId":"%s"}
                                """.formatted(vehiculoId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer tipoId = JsonPath.read(tipoBody, "$.id");

        // El registro de historial queda a nombre de quien lo hizo
        String historialBody = mockMvc.perform(post("/api/historiales")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invitado.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fechaMantenimiento":"2026-10-01T08:00:00.000+00:00","kilometrajeActual":2500,
                                 "tipoMantenimientoId":%d,"usuarioId":"%s","vehiculoId":"%s"}
                                """.formatted(tipoId, invitado.usuarioId(), vehiculoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(invitado.usuarioId()))
                .andReturn().getResponse().getContentAsString();
        Integer historialId = JsonPath.read(historialBody, "$.id");

        // Pero no puede cambiar los datos del vehiculo ni borrarlo
        mockMvc.perform(put("/api/vehiculos/" + vehiculoId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invitado.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"placa":"OTRA999","nombre":"Robado","marca":"Mazda"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/vehiculos/" + vehiculoId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invitado.token()))
                .andExpect(status().isForbidden());

        // El dueño ve a los dos usuarios con sus roles
        mockMvc.perform(get("/api/vehiculos/" + vehiculoId + "/accesos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + duenio.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].rol").value("PROPIETARIO"))
                .andExpect(jsonPath("$[1].rol").value("COLABORADOR"));

        // El dueño lo degrada a solo lectura y el colaborador ya no puede escribir
        mockMvc.perform(put("/api/vehiculos/" + vehiculoId + "/accesos/" + invitado.usuarioId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + duenio.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rol\":\"LECTURA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("LECTURA"));

        mockMvc.perform(patch("/api/vehiculos/" + vehiculoId + "/kilometraje")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invitado.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kilometrajeActual\":3000}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/vehiculos/" + vehiculoId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invitado.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("LECTURA"));

        // El dueño lo expulsa: pierde el acceso pero el historial se conserva
        mockMvc.perform(delete("/api/vehiculos/" + vehiculoId + "/accesos/" + invitado.usuarioId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + duenio.token()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/vehiculos/" + vehiculoId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invitado.token()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/vehiculos/" + vehiculoId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + duenio.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historial.length()").value(1))
                .andExpect(jsonPath("$.historial[0].id").value(historialId))
                .andExpect(jsonPath("$.historial[0].usuarioId").value(invitado.usuarioId()));

        // Revocar el codigo lo deja inutilizable
        mockMvc.perform(delete("/api/invitaciones/" + invitacionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + duenio.token()))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/invitaciones/canjear/" + codigo)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tercero.token()))
                .andExpect(status().isGone());
    }

    @Test
    void renuevaUnCodigoVencidoYLoPuedeVolverACompartir() throws Exception {
        Sesion duenio = sesion();
        Sesion invitado = sesion();
        String vehiculoId = crearVehiculo(duenio.token(), placaUnica());

        String body = mockMvc.perform(post("/api/invitaciones")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + duenio.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehiculoId":"%s","diasValidez":1}
                                """.formatted(vehiculoId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String codigo = JsonPath.read(body, "$.codigo");
        Integer invitacionId = JsonPath.read(body, "$.id");

        // Renovar reinicia la vigencia desde hoy
        mockMvc.perform(put("/api/invitaciones/" + invitacionId + "/renovar")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + duenio.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"diasValidez\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.estado").value("VIGENTE"))
                .andExpect(jsonPath("$.diasRestantes").value(30));

        // Un codigo inexistente responde 404
        mockMvc.perform(post("/api/invitaciones/canjear/ZZZZZZZZ")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invitado.token()))
                .andExpect(status().isNotFound());

        // Con el codigo renovado si puede entrar
        mockMvc.perform(post("/api/invitaciones/canjear/" + codigo)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invitado.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("COLABORADOR"));
    }
}
