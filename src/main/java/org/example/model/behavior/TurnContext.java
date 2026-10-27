package org.example.model.behavior;



import org.example.model.Animal;
import org.example.model.Entity;
import org.example.model.Grid;
import org.example.model.Position;

import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * Contesto di esecuzione di un singolo turno per un animale attraverso la Chain of Responsibility.
 */
public class TurnContext {

    private final Animal animal;
    private Position currentPosition;
    private final Grid grid;
    private final RandomGenerator random;
    private Position targetFoodOffset;
    private Entity reachedFood;

    public TurnContext(Animal animal, Position currentPosition, Grid grid, RandomGenerator random) {
        this.animal = animal;
        this.currentPosition = currentPosition;
        this.grid = grid;
        this.random = random;
    }

    public Animal getAnimal() { return animal; }
    public Position getCurrentPosition() { return currentPosition; }
    public void setCurrentPosition(Position currentPosition) { this.currentPosition = currentPosition; }
    public Grid getGrid() { return grid; }
    public RandomGenerator getRandom() { return random; }

    public Optional<Position> getTargetFoodOffset() { return Optional.ofNullable(targetFoodOffset); }
    public void setTargetFoodOffset(Position offset) { this.targetFoodOffset = offset; }

    public Optional<Entity> getReachedFood() { return Optional.ofNullable(reachedFood); }
    public void setReachedFood(Entity reachedFood) { this.reachedFood = reachedFood; }
}
