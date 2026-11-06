package org.example;

import org.example.parser.ConfigParser;
import org.example.parser.SimulationConfig;
import org.example.ui.SwingGuiView;

import javax.swing.SwingUtilities;

/**
 * Classe Main che consente di avviare il simulatore EcoSim
 * Avvia l'interfaccia grafica Swing con la configurazione iniziale di default
 */
public class Main {

    private static final String DEFAULT_CONFIG = """
            GRID 40 30 TOROIDAL
            BACKGROUND 34 139 34
            # Definizione delle specie
            PLANT Erba SYMBOL G COLOR 0 180 0 ENERGY 10 RATE 0.05
            ANIMAL Coniglio SYMBOL C COLOR 255 255 255 ENERGY 20 MAX_ENERGY 50 \\
            DIET PLANT AGE_MAX 40 REPR_MIN 5 REPR_MAX 35 SIGHT 3 SPEED 2
            ANIMAL Volpe SYMBOL F COLOR 200 80 0 ENERGY 30 MAX_ENERGY 80 \\
            DIET ANIMAL:Coniglio AGE_MAX 60 REPR_MIN 8 REPR_MAX 50 SIGHT 5 SPEED 3
            OBSTACLE Roccia SYMBOL R COLOR 128 128 128
            # Eventi globali
            EVENT Siccita EVERY 20 RATE_FACTOR 0.3 DURATION 5
            EVENT Proliferazione EVERY 35 RATE_FACTOR 2.0 DURATION 5
            EVENT Epidemia EVERY 50 DEATH_PROB 0.25
            # Popolamento iniziale
            SPAWN Erba 200
            SPAWN Coniglio 30
            SPAWN Volpe 8
            SPAWN Roccia 20
            """;

    /**
     * Avvia il simulatore EcoSim aprendo l'interfaccia grafica
     *
     * @param args argomenti da riga di comando (non utilizzati)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimulationConfig defaultConfig = new ConfigParser().parse(DEFAULT_CONFIG);
            SwingGuiView gui = new SwingGuiView(defaultConfig);
            gui.setVisible(true);
        });
    }
}