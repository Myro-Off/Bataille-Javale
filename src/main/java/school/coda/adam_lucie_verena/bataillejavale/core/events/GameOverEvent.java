package school.coda.adam_lucie_verena.bataillejavale.core.events;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Événement transportant les statistiques finales de la partie.
 */
public class GameOverEvent extends Event {
    public static final EventType<GameOverEvent> ANY = new EventType<>(Event.ANY, "GAME_OVER");

    private final boolean victory;
    private final int totalShots;
    private final int totalHits;

    /**
     * @param victory    Résultat de la bataille.
     * @param totalShots Nombre total de tirs effectués par le joueur.
     * @param totalHits  Nombre de coups au but réussis par le joueur.
     */
    public GameOverEvent(boolean victory, int totalShots, int totalHits) {
        super(ANY);
        this.victory = victory;
        this.totalShots = totalShots;
        this.totalHits = totalHits;
    }

    public boolean isVictory() { return victory; }
    public int getTotalShots() { return totalShots; }
    public int getTotalHits() { return totalHits; }
}