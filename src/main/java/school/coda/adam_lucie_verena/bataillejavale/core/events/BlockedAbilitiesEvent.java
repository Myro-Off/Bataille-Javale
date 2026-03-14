package school.coda.adam_lucie_verena.bataillejavale.core.events;

import school.coda.adam_lucie_verena.bataillejavale.core.engine.BattleEngine;

/**
 * Verrouille l'accès aux compétences spéciales (Ravitaillement, etc.).
 */
public class BlockedAbilitiesEvent extends RandomEvent {
    public BlockedAbilitiesEvent(int duration) {
        super(RandomEventType.CAPACITES_BLOQUEES, duration);
    }

    @Override
    public void apply(BattleEngine engine) {
        engine.setSpecialAbilitiesBlocked(true);
    }

    @Override
    public void remove(BattleEngine engine) {
        engine.setSpecialAbilitiesBlocked(false);
    }
}