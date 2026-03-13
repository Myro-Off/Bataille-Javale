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
import school.coda.adam_lucie_verena.bataillejavale.gui.component.FleetStatusView;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.GameLogView;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.NotificationView;
import school.coda.adam_lucie_verena.bataillejavale.gui.grid.GameView;
import school.coda.adam_lucie_verena.bataillejavale.gui.scene.*;
/**
 * Orchestrateur principal de l'application Bataille-Javale.
 * Gère le cycle de vie global et les transitions entre les différentes phases du jeu.
 */
public class BatailleJavaleApp extends GameApplication {
    private Board playerBoard;
    private Board enemyBoard;
    private GameConfig config;
    private BattleEngine engine;
    private CombatOrchestrator battleManager;
    private GameView playerView;
    private GameView enemyView;
    private CombatView combatView;
    private Node currentUI;
    private AchievementManager achievementManager = new AchievementManager();
    private NotificationView notificationView;
    /**
     * Configure les paramètres techniques du moteur FXGL.
     */
    @Override
    protected void initSettings(GameSettings gameSettings) {
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
     * Initialise la configuration par défaut et les écouteurs d'événements globaux.
     */
    @Override
    protected void initGame() {
        config = GameConfig.createDefault();
        DatabaseManager.testConnection();
        new PlayerDAO().getOrCreatePlayer(config.player1Name());
        FXGL.getEventBus().addEventHandler(GameOverEvent.ANY, event -> Platform.runLater(() -> switchUI(new GameOverView(event.isVictory(), event.getTotalShots(), event.getTotalHits(), this::startPlacementFlow, this::setupMainMenu))));
    }
    /**
     * Prépare l'affichage initial de l'application.
     */
    @Override
    protected void initUI() {
        FXGL.getGameScene().setCursor(Cursor.DEFAULT);
        setupMainMenu();
        notificationView = new NotificationView();
        FXGL.addUINode(notificationView);
        achievementManager.setNotificationView(notificationView);
    }
    /**
     * Remplace le composant graphique actuellement affiché par un nouveau.
     * @param newUI Le nouveau nœud à intégrer à la scène.
     */
    private void switchUI(Node newUI) {
        Platform.runLater(() -> {
            if (currentUI != null) FXGL.removeUINode(currentUI);
            currentUI = newUI;
            if (currentUI != null) FXGL.addUINode(currentUI);
        });
    }
    /**
     * Affiche l'écran du menu principal.
     */
    private void setupMainMenu() {
        switchUI(new MainMenuView(this::startSoloFlow));
    }
    /**
     * Démarre la phase de configuration de la partie.
     */
    private void startSoloFlow() {
        switchUI(new ConfigView(this.config, newConfig -> {
            this.config = newConfig;
            startPlacementFlow();
        }, this::setupMainMenu));
    }
    /**
     * Initialise le plateau du joueur et lance la phase de placement tactique.
     */
    private void startPlacementFlow() {
        playerBoard = new Board(config.gridWidth(), config.gridHeight());
        playerView = new GameView(playerBoard, true);
        switchUI(new PlacementView(playerView, playerBoard, config.shipCounts(), this::startSoloFlow, this::startGameplay));
    }
    /**
     * Configure et lance la phase de combat en initialisant l'orchestrateur de bataille.
     */
    private void startGameplay() {
        enemyBoard = new Board(config.gridWidth(), config.gridHeight());
        enemyBoard.placeShipsRandomly(config.shipCounts());
        engine = new BattleEngine(playerBoard, enemyBoard, config.difficulty(), achievementManager);
        playerView = new GameView(playerBoard, true);
        enemyView = new GameView(enemyBoard, false);
        GameLogView log = new GameLogView();
        FleetStatusView playerFleet = new FleetStatusView(playerBoard, "MA FLOTTE", Theme.CYAN);
        FleetStatusView enemyFleet = new FleetStatusView(enemyBoard, "SUIVI FLOTTE ENNEMIE", Theme.RED_ALERTE);
        combatView = new CombatView(playerView, enemyView, log, playerFleet, enemyFleet);
        battleManager = new CombatOrchestrator(engine, playerBoard, enemyBoard, playerView, enemyView, combatView);
        enemyView.setOnMouseClicked(e -> {
            Coordinate target = enemyView.getGridCoordinate(e.getX(), e.getY());
            battleManager.handlePlayerShot(target);
        });
        switchUI(combatView);
        engine.startGame();
        combatView.updateTurnInfo(true);
    }
    /**
     * Point d'entrée de la machine virtuelle Java.
     * @param args Arguments de la ligne de commande.
     */
    static void main(String[] args) { launch(args); }
}