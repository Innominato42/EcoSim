package org.example.model.behavior;

/**
 * Strategy per il movimento di un animale sulla griglia.
 */
public interface MovementStrategy {

    /**
     * Esegue il movimento dell'animale aggiornando la griglia e il contesto del turno.
     *
     * @param context contesto del turno corrente
     */
    void move(TurnContext context);
}