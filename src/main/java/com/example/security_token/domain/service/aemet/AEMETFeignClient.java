package com.example.security_token.domain.service.aemet;

import com.example.security_token.config.aemet.AemetAuthConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;

@FeignClient(
        name = "aemet-client",      // Debe coincidir con la configuración
        url = "${aemet.base-url-dominio}",
        configuration = AemetAuthConfig.class
)
public interface AEMETFeignClient {

    // Primera llamada (devuelve la URL de datos)
    @GetMapping("/opendata/api/prediccion/especifica/uvi/{dia}")
    ResponseEntity<AemetApiResponse> getPrediccionUV(
            @PathVariable("dia") String dia);

    // Segunda llamada (para seguir la URL de datos)
    @GetMapping
    ResponseEntity<String> followDataUrl(@SpringQueryMap URI fullUrl);
}

/*    
   https://opendata.aemet.es/opendata/sh/36a59e59


// Ejemplo: Obtener predicción diaria por municipio (usando código INE)
    @GetMapping("/prediccion/especifica/municipio/diaria/{codigoMunicipio}")
    ResponseEntity<String> getPrediccionDiariaMunicipio(
            @PathVariable("codigoMunicipio") String codigoMunicipio,
            @RequestHeader("api_key") String apiKey);

    // Ejemplo: Obtener datos de observación (valores actuales)
    @GetMapping("/observacion/convencional/datos/estacion/{idEstacion}")
    ResponseEntity<String> getDatosObservacion(
            @PathVariable("idEstacion") String idEstacion,
            @RequestHeader("api_key") String apiKey);

    // Ejemplo: Obtener metadatos de estaciones
    @GetMapping("/valores/climatologicos/inventarioestaciones/todasestaciones")
    ResponseEntity<String> getMetadatosEstaciones(
            @RequestHeader("api_key") String apiKey);
    

*/

