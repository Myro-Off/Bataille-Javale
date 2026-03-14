package school.coda.adam_lucie_verena.bataillejavale.core.engine;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import school.coda.adam_lucie_verena.bataillejavale.core.achievement.AchievementManager;
import school.coda.adam_lucie_verena.bataillejavale.core.ai.*;
import school.coda.adam_lucie_verena.bataillejavale.core.events.GameOverEvent;
import school.coda.adam_lucie_verena.bataillejavale.core.model.*;

/**
 * Moteur de jeu gérant la logique métier et le séquençage des manches.
 * Implémente un système de tour par tour strict où chaque tir met fin au tour actuel.
 * Communique les résultats de fin de partie via le bus d'événements FXGL.
 */
public class BattleEngine {

    private GameState currentState;
    private final Board playerBoard;
    private final Board enemyBoard;
    private final AIStrategy aiStrategy;
    private final AchievementManager achievementManager;
    private final Difficulty difficulty;

    private int totalPlayerShots = 0, totalPlayerHits = 0;
    private int totalEnemyShots = 0, totalEnemyHits = 0;
    private int roundNumber = 1;

    /**
     * Initialise le moteur de bataille avec les plateaux respectifs et la stratégie d'IA.
     * @param playerBoard        Plateau du joueur.
     * @param enemyBoard         Plateau de l'adversaire.
     * @param difficulty         Niveau de difficulté pour l'IA.
     * @param achievementManager Gestionnaire des succès et des statistiques persistantes.
     */
    public BattleEngine(Board playerBoard, Board enemyBoard, Difficulty difficulty, AchievementManager achievementManager) {
        this.playerBoard = playerBoard;
        this.enemyBoard = enemyBoard;
        this.achievementManager = achievementManager;
        this.difficulty = difficulty;
        this.achievementManager.resetMidGameStats();
        this.currentState = GameState.PLAYER_TURN;
        this.aiStrategy = difficulty.createAiStrategy();
    }

    /**
     * Initialise les paramètres d'une nouvelle partie (remise à zéro des compteurs).
     */
    public void startGame() {
        this.currentState = GameState.PLAYER_TURN;
        this.roundNumber = 1;
        this.totalPlayerShots = 0;
        this.totalPlayerHits = 0;
        this.totalEnemyShots = 0;
        this.totalEnemyHits = 0;
    }

    /**
     * Traite le tir du joueur sur le plateau ennemi.
     * @param coord Coordonnée ciblée par le joueur.
     * @return true si un navire a été touché.
     */
    public boolean handlePlayerShot(Coordinate coord) {
        if (currentState != GameState.PLAYER_TURN) return false;

        totalPlayerShots++;
        boolean hit = enemyBoard.receiveFire(coord);

        // Suivi de la série de tirs réussis pour les succès
        achievementManager.trackHitStreak(hit);

        if (hit) totalPlayerHits++;

        if (enemyBoard.allShipsSunk()) {
            finalizeGame(true);
        } else {
            currentState = GameState.AI_TURN;
        }

        return hit;
    }

    /**
     * Exécute le tour de l'intelligence artificielle.
     * @return La coordonnée attaquée par l'IA.
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
            finalizeGame(false);
        } else {
            currentState = GameState.PLAYER_TURN;
        }

        return target;
    }

    /**
     * Centralise la fin de partie : mise à jour des stats et envoi de l'événement.
     * @param won True si le joueur a gagné.
     */
    private void finalizeGame(boolean won) {
        currentState = GameState.GAME_OVER;

        // 1. Mettre à jour les statistiques globales et succès
        achievementManager.onGameEnd(won, roundNumber, totalPlayerShots, totalPlayerHits, difficulty);

        // 2. Récupérer la série de victoires actuelle
        int currentStreak = achievementManager.getCurrentStreak();

        // 3. Déclencher l'événement (avec un léger délai pour laisser l'animation de tir se finir)
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(_ -> {
            FXGL.getEventBus().fireEvent(new GameOverEvent(
                    won,
                    totalPlayerShots,
                    totalPlayerHits,
                    currentStreak
            ));
        });
        delay.play();
    }

    /**
     * Incrémente manuellement le compteur de manches.
     */
    public void nextRound() {
        this.roundNumber++;
    }

    // --- Getters ---

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