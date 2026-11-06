package model;

import org.example.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GridTest {

    private Grid toroidalGrid;
    private Grid boundedGrid;
    private Entity dummyRock;

    @BeforeEach
    void setUp() {
        RgbColor bg = new RgbColor(34, 139, 34);
        toroidalGrid = new Grid(10, 10, BorderType.TOROIDAL, bg);
        boundedGrid = new Grid(10, 10, BorderType.BOUNDED, bg);

        dummyRock = new ObstacleEntity("Roccia", 'R', new RgbColor(128, 128, 128));
    }

    @Test
    void testToroidalWrappingAtCorners() {
        Optional<Position> wrapped = toroidalGrid.resolvePosition(new Position(-1, -1));
        assertTrue(wrapped.isPresent());
        assertEquals(new Position(9, 9), wrapped.get());

        Optional<Position> wrappedOrigin = toroidalGrid.resolvePosition(new Position(10, 10));
        assertTrue(wrappedOrigin.isPresent());
        assertEquals(new Position(0, 0), wrappedOrigin.get());
    }

    @Test
    void testBoundedGridRejectsOutOfBounds() {
        assertTrue(boundedGrid.resolvePosition(new Position(-1, 0)).isEmpty());
        assertTrue(boundedGrid.resolvePosition(new Position(0, 10)).isEmpty());
        assertFalse(boundedGrid.placeEntity(new Position(10, 5), dummyRock));
    }

    @Test
    void testFreeAdjacentPositionsAtOriginCorner() {
        Position origin = new Position(0, 0);
        List<Position> boundedFree = boundedGrid.getFreeAdjacentPositions(origin);
        assertEquals(3, boundedFree.size());
        List<Position> toroidalFree = toroidalGrid.getFreeAdjacentPositions(origin);
        assertEquals(8, toroidalFree.size());
    }
}