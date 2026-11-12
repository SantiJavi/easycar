package com.auto.app.easycar.controller;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base para pruebas de integracion: levanta el contexto completo con H2 en memoria,
 * el security filter chain real y MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    protected static final String PASSWORD = "Easycar123";
    protected static final String CODIGO_REGISTRO = "codigo-de-prueba";

    @Autowired
    protected MockMvc mockMvc;

    protected String emailUnico() {
        return "tester-" + UUID.randomUUID() + "@easycar.com";
    }

    protected String registrar(String email) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuario":"tester","email":"%s","password":"%s","codigo":"%s"}
                                """.formatted(email, PASSWORD, CODIGO_REGISTRO)))
                .andExpect(status().isCreated());
        return email;
    }

    protected String login(String email) throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    /**
     * Crea un usuario nuevo y devuelve su token JWT.
     */
    protected String tokenNuevoUsuario() throws Exception {
        return sesion().token();
    }

    /**
     * Datos de una sesion recien creada: email, token y id del usuario.
     */
    protected record Sesion(String email, String token, String usuarioId) {
    }

    protected Sesion sesion() throws Exception {
        String email = registrar(emailUnico());
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new Sesion(email, JsonPath.read(body, "$.token"), JsonPath.read(body, "$.usuarioId"));
    }
}
