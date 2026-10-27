package org.example.model;

import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * Contratto per le piante, entità statiche alla base della catena alimentare.
 */
public interface Plant extends Entity {

    /**
     * Restituisce la quantità di energia fornita all'animale che mangia la pianta.
     *
     * @return valore di energia
     */
    int getEnergy();

    /**
     * Restituisce il tasso di riproduzione base configurato per la specie.
     *
     * @return probabilità base nell'intervallo [0.0, 1.0]
     */
    double getBaseReproductionRate();

    /**
     * Restituisce il tasso di riproduzione effettivo attuale (comprensivo di eventuali eventi globali).
     *
     * @return probabilità effettiva nell'intervallo [0.0, 1.0]
     */
    double getEffectiveReproductionRate();

    /**
     * Imposta un fattore moltiplicativo sul tasso di riproduzione (usato dagli eventi globali).
     *
     * @param factor fattore moltiplicativo (es. 0.3 per Siccità, 2.0 per Proliferazione, 1.0 per normale)
     */
    void setReproductionRateFactor(double factor);

    /**
     * Esegue il tentativo di riproduzione della pianta nel turno corrente,
     * generando una nuova pianta della stessa specie in una cella adiacente libera.
     *
     * @param currentPos posizione attuale della pianta sulla griglia
     * @param grid       griglia di simulazione
     * @param random     generatore di numeri casuali
     * @return Optional con la posizione della nuova pianta generata, o empty se non si è riprodotta
     */
    Optional<Position> reproduce(Position currentPos, Grid grid, RandomGenerator random);

    /**
     * Crea una nuova istanza della stessa specie di pianta (Prototype pattern).
     *
     * @return nuova pianta identica
     */
    Plant createOffspring();

    @Override
    default EntityType getType() {
        return EntityType.PLANT;
    }
}