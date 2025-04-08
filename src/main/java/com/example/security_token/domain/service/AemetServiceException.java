package com.example.security_token.domain.service;


import feign.FeignException;

import java.net.URISyntaxException;

/**
 * Excepción personalizada para errores en el servicio AEMET
 */
public class AemetServiceException extends RuntimeException {

    private final String codigoError;
    private final String detalleTecnico;

    // Constructor básico
    public AemetServiceException(String mensaje) {
        super(mensaje);
        this.codigoError = "AEMET_000";
        this.detalleTecnico = mensaje;
    }

    // Constructor con causa
    public AemetServiceException(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigoError = determinarCodigoError(causa);
        this.detalleTecnico = generarDetalleTecnico(causa);
    }

    // Constructor completo
    public AemetServiceException(String mensaje, String codigoError, String detalleTecnico, Throwable causa) {
        super(mensaje, causa);
        this.codigoError = codigoError;
        this.detalleTecnico = detalleTecnico;
    }

    // Métodos auxiliares
    private String determinarCodigoError(Throwable causa) {
        if (causa instanceof FeignException.NotFound) {
            return "AEMET_404";
        } else if (causa instanceof FeignException.Unauthorized) {
            return "AEMET_401";
        } else if (causa instanceof FeignException.ServiceUnavailable) {
            return "AEMET_503";
        } else if (causa instanceof URISyntaxException) {
            return "AEMET_URI_ERROR";
        } else {
            return "AEMET_500";
        }
    }

    private String generarDetalleTecnico(Throwable causa) {
        if (causa instanceof FeignException feignException) {
            return String.format("Feign Error: %s - %s",
                    feignException.status(),
                    feignException.contentUTF8());
        }
        return causa.getMessage();
    }

    // Getters
    public String getCodigoError() {
        return codigoError;
    }

    public String getDetalleTecnico() {
        return detalleTecnico;
    }

}