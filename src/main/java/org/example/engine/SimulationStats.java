package org.example.engine;

import java.util.*;

/**
 * Rappresenta una fotografia immutabile delle statistiche globali dell'ecosistema
 * al termine di un turno di simulazione.
 *
 * @param turn              numero del turno corrente
 * @param populationBySpecies mappa immutabile (Nome specie e numero di individui vivi per quella specie)
 * @param activeEvents      lista degli eventi globali attivi e turni rimanenti
 */
public record SimulationStats(
        int turn,
        Map<String, Integer> populationBySpecies,
        List<ActiveEventInfo> activeEvents) {

    /**
     * Dati di un evento globale attualmente attivo
     *
     * @param name           nome dell'evento
     * @param remainingTurns turni rimanenti prima della fine dell'effetto
     */
    public record ActiveEventInfo(String name, int remainingTurns) {}

    public SimulationStats {
        Objects.requireNonNull(populationBySpecies, "La mappa delle popolazioni non può essere null.");
        Objects.requireNonNull(activeEvents, "La lista degli eventi attivi non può essere null.");
        populationBySpecies = Collections.unmodifiableMap(new LinkedHashMap<>(populationBySpecies));
        activeEvents = List.copyOf(activeEvents);
    }
}