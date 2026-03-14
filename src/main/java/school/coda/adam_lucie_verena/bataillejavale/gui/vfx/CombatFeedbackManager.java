package school.coda.adam_lucie_verena.bataillejavale.gui.vfx;

import javafx.scene.paint.Color;
import school.coda.adam_lucie_verena.bataillejavale.core.model.Coordinate;
import school.coda.adam_lucie_verena.bataillejavale.core.model.Ship;
import school.coda.adam_lucie_verena.bataillejavale.gui.Theme;
import school.coda.adam_lucie_verena.bataillejavale.gui.AssetsManager;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.GameLogView;

/**
 * Traduit les événements de jeu en entrées textuelles et sonores.
 * Centralise les styles et les messages pour le journal de bord.
 */
public class CombatFeedbackManager {

    private final GameLogView logView;

    public CombatFeedbackManager(GameLogView logView) {
        this.logView = logView;
    }

    /**
     * Enregistre un tir réussi dans le log.
     */
    public void logHit(String shooter, Coordinate coord) {
        String pos = String.format("%c:%02d", (char)('A' + coord.x()), coord.y() + 1);
        Color color = shooter.equals("CMD") ? Theme.CYAN : Theme.RED_ALERTE;

        logView.addLog("[" + shooter + "]  IMPACT  >> " + pos, color);
        AssetsManager.playSFX("underwater_explosion.wav", 1);
    }

    /**
     * Enregistre un tir manqué dans le log.
     */
    public void logMiss(String shooter, Coordinate coord) {
        String pos = String.format("%c:%02d", (char)('A' + coord.x()), coord.y() + 1);

        logView.addLog("[" + shooter + "]  MANQUÉ  -- " + pos, Color.web("#64748b"));
        AssetsManager.playSFX("miss.wav", 0.6);
    }

    /**
     * Enregistre la destruction d'un navire.
     */
    public void logSunk(String team, Ship ship) {
        logView.addLog("UNITÉ " + team + " DÉTRUITE : " + ship.getType().getName(), Color.ORANGERED);
    }

    /**
     * Marque le début d'une nouvelle manche.
     */
    public void logRound(int roundNumber) {
        logView.addLog(":: ROUND " + roundNumber + " ::", Color.GOLD);
    }

    /**
     * Ajoute un message personnalisé dans le journal de combat.
     * @param message Le texte à afficher.
     * @param color La couleur du texte.
     */
    public void logEvent(String message, javafx.scene.paint.Color color) {
        // Si ta méthode dans GameLogView s'appelle autrement (ex: addMessage), change-la ici
        logView.addLog(message, color);
    }
}