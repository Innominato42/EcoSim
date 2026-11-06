package model;

import org.example.events.EpidemiaEvent;
import org.example.events.ProliferazioneEvent;
import org.example.events.SiccitaEvent;
import org.example.model.*;
import org.example.model.behavior.AnimalTurnChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GlobalEventTest {

    private Grid grid;
    private Plant grass1;
    private Plant grass2;

    @BeforeEach
    void setUp() {
        grid = new Grid(10, 10, BorderType.BOUNDED, new RgbColor(34, 139, 34));
        RgbColor green = new RgbColor(0, 180, 0);
        grass1 = new PlantEntity("Erba", 'G', green, 10, 0.20);
        grass2 = new PlantEntity("Erba", 'G', green, 10, 0.40);

        grid.placeEntity(new Position(1, 1), grass1);
        grid.placeEntity(new Position(5, 5), grass2);
    }

    @Test
    void testSiccitaReducesPlantReproductionRateAndRestoresAfterDuration() {
        SiccitaEvent siccita = new SiccitaEvent(20, 0.3, 3);

        siccita.updateTurn(20, grid);

        assertTrue(siccita.isActive());
        assertEquals(3, siccita.getRemainingTurns());
        assertEquals(0.06, grass1.getEffectiveReproductionRate(), 1e-6);
        assertEquals(0.12, grass2.getEffectiveReproductionRate(), 1e-6);

        siccita.updateTurn(21, grid);
        assertEquals(2, siccita.getRemainingTurns());
        assertEquals(0.06, grass1.getEffectiveReproductionRate(), 1e-6);

        siccita.updateTurn(22, grid);
        assertEquals(1, siccita.getRemainingTurns());

        siccita.updateTurn(23, grid);
        assertFalse(siccita.isActive());
        assertEquals(0, siccita.getRemainingTurns());
        assertEquals(0.20, grass1.getEffectiveReproductionRate(), 1e-6);
        assertEquals(0.40, grass2.getEffectiveReproductionRate(), 1e-6);
    }

    @Test
    void testProliferazioneAmplifiesPlantReproductionRate() {
        ProliferazioneEvent proliferazione = new ProliferazioneEvent(10, 2.5);

        proliferazione.apply(grid);

        assertTrue(proliferazione.isActive());
        assertEquals(5, proliferazione.getRemainingTurns());
        assertEquals(0.50, grass1.getEffectiveReproductionRate(), 1e-6);
        assertEquals(1.00, grass2.getEffectiveReproductionRate(), 1e-6);
    }

    @Test
    void testEpidemiaKillsAnimalsWithoutAffectingPlants() {
        AnimalTurnChain chain = AnimalTurnChain.createDefault();
        RgbColor white = new RgbColor(255, 255, 255);

        Animal r1 = new AnimalEntity("Coniglio", 'C', white, 40, 50, 40, 5, 35, Diet.herbivore(), 3, 2, chain);
        Animal r2 = new AnimalEntity("Coniglio", 'C', white, 40, 50, 40, 5, 35, Diet.herbivore(), 3, 2, chain);

        grid.placeEntity(new Position(2, 2), r1);
        grid.placeEntity(new Position(3, 3), r2);
        assertEquals(4, grid.getAllEntities().size()); // 2 piante + 2 conigli
        EpidemiaEvent epidemia = new EpidemiaEvent(15, 1.0, new Random(42));
        epidemia.apply(grid);

        assertFalse(r1.isAlive());
        assertFalse(r2.isAlive());
        assertTrue(grass1.isAlive());
        assertTrue(grass2.isAlive());
        assertEquals(2, grid.getAllEntities().size());
    }
}
