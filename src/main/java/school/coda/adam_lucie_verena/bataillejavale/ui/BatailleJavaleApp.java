package school.coda.adam_lucie_verena.bataillejavale.ui;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import school.coda.adam_lucie_verena.bataillejavale.core.data.DatabaseManager;
import school.coda.adam_lucie_verena.bataillejavale.core.engine.GameController;
import school.coda.adam_lucie_verena.bataillejavale.core.data.PlayerDAO;
import school.coda.adam_lucie_verena.bataillejavale.core.events.GameOverEvent;
import school.coda.adam_lucie_verena.bataillejavale.core.model.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe principale de l'application Bataille-Javale.
 * <p>
 * Cette classe orchestre le cycle de vie du jeu en utilisant le moteur FXGL.
 * Elle gère les transitions entre les différentes vues (Menu, Placement, Combat)
 * et fait le lien entre la logique métier (Controller) et le rendu visuel.
 * </p>
 */
public class BatailleJavaleApp extends GameApplication {

    private Board playerBoard;
    private Board enemyBoard;
    private GameConfig config;
    private GameController controller;
    private GameView playerView;
    private GameView enemyView;
    private CombatView combatView;
    private Node currentUI;

    /**
     * Configuration globale des paramètres du moteur de jeu.
     * Définit la taille de la fenêtre, le titre et la scène de chargement personnalisée.
     */
    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(1500);
        gameSettings.setHeight(1000);
        gameSettings.setTitle("Bataille-Javale : Tactical Command");
        gameSettings.setVersion("0.3.0");
        gameSettings.setGameMenuEnabled(false);
        gameSettings.setSceneFactory(new SceneFactory() {
            @NotNull @Override
            public LoadingScene newLoadingScene() { return new CustomLoadingScene(); }
        });
    }

    /**
     * Initialisation de la logique de jeu.
     * Prépare la configuration, vérifie la connexion à la base de données
     * et enregistre les écouteurs d'événements globaux (comme la fin de partie).
     */
    @Override
    protected void initGame() {
        config = GameConfig.createDefault();
        DatabaseManager.testConnection();
        new PlayerDAO().getOrCreatePlayer(config.player1Name());
        FXGL.getEventBus().addEventHandler(GameOverEvent.ANY, event -> handleGameOver(event.isVictory()));
    }

    /**
     * Initialisation de l'interface utilisateur initiale.
     * Configure le curseur et lance l'affichage du menu principal.
     */
    @Override
    protected void initUI() {
        FXGL.getGameScene().setCursor(Cursor.DEFAULT);
        setupMainMenu();
    }

    /**
     * Méthode utilitaire pour remplacer l'interface actuelle par une nouvelle.
     * Assure que l'ajout/suppression des nœuds se fait sur le thread JavaFX.
     * @param newUI Le nouveau composant graphique à afficher.
     */
    private void switchUI(Node newUI) {
        Platform.runLater(() -> {
            if (currentUI != null) FXGL.removeUINode(currentUI);
            currentUI = newUI;
            if (currentUI != null) FXGL.addUINode(currentUI);
        });
    }

    /** Affiche l'écran du menu principal. */
    private void setupMainMenu() { switchUI(new MainMenuView(this::startSoloFlow)); }

    /** Affiche l'écran de configuration avant le placement. */
    private void startSoloFlow() { switchUI(new ConfigView(this::startPlacementFlow)); }

    /**
     * Initialise le plateau du joueur et affiche la vue de placement des navires.
     */
    private void startPlacementFlow() {
        playerBoard = new Board(config.gridWidth(), config.gridHeight());
        playerView = new GameView(playerBoard, true);
        switchUI(new PlacementView(playerView, playerBoard, this::startGameplay));
    }

    /**
     * Lance la phase de combat.
     * Initialise le plateau ennemi, le contrôleur de jeu et l'interface de combat (CombatView).
     */
    private void startGameplay() {
        enemyBoard = new Board(config.gridWidth(), config.gridHeight());
        enemyBoard.placeShipsRandomly();
        controller = new GameController(playerBoard, enemyBoard);

        playerView = new GameView(playerBoard, true);
        enemyView = new GameView(enemyBoard, false);
        enemyView.setOnMouseClicked(e -> handlePlayerShot(e.getX(), e.getY()));

        combatView = new CombatView(playerView, enemyView, new GameLogView(), new FleetStatusView(enemyBoard));
        combatView.prefWidthProperty().bind(FXGL.getGameScene().getRoot().widthProperty());
        combatView.prefHeightProperty().bind(FXGL.getGameScene().getRoot().heightProperty());

        switchUI(combatView);
        controller.startGame();
        combatView.updateTurnInfo(true);
    }

    /**
     * Gère l'action de tir déclenchée par le joueur.
     * @param localX Coordonnée X locale du clic sur la GameView.
     * @param localY Coordonnée Y locale du clic sur la GameView.
     */
    private void handlePlayerShot(double localX, double localY) {
        if (controller.getCurrentState() != GameState.PLAYER_TURN) return;

        Coordinate target = enemyView.getGridCoordinate(localX, localY);

        if (enemyBoard.isNotWithinBounds(target) || enemyBoard.isAlreadyShot(target)) return;

        List<Ship> alreadySunk = enemyBoard.getShips().stream().filter(Ship::isSunk).collect(Collectors.toList());
        boolean isHit = controller.handlePlayerShot(target);

        if (isHit) {
            combatView.getGameLog().addLog("Touché en " + target.x() + ":" + target.y(), Color.ORANGE);
        } else {
            combatView.getGameLog().addLog("Manqué en " + target.x() + ":" + target.y(), Color.LIGHTBLUE);
        }

        enemyView.updateDisplay();
        VfxManager.playShotEffect(enemyView, target, isHit);
        checkSunkFeedback(enemyBoard, alreadySunk, "ENNEMI");

        if (enemyBoard.allShipsSunk()) {
            handleGameOver(true);
        } else {
            combatView.updateTurnInfo(false);
            triggerAIReprisal();
        }
    }

    /**
     * Déclenche la riposte de l'Intelligence Artificielle après un court délai.
     * Ce délai permet au joueur d'observer le résultat de son propre tir.
     */
    private void triggerAIReprisal() {
        combatView.getGameLog().addLog("L'ennemi analyse les radars...", Color.WHITE);
        PauseTransition pause = new PauseTransition(Duration.millis(800));
        pause.setOnFinished(_ -> {
            List<Ship> alreadySunk = playerBoard.getShips().stream().filter(Ship::isSunk).collect(Collectors.toList());
            controller.aiTurn();

            Coordinate lastAiTarget = playerBoard.getLastShotCoordinate();
            boolean isHit = playerBoard.getHitShots().contains(lastAiTarget);

            if (isHit) {
                combatView.getGameLog().addLog("L'IA vous a TOUCHE en " + lastAiTarget.x() + ":" + lastAiTarget.y(), Color.RED);
            } else {
                combatView.getGameLog().addLog("L'IA a MANQUE son tir.", Color.GRAY);
            }

            playerView.updateDisplay();
            VfxManager.playShotEffect(playerView, lastAiTarget, isHit);
            checkSunkFeedback(playerBoard, alreadySunk, "ALLIÉ");

            if (playerBoard.allShipsSunk()) {
                handleGameOver(false);
            } else {
                combatView.updateTurnInfo(true);
                combatView.getFleetStatus().update();
            }
        });
        pause.play();
    }

    /**
     * Vérifie si un navire a été coulé lors du dernier tir et ajoute un log si nécessaire.
     * * @param board Le plateau concerné.
     * @param previouslySunk Liste des navires qui étaient déjà coulés avant le tir.
     * @param team Nom de l'équipe (pour l'affichage du log).
     */
    private void checkSunkFeedback(Board board, List<Ship> previouslySunk, String team) {
        for (Ship s : board.getShips()) {
            if (s.isSunk() && !previouslySunk.contains(s)) {
                combatView.getGameLog().addLog("COULÉ : " + s.getType().getName() + " (" + team + ")", Color.RED);
            }
        }
    }

    /**
     * Gère la transition vers l'écran de fin de partie.
     * @param isVictory True si le joueur a gagné, false sinon.
     */
    private void handleGameOver(boolean isVictory) {
        Platform.runLater(() -> switchUI(new GameOverView(isVictory, 0, 0, this::setupMainMenu)));
    }

    /**
     * Point d'entrée standard de l'application Java.
     * @param args Arguments de la ligne de commande.
     */
    static void main(String[] args) { launch(args); }
}