package org.example.events;

/**
 * Evento globale Siccità: riduce il tasso di riproduzione di tutte le piante
 * moltiplicandolo per un fattore riduttivo (RATE_FACTOR < 1.0).
 */
public class SiccitaEvent extends AbstractPlantRateEvent {

    /**
     * Costruisce un evento Siccità con durata personalizzata.
     *
     * @param frequency  turni tra un'attivazione e l'altra (EVERY)
     * @param rateFactor fattore riduttivo nell'intervallo [0.0, 1.0]
     * @param duration   durata in turni
     */
    public SiccitaEvent(int frequency, double rateFactor, int duration) {
        super("Siccita", frequency, rateFactor, duration);
        if (rateFactor > 1.0) {
            throw new IllegalArgumentException("Per la Siccità il RATE_FACTOR deve essere <= 1.0.");
        }
    }

    /**
     * Costruisce un evento Siccità con durata di default (5 turni).
     *
     * @param frequency  turni tra un'attivazione e l'altra (EVERY)
     * @param rateFactor fattore riduttivo nell'intervallo [0.0, 1.0]
     */
    public SiccitaEvent(int frequency, double rateFactor) {
        this(frequency, rateFactor, DEFAULT_DURATION);
    }
}