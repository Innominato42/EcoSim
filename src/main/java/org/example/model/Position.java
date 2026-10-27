package org.example.model;

/**
 * Rappresenta una coordinata discreta (x, y) all'interno della griglia di simulazione.
 * L'origine (0, 0) corrisponde alla cella in basso a sinistra.
 *
 * @param x coordinata orizzontale (colonna)
 * @param y coordinata verticale (riga)
 */
public record Position(int x, int y) {

    /**
     * Calcola una nuova posizione traslata rispetto a quella corrente.
     *
     * @param dx spostamento lungo l'asse X
     * @param dy spostamento lungo l'asse Y
     * @return la nuova posizione calcolata
     */
    public Position translate(int dx, int dy) {
        return new Position(this.x + dx, this.y + dy);
    }
}