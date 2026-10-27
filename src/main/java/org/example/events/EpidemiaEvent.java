package org.example.events;

import org.example.model.Entity;
import org.example.model.EntityType;
import org.example.model.Grid;
import org.example.model.Position;

import java.util.*;
import java.util.random.RandomGenerator;

public class EpidemiaEvent implements GlobalEvent {

    private final int frequency;
    private final double deathProbability;
    private final RandomGenerator random;
    private int remainingTurns;

    /**
     * Costruisce un evento Epidemia con generatore di numeri casuali personalizzato (utile per i test).
     *
     * @param frequency        turni tra un'attivazione e l'altra (EVERY)
     * @param deathProbability probabilità di morte nell'intervallo [0.0, 1.0] (DEATH_PROB)
     * @param random           generatore di numeri casuali
     */
    public EpidemiaEvent(int frequency, double deathProbability, RandomGenerator random) {
        if (frequency <= 0) {
            throw new IllegalArgumentException("La frequenza (EVERY) deve essere positiva.");
        }
        if (deathProbability < 0.0 || deathProbability > 1.0) {
            throw new IllegalArgumentException("DEATH_PROB deve essere compreso tra 0.0 e 1.0.");
        }
        this.frequency = frequency;
        this.deathProbability = deathProbability;
        this.random = Objects.requireNonNull(random, "Il generatore random non può essere null.");
        this.remainingTurns = 0;
    }

    /**
     * Costruisce un evento Epidemia con generatore casuale predefinito.
     *
     * @param frequency        turni tra un'attivazione e l'altra (EVERY)
     * @param deathProbability probabilità di morte nell'intervallo [0.0, 1.0] (DEATH_PROB)
     */
    public EpidemiaEvent(int frequency, double deathProbability) {
        this(frequency, deathProbability, new Random());
    }

    @Override
    public void apply(Grid grid) {
        Objects.requireNonNull(grid, "La griglia non può essere null.");
        this.remainingTurns = 1;

        List<Position> victims = new ArrayList<>();
        for (Map.Entry<Position, Entity> entry : grid.getAllEntities().entrySet()) {
            Entity entity = entry.getValue();
            if (entity.getType() == EntityType.ANIMAL && entity.isAlive()) {
                if (random.nextDouble() < deathProbability) {
                    entity.die();
                    victims.add(entry.getKey());
                }
            }
        }
        for (Position pos : victims) {
            grid.removeEntityAt(pos);
        }
    }

    @Override
    public void updateTurn(int currentTurn, Grid grid) {
        Objects.requireNonNull(grid, "La griglia non può essere null.");
        if (currentTurn > 0 && currentTurn % frequency == 0) {
            apply(grid);
        } else if (remainingTurns > 0) {
            remainingTurns = 0;
        }
    }

    @Override
    public String getName() { return "Epidemia"; }

    @Override
    public int getFrequency() { return frequency; }

    @Override
    public int getDuration() { return 1; }

    @Override
    public int getRemainingTurns() { return remainingTurns; }

    public double getDeathProbability() { return deathProbability; }
}
