package org.example.model;

/**
 * Interfaccia base per tutte le entità ospitate nella griglia (piante, animali, ostacoli).
 */
public interface Entity {

    String getSpeciesName();

    /**
     * Restituisce il simbolo testuale associato all'entità
     *
     * @return carattere che rappresenta l'entità
     */
    char getSymbol();

    /**
     * Restituisce il colore dell'entità nella rappresentazione grafica
     *
     * @return colore RGB dell'entità
     */
    RgbColor getColor();

    /**
     * Indica se l'entità è ancora viva nella simulazione
     *
     * @return true se l'entità è viva, false se deve essere rimossa
     */
    default boolean isAlive() {
        return true;
    }
    /**
     * Restituisce la categoria dell'entità
     *
     * @return categoria dell'entità (PLANT, ANIMAL, OBSTACLE)
     */
    EntityType getType();

    /**
     * Restituisce il valore energetico dell'entità se consumata come cibo
     *
     * @return quantità di energia (0 se non commestibile)
     */
    default int getEnergy() {
        return 0;
    }

    /**
     * Fa morire un entità
     */
    void die();
}
