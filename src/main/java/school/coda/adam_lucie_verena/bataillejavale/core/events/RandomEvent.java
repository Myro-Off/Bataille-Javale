package school.coda.adam_lucie_verena.bataillejavale.core.events;

import school.coda.adam_lucie_verena.bataillejavale.core.engine.BattleEngine;

/**
 * Classe de base pour tout événement aléatoire.
 */
public abstract class RandomEvent {
    private final RandomEventType type;
    private int duration; // Nombre de tours où l'événement reste actif

    public RandomEvent(RandomEventType type, int duration) {
        this.type = type;
        this.duration = duration;
    }

    /**
     * Applique les effets de l'événement sur le moteur de jeu.
     * @param engine Le moteur de combat actuel.
     */
    public abstract void apply(BattleEngine engine);

    /**
     * Retire les effets de l'événement (fin de durée).
     * @param engine Le moteur de combat actuel.
     */
    public abstract void remove(BattleEngine engine);

    public RandomEventType getType() { return type; }
    public int getDuration() { return duration; }
    public void decreaseDuration() { duration--; }
    public boolean isExpired() { return duration <= 0; }
}