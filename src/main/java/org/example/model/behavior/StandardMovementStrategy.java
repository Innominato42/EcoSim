package org.example.model.behavior;

import org.example.model.Animal;
import org.example.model.Entity;
import org.example.model.Grid;
import org.example.model.Position;

import java.util.List;
import java.util.Optional;

/**
 * Strategia di movimento standard: muove l'animale passo-passo (fino alla sua velocità massima)
 * verso il cibo individuato oppure in direzione casuale se nessun cibo è visibile.
 */
public class StandardMovementStrategy implements MovementStrategy {

    @Override
    public void move(TurnContext context) {
        Animal animal = context.getAnimal();
        Grid grid = context.getGrid();
        int steps = animal.getSpeed();

        Optional<Position> targetOffsetOpt = context.getTargetFoodOffset();

        if (targetOffsetOpt.isPresent()) {
            moveTowardsTarget(context, targetOffsetOpt.get(), steps);
        } else {
            moveRandomly(context, steps);
        }
    }

    private void moveTowardsTarget(TurnContext context, Position offset, int maxSteps) {
        Animal animal = context.getAnimal();
        Grid grid = context.getGrid();
        int remainingDx = offset.x();
        int remainingDy = offset.y();

        for (int step = 0; step < maxSteps && (remainingDx != 0 || remainingDy != 0); step++) {
            int stepX = Integer.compare(remainingDx, 0);
            int stepY = Integer.compare(remainingDy, 0);

            Position current = context.getCurrentPosition();
            Optional<Position> nextPosOpt = grid.resolvePosition(current.translate(stepX, stepY));

            if (nextPosOpt.isEmpty()) {
                break;
            }
            Position nextPos = nextPosOpt.get();
            Optional<Entity> occupantOpt = grid.getEntityAt(nextPos);

            if (occupantOpt.isEmpty()) {
                // Cella libera: avanza
                grid.removeEntityAt(current);
                grid.placeEntity(nextPos, animal);
                context.setCurrentPosition(nextPos);
                remainingDx -= stepX;
                remainingDy -= stepY;
            } else if (animal.getDiet().canEat(occupantOpt.get())) {
                // Cella occupata da cibo commestibile: entra nella cella e preleva il cibo per la fase 5 (Mangia)
                Entity food = occupantOpt.get();
                grid.removeEntityAt(nextPos);
                grid.removeEntityAt(current);
                grid.placeEntity(nextPos, animal);
                context.setCurrentPosition(nextPos);
                context.setReachedFood(food);
                break; // Raggiunto il cibo, termina il movimento
            } else {
                // Cella bloccata da un ostacolo o da un'entità non commestibile: prova ad aggirare
                List<Position> freeAdj = grid.getFreeAdjacentPositions(current);
                if (freeAdj.isEmpty()) {
                    break;
                }
                Position detour = freeAdj.get(context.getRandom().nextInt(freeAdj.size()));
                grid.removeEntityAt(current);
                grid.placeEntity(detour, animal);
                context.setCurrentPosition(detour);
                break;
            }
        }
    }

    private void moveRandomly(TurnContext context, int maxSteps) {
        Animal animal = context.getAnimal();
        Grid grid = context.getGrid();

        for (int step = 0; step < maxSteps; step++) {
            Position current = context.getCurrentPosition();
            List<Position> freeAdj = grid.getFreeAdjacentPositions(current);
            if (freeAdj.isEmpty()) {
                break;
            }
            Position nextPos = freeAdj.get(context.getRandom().nextInt(freeAdj.size()));
            grid.removeEntityAt(current);
            grid.placeEntity(nextPos, animal);
            context.setCurrentPosition(nextPos);
        }
    }
}
