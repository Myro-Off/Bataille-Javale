package school.coda.adam_lucie_verena.bataillejavale.core.engine;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import school.coda.adam_lucie_verena.bataillejavale.core.achievement.AchievementManager;
import school.coda.adam_lucie_verena.bataillejavale.core.ai.*;
import school.coda.adam_lucie_verena.bataillejavale.core.events.GameOverEvent;
import school.coda.adam_lucie_verena.bataillejavale.core.events.RandomEventManager;
import school.coda.adam_lucie_verena.bataillejavale.core.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Moteur de jeu pilotant la logique métier, le séquençage des tours et les événements aléatoires.
 * Gère le mode Salve, applique les modificateurs de précision et orchestre la fin de partie.
 */
public class BattleEngine {

    private GameState currentState;
    private final Board playerBoard;
    private final Board enemyBoard;
    private final AIStrategy aiStrategy;
    private final AchievementManager achievementManager;
    private final Difficulty difficulty;
    private final RandomEventManager eventManager;
    private final Random random = new Random();

    private final boolean salveMode;
    private int shotsFiredThisTurn = 0;

    private int totalPlayerShots = 0, totalPlayerHits = 0;
    private int totalEnemyShots = 0, totalEnemyHits = 0;
    private int roundNumber = 1;

    private double accuracyModifier = 0.0;
    private boolean fogActive = false;
    private boolean skipNextTurn = false;
    private boolean specialAbilitiesBlocked = false;
    private boolean boostedVolleyActive = false;

    private final List<Coordinate> lastPlayerMeteors = new ArrayList<>();
    private final List<Coordinate> lastEnemyMeteors = new ArrayList<>();

    /**
     * Initialise le moteur de bataille en fonction de la configuration tactique.
     *
     * @param playerBoard        Plateau de l'état-major allié.
     * @param enemyBoard         Plateau radar de la flotte ennemie.
     * @param config             Configuration globale (dimensions, doctrine, événements).
     * @param achievementManager Gestionnaire assurant le suivi des exploits.
     */
    public BattleEngine(Board playerBoard, Board enemyBoard, GameConfig config, AchievementManager achievementManager) {
        this.playerBoard = playerBoard;
        this.enemyBoard = enemyBoard;
        this.difficulty = config.difficulty();
        this.salveMode = config.isSalveMode();
        this.achievementManager = achievementManager;
        if (this.salveMode) {
            this.specialAbilitiesBlocked = true;
        }
        this.currentState = GameState.PLAYER_TURN;
        this.aiStrategy = difficulty.createAiStrategy();
        this.eventManager = new RandomEventManager(this, config);
    }

    /**
     * Démarre une nouvelle partie et initialise les paramètres.
     */
    public void startGame() {
        this.currentState = GameState.PLAYER_TURN;
        this.roundNumber = 1;
        this.totalPlayerShots = 0;
        this.totalPlayerHits = 0;
        this.totalEnemyShots = 0;
        this.totalEnemyHits = 0;
        this.shotsFiredThisTurn = 0;
        this.accuracyModifier = 0.0;
        this.fogActive = false;
        this.skipNextTurn = false;
    }

    /**
     * Traite un tir du joueur. En mode Salve, le tour ne change que si tous les tirs sont effectués.
     *
     * @param coord Cible du tir.
     * @return {@code true} si un navire est touché.
     */
    public boolean handlePlayerShot(Coordinate coord) {
        if (currentState != GameState.PLAYER_TURN) return false;

        if (skipNextTurn) {
            skipNextTurn = false;
            switchTurn();
            return false;
        }

        totalPlayerShots++;
        shotsFiredThisTurn++;

        boolean hit = enemyBoard.receiveFire(coord);

        if (hit && accuracyModifier < 0 && random.nextDouble() < Math.abs(accuracyModifier)) {
            hit = false;
        }

        achievementManager.trackHitStreak(hit);
        if (hit) totalPlayerHits++;

        if (enemyBoard.allShipsSunk()) {
            finalizeGame(true);
        } else if (shotsFiredThisTurn >= getShotsAllowed()) {
            switchTurn();
        }

        return hit;
    }

    /**
     * Exécute un tir de l'intelligence artificielle.
     *
     * @return La coordonnée visée.
     */
    public Coordinate aiTurn() {
        if (currentState != GameState.AI_TURN) return null;

        if (skipNextTurn) {
            skipNextTurn = false;
            switchTurn();
            return null;
        }

        Coordinate target = aiStrategy.chooseTarget(playerBoard);
        totalEnemyShots++;
        shotsFiredThisTurn++;

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
        } else if (shotsFiredThisTurn >= getShotsAllowed()) {
            switchTurn();
        }

        return target;
    }

    /**
     * Bascule le tour entre le joueur et l'IA et réinitialise le compteur de salve.
     */
    private void switchTurn() {
        shotsFiredThisTurn = 0;
        if (currentState == GameState.PLAYER_TURN) {
            currentState = GameState.AI_TURN;
        } else {
            currentState = GameState.PLAYER_TURN;
            nextRound();
        }
    }

    /**
     * Calcule le nombre de tirs autorisés pour le tour actuel.
     *
     * @return Nombre de tirs possibles.
     */
    public int getShotsAllowed() {
        if (!salveMode) return 1;
        if (currentState == GameState.PLAYER_TURN && boostedVolleyActive) return 5;

        Board activeBoard = (currentState == GameState.PLAYER_TURN) ? playerBoard : enemyBoard;
        return (int) activeBoard.getShips().stream().filter(s -> !s.isSunk()).count();
    }

    /**
     * Termine la partie, lève le brouillard et notifie les systèmes.
     */
    private void finalizeGame(boolean won) {
        currentState = GameState.GAME_OVER;
        setFogActive(false);

        if (salveMode && won) achievementManager.onWinSalve();

        achievementManager.onGameEnd(won, roundNumber, totalPlayerShots, totalPlayerHits, difficulty);
        int currentStreak = achievementManager.getCurrentStreak();

        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(_ -> FXGL.getEventBus().fireEvent(new GameOverEvent(
                won, totalPlayerShots, totalPlayerHits, currentStreak
        )));
        delay.play();
    }

    public void nextRound() {
        this.roundNumber++;
        this.eventManager.update();
    }

    public void setAccuracyModifier(double modifier) { this.accuracyModifier = modifier; }
    public void setFogActive(boolean active) {
        this.fogActive = active;
        playerBoard.setFogActive(active);
        enemyBoard.setFogActive(active);
    }

    public void setSkipNextTurn(boolean skip) { this.skipNextTurn = skip; }
    public void setSpecialAbilitiesBlocked(boolean blocked) { this.specialAbilitiesBlocked = blocked; }
    public void setBoostedVolleyActive(boolean active) { this.boostedVolleyActive = active; }

    public void triggerMeteorImpactsSilent(int count) {
        lastPlayerMeteors.clear();
        lastEnemyMeteors.clear();
        for (int i = 0; i < count; i++) {
            Coordinate pCoord = new Coordinate(random.nextInt(playerBoard.getWidth()), random.nextInt(playerBoard.getHeight()));
            Coordinate eCoord = new Coordinate(random.nextInt(enemyBoard.getWidth()), random.nextInt(enemyBoard.getHeight()));
            lastPlayerMeteors.add(pCoord);
            lastEnemyMeteors.add(eCoord);
            processMeteorStrike(playerBoard, pCoord);
            processMeteorStrike(enemyBoard, eCoord);
        }
    }

    private void processMeteorStrike(Board board, Coordinate coord) {
        board.markAsMeteorImpact(coord);
        boolean hit = board.receiveFire(coord, true);
        if (hit) {
            board.getShips().stream()
                    .filter(s -> s.isAt(coord) && s.isSunk())
                    .findFirst()
                    .ifPresent(_ -> achievementManager.onMeteorSunkShip());
        }
    }

    public int getRoundNumber() { return roundNumber; }
    public int getTotalPlayerShots() { return totalPlayerShots; }
    public int getTotalPlayerHits() { return totalPlayerHits; }
    public GameState getCurrentState() { return currentState; }
    public double getPlayerAccuracy() { return calculateAcc(totalPlayerShots, totalPlayerHits); }
    public int getTotalEnemyShots() { return totalEnemyShots; }
    public int getTotalEnemyHits() { return totalEnemyHits; }
    public double getEnemyAccuracy() { return calculateAcc(totalEnemyShots, totalEnemyHits); }
    public RandomEventManager getEventManager() { return eventManager; }
    public AchievementManager getAchievementManager() { return achievementManager; }
    public boolean areAbilitiesBlocked() { return specialAbilitiesBlocked; }
    public List<Coordinate> getLastPlayerMeteors() { return lastPlayerMeteors; }
    public List<Coordinate> getLastEnemyMeteors() { return lastEnemyMeteors; }
    public int getShotsFiredThisTurn() { return shotsFiredThisTurn; }

    public int getVolleyCount() {
        if (boostedVolleyActive) return 5;
        return (int) playerBoard.getShips().stream().filter(s -> !s.isSunk()).count();
    }

    private double calculateAcc(int s, int h) {
        return s == 0 ? 0.0 : (double) h / s * 100.0;
    }

    /**
     * Version sécurisée pour le ravitaillement.
    */
    public void rechargeSpecialAbilities() {
        if (salveMode) {
            System.out.println("Tentative de ravitaillement annulée : Mode Salve actif.");
            return;
        }
        System.out.println("Capacités spéciales rechargées par le ravitaillement !");
    }

    public boolean isSalveMode() { return salveMode; }
}