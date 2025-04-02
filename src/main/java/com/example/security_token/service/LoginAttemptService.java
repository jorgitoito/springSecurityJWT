package com.example.security_token.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio que provee seguridad. 
 * Limitar la cantidad de solicitudes erroneas. X numero de llamadas en un tiempo.
 * En vez de esta solucion Custom se podria usar:  Bucket4j o Resilience4j
 */
@Service
public class LoginAttemptService {

    @Value("${auth.max-attempts}")
    private int MAX_ATTEMPTS;  // Máximo de intentos permitidos

    @Value("${auth.lock-time}")
    private long LOCK_TIME;  // Tiempo de bloqueo en milisegundos

    // HashMap para almacenar los intentos fallidos
    private final ConcurrentHashMap<String, LoginAttempt> attemptsCache = new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {
        LoginAttempt attempt = attemptsCache.get(username);

        if (attempt == null) {
            return false;
        }

        // Si ha pasado más tiempo que el límite de bloqueo, reiniciamos los intentos
        if (System.currentTimeMillis() - attempt.getTimestamp() > LOCK_TIME) {
            attemptsCache.remove(username);
            return false;
        }

        // Si el número de intentos supera el límite, bloqueamos el acceso
        return attempt.getAttempts() >= MAX_ATTEMPTS;
    }

    public void loginSucceeded(String username) {
        attemptsCache.remove(username);  // Limpiar el registro de intentos después de un login exitoso
    }

    public void loginFailed(String username) {
        LoginAttempt attempt = attemptsCache.get(username);

        if (attempt == null) {
            attempt = new LoginAttempt();
            attemptsCache.put(username, attempt);
        }

        attempt.incrementAttempts();
    }

    private static class LoginAttempt {
        private int attempts;
        private long timestamp;

        public LoginAttempt() {
            this.attempts = 0;
            this.timestamp = System.currentTimeMillis();
        }

        public int getAttempts() {
            return attempts;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void incrementAttempts() {
            this.attempts++;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
