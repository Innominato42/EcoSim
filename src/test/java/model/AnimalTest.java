package model;

import org.example.model.*;

import org.example.model.behavior.AnimalTurnChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AnimalTest {

    private Grid grid;
    private AnimalTurnChain defaultChain;
    private RgbColor white;
    private RgbColor orange;

    @BeforeEach
    void setUp() {
        grid = new Grid(10, 10, BorderType.BOUNDED, new RgbColor(34, 139, 34));
        defaultChain = AnimalTurnChain.createDefault();
        white = new RgbColor(255, 255, 255);
        orange = new RgbColor(200, 80, 0);
    }

    @Test
    void testAnimalDiesWhenEnergyReachesZero() {
        Animal rabbit = new AnimalEntity(
                "Coniglio", 'C', white, 1, 50, 40, 5, 35,
                Diet.herbivore(), 3, 2, defaultChain
        );
        Position start = new Position(5, 5);
        grid.placeEntity(start, rabbit);

        rabbit.act(start, grid, new Random(42));

        assertFalse(rabbit.isAlive());
        assertEquals(0, rabbit.getEnergy());
        assertEquals(Optional.of(rabbit), grid.getEntityAt(start));
    }

    @Test
    void testHerbivoreSeeksMovesAndEatsPlant() {
        Animal rabbit = new AnimalEntity(
                "Coniglio", 'C', white, 20, 50, 40, 5, 35,
                Diet.herbivore(), 3, 2, defaultChain
        );
        Plant grass = new PlantEntity("Erba", 'G', new RgbColor(0, 180, 0), 15, 0.05);

        Position rabbitPos = new Position(2, 2);
        Position grassPos = new Position(4, 2);
        grid.placeEntity(rabbitPos, rabbit);
        grid.placeEntity(grassPos, grass);

        rabbit.act(rabbitPos, grid, new Random(42));
        assertEquals(34, rabbit.getEnergy());
        assertFalse(grass.isAlive());
        assertEquals(rabbit, grid.getEntityAt(grassPos).orElse(null));
        assertTrue(grid.getEntityAt(rabbitPos).isEmpty());
    }

    @Test
    void testCarnivoreHuntsSpecificPreyAndIgnoresPlants() {
        Animal fox = new AnimalEntity(
                "Volpe", 'F', orange, 30, 80, 60, 8, 50,
                Diet.carnivore(Set.of("Coniglio")), 5, 3, defaultChain
        );
        Animal rabbit = new AnimalEntity(
                "Coniglio", 'C', white, 20, 50, 40, 5, 35,
                Diet.herbivore(), 3, 2, defaultChain
        );
        Plant grass = new PlantEntity("Erba", 'G', new RgbColor(0, 180, 0), 10, 0.05);

        grid.placeEntity(new Position(1, 1), fox);
        grid.placeEntity(new Position(1, 2), grass);  // Più vicina ma ignorata dalla volpe
        grid.placeEntity(new Position(3, 1), rabbit); // A distanza 2: preda valida

        fox.act(new Position(1, 1), grid, new Random(42));

        assertFalse(rabbit.isAlive());
        assertTrue(grass.isAlive());
        assertEquals(49, fox.getEnergy());
        assertEquals(fox, grid.getEntityAt(new Position(3, 1)).orElse(null));
    }

    @Test
    void testAnimalReproductionWhenConditionsAreMet() {
        AnimalEntity rabbit = new AnimalEntity(
                "Coniglio", 'C', white, 20, 50, 40, 5, 35,
                Diet.herbivore(), 3, 2, defaultChain
        );
        rabbit.restoreState(45, 10);

        Position start = new Position(5, 5);
        grid.placeEntity(start, rabbit);

        rabbit.act(start, grid, new Random(42));
        assertEquals(2, grid.getAllEntities().size());
    }
}