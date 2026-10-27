package org.example.model;

/**
 * Contratto per gli ostacoli statici e invalicabili presenti sulla griglia
 */
public interface Obstacle extends Entity{
    @Override
    default EntityType getType() {
        return EntityType.OBSTACLE;
    }
}
