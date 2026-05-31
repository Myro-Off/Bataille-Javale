package school.coda.adam_lucie_verena.bataillejavale.core.events;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Événement déclenché à la fin d'une partie.
 * Transporte l'ensemble des statistiques finales (résultat, tirs, impacts et série)
 * du moteur de jeu vers l'interface utilisateur.
 */
// 🚨 GameOverEvent n'est pas un événement au sens des événements aléatoires
// Il devrait se situer dans un autre package pour éviter la confusion
public class GameOverEvent extends Event {

    /** Type d'événement générique pour la fin de partie. */
    public static final EventType<GameOverEvent> ANY = new EventType<>(Event.ANY, "GAME_OVER");

    private final boolean victory;
    private final int totalShots;
    private final int totalHits;
    private final int streak;

    /**
     * Crée un nouvel événement de fin de partie.
     * * @param victory    True si le joueur a remporté la victoire.
     * @param totalShots Nombre total de projectiles lancés par le joueur.
     * @param totalHits  Nombre de tirs ayant touché un navire ennemi.
     * @param streak     Nombre de victoires consécutives actuelles (série).
     */
    public GameOverEvent(boolean victory, int totalShots, int totalHits, int streak) {
        super(ANY);
        this.victory = victory;
        this.totalShots = totalShots;
        this.totalHits = totalHits;
        this.streak = streak;
    }

    /** @return True en cas de victoire, false sinon. */
    public boolean isVictory() {
        return victory;
    }

    /** @return Le volume total de tirs effectués. */
    public int getTotalShots() {
        return totalShots;
    }

    /** @return Le nombre de tirs réussis. */
    public int getTotalHits() {
        return totalHits;
    }

    /** @return La série de victoires consécutives stockée dans l'AchievementManager. */
    public int getStreak() {
        return streak;
    }
}