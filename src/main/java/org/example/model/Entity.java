package org.example.model;

/**
 * Contratto base per tutte le entità ospitate nella griglia (piante, animali, ostacoli).
 */
public interface Entity {

    /**
     * Restituisce il nome della specie o del tipo di entità (es. "Erba", "Coniglio", "Roccia").
     *
     * @return identificativo della specie
     */
    String getSpeciesName();

    /**
     * Restituisce il simbolo testuale associato all'entità.
     *
     * @return carattere rappresentativo
     */
    char getSymbol();

    /**
     * Restituisce il colore associato all'entità per la rappresentazione grafica.
     *
     * @return colore RGB dell'entità
     */
    RgbColor getColor();

    /**
     * Indica se l'entità è ancora viva/attiva nella simulazione.
     *
     * @return true se l'entità è viva, false se deve essere rimossa
     */
    default boolean isAlive() {
        return true;
    }
    /**
     * Restituisce la categoria dell'entità senza ricorrere a controlli sul tipo concreto.
     *
     * @return categoria dell'entità (PLANT, ANIMAL, OBSTACLE)
     */
    EntityType getType();

    /**
     * Restituisce il valore energetico dell'entità se consumata come cibo.
     *
     * @return quantità di energia (default 0 per entità non commestibili)
     */
    default int getEnergy() {
        return 0;
    }

    /**
     * Segna l'entità come morta o consumata.
     */
    void die();
}
