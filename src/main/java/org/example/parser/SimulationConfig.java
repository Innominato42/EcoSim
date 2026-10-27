package org.example.parser;

import org.example.events.GlobalEvent;
import org.example.model.Entity;
import org.example.model.Grid;

import java.util.*;
import java.util.function.Supplier;

/**
 * Incapsula il risultato del parsing di un file di configurazione:
 * la griglia inizializzata, la lista degli eventi globali e il registro delle specie.
 */
public class SimulationConfig {

    private final Grid grid;
    private final List<GlobalEvent> events;
    private final Map<String, Supplier<Entity>> speciesFactories;
    private final Map<String, Entity> speciesPrototypes;

    public SimulationConfig(
            Grid grid,
            List<GlobalEvent> events,
            Map<String, Supplier<Entity>> speciesFactories,
            Map<String, Entity> speciesPrototypes) {
        this.grid = Objects.requireNonNull(grid, "La griglia non può essere null.");
        this.events = List.copyOf(Objects.requireNonNull(events, "La lista eventi non può essere null."));
        this.speciesFactories = Collections.unmodifiableMap(new LinkedHashMap<>(speciesFactories));
        this.speciesPrototypes = Collections.unmodifiableMap(new LinkedHashMap<>(speciesPrototypes));
    }

    /**
     * Crea una nuova istanza di un'entità a partire dal nome della specie registrata.
     *
     * @param speciesName nome della specie (es. "Coniglio", "Erba", "Roccia")
     * @return nuova istanza dell'entità
     * @throws ConfigurationException se la specie non è registrata
     */
    public Entity createEntity(String speciesName) {
        Supplier<Entity> factory = speciesFactories.get(speciesName);
        if (factory == null) {
            throw new ConfigurationException("Specie non riconosciuta o non configurata: " + speciesName);
        }
        return factory.get();
    }

    public Grid getGrid() { return grid; }
    public List<GlobalEvent> getEvents() { return events; }
    public Map<String, Entity> getSpeciesPrototypes() { return speciesPrototypes; }
    public Map<String, Supplier<Entity>> getSpeciesFactories() { return speciesFactories; }
}
