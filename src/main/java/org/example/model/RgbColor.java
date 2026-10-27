package org.example.model;

/**
 * Rappresenta un colore nel formato RGB con componenti nell'intervallo [0, 255].
 * Mantiene il modello indipendente dal toolkit grafico scelto per la UI.
 *
 * @param r componente rossa (0-255)
 * @param g componente verde (0-255)
 * @param b componente blu (0-255)
 */
public record RgbColor(int r, int g, int b) {
    public RgbColor {
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            throw new IllegalArgumentException("I valori RGB devono essere compresi tra 0 e 255.");
        }
    }
}