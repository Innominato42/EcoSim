package org.example.model;




import org.example.model.behavior.AnimalTurnChain;
import org.example.model.behavior.TurnContext;

import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Implementazione concreta di un animale il cui ciclo di vita è delegato
 * a una catena di responsabilità e a strategie comportamentali componibili.
 */
public class AnimalEntity extends AbstractEntity implements Animal {

    private final int initialEnergy;
    private int energy;
    private final int maxEnergy;
    private int age;
    private final int maxAge;
    private final int minReproductiveAge;
    private final int maxReproductiveAge;
    private final Diet diet;
    private final int sightRange;
    private final int speed;
    private final AnimalTurnChain turnChain;

    /**
     * Costruisce un nuovo animale con tutte le caratteristiche della specie.
     */
    public AnimalEntity(
            String speciesName,
            char symbol,
            RgbColor color,
            int initialEnergy,
            int maxEnergy,
            int maxAge,
            int minReproductiveAge,
            int maxReproductiveAge,
            Diet diet,
            int sightRange,
            int speed,
            AnimalTurnChain turnChain) {
        super(speciesName, symbol, color);
        if (initialEnergy <= 0 || maxEnergy < initialEnergy) {
            throw new IllegalArgumentException("Valori di energia non validi.");
        }
        if (maxAge <= 0 || minReproductiveAge < 0 || maxReproductiveAge < minReproductiveAge) {
            throw new IllegalArgumentException("Parametri di età non validi.");
        }
        if (sightRange < 0 || speed <= 0) {
            throw new IllegalArgumentException("Raggio visivo e velocità devono essere validi.");
        }
        this.initialEnergy = initialEnergy;
        this.energy = initialEnergy;
        this.maxEnergy = maxEnergy;
        this.age = 0;
        this.maxAge = maxAge;
        this.minReproductiveAge = minReproductiveAge;
        this.maxReproductiveAge = maxReproductiveAge;
        this.diet = Objects.requireNonNull(diet, "La dieta non può essere null.");
        this.sightRange = sightRange;
        this.speed = speed;
        this.turnChain = Objects.requireNonNull(turnChain, "La catena comportamentale non può essere null.");
    }

    @Override
    public void act(Position currentPos, Grid grid, RandomGenerator random) {
        if (!isAlive()) {
            return;
        }
        TurnContext context = new TurnContext(this, currentPos, grid, random);
        turnChain.execute(context);
    }

    @Override
    public Animal createOffspring() {
        return new AnimalEntity(
                getSpeciesName(),
                getSymbol(),
                getColor(),
                initialEnergy,
                maxEnergy,
                maxAge,
                minReproductiveAge,
                maxReproductiveAge,
                diet,
                sightRange,
                speed,
                turnChain
        );
    }

    @Override
    public int getEnergy() { return energy; }

    @Override
    public int getMaxEnergy() { return maxEnergy; }

    @Override
    public void addEnergy(int amount) {
        if (amount > 0) {
            this.energy = Math.min(maxEnergy, this.energy + amount);
        }
    }

    @Override
    public void consumeEnergy(int amount) {
        if (amount > 0) {
            this.energy = Math.max(0, this.energy - amount);
        }
    }

    /**
     * Imposta direttamente l'energia e l'età (usato nella ricostruzione da File di Stato).
     *
     * @param energy energia salvata
     * @param age    età salvata
     */
    public void restoreState(int energy, int age) {
        this.energy = Math.min(maxEnergy, Math.max(0, energy));
        this.age = Math.max(0, age);
        if (this.energy == 0 || this.age > maxAge) {
            die();
        }
    }

    @Override
    public int getAge() { return age; }

    @Override
    public int getMaxAge() { return maxAge; }

    @Override
    public void incrementAge() { this.age++; }

    @Override
    public int getMinReproductiveAge() { return minReproductiveAge; }

    @Override
    public int getMaxReproductiveAge() { return maxReproductiveAge; }

    @Override
    public boolean isInReproductiveAge() {
        return age >= minReproductiveAge && age <= maxReproductiveAge;
    }

    @Override
    public Diet getDiet() { return diet; }

    @Override
    public int getSightRange() { return sightRange; }

    @Override
    public int getSpeed() { return speed; }

    public int getInitialEnergy() { return initialEnergy; }
}