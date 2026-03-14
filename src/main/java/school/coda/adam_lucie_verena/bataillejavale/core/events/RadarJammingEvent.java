package school.coda.adam_lucie_verena.bataillejavale.core.events;

import school.coda.adam_lucie_verena.bataillejavale.core.engine.BattleEngine;

/**
 * Désactive la visibilité des tirs passés sur la grille radar.
 */
public class RadarJammingEvent extends RandomEvent {
    public RadarJammingEvent(int duration) {
        super(RandomEventType.BROUILLAGE, duration);
    }

    @Override
    public void apply(BattleEngine engine) {
        engine.setFogActive(true);
    }

    @Override
    public void remove(BattleEngine engine) {
        engine.setFogActive(false);
    }
}