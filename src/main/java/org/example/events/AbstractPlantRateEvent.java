package org.example.events;


import org.example.model.Entity;
import org.example.model.Grid;
import org.example.model.Plant;

import java.util.Objects;

/**
 * Classe base astratta per gli eventi globali che alterano temporaneamente
 * il tasso di riproduzione delle piante per un determinato numero di turni.
 */
public abstract class AbstractPlantRateEvent implements GlobalEvent {

    public static final int DEFAULT_DURATION = 5;

    private final String name;
    private final int frequency;
    private final double rateFactor;
    private final int duration;
    private int remainingTurns;

    /**
     * Costruisce un evento basato sulla modifica del tasso di riproduzione delle piante.
     *
     * @param name       nome dell'evento
     * @param frequency  frequenza di attivazione (EVERY)
     * @param rateFactor fattore moltiplicativo del tasso di riproduzione (RATE_FACTOR)
     * @param duration   durata dell'evento in turni (DURATION)
     */
    protected AbstractPlantRateEvent(String name, int frequency, double rateFactor, int duration) {
        if (frequency <= 0) {
            throw new IllegalArgumentException("La frequenza (EVERY) deve essere positiva.");
        }
        if (rateFactor < 0.0) {
            throw new IllegalArgumentException("Il fattore RATE_FACTOR non può essere negativo.");
        }
        if (duration <= 0) {
            throw new IllegalArgumentException("La durata (DURATION) deve essere positiva.");
        }
        this.name = Objects.requireNonNull(name, "Il nome dell'evento non può essere null.");
        this.frequency = frequency;
        this.rateFactor = rateFactor;
        this.duration = duration;
        this.remainingTurns = 0;
    }

    @Override
    public void apply(Grid grid) {
        Objects.requireNonNull(grid, "La griglia non può essere null.");
        this.remainingTurns = this.duration;
        updatePlantsFactor(grid, this.rateFactor);
    }

    @Override
    public void updateTurn(int currentTurn, Grid grid) {
        Objects.requireNonNull(grid, "La griglia non può essere null.");
        if (currentTurn > 0 && currentTurn % frequency == 0) {
            apply(grid);
        } else if (remainingTurns > 0) {
            remainingTurns--;
            if (remainingTurns > 0) {
                // Mantiene il fattore attivo anche sulle piante nate durante l'evento
                updatePlantsFactor(grid, this.rateFactor);
            } else {
                // Ripristina il tasso normale al termine della durata
                updatePlantsFactor(grid, 1.0);
            }
        }
    }

    private void updatePlantsFactor(Grid grid, double factor) {
        for (Entity entity : grid.getAllEntities().values()) {
            if (entity instanceof Plant plant) {
                plant.setReproductionRateFactor(factor);
            }
        }
    }

    @Override
    public String getName() { return name; }

    @Override
    public int getFrequency() { return frequency; }

    @Override
    public int getDuration() { return duration; }

    @Override
    public int getRemainingTurns() { return remainingTurns; }

    public double getRateFactor() { return rateFactor; }
}