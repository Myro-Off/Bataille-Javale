package school.coda.adam_lucie_verena.bataillejavale.core.events;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Événement personnalisé signalant la fin d'une partie de Bataille-Javale.
 * <p>
 * Cet événement est publié sur le {@link com.almasb.fxgl.event.EventBus} par le contrôleur
 * dès qu'une condition de victoire ou de défaite est détectée.
 * </p>
 */
public class GameOverEvent extends Event {

    // ------------------------------------------------------------------------------------------
    // CONSTANTES D'ÉVÉNEMENT
    // ------------------------------------------------------------------------------------------

    /** Type d'événement de base pour la fin de partie. */
    public static final EventType<GameOverEvent> ANY = new EventType<>(Event.ANY, "GAME_OVER");

    // ------------------------------------------------------------------------------------------
    // ATTRIBUTS
    // ------------------------------------------------------------------------------------------

    /** Indique si l'événement correspond à une victoire du joueur humain. */
    private final boolean victory;

    // ------------------------------------------------------------------------------------------
    // CONSTRUCTEUR
    // ------------------------------------------------------------------------------------------

    /**
     * Crée une nouvelle instance de l'événement de fin de partie.
     *
     * @param victory {@code true} si le joueur a gagné, {@code false} s'il a perdu.
     */
    public GameOverEvent(boolean victory) {
        super(ANY);
        this.victory = victory;
    }

    // ------------------------------------------------------------------------------------------
    // GETTERS
    // ------------------------------------------------------------------------------------------

    /**
     * @return {@code true} si le joueur est victorieux.
     */
    public boolean isVictory() {
        return victory;
    }
}