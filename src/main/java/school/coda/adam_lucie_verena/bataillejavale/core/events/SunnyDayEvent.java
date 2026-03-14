package school.coda.adam_lucie_verena.bataillejavale.core.events;

import school.coda.adam_lucie_verena.bataillejavale.core.engine.BattleEngine;

/**
 * Événement pacifique déclenchant l'obtention d'un succès.
 */
public class SunnyDayEvent extends RandomEvent {
    public SunnyDayEvent() {
        super(RandomEventType.JOURNEE_ENSOLEILLEE, 1);
    }

    @Override
    public void apply(BattleEngine engine) {
        engine.getAchievementManager().unlockSunnyDay();
    }

    @Override
    public void remove(BattleEngine engine) {
    }
}