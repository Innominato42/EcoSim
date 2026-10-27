package org.example.model.behavior;

/**
 * Rappresenta un singolo anello della Catena di Responsabilità del turno di un animale.
 */
public abstract class TurnStep {

    private TurnStep next;

    /**
     * Collega il prossimo passo della catena e lo restituisce per consentire il concatenamento fluido.
     *
     * @param next prossimo passo da eseguire
     * @return il passo appena collegato
     */
    public TurnStep setNext(TurnStep next) {
        this.next = next;
        return next;
    }

    /**
     * Esegue la logica del passo corrente e, se la catena non deve interrompersi,
     * passa il controllo all'anello successivo.
     *
     * @param context contesto del turno dell'animale
     */
    public final void handle(TurnContext context) {
        if (executeStep(context) && next != null) {
            next.handle(context);
        }
    }

    /**
     * Logica specifica dello step.
     *
     * @param context contesto del turno
     * @return true se la catena deve proseguire al passo successivo, false per interromperla
     */
    protected abstract boolean executeStep(TurnContext context);
}