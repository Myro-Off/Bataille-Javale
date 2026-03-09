package school.coda.adam_lucie_verena.bataillejavale.core.engine;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.logging.Logger;
import school.coda.adam_lucie_verena.bataillejavale.core.events.GameOverEvent;
import school.coda.adam_lucie_verena.bataillejavale.core.model.*;
import java.util.Random;

/**
 * Arbitre central de la logique de jeu (Game Engine).
 * <p>
 * Cette classe gère l'alternance des tours entre le joueur et l'IA,
 * valide les tirs et déclenche les événements de fin de partie via le bus d'événements.
 * </p>
 */
public class GameController {
    /** Logger officiel pour le suivi du moteur de jeu. */
    private static final Logger log = Logger.get(GameController.class);
    /** État actuel de la partie (Tour du joueur, de l'IA, etc.). */
    private GameState currentState;
    /** Plateau de jeu du joueur humain (cible de l'IA). */
    private final Board playerBoard;
    /** Plateau de jeu de l'IA (cible du joueur). */
    private final Board enemyBoard;
    /** Générateur aléatoire pour les décisions de l'IA. */
    private final Random random = new Random();

    /**
     * Initialise le contrôleur avec les deux plateaux de jeu.
     * Le tour commence par défaut avec le joueur.
     * @param playerBoard Plateau du joueur.
     * @param enemyBoard  Plateau de l'adversaire.
     */
    public GameController(Board playerBoard, Board enemyBoard) {
        this.playerBoard = playerBoard;
        this.enemyBoard = enemyBoard;
        this.currentState = GameState.PLAYER_TURN;
    }

    /**
     * Traite une intention de tir du joueur humain.
     * <p>
     * Si c'est bien le tour du joueur, le tir est appliqué sur la grille ennemie
     * et l'état passe au tour de l'ordinateur.
     * </p>
     * @param coord Coordonnée cible du tir.
     * @return {@code true} si un navire a été touché, {@code false} sinon.
     */
    public boolean handlePlayerShot(Coordinate coord) {
        if (currentState != GameState.PLAYER_TURN) {
            log.warning("Tentative de tir en dehors du tour joueur ignorée.");
            return false;
        }

        boolean hit = enemyBoard.receiveFire(coord);
        log.info("Joueur tire en " + coord + " -> " + (hit ? "TOUCHÉ" : "DANS L'EAU"));

        // On change l'état : c'est maintenant au tour de l'ordinateur
        this.currentState = GameState.AI_TURN;

        return hit;
    }

    /**
     * Exécute la logique de tir de l'Intelligence Artificielle.
     * <p>
     * L'IA utilise une stratégie de tir aléatoire ("Random Fire") jusqu'à trouver
     * une case non explorée sur le plateau du joueur.
     * </p>
     */
    public void aiTurn() {
        if (currentState != GameState.AI_TURN) return;

        boolean shotValid = false;
        while (!shotValid) {
            int x = random.nextInt(playerBoard.getWidth());
            int y = random.nextInt(playerBoard.getHeight());
            Coordinate target = new Coordinate(x, y);

            // Vérification que la case n'a pas déjà été visée
            if (!playerBoard.getHitShots().contains(target) &&
                    !playerBoard.getMissedShots().contains(target)) {

                boolean hit = playerBoard.receiveFire(target);
                shotValid = true;

                log.info("L'IA tire en " + x + ", " + y + " -> " + (hit ? "TOUCHÉ" : "DANS L'EAU"));
            }
        }

        // Le tour de l'IA est fini, on repasse la main au joueur
        this.currentState = GameState.PLAYER_TURN;
    }

    /**
     * Analyse l'état des plateaux pour détecter une condition de victoire ou de défaite.
     * <p>
     * Si tous les navires d'un camp sont coulés, un {@link GameOverEvent} est émis.
     * </p>
     */
    public void checkGameOver() {
        if (enemyBoard.allShipsSunk()) {
            log.info("Condition de victoire détectée : Flotte ennemie coulée.");
            FXGL.getEventBus().fireEvent(new GameOverEvent(true));
        } else if (playerBoard.allShipsSunk()) {
            log.info("Condition de défaite détectée : Flotte joueur coulée.");
            FXGL.getEventBus().fireEvent(new GameOverEvent(false));
        }
    }

    /** @return L'état actuel de la machine à états du jeu. */
    public GameState getCurrentState() { return currentState; }

    /** @param state Le nouvel état à appliquer au contrôleur. */
    public void setCurrentState(GameState state) { this.currentState = state; }
}