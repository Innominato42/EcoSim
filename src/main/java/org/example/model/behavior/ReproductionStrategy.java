package org.example.model.behavior;


import org.example.model.Position;

import java.util.Optional;

/**
 * Strategy per la riproduzione degli animali.
 */
public interface ReproductionStrategy {

    /**
     * Valuta le condizioni riproduttive (età ed energia) ed eventualmente genera un figlio
     * in una cella adiacente libera.
     *
     * @param context contesto del turno corrente
     * @return Optional con la posizione del nuovo nato, o empty se non si è riprodotto
     */
    Optional<Position> reproduce(TurnContext context);
}
