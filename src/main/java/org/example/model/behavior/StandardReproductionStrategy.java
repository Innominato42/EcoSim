package org.example.model.behavior;

import org.example.model.Animal;
import org.example.model.Grid;
import org.example.model.Position;

import java.util.List;
import java.util.Optional;

/**
 * Strategia di riproduzione standard: richiede che l'animale sia in età riproduttiva
 * e che la sua energia superi una frazione configurabile dell'energia massima.
 */
public class StandardReproductionStrategy implements ReproductionStrategy {

    private final double energyThresholdRatio;
    private final double energyCostRatio;

    /**
     * Costruisce la strategia con soglia di energia al 60% del massimo e costo energetico del 40%.
     */
    public StandardReproductionStrategy() {
        this(0.60, 0.40);
    }

    public StandardReproductionStrategy(double energyThresholdRatio, double energyCostRatio) {
        this.energyThresholdRatio = energyThresholdRatio;
        this.energyCostRatio = energyCostRatio;
    }

    @Override
    public Optional<Position> reproduce(TurnContext context) {
        Animal parent = context.getAnimal();
        Grid grid = context.getGrid();

        int requiredEnergy = (int) Math.ceil(parent.getMaxEnergy() * energyThresholdRatio);
        if (!parent.isAlive() || !parent.isInReproductiveAge() || parent.getEnergy() <= requiredEnergy) {
            return Optional.empty();
        }

        List<Position> freePositions = grid.getFreeAdjacentPositions(context.getCurrentPosition());
        if (freePositions.isEmpty()) {
            return Optional.empty();
        }

        Position babyPos = freePositions.get(context.getRandom().nextInt(freePositions.size()));
        Animal offspring = parent.createOffspring();
        if (grid.placeEntity(babyPos, offspring)) {
            int cost = Math.max(1, (int) (parent.getMaxEnergy() * energyCostRatio));
            parent.consumeEnergy(cost);
            return Optional.of(babyPos);
        }
        return Optional.empty();
    }
}
