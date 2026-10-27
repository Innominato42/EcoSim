package org.example.model;

import java.util.Optional;

/**
 * Definisce la topologia dei bordi della griglia e la strategia di risoluzione delle coordinate.
 */
public enum BorderType {

    /**
     * Bordi invalicabili: le coordinate fuori dai limiti non sono valide.
     */
    BOUNDED {
        @Override
        public Optional<Position> resolve(Position pos, int width, int height) {
            if (pos.x() >= 0 && pos.x() < width && pos.y() >= 0 && pos.y() < height) {
                return Optional.of(pos);
            }
            return Optional.empty();
        }
    },

    /**
     * Griglia toroidale: il lato destro è adiacente al sinistro e l'alto al basso.
     */
    TOROIDAL {
        @Override
        public Optional<Position> resolve(Position pos, int width, int height) {
            int wrappedX = Math.floorMod(pos.x(), width);
            int wrappedY = Math.floorMod(pos.y(), height);
            return Optional.of(new Position(wrappedX, wrappedY));
        }
    };

    /**
     * Risolve una posizione rispetto alle dimensioni della griglia.
     *
     * @param pos    la posizione da verificare o normalizzare
     * @param width  larghezza della griglia
     * @param height altezza della griglia
     * @return un Optional contenente la posizione valida, oppure vuoto se fuori dai bordi invalicabili
     */
    public abstract Optional<Position> resolve(Position pos, int width, int height);
}
