package org.example.engine;

import org.example.model.Grid;

/**
 * Contratto per gli osservatori (UI grafica, interfaccia a console, logger)
 * interessati a ricevere notifiche ad ogni avanzamento di turno della simulazione.
 */
public interface SimulationObserver {

    /**
     * Metodo invocato dal motore di simulazione al termine di ogni turno.
     *
     * @param grid  griglia di simulazione aggiornata
     * @param stats statistiche globali calcolate a fine turno
     */
    void onTurnCompleted(Grid grid, SimulationStats stats);
}
