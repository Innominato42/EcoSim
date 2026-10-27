package org.example.model.behavior;



import org.example.model.Animal;

import java.util.Objects;

/**
 * Costruisce e coordina la Catena di Responsabilità configurabile per il turno di un animale.
 */
public class AnimalTurnChain {

    private final TurnStep head;

    /**
     * Costruisce una catena personalizzata a partire dal primo anello.
     *
     * @param head primo passo della catena
     */
    public AnimalTurnChain(TurnStep head) {
        this.head = Objects.requireNonNull(head, "La testa della catena non può essere null.");
    }

    /**
     * Crea la catena standard a 6 passi prevista dalla specifica combinando le tre Strategy.
     *
     * @param feedingStrategy      strategia di ricerca cibo e nutrimento
     * @param movementStrategy     strategia di movimento
     * @param reproductionStrategy strategia di riproduzione
     * @return istanza configurata di AnimalTurnChain
     */
    public static AnimalTurnChain createStandardChain(
            FeedingStrategy feedingStrategy,
            MovementStrategy movementStrategy,
            ReproductionStrategy reproductionStrategy) {

        // 1. Invecchia
        TurnStep agingStep = new TurnStep() {
            @Override
            protected boolean executeStep(TurnContext ctx) {
                Animal a = ctx.getAnimal();
                a.incrementAge();
                a.consumeEnergy(1);
                return true;
            }
        };

        // 2. Controlla vitalità
        TurnStep vitalityStep = new TurnStep() {
            @Override
            protected boolean executeStep(TurnContext ctx) {
                Animal a = ctx.getAnimal();
                if (a.getEnergy() <= 0 || a.getAge() > a.getMaxAge()) {
                    a.die();
                    return false; // Interrompe la catena: l'animale è morto
                }
                return true;
            }
        };

        // 3. Cerca cibo
        TurnStep seekFoodStep = new TurnStep() {
            @Override
            protected boolean executeStep(TurnContext ctx) {
                feedingStrategy.findClosestFoodOffset(ctx.getAnimal(), ctx.getCurrentPosition(), ctx.getGrid())
                        .ifPresent(ctx::setTargetFoodOffset);
                return true;
            }
        };

        // 4. Si muove
        TurnStep moveStep = new TurnStep() {
            @Override
            protected boolean executeStep(TurnContext ctx) {
                movementStrategy.move(ctx);
                return true;
            }
        };

        // 5. Mangia
        TurnStep eatStep = new TurnStep() {
            @Override
            protected boolean executeStep(TurnContext ctx) {
                ctx.getReachedFood().ifPresent(food -> feedingStrategy.eat(ctx.getAnimal(), food));
                return true;
            }
        };

        // 6. Si riproduce
        TurnStep reproduceStep = new TurnStep() {
            @Override
            protected boolean executeStep(TurnContext ctx) {
                reproductionStrategy.reproduce(ctx);
                return true;
            }
        };

        agingStep.setNext(vitalityStep)
                .setNext(seekFoodStep)
                .setNext(moveStep)
                .setNext(eatStep)
                .setNext(reproduceStep);

        return new AnimalTurnChain(agingStep);
    }

    /**
     * Crea la catena standard con le implementazioni di default delle tre Strategy.
     *
     * @return catena standard pronta all'uso
     */
    public static AnimalTurnChain createDefault() {
        return createStandardChain(
                new StandardFeedingStrategy(),
                new StandardMovementStrategy(),
                new StandardReproductionStrategy()
        );
    }

    /**
     * Avvia l'esecuzione della catena di responsabilità sul contesto fornito.
     *
     * @param context contesto del turno
     */
    public void execute(TurnContext context) {
        head.handle(context);
    }
}
