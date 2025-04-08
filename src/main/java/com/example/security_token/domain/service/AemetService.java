package com.example.security_token.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${aemet.api.key}")
    private String apiKey;

    public String getUvPredictionData(String date) {
        try {
            // Primera llamada
            ResponseEntity<AemetApiResponse> response = aemetClient.getPrediccionUV(date);

            if (!response.getBody().isSuccess()) {
                throw new AemetServiceException("Respuesta no exitosa de AEMET");
            }

            // Segunda llamada (directamente a la URL completa)
            URI dataUri = new URI(response.getBody().getDatos());
            ResponseEntity<String> dataResponse = aemetClient.followDataUrl(dataUri);

            return dataResponse.getBody();

        } catch (URISyntaxException e) {
            throw new AemetServiceException("URL mal formada", e);
        }
    }


}
