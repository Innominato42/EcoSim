package model;

import org.example.engine.SimulationEngine;
import org.example.engine.SimulationStats;
import org.example.events.GlobalEvent;
import org.example.events.SiccitaEvent;
import org.example.model.*;
import org.example.model.behavior.AnimalTurnChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationEngineTest {

    private Grid grid;
    private AnimalTurnChain chain;

    @BeforeEach
    void setUp() {
        grid = new Grid(10, 10, BorderType.TOROIDAL, new RgbColor(34, 139, 34));
        chain = AnimalTurnChain.createDefault();
    }

    @Test
    void testDeadAnimalsAreRemovedFromGridAtEndOfTurn() {
        // Coniglio con energia 1: muore nel primo turno per invecchiamento e deve essere rimosso al passo 9
        Animal dyingRabbit = new AnimalEntity(
                "Coniglio", 'C', new RgbColor(255, 255, 255),
                1, 50, 40, 5, 35, Diet.herbivore(), 3, 2, chain
        );
        Position pos = new Position(4, 4);
        grid.placeEntity(pos, dyingRabbit);

        SimulationEngine engine = new SimulationEngine(grid, List.of(), 0, new Random(42));
        SimulationStats stats = engine.step();

        assertEquals(1, stats.turn());
        assertTrue(grid.getAllEntities().isEmpty());
        assertEquals(0, stats.populationBySpecies().getOrDefault("Coniglio", 0));
    }

    @Test
    void testEngineTriggersGlobalEventsAndNotifiesObservers() {
        Plant grass = new PlantEntity("Erba", 'G', new RgbColor(0, 180, 0), 10, 0.0);
        grid.placeEntity(new Position(2, 2), grass);

        // Evento Siccità che si attiva ogni 2 turni con durata 4
        GlobalEvent siccita = new SiccitaEvent(2, 0.5, 4);
        SimulationEngine engine = new SimulationEngine(grid, List.of(siccita), 0, new Random(42));

        List<SimulationStats> receivedNotifications = new ArrayList<>();
        engine.addObserver((g, stats) -> receivedNotifications.add(stats));

        // Eseguiamo 2 turni
        engine.run(2);

        assertEquals(2, receivedNotifications.size());

        // Al turno 1 l'evento non era ancora attivo
        assertTrue(receivedNotifications.get(0).activeEvents().isEmpty());

        // Al turno 2 l'evento Siccità si è attivato
        SimulationStats turn2Stats = receivedNotifications.get(1);
        assertEquals(2, turn2Stats.turn());
        assertEquals(1, turn2Stats.activeEvents().size());
        assertEquals("Siccita", turn2Stats.activeEvents().get(0).name());
        assertEquals(4, turn2Stats.activeEvents().get(0).remainingTurns());
        assertEquals(1, turn2Stats.populationBySpecies().get("Erba"));
    }
}
