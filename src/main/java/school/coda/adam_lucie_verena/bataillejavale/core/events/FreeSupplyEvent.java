package school.coda.adam_lucie_verena.bataillejavale.core.events;

import school.coda.adam_lucie_verena.bataillejavale.core.engine.BattleEngine;

/**
 * Recharge instantanément les capacités de tous les joueurs.
 */
public class FreeSupplyEvent extends RandomEvent {
    public FreeSupplyEvent() {
        super(RandomEventType.RAVITAILLEMENT_GRATUIT, 1);
    }

    @Override
    public void apply(BattleEngine engine) {
        engine.rechargeSpecialAbilities();
    }

    @Override
    public void remove(BattleEngine engine) {}
}