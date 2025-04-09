package com.example.security_token.domain.service.aemet;

import com.example.security_token.api.aemet.exception.AemetServiceException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

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

    /**
     * Prediccion UV
     * 
     * @param date 0 es hoy, 1 mañana, ... hasta 4
     * @return prediccion UV
     */
    public String getUvPredictionData(@NotNull String date) {
        return getAemetData(
                date,
                "UV",
                param -> aemetClient.getPrediccionUV(param)
        );
    }

    /**
     * codPlaya:
     * 0301101
     * 0301401
     * 0301408
     * 0301410
     * 0301808
     * 
     * @param codPlaya codigo playa
     * @return prediccion playa
     */
    public String getPredictionPlaya(@NotNull String codPlaya) {
        return getAemetData(
                codPlaya,
                "Playa",
                param -> aemetClient.getPrediccionPlaya(param)
        );
    }

    /**
     * Dos pasos:
     * 1. Devuelve datos con la url donde estan los datos
     * 2. Seguir esa url y recuperar los datos.
     * 
     * @param param parametro de consultas con un solo parametro
     * @param tipoConsulta tipo consultas
     * @param apiCall la llamada a realizar
     * @return datos de aemet
     */

    private String getAemetData(@NotNull String param, String tipoConsulta,
                                Function<String, ResponseEntity<AemetApiResponse>> apiCall) {
        try {
            log.info("Solicitando predicción {} para el parámetro: {}", tipoConsulta, param);

            // Primera llamada
            ResponseEntity<AemetApiResponse> response = apiCall.apply(param);

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
            String dataResponseBody = getDataFromUri(dataUri);

            log.debug("Datos {} obtenidos correctamente para el parámetro: {}", tipoConsulta, param);
            return dataResponseBody;

        } catch (Exception e) {
            handleException(e, tipoConsulta, param);
            // Esta línea nunca se ejecutará pero es necesaria para el compilador
            return null;
        }
    }

    private String getDataFromUri(URI dataUri) {
        ResponseEntity<String> dataResponse = aemetClient.followDataUrl(dataUri);

        // Verificar si la segunda respuesta es nula
        if (dataResponse == null) {
            throw new AemetServiceException("No se recibió respuesta al obtener los datos");
        }

        // Verificar. Podemos asumir que response.getBody() no será nulo con Feign Client
        String dataResponseBody = dataResponse.getBody();
        if (dataResponseBody.isEmpty()) {
            throw new AemetServiceException("El cuerpo de la respuesta de datos está vacío");
        }

        return dataResponseBody;
    }

    private void handleException(Exception e, String tipoConsulta, String param) {
        if (e instanceof AemetServiceException aemetServiceException) {
            throw aemetServiceException;
        }
        log.error("Error al obtener datos de predicción {}: {}", tipoConsulta, e.getMessage(), e);
        throw new AemetServiceException("Error al obtener datos de predicción " + tipoConsulta, e);
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