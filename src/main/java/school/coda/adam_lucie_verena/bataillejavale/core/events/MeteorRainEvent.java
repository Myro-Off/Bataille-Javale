package school.coda.adam_lucie_verena.bataillejavale.core.events;

import school.coda.adam_lucie_verena.bataillejavale.core.engine.BattleEngine;

/**
 * Frappe les grilles de manière aléatoire et silencieuse.
 */
public class MeteorRainEvent extends RandomEvent {
    public MeteorRainEvent() {
        super(RandomEventType.METEORES, 1);
    }

    @Override
    public void apply(BattleEngine engine) {
        engine.triggerMeteorImpactsSilent(2);
    }

    @Override
    public void remove(BattleEngine engine) {}
}