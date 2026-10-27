package org.example.ui;

import org.example.engine.SimulationEngine;
import org.example.engine.SimulationObserver;
import org.example.engine.SimulationStats;
import org.example.model.Entity;
import org.example.model.Grid;
import org.example.model.Position;
import org.example.parser.StateSerializer;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Interfaccia utente a console (Implementazione Media).
 * Stampa a schermo la griglia testuale e le statistiche turno per turno
 * e salva automaticamente lo stato finale al termine dell'esecuzione.
 */
public class ConsoleView implements SimulationObserver {

    @Override
    public void onTurnCompleted(Grid grid, SimulationStats stats) {
        System.out.println("========================================");
        System.out.println("TURNO: " + stats.turn());

        if (!stats.activeEvents().isEmpty()) {
            String eventsStr = stats.activeEvents().stream()
                    .map(e -> e.name() + " (" + e.remainingTurns() + " turni rimanenti)")
                    .collect(Collectors.joining(", "));
            System.out.println("EVENTI GLOBALI ATTIVI: " + eventsStr);
        } else {
            System.out.println("EVENTI GLOBALI ATTIVI: Nessuno");
        }

        System.out.println("POPOLAZIONE PER SPECIE: " + stats.populationBySpecies());
        System.out.println("----------------------------------------");
        printGrid(grid);
        System.out.println();
    }

    /**
     * Stampa la griglia usando i simboli delle entità, rispettando la convenzione
     * secondo cui (0, 0) è la cella in basso a sinistra.
     *
     * @param grid griglia di simulazione
     */
    public void printGrid(Grid grid) {
        for (int y = grid.getHeight() - 1; y >= 0; y--) {
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < grid.getWidth(); x++) {
                Optional<Entity> entityOpt = grid.getEntityAt(new Position(x, y));
                row.append(entityOpt.map(Entity::getSymbol).orElse('.')).append(' ');
            }
            System.out.println(row);
        }
    }

    /**
     * Esegue la simulazione a console per N turni e salva il file di stato finale.
     *
     * @param engine     motore di simulazione
     * @param turns      numero di turni da eseguire
     * @param outputPath percorso dove salvare il file di stato finale
     */
    public void runConsoleSimulation(SimulationEngine engine, int turns, Path outputPath) {
        engine.addObserver(this);
        System.out.println("Avvio simulazione a console per " + turns + " turni...");
        engine.run(turns);

        StateSerializer serializer = new StateSerializer();
        serializer.saveToFile(engine.getGrid(), engine.getCurrentTurn(), outputPath);
        System.out.println("Simulazione terminata al turno " + engine.getCurrentTurn() + ".");
        System.out.println("File di stato salvato correttamente in: " + outputPath.toAbsolutePath());
    }
}