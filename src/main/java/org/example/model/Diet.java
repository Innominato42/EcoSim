package org.example.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Rappresenta il regime alimentare di un animale (erbivoro, carnivoro, onnivoro).
 * Determina quali entità possono essere consumate come cibo.
 */
public class Diet {

    private final boolean eatsPlants;
    private final Set<String> preySpecies;

    /**
     * Costruisce una dieta personalizzata.
     *
     * @param eatsPlants  true se l'animale si nutre di piante
     * @param preySpecies insieme dei nomi delle specie animali predate (o "*" per tutte)
     */
    public Diet(boolean eatsPlants, Set<String> preySpecies) {
        this.eatsPlants = eatsPlants;
        this.preySpecies = Collections.unmodifiableSet(new HashSet<>(
                Objects.requireNonNull(preySpecies, "L'insieme delle prede non può essere null.")
        ));
    }

    /**
     * Crea una dieta esclusivamente erbivora (DIET PLANT).
     *
     * @return istanza di Diet erbivora
     */
    public static Diet herbivore() {
        return new Diet(true, Set.of());
    }

    /**
     * Crea una dieta carnivora rivolta a specifiche specie (es. DIET ANIMAL:Coniglio).
     *
     * @param species nomi delle specie animali cacciate
     * @return istanza di Diet carnivora
     */
    public static Diet carnivore(Set<String> species) {
        return new Diet(false, species);
    }

    /**
     * Crea una dieta onnivora che include sia piante sia determinate specie animali.
     *
     * @param species nomi delle specie animali cacciate
     * @return istanza di Diet onnivora
     */
    public static Diet omnivore(Set<String> species) {
        return new Diet(true, species);
    }

    /**
     * Verifica se l'entità specificata rientra nella dieta dell'animale.
     *
     * @param target entità candidata ad essere mangiata
     * @return true se l'entità è viva ed è commestibile secondo questa dieta
     */
    public boolean canEat(Entity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (target.getType() == EntityType.PLANT) {
            return eatsPlants;
        }
        if (target.getType() == EntityType.ANIMAL) {
            return preySpecies.contains("*") || preySpecies.contains(target.getSpeciesName());
        }
        return false;
    }

    public boolean eatsPlants() { return eatsPlants; }
    public Set<String> getPreySpecies() { return preySpecies; }

    @Override
    public String toString() {
        if (eatsPlants && preySpecies.isEmpty()) return "Erbivoro (PLANT)";
        if (!eatsPlants) return "Carnivoro (ANIMAL:" + String.join(",", preySpecies) + ")";
        return "Onnivoro (PLANT + ANIMAL:" + String.join(",", preySpecies) + ")";
    }
}