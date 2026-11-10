package com.auto.app.easycar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Verificacion de disponibilidad del servicio")
@SecurityRequirements
public class HealthProject {

    @Operation(summary = "Estado del servicio", description = "Endpoint publico para verificar que la API esta arriba")
    @GetMapping("/status")
    public ResponseEntity<?> getStatusHealth(){
        return ResponseEntity.status(200).body("Funciona pero funciona mejor");
    }
}
