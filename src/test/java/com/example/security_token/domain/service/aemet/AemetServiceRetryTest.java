package com.example.security_token.domain.service.aemet;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class AemetServiceRetryTest {

    @Mock
    private AEMETFeignClient aemetFeignClient; // Mock del Feign Client

    @InjectMocks
    private AemetService aemetService; // La clase a probar, inyecta los mocks automáticamente

    private AemetApiResponse mockResponse;

    @BeforeEach
    public void setUp() {
        // Configuración del mock que devuelve una respuesta exitosa
        mockResponse = new AemetApiResponse();
        mockResponse.setDescripcion("Predicción UV");
        mockResponse.setEstado(HttpStatus.OK.value());
        mockResponse.setDatos("https://example.com/data"); // URL de datos
        mockResponse.setMetadatos("Metadata");

        // Mock de followDataUrl, que debería devolver una respuesta con datos correctos
        when(aemetFeignClient.followDataUrl(any()))
                .thenReturn(new ResponseEntity<>("Datos de prueba obtenidos correctamente", HttpStatus.OK));

        // Agregar logs para ver qué respuestas estamos configurando
        log.info("Configuración de Mock para 'getPrediccionUV' exitosa.");
    }

    //  los reintentos no están siendo controlados por Spring/Feign, ya que en un unit test con mocks,
    //  el @FeignClient nunca entra por la configuración de Retryer.
    // NO es un test de integración (con contexto Spring + Retry real)
    @Test
    void testRetryMechanism() {
        // Simular comportamiento del Feign client
        when(aemetFeignClient.getPrediccionUV("0"))
                .thenThrow(new RuntimeException("503"))    // Primer intento
                .thenThrow(new RuntimeException("503"))    // Segundo intento
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK)); // Éxito

        when(aemetFeignClient.followDataUrl(any()))
                .thenReturn(new ResponseEntity<>("Datos de prueba obtenidos correctamente", HttpStatus.OK));

        String result = null;
        int attempts = 0;

        while (attempts < 3 && result == null) {
            try {
                result = aemetService.getUvPredictionData("0");
            } catch (RuntimeException e) {
                log.warn("Intento {} fallido: {}", attempts + 1, e.getMessage());
                attempts++;
            }
        }

        assertNotNull(result);
        assertEquals("Datos de prueba obtenidos correctamente", result);
        verify(aemetFeignClient, times(3)).getPrediccionUV("0");
    }
}


