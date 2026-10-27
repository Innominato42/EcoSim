package org.example.engine;

import org.example.events.GlobalEvent;
import org.example.model.*;

import java.util.*;
import java.util.random.RandomGenerator;

/**
 * Coordina l'esecuzione a turni della simulazione, l'applicazione degli eventi globali,
 * il calcolo delle statistiche e la notifica agli osservatori.
 * Dipende esclusivamente dalle astrazioni (Entity, Plant, Animal, GlobalEvent, SimulationObserver).
 */
public class SimulationEngine {

    private final Grid grid;
    private final List<GlobalEvent> globalEvents;
    private final List<SimulationObserver> observers;
    private final RandomGenerator random;
    private int currentTurn;
    private SimulationStats latestStats;

    /**
     * Costruisce un nuovo motore di simulazione.
     *
     * @param grid         griglia dell'ecosistema
     * @param globalEvents lista degli eventi globali configurati
     * @param initialTurn  turno di partenza (0 per nuova simulazione, > 0 se caricato da file di stato)
     * @param random       generatore di numeri casuali
     */
    public SimulationEngine(Grid grid, List<GlobalEvent> globalEvents, int initialTurn, RandomGenerator random) {
        if (initialTurn < 0) {
            throw new IllegalArgumentException("Il turno iniziale non può essere negativo.");
        }
        this.grid = Objects.requireNonNull(grid, "La griglia non può essere null.");
        this.globalEvents = new ArrayList<>(Objects.requireNonNull(globalEvents, "La lista eventi non può essere null."));
        this.observers = new ArrayList<>();
        this.random = Objects.requireNonNull(random, "Il generatore random non può essere null.");
        this.currentTurn = initialTurn;
        this.latestStats = computeStats();
    }

    /**
     * Costruttore semplificato che parte dal turno 0 con generatore casuale predefinito.
     *
     * @param grid         griglia dell'ecosistema
     * @param globalEvents lista degli eventi globali configurati
     */
    public SimulationEngine(Grid grid, List<GlobalEvent> globalEvents) {
        this(grid, globalEvents, 0, new Random());
    }

    /**
     * Registra un osservatore che verrà notificato ad ogni fine turno.
     *
     * @param observer osservatore da aggiungere
     */
    public void addObserver(SimulationObserver observer) {
        Objects.requireNonNull(observer, "L'osservatore non può essere null.");
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Rimuove un osservatore precedentemente registrato.
     *
     * @param observer osservatore da rimuovere
     */
    public void removeObserver(SimulationObserver observer) {
        observers.remove(observer);
    }

    /**
     * Esegue un singolo turno completo della simulazione seguendo i passi della specifica:
     * 1. Riproduzione di tutte le piante
     * 2. Ciclo comportamentale di tutti gli animali in ordine casuale
     * 3. Rimozione delle entità morte dalla griglia
     * 4. Applicazione/aggiornamento degli eventi globali
     * 5. Aggiornamento delle statistiche globali
     * 6. Notifica agli osservatori
     *
     * @return le statistiche aggiornate al termine del turno
     */
    public SimulationStats step() {
        currentTurn++;

        // Passo 7: Tutte le piante presenti a inizio turno eseguono la propria logica di riproduzione
        executePlantsPhase();

        // Passo 8: Tutti gli animali (in ordine casuale) eseguono il ciclo comportamentale
        executeAnimalsPhase();

        // Passo 9: Le entità morte vengono rimosse dalla griglia
        removeDeadEntities();

        // Passo 10: Aggiornamento e attivazione degli eventi globali se il turno è multiplo della frequenza
        executeGlobalEventsPhase();

        // Passo 11: Aggiornamento delle statistiche globali della simulazione
        this.latestStats = computeStats();

        // Passo 12: Notifica dello stato aggiornato agli osservatori
        notifyObservers();

        return this.latestStats;
    }

    /**
     * Esegue la simulazione per un numero specificato di turni.
     *
     * @param turns numero di turni da eseguire (> 0)
     */
    public void run(int turns) {
        if (turns < 0) {
            throw new IllegalArgumentException("Il numero di turni non può essere negativo.");
        }
        for (int i = 0; i < turns; i++) {
            step();
        }
    }

    private void executePlantsPhase() {
        List<Map.Entry<Position, Plant>> plantsSnapshot = new ArrayList<>();
        for (Map.Entry<Position, Entity> entry : grid.getAllEntities().entrySet()) {
            if (entry.getValue() instanceof Plant plant && plant.isAlive()) {
                plantsSnapshot.add(Map.entry(entry.getKey(), plant));
            }
        }
        for (Map.Entry<Position, Plant> entry : plantsSnapshot) {
            Plant plant = entry.getValue();
            if (plant.isAlive()) {
                plant.reproduce(entry.getKey(), grid, random);
            }
        }
    }

    private void executeAnimalsPhase() {
        List<Map.Entry<Position, Animal>> animalsSnapshot = new ArrayList<>();
        for (Map.Entry<Position, Entity> entry : grid.getAllEntities().entrySet()) {
            if (entry.getValue() instanceof Animal animal && animal.isAlive()) {
                animalsSnapshot.add(Map.entry(entry.getKey(), animal));
            }
        }
        // Mescola la lista per garantire l'esecuzione in ordine casuale
        Collections.shuffle(animalsSnapshot, Random.from(random));

        for (Map.Entry<Position, Animal> entry : animalsSnapshot) {
            Animal animal = entry.getValue();
            // Verifica che l'animale non sia stato mangiato da un predatore prima del suo turno d'azione
            if (animal.isAlive()) {
                animal.act(entry.getKey(), grid, random);
            }
        }
    }

    private void removeDeadEntities() {
        List<Position> deadPositions = new ArrayList<>();
        for (Map.Entry<Position, Entity> entry : grid.getAllEntities().entrySet()) {
            if (!entry.getValue().isAlive()) {
                deadPositions.add(entry.getKey());
            }
        }
        for (Position pos : deadPositions) {
            grid.removeEntityAt(pos);
        }
    }

    private void executeGlobalEventsPhase() {
        for (GlobalEvent event : globalEvents) {
            event.updateTurn(currentTurn, grid);
        }
        // Rimuove eventuali entità decedute a causa di eventi istantanei
        removeDeadEntities();
    }

    private SimulationStats computeStats() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Entity entity : grid.getAllEntities().values()) {
            if (entity.isAlive()) {
                counts.merge(entity.getSpeciesName(), 1, Integer::sum);
            }
        }

        List<SimulationStats.ActiveEventInfo> activeEvents = new ArrayList<>();
        for (GlobalEvent event : globalEvents) {
            if (event.isActive()) {
                activeEvents.add(new SimulationStats.ActiveEventInfo(
                        event.getName(),
                        event.getRemainingTurns()
                ));
            }
        }

        return new SimulationStats(currentTurn, counts, activeEvents);
    }

    private void notifyObservers() {
        for (SimulationObserver observer : observers) {
            observer.onTurnCompleted(grid, latestStats);
        }
    }

    public Grid getGrid() { return grid; }
    public int getCurrentTurn() { return currentTurn; }
    public SimulationStats getLatestStats() { return latestStats; }
    public List<GlobalEvent> getGlobalEvents() { return Collections.unmodifiableList(globalEvents); }
}
