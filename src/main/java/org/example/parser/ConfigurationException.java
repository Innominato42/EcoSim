package org.example.parser;

/**
 * Eccezione sollevata in caso di errori di sintassi o parametri non validi
 * durante il parsing dei file di configurazione o di stato.
 */
public class ConfigurationException extends RuntimeException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
