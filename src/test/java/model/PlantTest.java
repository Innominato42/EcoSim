package model;

import org.example.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class PlantTest {

    private Grid grid;
    private Plant grass;
    private RgbColor green;

    @BeforeEach
    void setUp() {
        green = new RgbColor(0, 180, 0);
        grid = new Grid(5, 5, BorderType.BOUNDED, new RgbColor(34, 139, 34));
        // Tasso di riproduzione 1.0 (100%) per testare la propagazione deterministica
        grass = new PlantEntity("Erba", 'G', green, 10, 1.0);
    }

    @Test
    void testPlantReproductionInFreeAdjacentCell() {
        Position center = new Position(2, 2);
        grid.placeEntity(center, grass);

        Optional<Position> childPos = grass.reproduce(center, grid, new Random(42));

        assertTrue(childPos.isPresent());
        assertEquals(2, grid.getAllEntities().size());
        assertTrue(grid.getEntityAt(childPos.get()).isPresent());
        assertEquals("Erba", grid.getEntityAt(childPos.get()).get().getSpeciesName());
    }

    @Test
    void testPlantCannotReproduceWhenSurroundedByObstacles() {
        Position corner = new Position(0, 0);
        grid.placeEntity(corner, grass);

        // Blocchiamo tutte e 3 le celle adiacenti all'angolo (0,0) su griglia BOUNDED
        Obstacle rock = new ObstacleEntity("Roccia", 'R', new RgbColor(128, 128, 128));
        grid.placeEntity(new Position(1, 0), rock);
        grid.placeEntity(new Position(0, 1), rock);
        grid.placeEntity(new Position(1, 1), rock);

        Optional<Position> childPos = grass.reproduce(corner, grid, new Random(42));

        assertTrue(childPos.isEmpty());
        assertEquals(4, grid.getAllEntities().size());
    }

    @Test
    void testReproductionRateFactorModification() {
        Plant normalGrass = new PlantEntity("Erba", 'G', green, 10, 0.20);
        assertEquals(0.20, normalGrass.getEffectiveReproductionRate(), 1e-6);

        // Simuliamo l'effetto di Siccità (fattore 0.3)
        normalGrass.setReproductionRateFactor(0.3);
        assertEquals(0.06, normalGrass.getEffectiveReproductionRate(), 1e-6);

        // Simuliamo l'effetto di Proliferazione (fattore 2.0)
        normalGrass.setReproductionRateFactor(2.0);
        assertEquals(0.40, normalGrass.getEffectiveReproductionRate(), 1e-6);
    }
}