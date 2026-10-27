package org.example.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * Implementazione concreta di una pianta capace di propagarsi nelle celle adiacenti libere.
 */
public class PlantEntity extends AbstractEntity implements Plant {

    private final int energy;
    private final double baseReproductionRate;
    private double rateFactor;

    /**
     * Costruisce una nuova pianta.
     *
     * @param speciesName          nome della specie (es. "Erba")
     * @param symbol               simbolo testuale (es. 'G')
     * @param color                colore RGB
     * @param energy               energia fornita a chi la consuma (> 0)
     * @param baseReproductionRate probabilità di riproduzione per turno [0.0, 1.0]
     */
    public PlantEntity(String speciesName, char symbol, RgbColor color, int energy, double baseReproductionRate) {
        super(speciesName, symbol, color);
        if (energy <= 0) {
            throw new IllegalArgumentException("L'energia della pianta deve essere positiva.");
        }
        if (baseReproductionRate < 0.0 || baseReproductionRate > 1.0) {
            throw new IllegalArgumentException("Il tasso di riproduzione deve essere compreso tra 0.0 e 1.0.");
        }
        this.energy = energy;
        this.baseReproductionRate = baseReproductionRate;
        this.rateFactor = 1.0;
    }

    @Override
    public int getEnergy() {
        return energy;
    }

    @Override
    public double getBaseReproductionRate() {
        return baseReproductionRate;
    }

    @Override
    public double getEffectiveReproductionRate() {
        return Math.min(1.0, baseReproductionRate * rateFactor);
    }

    @Override
    public void setReproductionRateFactor(double factor) {
        if (factor < 0.0) {
            throw new IllegalArgumentException("Il fattore moltiplicativo non può essere negativo.");
        }
        this.rateFactor = factor;
    }

    @Override
    public Optional<Position> reproduce(Position currentPos, Grid grid, RandomGenerator random) {
        Objects.requireNonNull(currentPos, "La posizione corrente non può essere null.");
        Objects.requireNonNull(grid, "La griglia non può essere null.");
        Objects.requireNonNull(random, "Il generatore random non può essere null.");

        if (!isAlive()) {
            return Optional.empty();
        }

        if (random.nextDouble() < getEffectiveReproductionRate()) {
            List<Position> freePositions = grid.getFreeAdjacentPositions(currentPos);
            if (!freePositions.isEmpty()) {
                Position target = freePositions.get(random.nextInt(freePositions.size()));
                Plant offspring = createOffspring();
                if (grid.placeEntity(target, offspring)) {
                    return Optional.of(target);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Plant createOffspring() {
        PlantEntity child = new PlantEntity(getSpeciesName(), getSymbol(), getColor(), energy, baseReproductionRate);
        child.setReproductionRateFactor(this.rateFactor);
        return child;
    }
}