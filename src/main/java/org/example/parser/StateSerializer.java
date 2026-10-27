package org.example.parser;

import org.example.engine.SimulationEngine;
import org.example.model.*;
import org.example.model.behavior.AnimalTurnChain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Gestisce la serializzazione (scrittura) e deserializzazione (lettura) del file di stato
 * della simulazione secondo il formato definito nella sezione 2.5.
 */
public class StateSerializer {

    /**
     * Converte lo stato corrente della griglia e del turno nel formato testuale di stato.
     *
     * @param grid griglia di simulazione
     * @param turn turno corrente
     * @return stringa formattata pronta per il salvataggio
     */
    public String serialize(Grid grid, int turn) {
        Objects.requireNonNull(grid, "La griglia non può essere null.");
        StringBuilder sb = new StringBuilder();
        sb.append("STATE TURN ").append(turn).append(System.lineSeparator());

        // Ordina le posizioni per produrre un file di stato pulito e deterministico
        List<Map.Entry<Position, Entity>> entries = new ArrayList<>(grid.getAllEntities().entrySet());
        entries.sort(Comparator.comparingInt((Map.Entry<Position, Entity> e) -> e.getKey().x())
                .thenComparingInt(e -> e.getKey().y()));

        for (Map.Entry<Position, Entity> entry : entries) {
            Position pos = entry.getKey();
            Entity entity = entry.getValue();
            if (!entity.isAlive()) {
                continue;
            }
            sb.append("ENTITY ")
                    .append(entity.getSpeciesName()).append(" ")
                    .append(pos.x()).append(" ")
                    .append(pos.y());

            if (entity instanceof Animal animal) {
                sb.append(" ENERGY ").append(animal.getEnergy())
                        .append(" AGE ").append(animal.getAge());
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * Salva lo stato corrente della simulazione su un file testuale.
     *
     * @param grid     griglia di simulazione
     * @param turn     turno corrente
     * @param filePath percorso del file di destinazione
     */
    public void saveToFile(Grid grid, int turn, Path filePath) {
        try {
            String content = serialize(grid, turn);
            Files.writeString(filePath, content);
        } catch (IOException e) {
            throw new ConfigurationException("Errore durante il salvataggio del file di stato: " + filePath, e);
        }
    }

    /**
     * Ricostruisce un motore di simulazione a partire da un file di stato e da una configurazione base.
     *
     * @param filePath   percorso del file di stato
     * @param baseConfig configurazione contenente dimensioni griglia, eventi e definizioni delle specie
     * @return istanza di SimulationEngine ripristinata al turno e stato salvati
     */
    public SimulationEngine loadFromFile(Path filePath, SimulationConfig baseConfig) {
        try {
            String content = Files.readString(filePath);
            return deserialize(content, baseConfig);
        } catch (IOException e) {
            throw new ConfigurationException("Impossibile leggere il file di stato: " + filePath, e);
        }
    }

    /**
     * Ricostruisce un motore di simulazione dal testo di un file di stato.
     *
     * @param content    contenuto testuale del file di stato
     * @param baseConfig configurazione di riferimento (se null, usa le specie standard di default)
     * @return istanza di SimulationEngine pronta per riprendere la simulazione
     */
    public SimulationEngine deserialize(String content, SimulationConfig baseConfig) {
        if (content == null || content.isBlank()) {
            throw new ConfigurationException("Il file di stato è vuoto.");
        }

        SimulationConfig effectiveConfig = (baseConfig != null) ? baseConfig : createDefaultFallbackConfig();
        Grid templateGrid = effectiveConfig.getGrid();
        Grid restoredGrid = new Grid(
                templateGrid.getWidth(),
                templateGrid.getHeight(),
                templateGrid.getBorderType(),
                templateGrid.getBackgroundColor()
        );

        int restoredTurn = -1;

        for (String rawLine : content.lines().toList()) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] tokens = line.split("\\s+");
            if (tokens[0].equalsIgnoreCase("STATE")) {
                if (tokens.length < 3 || !tokens[1].equalsIgnoreCase("TURN")) {
                    throw new ConfigurationException("Intestazione STATE TURN non valida: " + line);
                }
                try {
                    restoredTurn = Integer.parseInt(tokens[2]);
                } catch (NumberFormatException e) {
                    throw new ConfigurationException("Numero di turno non valido in: " + line, e);
                }
            } else if (tokens[0].equalsIgnoreCase("ENTITY")) {
                parseEntityLine(tokens, line, restoredGrid, effectiveConfig);
            } else {
                throw new ConfigurationException("Riga non riconosciuta nel file di stato: " + line);
            }
        }

        if (restoredTurn < 0) {
            throw new ConfigurationException("Intestazione 'STATE TURN <n>' mancante nel file di stato.");
        }

        return new SimulationEngine(restoredGrid, effectiveConfig.getEvents(), restoredTurn, new Random());
    }

    private void parseEntityLine(String[] tokens, String rawLine, Grid grid, SimulationConfig config) {
        if (tokens.length < 4) {
            throw new ConfigurationException("Riga ENTITY incompleta: " + rawLine);
        }
        String species = tokens[1];
        int x;
        int y;
        try {
            x = Integer.parseInt(tokens[2]);
            y = Integer.parseInt(tokens[3]);
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Coordinate non valide nella riga: " + rawLine, e);
        }

        Entity entity = createOrFallbackEntity(species, config);
        if (entity instanceof AnimalEntity animal && tokens.length > 4) {
            int energy = animal.getEnergy();
            int age = animal.getAge();
            int idx = 4;
            while (idx < tokens.length) {
                String key = tokens[idx++].toUpperCase();
                if (idx >= tokens.length) {
                    throw new ConfigurationException("Valore mancante per " + key + " nella riga: " + rawLine);
                }
                try {
                    int val = Integer.parseInt(tokens[idx++]);
                    if (key.equals("ENERGY")) {
                        energy = val;
                    } else if (key.equals("AGE")) {
                        age = val;
                    } else {
                        throw new ConfigurationException("Attributo sconosciuto '" + key + "' in: " + rawLine);
                    }
                } catch (NumberFormatException e) {
                    throw new ConfigurationException("Valore numerico non valido in: " + rawLine, e);
                }
            }
            animal.restoreState(energy, age);
        }

        grid.placeEntity(new Position(x, y), entity);
    }

    private Entity createOrFallbackEntity(String species, SimulationConfig config) {
        if (config.getSpeciesFactories().containsKey(species)) {
            return config.createEntity(species);
        }
        AnimalTurnChain chain = AnimalTurnChain.createDefault();
        return switch (species.toLowerCase()) {
            case "erba" -> new PlantEntity("Erba", 'G', new RgbColor(0, 180, 0), 10, 0.05);
            case "coniglio" -> new AnimalEntity("Coniglio", 'C', new RgbColor(255, 255, 255), 20, 50, 40, 5, 35, Diet.herbivore(), 3, 2, chain);
            case "volpe" -> new AnimalEntity("Volpe", 'F', new RgbColor(200, 80, 0), 30, 80, 60, 8, 50, Diet.carnivore(Set.of("Coniglio")), 5, 3, chain);
            case "roccia" -> new ObstacleEntity("Roccia", 'R', new RgbColor(128, 128, 128));
            default -> throw new ConfigurationException("Specie '" + species + "' non definita nella configurazione.");
        };
    }

    private SimulationConfig createDefaultFallbackConfig() {
        String defaultSpec = """
                GRID 40 30 TOROIDAL
                BACKGROUND 34 139 34
                PLANT Erba SYMBOL G COLOR 0 180 0 ENERGY 10 RATE 0.05
                ANIMAL Coniglio SYMBOL C COLOR 255 255 255 ENERGY 20 MAX_ENERGY 50 DIET PLANT AGE_MAX 40 REPR_MIN 5 REPR_MAX 35 SIGHT 3 SPEED 2
                ANIMAL Volpe SYMBOL F COLOR 200 80 0 ENERGY 30 MAX_ENERGY 80 DIET ANIMAL:Coniglio AGE_MAX 60 REPR_MIN 8 REPR_MAX 50 SIGHT 5 SPEED 3
                OBSTACLE Roccia SYMBOL R COLOR 128 128 128
                """;
        return new ConfigParser().parse(defaultSpec);
    }
}
