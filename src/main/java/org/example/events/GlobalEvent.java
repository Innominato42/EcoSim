package org.example.events;


import org.example.model.Grid;

/**
 * Interfaccia per gli eventi globali che agiscono periodicamente l'ecosistema.
 * Si possono aggiungere nuovi tipi di evento semplicemente implementando questa interfaccia,
 * senza modificare il SimulationEngine
 */
public interface GlobalEvent {

    String getName();

    /**
     * Restituisce la frequenza di attivazione dell'evento
     *
     * @return numero di turni tra un'attivazione e la successiva
     */
    int getFrequency();

    /**
     * Restituisce la durata complessiva dell'effetto dell'evento
     *
     * @return durata in turni
     */
    int getDuration();

    /**
     * Restituisce il numero di turni rimanenti quando l'evento è attivo
     *
     * @return turni rimanenti (0 se l'evento non è attivo)
     */
    int getRemainingTurns();

    /**
     * Indica se l'evento è attualmente in corso
     *
     * @return true se attivo, false altrimenti
     */
    default boolean isActive() {
        return getRemainingTurns() > 0;
    }

    /**
     * Applica l'effetto dell'evento globale sulla griglia di simulazione
     *
     * @param grid griglia dell'ecosistema
     */
    void apply(Grid grid);

    /**
     * Aggiorna il ciclo di vita dell'evento al turno corrente: lo attiva se il turno è
     * multiplo della frequenza, mantiene l'effetto durante la durata o lo ripristina al termine
     *
     * @param currentTurn numero del turno corrente (> 0)
     * @param grid        griglia dell'ecosistema
     */
    void updateTurn(int currentTurn, Grid grid);
}