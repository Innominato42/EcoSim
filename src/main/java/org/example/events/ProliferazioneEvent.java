package org.example.events;

/**
 * Evento globale Proliferazione: aumenta il tasso di riproduzione di tutte le piante
 * moltiplicandolo per un fattore amplificativo (RATE_FACTOR >= 1.0).
 */
public class ProliferazioneEvent extends AbstractPlantRateEvent {

    /**
     * Costruisce un evento Proliferazione con durata personalizzata.
     *
     * @param frequency  turni tra un'attivazione e l'altra (EVERY)
     * @param rateFactor fattore amplificativo (>= 1.0)
     * @param duration   durata in turni
     */
    public ProliferazioneEvent(int frequency, double rateFactor, int duration) {
        super("Proliferazione", frequency, rateFactor, duration);
        if (rateFactor < 1.0) {
            throw new IllegalArgumentException("Per la Proliferazione il RATE_FACTOR deve essere >= 1.0.");
        }
    }

    /**
     * Costruisce un evento Proliferazione con durata di default (5 turni).
     *
     * @param frequency  turni tra un'attivazione e l'altra (EVERY)
     * @param rateFactor fattore amplificativo (>= 1.0)
     */
    public ProliferazioneEvent(int frequency, double rateFactor) {
        this(frequency, rateFactor, DEFAULT_DURATION);
    }
}