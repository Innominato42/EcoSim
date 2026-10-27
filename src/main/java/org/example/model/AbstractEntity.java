package org.example.model;

import java.util.Objects;

/**
 * Implementazione base comune a tutte le entità dell'ecosistema.
 * Gestisce i dati identificativi, estetici e lo stato vitale.
 */
public abstract class AbstractEntity implements Entity {

    private final String speciesName;
    private final char symbol;
    private final RgbColor color;
    private boolean alive;

    /**
     * Costruisce una nuova entità con i parametri base.
     *
     * @param speciesName nome della specie o tipo (es. "Erba", "Coniglio", "Roccia")
     * @param symbol      carattere per la visualizzazione testuale
     * @param color       colore RGB per la visualizzazione grafica
     */
    protected AbstractEntity(String speciesName, char symbol, RgbColor color) {
        if (speciesName == null || speciesName.isBlank()) {
            throw new IllegalArgumentException("Il nome della specie non può essere vuoto.");
        }
        this.speciesName = speciesName;
        this.symbol = symbol;
        this.color = Objects.requireNonNull(color, "Il colore non può essere null.");
        this.alive = true;
    }

    @Override
    public String getSpeciesName() {
        return speciesName;
    }

    @Override
    public char getSymbol() {
        return symbol;
    }

    @Override
    public RgbColor getColor() {
        return color;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    /**
     * Segna l'entità come morta/consumata per la successiva rimozione dalla griglia.
     */
    public void die() {
        this.alive = false;
    }
}