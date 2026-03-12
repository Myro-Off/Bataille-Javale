package school.coda.adam_lucie_verena.bataillejavale.core.engine;
import com.almasb.fxgl.dsl.FXGL;
import school.coda.adam_lucie_verena.bataillejavale.core.events.GameOverEvent;
import school.coda.adam_lucie_verena.bataillejavale.core.model.*;
import java.util.Random;
/**
 * Arbitre central gérant les règles métier, les tours et les statistiques de combat.
 * Cette classe est indépendante de toute représentation graphique.
 */
public class BattleEngine {
    private GameState currentState;
    private final Board playerBoard;
    private final Board enemyBoard;
    private final Random random = new Random();
    private int totalPlayerShots = 0;
    private int totalPlayerHits = 0;
    /**
     * Initialise le moteur avec les plateaux respectifs des joueurs.
     * @param playerBoard Plateau du joueur humain.
     * @param enemyBoard Plateau de l'adversaire (IA).
     */
    public BattleEngine(Board playerBoard, Board enemyBoard) {
        this.playerBoard = playerBoard;
        this.enemyBoard = enemyBoard;
        this.currentState = GameState.PLAYER_TURN;
    }
    /**
     * Traite une intention de tir du joueur et met à jour les statistiques.
     * @param coord Coordonnée ciblée par le joueur.
     * @return True si un navire a été touché, false sinon.
     */
    public boolean handlePlayerShot(Coordinate coord) {
        if (currentState != GameState.PLAYER_TURN) return false;
        totalPlayerShots++;
        boolean hit = enemyBoard.receiveFire(coord);
        if (hit) totalPlayerHits++;
        if (enemyBoard.allShipsSunk()) {
            currentState = GameState.GAME_OVER;
            FXGL.getEventBus().fireEvent(new GameOverEvent(true, totalPlayerShots, totalPlayerHits));
        } else {
            currentState = GameState.AI_TURN;
        }
        return hit;
    }
    /**
     * Calcule et exécute la riposte de l'Intelligence Artificielle.
     * @return La coordonnée sélectionnée par l'IA pour son tir.
     */
    public Coordinate aiTurn() {
        if (currentState != GameState.AI_TURN) return null;
        Coordinate target;
        do {
            target = new Coordinate(random.nextInt(playerBoard.getWidth()), random.nextInt(playerBoard.getHeight()));
        } while (playerBoard.isAlreadyShot(target));
        playerBoard.receiveFire(target);
        if (playerBoard.allShipsSunk()) {
            currentState = GameState.GAME_OVER;
            FXGL.getEventBus().fireEvent(new GameOverEvent(false, totalPlayerShots, totalPlayerHits));
        } else {
            currentState = GameState.PLAYER_TURN;
        }
        return target;
    }
    /**
     * Positionne l'état initial pour le début du combat.
     */
    public void startGame() {
        currentState = GameState.PLAYER_TURN;
    }
    /**
     * @return L'état actuel de la partie (tour du joueur, IA ou fin de partie).
     */
    public GameState getCurrentState() {
        return currentState;
    }
}