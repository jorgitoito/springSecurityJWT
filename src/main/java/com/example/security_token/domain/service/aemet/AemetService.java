package com.example.security_token.domain.service.aemet;

import com.example.security_token.api.aemet.exception.AemetServiceException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * sequenceDiagram (mermaid):
 * Client->>+AemetService: getUvPredictionData()
 * AemetService->>+AEMETFeignClient: getPrediccionUV()
 * AEMETFeignClient-->>-AemetService: {datos: "https://..."}
 * AemetService->>+AEMETFeignClient: followDataUrl(URI)
 * AEMETFeignClient-->>-AemetService: Datos reales
 * AemetService-->>-Client: Resultado
 */
@Service
@Slf4j
public class AemetService {

    @Autowired
    private AEMETFeignClient aemetClient;

    public String getUvPredictionData(@NotNull String date) {
        try {
            log.info("Solicitando predicción UV para la fecha: {}", date);

            // Primera llamada
            ResponseEntity<AemetApiResponse> response = aemetClient.getPrediccionUV(date);

            // Verificar si la respuesta es nula
            if (response == null) {
                throw new AemetServiceException("No se recibió respuesta del servicio AEMET");
            }

            // Verificar. Podemos asumir que response.getBody() no será nulo con Feign Client
            AemetApiResponse responseBody = response.getBody();
            if (!responseBody.isSuccess()) {
                log.error("Respuesta no exitosa de AEMET: {}", responseBody);
                throw new AemetServiceException("Respuesta no exitosa de AEMET");
            }

            String datosUrl = responseBody.getDatos();
            if (datosUrl == null || datosUrl.isEmpty()) {
                throw new AemetServiceException("URL de datos no disponible en la respuesta");
            }

            URI dataUri = this.getDataUri(datosUrl);

            ResponseEntity<String> dataResponse = aemetClient.followDataUrl(dataUri);

            // Verificar si la segunda respuesta es nula
            if (dataResponse == null) {
                throw new AemetServiceException("No se recibió respuesta al obtener los datos UV");
            }

            // Verificar. Podemos asumir que response.getBody() no será nulo con Feign Client
            String dataResponseBody = dataResponse.getBody();
            if (dataResponseBody.isEmpty()) {
                throw new AemetServiceException("El cuerpo de la respuesta de datos UV está vacío o es nulo");
            }

            log.debug("Datos UV obtenidos correctamente para la fecha: {}", date);
            return dataResponseBody;

        } catch (Exception e) {
            if (e instanceof AemetServiceException aemetServiceException) {
                throw aemetServiceException;
            }
            log.error("Error al obtener datos de predicción UV: {}", e.getMessage(), e);
            throw new AemetServiceException("Error al obtener datos de predicción UV", e);
        }
    }

    private URI getDataUri(@NotNull String datosUrl) {
        try {
            return new URI(datosUrl);
        } catch (URISyntaxException e) {
            log.error("URL mal formada: {}", datosUrl, e);
            throw new AemetServiceException("URL mal formada: " + datosUrl, e);
        }
    }
}
