package org.example.model.behavior;



import org.example.model.Animal;
import org.example.model.Entity;
import org.example.model.Grid;
import org.example.model.Position;

import java.util.Optional;

/**
 * Implementazione standard della strategia di alimentazione: cerca la preda/pianta
 * commestibile a distanza minima entro il raggio visivo e ne assorbe l'energia.
 */
public class StandardFeedingStrategy implements FeedingStrategy {

    @Override
    public Optional<Position> findClosestFoodOffset(Animal animal, Position currentPos, Grid grid) {
        int sight = animal.getSightRange();
        Position bestOffset = null;
        double minDistance = Double.MAX_VALUE;

        for (int dx = -sight; dx <= sight; dx++) {
            for (int dy = -sight; dy <= sight; dy++) {
                if (dx == 0 && dy == 0) continue;

                double dist = Math.hypot(dx, dy);
                // Consideriamo il raggio visivo in distanza di Chebyshev (quadrato di lato 2*sight)
                if (Math.max(Math.abs(dx), Math.abs(dy)) <= sight) {
                    Optional<Position> candidatePos = grid.resolvePosition(currentPos.translate(dx, dy));
                    if (candidatePos.isPresent()) {
                        Optional<Entity> entityOpt = grid.getEntityAt(candidatePos.get());
                        if (entityOpt.isPresent() && animal.getDiet().canEat(entityOpt.get())) {
                            if (dist < minDistance) {
                                minDistance = dist;
                                bestOffset = new Position(dx, dy);
                            }
                        }
                    }
                }
            }
        }
        return Optional.ofNullable(bestOffset);
    }

    @Override
    public void eat(Animal animal, Entity food) {
        if (food != null && food.isAlive() && animal.getDiet().canEat(food)) {
            animal.addEnergy(food.getEnergy());
            food.die();
        }
    }
}