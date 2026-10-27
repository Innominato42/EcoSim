package org.example.parser;

import org.example.events.EpidemiaEvent;
import org.example.events.GlobalEvent;
import org.example.events.ProliferazioneEvent;
import org.example.events.SiccitaEvent;
import org.example.model.*;
import org.example.model.behavior.AnimalTurnChain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

/**
 * Parser per il file di configurazione testuale della simulazione (sezione 2.4).
 * Costruisce la griglia, registra le specie, configura gli eventi globali ed esegue lo spawn iniziale.
 */
public class ConfigParser {

    private final RandomGenerator random;
    private final AnimalTurnChain defaultChain;

    public ConfigParser(RandomGenerator random) {
        this.random = Objects.requireNonNull(random, "Il generatore random non può essere null.");
        this.defaultChain = AnimalTurnChain.createDefault();
    }

    public ConfigParser() {
        this(new Random());
    }

    /**
     * Legge e interpreta un file di configurazione da disco.
     *
     * @param filePath percorso del file di configurazione
     * @return oggetto SimulationConfig pronto per inizializzare il motore
     */
    public SimulationConfig parseFile(Path filePath) {
        try {
            String content = Files.readString(filePath);
            return parse(content);
        } catch (IOException e) {
            throw new ConfigurationException("Impossibile leggere il file di configurazione: " + filePath, e);
        }
    }

    /**
     * Interpreta il contenuto testuale di una configurazione.
     *
     * @param content testo della configurazione
     * @return oggetto SimulationConfig
     */
    public SimulationConfig parse(String content) {
        if (content == null || content.isBlank()) {
            throw new ConfigurationException("Il contenuto della configurazione è vuoto.");
        }

        String[] tokens = tokenize(content);
        int width = 40;
        int height = 30;
        BorderType borderType = BorderType.TOROIDAL;
        RgbColor background = new RgbColor(34, 139, 34);

        Map<String, Supplier<Entity>> factories = new LinkedHashMap<>();
        Map<String, Entity> prototypes = new LinkedHashMap<>();
        List<GlobalEvent> events = new ArrayList<>();
        Map<String, Integer> spawns = new LinkedHashMap<>();

        int i = 0;
        while (i < tokens.length) {
            String cmd = tokens[i++];
            switch (cmd.toUpperCase()) {
                case "GRID" -> {
                    width = parseInt(tokens, i++, "larghezza GRID");
                    height = parseInt(tokens, i++, "altezza GRID");
                    borderType = parseBorderType(nextToken(tokens, i++, "tipo bordo GRID"));
                }
                case "BACKGROUND" -> {
                    int r = parseInt(tokens, i++, "R di BACKGROUND");
                    int g = parseInt(tokens, i++, "G di BACKGROUND");
                    int b = parseInt(tokens, i++, "B di BACKGROUND");
                    background = createColor(r, g, b);
                }
                case "PLANT" -> i = parsePlant(tokens, i, factories, prototypes);
                case "ANIMAL" -> i = parseAnimal(tokens, i, factories, prototypes);
                case "OBSTACLE" -> i = parseObstacle(tokens, i, factories, prototypes);
                case "EVENT" -> i = parseEvent(tokens, i, events);
                case "SPAWN" -> {
                    String species = nextToken(tokens, i++, "specie SPAWN");
                    int count = parseInt(tokens, i++, "quantità SPAWN");
                    if (count < 0) {
                        throw new ConfigurationException("La quantità di SPAWN non può essere negativa per " + species);
                    }
                    spawns.merge(species, count, Integer::sum);
                }
                default -> throw new ConfigurationException("Token o comando non riconosciuto: '" + cmd + "'");
            }
        }

        Grid grid = new Grid(width, height, borderType, background);
        populateInitialSpawns(grid, factories, spawns);

        return new SimulationConfig(grid, events, factories, prototypes);
    }

    private int parsePlant(
            String[] tokens,
            int index,
            Map<String, Supplier<Entity>> factories,
            Map<String, Entity> prototypes) {
        String name = nextToken(tokens, index++, "nome specie PLANT");
        char symbol = name.charAt(0);
        RgbColor color = new RgbColor(0, 180, 0);
        int energy = 10;
        double rate = 0.05;

        while (index < tokens.length && !isTopLevelCommand(tokens[index])) {
            String param = tokens[index++].toUpperCase();
            switch (param) {
                case "SYMBOL" -> symbol = nextToken(tokens, index++, "SYMBOL di " + name).charAt(0);
                case "COLOR" -> {
                    int r = parseInt(tokens, index++, "R");
                    int g = parseInt(tokens, index++, "G");
                    int b = parseInt(tokens, index++, "B");
                    color = createColor(r, g, b);
                }
                case "ENERGY" -> energy = parseInt(tokens, index++, "ENERGY di " + name);
                case "RATE" -> rate = parseDouble(tokens, index++, "RATE di " + name);
                default -> throw new ConfigurationException("Parametro PLANT non valido: " + param);
            }
        }

        final char finalSymbol = symbol;
        final RgbColor finalColor = color;
        final int finalEnergy = energy;
        final double finalRate = rate;

        Supplier<Entity> supplier = () -> new PlantEntity(name, finalSymbol, finalColor, finalEnergy, finalRate);
        prototypes.put(name, supplier.get());
        factories.put(name, supplier);
        return index;
    }

    private int parseAnimal(
            String[] tokens,
            int index,
            Map<String, Supplier<Entity>> factories,
            Map<String, Entity> prototypes) {
        String name = nextToken(tokens, index++, "nome specie ANIMAL");
        char symbol = name.charAt(0);
        RgbColor color = new RgbColor(255, 255, 255);
        int energy = 20;
        int maxEnergy = 50;
        Diet diet = Diet.herbivore();
        int ageMax = 40;
        int reprMin = 5;
        int reprMax = 35;
        int sight = 3;
        int speed = 1;

        while (index < tokens.length && !isTopLevelCommand(tokens[index])) {
            String param = tokens[index++].toUpperCase();
            switch (param) {
                case "SYMBOL" -> symbol = nextToken(tokens, index++, "SYMBOL di " + name).charAt(0);
                case "COLOR" -> {
                    int r = parseInt(tokens, index++, "R");
                    int g = parseInt(tokens, index++, "G");
                    int b = parseInt(tokens, index++, "B");
                    color = createColor(r, g, b);
                }
                case "ENERGY" -> energy = parseInt(tokens, index++, "ENERGY di " + name);
                case "MAX_ENERGY" -> maxEnergy = parseInt(tokens, index++, "MAX_ENERGY di " + name);
                case "MAX" -> {
                    expectToken(tokens, index++, "ENERGY");
                    maxEnergy = parseInt(tokens, index++, "MAX ENERGY di " + name);
                }
                case "DIET" -> {
                    String dietToken = nextToken(tokens, index++, "DIET di " + name);
                    if (dietToken.equalsIgnoreCase("ANIMAL:") && index < tokens.length) {
                        dietToken = "ANIMAL:" + tokens[index++];
                    }
                    diet = parseDiet(dietToken);
                }
                case "AGE_MAX" -> ageMax = parseInt(tokens, index++, "AGE_MAX di " + name);
                case "AGE" -> {
                    expectToken(tokens, index++, "MAX");
                    ageMax = parseInt(tokens, index++, "AGE MAX di " + name);
                }
                case "REPR_MIN" -> reprMin = parseInt(tokens, index++, "REPR_MIN di " + name);
                case "REPR_MAX" -> reprMax = parseInt(tokens, index++, "REPR_MAX di " + name);
                case "REPR" -> {
                    String sub = nextToken(tokens, index++, "MIN/MAX dopo REPR").toUpperCase();
                    if (sub.equals("MIN")) {
                        reprMin = parseInt(tokens, index++, "REPR MIN di " + name);
                    } else if (sub.equals("MAX")) {
                        reprMax = parseInt(tokens, index++, "REPR MAX di " + name);
                    } else {
                        throw new ConfigurationException("Atteso MIN o MAX dopo REPR, trovato: " + sub);
                    }
                }
                case "SIGHT" -> sight = parseInt(tokens, index++, "SIGHT di " + name);
                case "SPEED" -> speed = parseInt(tokens, index++, "SPEED di " + name);
                default -> throw new ConfigurationException("Parametro ANIMAL non valido: " + param);
            }
        }

        final char fSymbol = symbol;
        final RgbColor fColor = color;
        final int fEnergy = energy;
        final int fMaxEnergy = maxEnergy;
        final int fAgeMax = ageMax;
        final int fReprMin = reprMin;
        final int fReprMax = reprMax;
        final Diet fDiet = diet;
        final int fSight = sight;
        final int fSpeed = speed;

        Supplier<Entity> supplier = () -> new AnimalEntity(
                name, fSymbol, fColor, fEnergy, fMaxEnergy, fAgeMax, fReprMin, fReprMax, fDiet, fSight, fSpeed, defaultChain
        );
        prototypes.put(name, supplier.get());
        factories.put(name, supplier);
        return index;
    }

    private int parseObstacle(
            String[] tokens,
            int index,
            Map<String, Supplier<Entity>> factories,
            Map<String, Entity> prototypes) {
        String name = nextToken(tokens, index++, "nome OBSTACLE");
        char symbol = name.charAt(0);
        RgbColor color = new RgbColor(128, 128, 128);

        while (index < tokens.length && !isTopLevelCommand(tokens[index])) {
            String param = tokens[index++].toUpperCase();
            switch (param) {
                case "SYMBOL" -> symbol = nextToken(tokens, index++, "SYMBOL di " + name).charAt(0);
                case "COLOR" -> {
                    int r = parseInt(tokens, index++, "R");
                    int g = parseInt(tokens, index++, "G");
                    int b = parseInt(tokens, index++, "B");
                    color = createColor(r, g, b);
                }
                default -> throw new ConfigurationException("Parametro OBSTACLE non valido: " + param);
            }
        }

        final char fSymbol = symbol;
        final RgbColor fColor = color;
        Supplier<Entity> supplier = () -> new ObstacleEntity(name, fSymbol, fColor);
        prototypes.put(name, supplier.get());
        factories.put(name, supplier);
        return index;
    }

    private int parseEvent(String[] tokens, int index, List<GlobalEvent> events) {
        String eventType = nextToken(tokens, index++, "tipo EVENT");
        int every = 20;
        double rateFactor = 1.0;
        int duration = 5;
        double deathProb = 0.5;

        while (index < tokens.length && !isTopLevelCommand(tokens[index])) {
            String param = tokens[index++].toUpperCase();
            switch (param) {
                case "EVERY" -> every = parseInt(tokens, index++, "EVERY di " + eventType);
                case "RATE_FACTOR" -> rateFactor = parseDouble(tokens, index++, "RATE_FACTOR di " + eventType);
                case "RATE" -> {
                    expectToken(tokens, index++, "FACTOR");
                    rateFactor = parseDouble(tokens, index++, "RATE FACTOR di " + eventType);
                }
                case "DURATION" -> duration = parseInt(tokens, index++, "DURATION di " + eventType);
                case "DEATH_PROB" -> deathProb = parseDouble(tokens, index++, "DEATH_PROB di " + eventType);
                case "DEATH" -> {
                    expectToken(tokens, index++, "PROB");
                    deathProb = parseDouble(tokens, index++, "DEATH PROB di " + eventType);
                }
                default -> throw new ConfigurationException("Parametro EVENT non valido: " + param);
            }
        }

        try {
            GlobalEvent event = switch (eventType.toLowerCase()) {
                case "siccita", "siccità" -> new SiccitaEvent(every, rateFactor, duration);
                case "proliferazione" -> new ProliferazioneEvent(every, rateFactor, duration);
                case "epidemia" -> new EpidemiaEvent(every, deathProb, random);
                default -> throw new ConfigurationException("Tipo di evento globale sconosciuto: " + eventType);
            };
            events.add(event);
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException("Parametri non validi per l'evento " + eventType + ": " + e.getMessage(), e);
        }
        return index;
    }

    private void populateInitialSpawns(
            Grid grid,
            Map<String, Supplier<Entity>> factories,
            Map<String, Integer> spawns) {
        List<Position> allPositions = new ArrayList<>(grid.getWidth() * grid.getHeight());
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                allPositions.add(new Position(x, y));
            }
        }
        Collections.shuffle(allPositions, Random.from(random));

        int posIndex = 0;
        for (Map.Entry<String, Integer> entry : spawns.entrySet()) {
            String species = entry.getKey();
            int quantity = entry.getValue();
            Supplier<Entity> factory = factories.get(species);
            if (factory == null) {
                throw new ConfigurationException("SPAWN richiesto per una specie non definita: " + species);
            }
            for (int c = 0; c < quantity && posIndex < allPositions.size(); c++) {
                grid.placeEntity(allPositions.get(posIndex++), factory.get());
            }
        }
    }

    private Diet parseDiet(String token) {
        boolean eatsPlants = false;
        Set<String> preys = new HashSet<>();

        String[] parts = token.split("\\+");
        for (String part : parts) {
            if (part.equalsIgnoreCase("PLANT")) {
                eatsPlants = true;
            } else if (part.toUpperCase().startsWith("ANIMAL:")) {
                String speciesList = part.substring("ANIMAL:".length());
                for (String s : speciesList.split(",")) {
                    if (!s.isBlank()) {
                        preys.add(s.trim());
                    }
                }
            } else if (part.equalsIgnoreCase("ANIMAL")) {
                preys.add("*");
            } else {
                throw new ConfigurationException("Formato DIET non valido: " + token);
            }
        }
        return new Diet(eatsPlants, preys);
    }

    private BorderType parseBorderType(String token) {
        return switch (token.toUpperCase()) {
            case "TOROIDAL" -> BorderType.TOROIDAL;
            case "BOUNDED", "INVALICABILE", "CLOSED", "FIXED" -> BorderType.BOUNDED;
            default -> throw new ConfigurationException("Tipo di bordo non riconosciuto: " + token);
        };
    }

    private RgbColor createColor(int r, int g, int b) {
        try {
            return new RgbColor(r, g, b);
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException("Valori colore RGB fuori range [0, 255]: " + r + " " + g + " " + b, e);
        }
    }

    private String[] tokenize(String content) {
        StringBuilder cleaned = new StringBuilder();
        for (String line : content.lines().toList()) {
            int commentIdx = line.indexOf('#');
            String activeLine = (commentIdx >= 0) ? line.substring(0, commentIdx) : line;
            activeLine = activeLine.replace("\\", " ");
            cleaned.append(activeLine).append(" ");
        }
        String trimmed = cleaned.toString().trim();
        if (trimmed.isEmpty()) {
            throw new ConfigurationException("Il file di configurazione non contiene istruzioni valide.");
        }
        return trimmed.split("\\s+");
    }

    private boolean isTopLevelCommand(String token) {
        return switch (token.toUpperCase()) {
            case "GRID", "BACKGROUND", "PLANT", "ANIMAL", "OBSTACLE", "EVENT", "SPAWN" -> true;
            default -> false;
        };
    }

    private String nextToken(String[] tokens, int index, String description) {
        if (index >= tokens.length) {
            throw new ConfigurationException("Fine inattesa del file: mancante " + description);
        }
        return tokens[index];
    }

    private void expectToken(String[] tokens, int index, String expected) {
        String actual = nextToken(tokens, index, expected);
        if (!actual.equalsIgnoreCase(expected)) {
            throw new ConfigurationException("Atteso '" + expected + "', trovato '" + actual + "'");
        }
    }

    private int parseInt(String[] tokens, int index, String description) {
        String val = nextToken(tokens, index, description);
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Valore intero non valido per " + description + ": '" + val + "'", e);
        }
    }

    private double parseDouble(String[] tokens, int index, String description) {
        String val = nextToken(tokens, index, description);
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Valore decimale non valido per " + description + ": '" + val + "'", e);
        }
    }
}