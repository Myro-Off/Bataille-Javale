package school.coda.adam_lucie_verena.bataillejavale;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import org.jetbrains.annotations.NotNull;
import school.coda.adam_lucie_verena.bataillejavale.core.achievement.AchievementManager;
import school.coda.adam_lucie_verena.bataillejavale.core.data.DatabaseManager;
import school.coda.adam_lucie_verena.bataillejavale.controller.CombatOrchestrator;
import school.coda.adam_lucie_verena.bataillejavale.core.engine.BattleEngine;
import school.coda.adam_lucie_verena.bataillejavale.core.data.PlayerDAO;
import school.coda.adam_lucie_verena.bataillejavale.core.events.GameOverEvent;
import school.coda.adam_lucie_verena.bataillejavale.core.model.*;
import school.coda.adam_lucie_verena.bataillejavale.gui.Theme;
import school.coda.adam_lucie_verena.bataillejavale.gui.AssetsManager;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.AudioControlOverlay;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.FleetStatusView;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.GameLogView;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.NotificationView;
import school.coda.adam_lucie_verena.bataillejavale.gui.grid.GameView;
import school.coda.adam_lucie_verena.bataillejavale.gui.scene.*;

/**
 * Classe principale pilotant le cycle de vie de la Bataille-Javale.
 * Gère l'initialisation du moteur FXGL, la navigation entre les vues et l'orchestration des combats.
 */
public class BatailleJavaleApp extends GameApplication {
    private Board playerBoard;
    private GameConfig config;
    private CombatOrchestrator battleManager;
    private GameView playerView;
    private GameView enemyView;
    private Node currentUI;
    private final AchievementManager achievementManager = new AchievementManager();
    private NotificationView notificationView;
    private AudioControlOverlay audioOverlay;

    /**
     * Définit les paramètres globaux du moteur de jeu (fenêtrage, titre, version).
     */
    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setFullScreenAllowed(true);
        gameSettings.setManualResizeEnabled(false);

        gameSettings.setWidth(1800);
        gameSettings.setHeight(1080);
        gameSettings.setTitle("Bataille-Javale Ma Salive");
        gameSettings.setVersion("0.5.0");

        gameSettings.setGameMenuEnabled(false);
        gameSettings.setSceneFactory(new SceneFactory() {
            @NotNull @Override
            public LoadingScene newLoadingScene() { return new CustomLoadingScene(); }
        });
    }

    /**
     * Initialise la logique métier au démarrage.
     */
    @Override
    protected void initGame() {
        if (FXGL.getSettings().isFullScreenAllowed()) {
            Platform.runLater(() -> FXGL.getPrimaryStage().setFullScreen(true));
        }
        try {
            AssetsManager.playMusic("mainmenu.mp3", 0.2);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        config = GameConfig.createDefault();
        DatabaseManager.testConnection();
        new PlayerDAO().getOrCreatePlayer(config.player1Name());

        // Fusion : Utilisation du nouveau constructeur GameOverView avec la série (streak)
        FXGL.getEventBus().addEventHandler(GameOverEvent.ANY, event -> Platform.runLater(() ->
                switchUI(new GameOverView(
                        event.isVictory(),
                        event.getTotalShots(),
                        event.getTotalHits(),
                        event.getStreak(),
                        this::startPlacementFlow,
                        this::setupMainMenu
                ))
        ));
    }

    @Override
    protected void initUI() {
        FXGL.getGameScene().setCursor(Cursor.DEFAULT);
        setupMainMenu();
        notificationView = new NotificationView();
        audioOverlay = new AudioControlOverlay();
        FXGL.addUINode(notificationView);
        FXGL.addUINode(audioOverlay);
        audioOverlay.setPrefWidth(FXGL.getAppWidth());
        achievementManager.setNotificationView(notificationView);
    }

    private void switchUI(Node newUI) {
        Platform.runLater(() -> {
            if (currentUI != null) FXGL.removeUINode(currentUI);
            currentUI = newUI;
            if (currentUI != null) FXGL.addUINode(currentUI);
            if (audioOverlay != null) audioOverlay.toFront();
            if (notificationView != null) notificationView.toFront();
        });
    }

    private void setupMainMenu() {
        switchUI(new MainMenuView(this::startSoloFlow, this::showAchievementMenu));
    }

    private void showAchievementMenu() {
        switchUI(new AchievementMenuView(achievementManager, this::setupMainMenu));
    }

    private void startSoloFlow() {
        switchUI(new ConfigView(this.config, newConfig -> {
            this.config = newConfig;
            startPlacementFlow();
        }, this::setupMainMenu));
    }

    private void startPlacementFlow() {
        playerBoard = new Board(config.gridWidth(), config.gridHeight());
        playerView = new GameView(playerBoard, true);
        switchUI(new PlacementView(playerView, playerBoard, config.shipCounts(), this::startSoloFlow, this::startGameplay));
    }

    private void startGameplay() {
        Board enemyBoard = new Board(config.gridWidth(), config.gridHeight());
        enemyBoard.placeShipsRandomly(config.shipCounts());

        BattleEngine engine = new BattleEngine(playerBoard, enemyBoard, config.difficulty(), achievementManager);

        playerView = new GameView(playerBoard, true);
        enemyView = new GameView(enemyBoard, false);

        GameLogView log = new GameLogView();
        FleetStatusView playerFleet = new FleetStatusView(playerBoard, "MA FLOTTE", Theme.CYAN);
        FleetStatusView enemyFleet = new FleetStatusView(enemyBoard, "SUIVI FLOTTE ENNEMIE", Theme.RED_ALERTE);

        CombatView combatView = new CombatView(playerView, enemyView, log, playerFleet, enemyFleet);
        battleManager = new CombatOrchestrator(engine, playerBoard, enemyBoard, playerView, enemyView, combatView);

        enemyView.setOnMouseClicked(e -> {
            Coordinate target = enemyView.getGridCoordinate(e.getX(), e.getY());
            battleManager.handlePlayerShot(target);
        });

        switchUI(combatView);
        engine.startGame();
        AssetsManager.playMusic("game.mp3", 0.4);
        combatView.updateTurnInfo(true);
    }

    /**
     * Point d'entrée de la machine virtuelle Java.
     */
    public static void main(String[] args) { launch(args); }
}