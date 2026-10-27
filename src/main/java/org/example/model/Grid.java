package org.example.model;

import java.util.*;

/**
 * Rappresenta la griglia discreta 2D in cui si svolge la simulazione.
 */
public class Grid {

    private final int width;
    private final int height;
    private final BorderType borderType;
    private final RgbColor backgroundColor;
    private final Map<Position, Entity> cells;

    /**
     * Costruisce una nuova griglia di simulazione.
     *
     * @param width           larghezza in celle (> 0)
     * @param height          altezza in celle (> 0)
     * @param borderType      tipo di bordo (invalicabile o toroidale)
     * @param backgroundColor colore di sfondo del bioma
     */
    public Grid(int width, int height, BorderType borderType, RgbColor backgroundColor) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Le dimensioni della griglia devono essere positive.");
        }
        this.width = width;
        this.height = height;
        this.borderType = Objects.requireNonNull(borderType, "Il tipo di bordo non può essere null.");
        this.backgroundColor = Objects.requireNonNull(backgroundColor, "Il colore di sfondo non può essere null.");
        this.cells = new HashMap<>();
    }

    /**
     * Normalizza o valida una coordinata in base alla topologia dei bordi della griglia.
     *
     * @param position posizione candidata
     * @return Optional con la posizione effettiva sulla griglia, o empty se invalida
     */
    public Optional<Position> resolvePosition(Position position) {
        return borderType.resolve(position, width, height);
    }

    /**
     * Posiziona un'entità nella cella specificata se la coordinata è valida e libera.
     *
     * @param position coordinata di destinazione
     * @param entity   entità da posizionare
     * @return true se l'inserimento ha avuto successo, false se fuori dai bordi o cella occupata
     */
    public boolean placeEntity(Position position, Entity entity) {
        Objects.requireNonNull(entity, "L'entità non può essere null.");
        Optional<Position> resolved = resolvePosition(position);
        if (resolved.isEmpty() || cells.containsKey(resolved.get())) {
            return false;
        }
        cells.put(resolved.get(), entity);
        return true;
    }

    /**
     * Restituisce l'entità presente in una determinata posizione.
     *
     * @param position coordinata da ispezionare
     * @return Optional contenente l'entità se presente, altrimenti empty
     */
    public Optional<Entity> getEntityAt(Position position) {
        return resolvePosition(position).map(cells::get);
    }

    /**
     * Rimuove l'entità presente nella posizione indicata.
     *
     * @param position coordinata da liberare
     * @return Optional con l'entità rimossa, se era presente
     */
    public Optional<Entity> removeEntityAt(Position position) {
        return resolvePosition(position).map(cells::remove);
    }

    /**
     * Restituisce le posizioni adiacenti (nelle 4 direzioni cardinali o 8 direzioni) libere.
     * Utile per la propagazione delle piante e la nascita di nuovi animali.
     *
     * @param center posizione centrale
     * @return lista di posizioni adiacenti valide e attualmente prive di entità
     */
    public List<Position> getFreeAdjacentPositions(Position center) {
        List<Position> freePositions = new ArrayList<>();
        int[] deltas = {-1, 0, 1};
        for (int dx : deltas) {
            for (int dy : deltas) {
                if (dx == 0 && dy == 0) continue;
                resolvePosition(center.translate(dx, dy)).ifPresent(pos -> {
                    if (!cells.containsKey(pos) && !freePositions.contains(pos)) {
                        freePositions.add(pos);
                    }
                });
            }
        }
        return freePositions;
    }

    /**
     * Restituisce una vista immutabile di tutte le entità posizionate sulla griglia.
     *
     * @return mappa immutabile Posizione -> Entità
     */
    public Map<Position, Entity> getAllEntities() {
        return Collections.unmodifiableMap(cells);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public BorderType getBorderType() { return borderType; }
    public RgbColor getBackgroundColor() { return backgroundColor; }
}