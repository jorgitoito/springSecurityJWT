package com.example.security_token.api.aemet;


import com.example.security_token.domain.service.aemet.AemetService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aemet")
@Slf4j
public class AemetController {

    private final AemetService aemetService;

    public AemetController(AemetService aemetService) {
        this.aemetService = aemetService;
    }

    @Operation(summary = "get All UV AEMET data")
    @GetMapping("/UV")
    public ResponseEntity<String> getAllUVData() {
        log.info("Recibida solicitud para obtener datos de UV para hoy: 0");
        return ResponseEntity.ok(aemetService.getUvPredictionData("0"));
    }

    @Operation(summary = "get Playa AEMET data")
    @GetMapping("/playa/{codPlaya}")
    public ResponseEntity<String> getPlayaData(@PathVariable @NotNull String codPlaya) {
        log.info("Recibida solicitud para obtener datos de playa con código: {}", codPlaya);
        return ResponseEntity.ok(aemetService.getPredictionPlaya(codPlaya));
    }

}
