package school.coda.adam_lucie_verena.bataillejavale.view.vfx;

import javafx.scene.paint.Color;
import school.coda.adam_lucie_verena.bataillejavale.core.model.Coordinate;
import school.coda.adam_lucie_verena.bataillejavale.core.model.Ship;
import school.coda.adam_lucie_verena.bataillejavale.view.audio.SoundManager;
import school.coda.adam_lucie_verena.bataillejavale.view.component.GameLogView;

/**
 * Traduit les événements de jeu en entrées textuelles pour le journal de bord.
 * Centralise les styles (couleurs, préfixes) pour une maintenance facilitée.
 */
public class CombatFeedbackManager {

    private final GameLogView logView;

    /**
     * @param logView L'instance du journal de bord à alimenter.
     */
    public CombatFeedbackManager(GameLogView logView) {
        this.logView = logView;
    }

    /**
     * Log un tir réussi.
     * @param shooter Nom de l'entité qui tire.
     * @param coord Coordonnée de l'impact.
     */
    public void logHit(String shooter, Coordinate coord) {
        logView.addLog(shooter + " : IMPACT CONFIRMÉ en " + (char)('A' + coord.x()) + (coord.y() + 1), Color.web("#ff4757"));
        SoundManager.playSFX("canon.wav");
    }

    /**
     * Log un tir manqué.
     * @param shooter Nom de l'entité qui tire.
     */
    public void logMiss(String shooter) {
        logView.addLog(shooter + " : Projectile perdu en mer.", Color.web("#94a3b8"));
        SoundManager.playSFX("a_l_eau.wav");
    }

    /**
     * Log la destruction d'un navire.
     * @param team Nom de l'équipe du navire.
     * @param ship Navire coulé.
     */
    public void logSunk(String team, Ship ship) {
        logView.addLog("UNITÉ " + team + " DÉTRUITE : " + ship.getType().getName(), Color.ORANGERED);
    }

    /**
     * Affiche un message d'information système.
     * @param message Contenu du message.
     */
    public void logInfo(String message) {
        logView.addLog("SYSTEM : " + message, Color.web("#00d2d3"));
    }
}

/* =========================================================================================
 * 🔔 TODO : ALERTE RADAR (NOTIFICATIONS DE SUCCÈS)
 * =========================================================================================
 * OBJECTIF : Notifier le joueur via FXGL lorsqu'un succès est validé.
 *
 * ÉTAPES :
 * 1. Intégrer le service de notification FXGL :
 * -> FXGL.getNotificationService().pushNotification("SUCCÈS : " + achievementName);
 * 2. Ajouter un feedback sonore via SoundManager :
 * -> SoundManager.playSound("achievement_unlocked.wav");
 * 3. Mettre à jour graphiquement le 'AchievementTrackerView' pour refléter le changement.
 *
 * NOTE : La logique de vérification (si déjà débloqué) doit être gérée en amont
 * pour éviter de spammer les notifications durant le combat.
 * ========================================================================================= */

/* =========================================================================================
 * 🔊 TODO : SONAR & EXPLOSIONS (SYSTÈME SFX)
 * =========================================================================================
 * OBJECTIF : Créer une ambiance immersive via des retours sonores tactiques.
 *
 * ÉTAPES :
 * 1. Ressources : Placer les fichiers .wav dans 'src/main/resources/assets/sounds/'.
 * 2. Implémentation : Utiliser SoundManager (ou FXGL.play() en direct) :
 * - Dans logHit() -> "hit.wav"
 * - Dans logMiss() -> "miss.wav"
 * - Dans logSunk() -> "explosion.wav"
 * 3. Bonus : Ajouter un son de "ping" sonar lors de l'activation des capacités spéciales.
 *
 * NOTE : Attention au volume sonore pour ne pas saturer l'expérience utilisateur.
 * ========================================================================================= */