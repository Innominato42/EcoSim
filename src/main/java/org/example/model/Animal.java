package org.example.model;

import java.util.random.RandomGenerator;

/**
 * Contratto per gli animali, entità mobili che si muovono, si nutrono e si riproducono.
 */
public interface Animal extends Entity {

    @Override
    default EntityType getType() {
        return EntityType.ANIMAL;
    }

    int getMaxEnergy();
    void addEnergy(int amount);
    void consumeEnergy(int amount);

    int getAge();
    int getMaxAge();
    void incrementAge();

    int getMinReproductiveAge();
    int getMaxReproductiveAge();
    boolean isInReproductiveAge();

    Diet getDiet();
    int getSightRange();
    int getSpeed();

    /**
     * Esegue il ciclo comportamentale completo dell'animale per il turno corrente.
     *
     * @param currentPos posizione iniziale dell'animale all'avvio del turno
     * @param grid       griglia di simulazione
     * @param random     generatore di numeri casuali
     */
    void act(Position currentPos, Grid grid, RandomGenerator random);

    /**
     * Genera un nuovo esemplare giovane della stessa specie (con età 0 ed energia iniziale).
     *
     * @return nuovo animale figlio
     */
    Animal createOffspring();
}