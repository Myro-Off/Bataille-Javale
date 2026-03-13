package school.coda.adam_lucie_verena.bataillejavale.core.engine;

import com.almasb.fxgl.dsl.FXGL;
import school.coda.adam_lucie_verena.bataillejavale.core.achievement.AchievementManager;
import school.coda.adam_lucie_verena.bataillejavale.core.ai.*;
import school.coda.adam_lucie_verena.bataillejavale.core.events.GameOverEvent;
import school.coda.adam_lucie_verena.bataillejavale.core.model.*;

/**
 * Moteur de jeu gérant la logique métier et le séquençage des manches.
 * Implémente un système de tour par tour strict où chaque tir met fin au tour actuel.
 */
public class BattleEngine {

    private GameState currentState;
    private final Board playerBoard;
    private final Board enemyBoard;
    private final AIStrategy aiStrategy;
    private final AchievementManager achievementManager;

    private int totalPlayerShots = 0, totalPlayerHits = 0;
    private int totalEnemyShots = 0, totalEnemyHits = 0;
    private int roundNumber = 1;

    /**
     * @param playerBoard Plateau du joueur.
     * @param enemyBoard Plateau de l'adversaire.
     * @param difficulty Niveau de difficulté pour l'IA.
     */
    public BattleEngine(Board playerBoard, Board enemyBoard, Difficulty difficulty, AchievementManager achievementManager) {
        this.playerBoard = playerBoard;
        this.enemyBoard = enemyBoard;
        this.achievementManager = achievementManager;
        this.currentState = GameState.PLAYER_TURN;

//        this.aiStrategy = switch (difficulty) {
//            case EASY -> new RandomAI();
//            case NORMAL -> new HuntingAI();
//            case EXPERT -> new TacticalAI();
//        };
        this.aiStrategy = difficulty.createAiStrategy();
    }

    /**
     * Initialise les paramètres d'une nouvelle partie.
     */
    public void startGame() {
        this.currentState = GameState.PLAYER_TURN;
        this.roundNumber = 1;
        this.totalPlayerShots = 0; this.totalPlayerHits = 0;
        this.totalEnemyShots = 0; this.totalEnemyHits = 0;
    }

    /**
     * Traite le tir du joueur et bascule systématiquement le tour vers l'IA.
     * @param coord Cible du tir.
     * @return true si un navire est touché.
     */
    public boolean handlePlayerShot(Coordinate coord) {
        if (currentState != GameState.PLAYER_TURN) return false;

        totalPlayerShots++;
        boolean hit = enemyBoard.receiveFire(coord);

        if (hit) totalPlayerHits++;

        if (enemyBoard.allShipsSunk()) {
            achievementManager.onGameEnd(true, roundNumber);
            currentState = GameState.GAME_OVER;
            FXGL.getEventBus().fireEvent(new GameOverEvent(true, totalPlayerShots, totalPlayerHits));
        } else {
            currentState = GameState.AI_TURN;
        }

        return hit;
    }

    /**
     * Exécute le tir de l'IA et bascule systématiquement le tour vers le joueur.
     * @return La coordonnée attaquée.
     */
    public Coordinate aiTurn() {
        if (currentState != GameState.AI_TURN) return null;

        Coordinate target = aiStrategy.chooseTarget(playerBoard);
        totalEnemyShots++;

        boolean hit = playerBoard.receiveFire(target);
        boolean sunk = false;

        if (hit) {
            totalEnemyHits++;
            sunk = playerBoard.getShips().stream()
                    .filter(s -> s.isAt(target))
                    .findFirst()
                    .map(Ship::isSunk)
                    .orElse(false);
        }

        aiStrategy.informResult(target, hit, sunk);

        if (playerBoard.allShipsSunk()) {
            achievementManager.onGameEnd(false, roundNumber);
            currentState = GameState.GAME_OVER;
            FXGL.getEventBus().fireEvent(new GameOverEvent(false, totalPlayerShots, totalPlayerHits));
        } else {
            currentState = GameState.PLAYER_TURN;
        }

        return target;
    }

    /**
     * Incrémente le compteur de manches.
     */
    public void nextRound() {
        this.roundNumber++;
    }

    public int getRoundNumber() { return roundNumber; }
    public int getTotalPlayerShots() { return totalPlayerShots; }
    public int getTotalPlayerHits() { return totalPlayerHits; }
    public int getTotalEnemyShots() { return totalEnemyShots; }
    public int getTotalEnemyHits() { return totalEnemyHits; }
    public GameState getCurrentState() { return currentState; }
    public double getPlayerAccuracy() { return calculateAcc(totalPlayerShots, totalPlayerHits); }
    public double getEnemyAccuracy() { return calculateAcc(totalEnemyShots, totalEnemyHits); }

    private double calculateAcc(int s, int h) {
        return s == 0 ? 0.0 : (double) h / s * 100.0;
    }
}