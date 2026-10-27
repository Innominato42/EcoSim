package org.example;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import org.example.engine.SimulationEngine;
import org.example.parser.ConfigParser;
import org.example.parser.ConfigurationException;
import org.example.parser.SimulationConfig;
import org.example.parser.StateSerializer;
import org.example.ui.ConsoleView;
import org.example.ui.SwingGuiView;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Punto di ingresso principale dell'applicazione EcoSim.
 * Supporta sia l'avvio dell'interfaccia grafica (default con gradle run),
 * sia l'esecuzione a console passando il file di configurazione/stato da riga di comando.
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
     * Avvia il simulatore EcoSim.
     * Senza argomenti: avvia la GUI Swing con la configurazione di default.
     * Con argomenti (<percorso_file> [numero_turni] [output_stato]): esegue in modalità Console.
     *
     * @param args argomenti da riga di comando
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            SwingUtilities.invokeLater(() -> {
                SimulationConfig defaultConfig = new ConfigParser().parse(DEFAULT_CONFIG);
                SwingGuiView gui = new SwingGuiView(defaultConfig);
                gui.setVisible(true);
            });
            return;
        }

        // Modalità Console (Implementazione Media)
        try {
            Path inputPath = Path.of(args[0]);
            int turns = (args.length >= 2) ? Integer.parseInt(args[1]) : 50;
            Path outputPath = (args.length >= 3) ? Path.of(args[2]) : Path.of("final_state.txt");

            String content = Files.readString(inputPath).trim();
            SimulationEngine engine;
            if (content.startsWith("STATE")) {
                engine = new StateSerializer().deserialize(content, null);
            } else {
                SimulationConfig config = new ConfigParser().parse(content);
                engine = new SimulationEngine(config.getGrid(), config.getEvents());
            }

            ConsoleView consoleView = new ConsoleView();
            consoleView.runConsoleSimulation(engine, turns, outputPath);
        } catch (NumberFormatException e) {
            System.err.println("Errore: il numero di turni specificato non è un intero valido.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Errore di I/O: impossibile leggere il file specificato (" + e.getMessage() + ").");
            System.exit(1);
        } catch (ConfigurationException | IllegalArgumentException e) {
            System.err.println("Errore di configurazione: " + e.getMessage());
            System.exit(1);
        }
    }
}