package org.example.model;

/**
 * Implementazione concreta di un ostacolo statico e invalicabile.
 */
public class ObstacleEntity extends AbstractEntity implements Obstacle {

    /**
     * Costruisce un nuovo ostacolo.
     *
     * @param speciesName nome del tipo di ostacolo (es. "Roccia")
     * @param symbol      simbolo testuale
     * @param color       colore RGB
     */
    public ObstacleEntity(String speciesName, char symbol, RgbColor color) {
        super(speciesName, symbol, color);
    }
}