package com.example.security_token.domain.service.aemet;

import com.example.security_token.config.aemet.AemetAuthConfig;
import com.example.security_token.config.aemet.AemetRetryConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;

@FeignClient(
        name = "aemet-client",      // Debe coincidir con la configuración
        url = "${aemet.base-url-dominio}",
        configuration = {AemetAuthConfig.class, AemetRetryConfig.class}
)
public interface AEMETFeignClient {

    // Primera llamada (devuelve la URL de datos)
    @GetMapping("/opendata/api/prediccion/especifica/uvi/{dia}")
    ResponseEntity<AemetApiResponse> getPrediccionUV(
            @PathVariable("dia") String dia);

    // Primera llamada (devuelve la URL de datos)
    @GetMapping("/opendata/api/prediccion/especifica/playa/{codPlaya}")
    ResponseEntity<AemetApiResponse> getPrediccionPlaya(
            @PathVariable("codPlaya") String codPlaya);
    

    // Segunda llamada (para seguir la URL de datos)
    @GetMapping
    ResponseEntity<String> followDataUrl(@SpringQueryMap URI fullUrl);
}

