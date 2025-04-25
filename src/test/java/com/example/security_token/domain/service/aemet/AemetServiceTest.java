package com.example.security_token.domain.service.aemet;

import com.example.security_token.api.aemet.exception.AemetServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Configura el uso de Mockito con JUnit 5
class AemetServiceTest {

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

        // Configuramos el comportamiento del mock para simular la llamada al cliente Feign
        when(aemetFeignClient.getPrediccionUV("0")).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Uso de lenient para permitir el stub no utilizado
        lenient().when(aemetFeignClient.followDataUrl(any(URI.class)))
                .thenReturn(new ResponseEntity<>("Datos de prueba obtenidos correctamente", HttpStatus.OK));

    }

    @Test
    void testGetUvPredictionData_Success() {
        // Ejecución del servicio con un valor válido
        String result = aemetService.getUvPredictionData("0");

        // Aseguramos que la respuesta no sea nula y que sea la esperada
        assertNotNull(result);
        assertEquals("Datos de prueba obtenidos correctamente", result);
    }

    @Test
    void testGetUvPredictionData_Failure_NoDataUrl() {
        // Cambiamos el comportamiento del mock para simular que no se devuelve una URL de datos
        mockResponse.setDatos(null);
        when(aemetFeignClient.getPrediccionUV("0")).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Comprobamos que se lanza una excepción al no recibir la URL de datos
        AemetServiceException exception = assertThrows(AemetServiceException.class, () -> {
            aemetService.getUvPredictionData("0");
        });

        assertEquals("URL de datos no disponible en la respuesta", exception.getMessage());
    }

    @Test
    void testGetUvPredictionData_Failure_ServiceError() {
        // Configuramos el mock para que devuelva una respuesta no exitosa
        mockResponse.setEstado(HttpStatus.INTERNAL_SERVER_ERROR.value());
        when(aemetFeignClient.getPrediccionUV("0")).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.INTERNAL_SERVER_ERROR));

        // Comprobamos que se lanza una excepción debido al error en el servicio AEMET
        AemetServiceException exception = assertThrows(AemetServiceException.class, () -> {
            aemetService.getUvPredictionData("0");
        });

        assertEquals("Respuesta no exitosa de AEMET", exception.getMessage());
    }
}
