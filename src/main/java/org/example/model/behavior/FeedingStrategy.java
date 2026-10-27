package org.example.model.behavior;

import org.example.model.Animal;
import org.example.model.Entity;
import org.example.model.Grid;
import org.example.model.Position;

import java.util.Optional;

/**
 * Strategy per la ricerca del cibo e l'alimentazione di un animale.
 */
public interface FeedingStrategy {

    /**
     * Scansiona il raggio visivo dell'animale e restituisce lo spostamento (dx, dy)
     * verso il cibo commestibile più vicino.
     *
     * @param animal     animale che cerca cibo
     * @param currentPos posizione attuale dell'animale
     * @param grid       griglia di simulazione
     * @return Optional contenente l'offset (dx, dy) verso il cibo più vicino, o empty se assente
     */
    Optional<Position> findClosestFoodOffset(Animal animal, Position currentPos, Grid grid);

    /**
     * Consuma l'entità cibo raggiunta e incrementa l'energia dell'animale.
     *
     * @param animal animale che si nutre
     * @param food   entità cibo raggiunta
     */
    void eat(Animal animal, Entity food);
}