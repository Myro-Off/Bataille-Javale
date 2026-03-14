package school.coda.adam_lucie_verena.bataillejavale.core.events;

import school.coda.adam_lucie_verena.bataillejavale.core.engine.BattleEngine;

/**
 * Augmente la puissance de feu de la prochaine salve.
 */
public class BoostedVolleyEvent extends RandomEvent {
    public BoostedVolleyEvent() {
        super(RandomEventType.SALVE_BOOSTEE, 1);
    }

    @Override
    public void apply(BattleEngine engine) {
        engine.setBoostedVolleyActive(true);
    }

    @Override
    public void remove(BattleEngine engine) {
    }
}