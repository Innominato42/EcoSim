package model;

import org.example.engine.SimulationEngine;
import org.example.model.*;
import org.example.parser.ConfigParser;
import org.example.parser.ConfigurationException;
import org.example.parser.SimulationConfig;
import org.example.parser.StateSerializer;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void testParseSpecificationConfigurationExample() {
        String configText = """
                GRID 40 30 TOROIDAL
                BACKGROUND 34 139 34
                # Definizione delle specie
                PLANT Erba SYMBOL G COLOR 0 180 0 ENERGY 10 RATE 0.05
                ANIMAL Coniglio SYMBOL C COLOR 255 255 255 ENERGY 20 MAX_ENERGY 50 \\
                DIET PLANT AGE_MAX 40 REPR_MIN 5 REPR_MAX 35 SIGHT 3 SPEED 2
                ANIMAL Volpe SYMBOL F COLOR 200 80 0 ENERGY 30 MAX_ENERGY 80 \\
                DIET ANIMAL:Coniglio AGE_MAX 60 REPR_MIN 8 REPR_MAX 50 SIGHT 5 SPEED 3
                OBSTACLE Roccia SYMBOL R COLOR 128 128 128
                # Eventi globali: EVENT <tipo> EVERY <n> [parametri]
                EVENT Siccita EVERY 20 RATE_FACTOR 0.3
                # Popolamento iniziale: SPAWN <specie> <quantita>
                SPAWN Erba 200
                SPAWN Coniglio 30
                SPAWN Volpe 8
                SPAWN Roccia 20
                """;

        ConfigParser parser = new ConfigParser(new Random(42));
        SimulationConfig config = parser.parse(configText);

        Grid grid = config.getGrid();
        assertEquals(40, grid.getWidth());
        assertEquals(30, grid.getHeight());
        assertEquals(BorderType.TOROIDAL, grid.getBorderType());
        assertEquals(new RgbColor(34, 139, 34), grid.getBackgroundColor());

        // Verifica che siano state posizionate tutte le 200 + 30 + 8 + 20 = 258 entità
        assertEquals(258, grid.getAllEntities().size());

        // Verifica l'evento globale configurato
        assertEquals(1, config.getEvents().size());
        assertEquals("Siccita", config.getEvents().get(0).getName());
        assertEquals(20, config.getEvents().get(0).getFrequency());
    }

    @Test
    void testStateSerializationAndDeserialization() {
        String stateText = """
                STATE TURN 143
                ENTITY Coniglio 12 7 ENERGY 18 AGE 11
                ENTITY Volpe 5 22 ENERGY 45 AGE 3
                ENTITY Erba 10 10
                """;

        StateSerializer serializer = new StateSerializer();
        SimulationEngine engine = serializer.deserialize(stateText, null);

        assertEquals(143, engine.getCurrentTurn());
        Grid grid = engine.getGrid();
        assertEquals(3, grid.getAllEntities().size());

        Entity rabbitEntity = grid.getEntityAt(new Position(12, 7)).orElseThrow();
        assertTrue(rabbitEntity instanceof Animal);
        Animal rabbit = (Animal) rabbitEntity;
        assertEquals("Coniglio", rabbit.getSpeciesName());
        assertEquals(18, rabbit.getEnergy());
        assertEquals(11, rabbit.getAge());

        Entity grassEntity = grid.getEntityAt(new Position(10, 10)).orElseThrow();
        assertEquals("Erba", grassEntity.getSpeciesName());

        // Verifica che la ri-serializzazione produca i medesimi dati
        String serializedAgain = serializer.serialize(grid, engine.getCurrentTurn());
        assertTrue(serializedAgain.contains("STATE TURN 143"));
        assertTrue(serializedAgain.contains("ENTITY Coniglio 12 7 ENERGY 18 AGE 11"));
        assertTrue(serializedAgain.contains("ENTITY Volpe 5 22 ENERGY 45 AGE 3"));
        assertTrue(serializedAgain.contains("ENTITY Erba 10 10"));
    }

    @Test
    void testMalformedConfigurationThrowsClearException() {
        ConfigParser parser = new ConfigParser();
        ConfigurationException ex = assertThrows(
                ConfigurationException.class,
                () -> parser.parse("GRID 40 INVALID_HEIGHT TOROIDAL")
        );
        assertTrue(ex.getMessage().contains("altezza GRID"));
    }
}
