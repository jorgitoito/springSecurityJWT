package com.example.security_token.domain.service.aemet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.http.HttpStatus;

// Clase para la estructura de respuesta
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AemetApiResponse {
    private String descripcion;
    private int estado;
    private String datos;
    private String metadatos;

    // Método útil para extraer el nombre del recurso de la URL
    public String getResourceId() {
        return datos.substring(datos.lastIndexOf('/') + 1);
    }

    public boolean isSuccess() {
        return estado == HttpStatus.OK.value();
    }
}
