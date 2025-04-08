package com.example.security_token.api.aemet;


import com.example.security_token.domain.service.AemetService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
        try {
            // Tu lógica
            return ResponseEntity.ok(aemetService.getUvPredictionData("0"));
        } catch (Exception e) {
            // Si quieres manejar la excepción aquí en lugar de en el GlobalExceptionHandler
            log.error(String.valueOf(e));
            throw e;
        }
    }

}
