package school.coda.adam_lucie_verena.bataillejavale;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
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
import school.coda.adam_lucie_verena.bataillejavale.gui.component.SettingsControlOverlay;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.FleetStatusView;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.GameLogView;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.NotificationView;
import school.coda.adam_lucie_verena.bataillejavale.gui.grid.GameView;
import school.coda.adam_lucie_verena.bataillejavale.gui.scene.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrateur principal de l'application Bataille-Javale.
 * <p>
 * Gère le cycle de vie du jeu, la persistance de la session (Rejouer avec la même config)
 * et la navigation entre les différentes scènes tactiques.
 * </p>
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
    private SettingsControlOverlay settingsOverlay;

    /** Séquence Konami pour le succès secret. */
    private final List<KeyCode> konamiSequence = List.of(
            KeyCode.UP, KeyCode.UP, KeyCode.DOWN, KeyCode.DOWN,
            KeyCode.LEFT, KeyCode.RIGHT, KeyCode.LEFT, KeyCode.RIGHT,
            KeyCode.B, KeyCode.A
    );

    private final List<KeyCode> inputHistory = new ArrayList<>();

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setFullScreenAllowed(true);
        gameSettings.setManualResizeEnabled(false);
        gameSettings.setWidth(1800);
        gameSettings.setHeight(1080);
        gameSettings.setTitle("Bataille-Javale : Ma Salive");
        gameSettings.setVersion("0.6.0");
        gameSettings.setGameMenuEnabled(false);
        gameSettings.setSceneFactory(new SceneFactory() {
            @NotNull @Override
            public LoadingScene newLoadingScene() { return new CustomLoadingScene(); }
        });
    }

    @Override
    protected void initGame() {
        if (FXGL.getSettings().isFullScreenAllowed()) {
            Platform.runLater(() -> FXGL.getPrimaryStage().setFullScreen(true));
        }

        try {
            // 💡 "mainmenu.mp3" pourrait être une constante ou une enum
            // 0.2 pourrait être une constante ex. DEFAULT_MAIN_MENU_VOLUME
            AssetsManager.playMusic("mainmenu.mp3", 0.2);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        config = GameConfig.createDefault();

        // 🚨 Code "au cas où" (pas encore utilisé)
        DatabaseManager.testConnection();
        new PlayerDAO().getOrCreatePlayer(config.player1Name());

        // 💡 Utilisation du Design-pattern Observateur (sous la forme d'un Event Bus)
        // Voir https://refactoring.guru/fr/design-patterns/observer
        FXGL.getEventBus().addEventHandler(GameOverEvent.ANY, event -> Platform.runLater(() -> {
            if (notificationView != null) notificationView.clearEvents();

            switchUI(new GameOverView(
                    // 💡 on pourrait passer un GameOverEvent directement au lieu de
                    // event.isVictory(), event.getTotalShots(), event.getTotalHits(),event.getStreak(),
                    event.isVictory(),
                    event.getTotalShots(),
                    event.getTotalHits(),
                    event.getStreak(),
                    this::startPlacementFlow,
                    this::setupMainMenu
            ));
        }));
    }

    @Override
    protected void initUI() {
        FXGL.getGameScene().setCursor(Cursor.DEFAULT);
        setupMainMenu();

        notificationView = new NotificationView();
        settingsOverlay = new SettingsControlOverlay();

        FXGL.addUINode(notificationView);
        FXGL.addUINode(settingsOverlay);

        settingsOverlay.setPrefWidth(FXGL.getAppWidth());
        // 💡 On peut éviter de passer la notificationView dans le achievementManager
        // grâce au bus d'événement
        achievementManager.setNotificationView(notificationView);

        // 💡 La gestion des notifications lorsqu'un achievement est dévérouillé peut se faire ici.
        // FXGL.getEventBus().addEventHandler(AchievementUnlockedEvent.ANY, event -> Platform.runLater(() -> {
        //     notificationView.showAchievement(event.type);
        // }));
    }

    @Override
    protected void initInput() {
        FXGL.getInput().addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            inputHistory.add(e.getCode());
            if (inputHistory.size() > konamiSequence.size()) {
                inputHistory.removeFirst();
            }
            if (inputHistory.equals(konamiSequence)) {
                achievementManager.unlockKonamiCode();
                AssetsManager.playSFX("secret.wav", 1.0);
                inputHistory.clear();
            }
        });
    }

    /**
     * Permute l'interface active.
     * @param newUI Le nouveau composant à afficher.
     */
    private void switchUI(Node newUI) {
        Platform.runLater(() -> {
            if (currentUI != null) FXGL.removeUINode(currentUI);
            currentUI = newUI;
            if (currentUI != null) FXGL.addUINode(currentUI);

            // Overlays toujours au premier plan
            if (settingsOverlay != null) settingsOverlay.toFront();
            if (notificationView != null) notificationView.toFront();
        });
    }

    private void setupMainMenu() {
        switchUI(new MainMenuView(this::startSoloFlow, this::showAchievementMenu));
    }

    private void showAchievementMenu() {
        switchUI(new AchievementMenuView(achievementManager, this::setupMainMenu));
    }

    /**
     * Lance le flux de configuration.
     * En cas de validation, l'objet 'config' de la classe est mis à jour.
     */
    private void startSoloFlow() {
        switchUI(new ConfigView(this.config, newConfig -> {
            this.config = newConfig; // Sauvegarde des nouveaux réglages
            startPlacementFlow();
        }, this::setupMainMenu));
    }

    /**
     * Phase de placement. Réutilise 'config' (soit par défaut, soit modifiée, soit rejouée).
     */
    private void startPlacementFlow() {
        playerBoard = new Board(config.gridWidth(), config.gridHeight());
        playerView = new GameView(playerBoard, true);
        switchUI(new PlacementView(playerView, playerBoard, config.shipCounts(), this::startSoloFlow, this::startGameplay));
    }

    /**
     * Lance la bataille.
     */
    private void startGameplay() {
        Board enemyBoard = new Board(config.gridWidth(), config.gridHeight());
        enemyBoard.placeShipsRandomly(config.shipCounts());

        // Passage de l'objet config complet au moteur
        BattleEngine engine = new BattleEngine(playerBoard, enemyBoard, config, achievementManager);

        // 👍 Bonne réutilisation de code : GameView
        playerView = new GameView(playerBoard, true);
        enemyView = new GameView(enemyBoard, false);

        GameLogView log = new GameLogView();
        // 👍 Bonne réutilisation de code : FleetStatusView
        FleetStatusView playerFleet = new FleetStatusView(playerBoard, "MA FLOTTE", Theme.CYAN);
        FleetStatusView enemyFleet = new FleetStatusView(enemyBoard, "SUIVI FLOTTE ENNEMIE", Theme.RED_ALERTE);

        CombatView combatView = new CombatView(playerView, enemyView, log, playerFleet, enemyFleet, config);

        battleManager = new CombatOrchestrator(engine, playerBoard, enemyBoard, playerView, enemyView, combatView, notificationView);

        enemyView.setOnMouseClicked(e -> {
            Coordinate target = enemyView.getGridCoordinate(e.getX(), e.getY());
            battleManager.handlePlayerShot(target);
            // 💡 Si enemyView.getGridCoordinate(...) retourn un Optional<Coordinate>
            // Pourrait s'écrire ainsi :
            // enemyView.getGridCoordinate(e.getX(), e.getY())
            //          .ifPresent(battleManager::handlePlayerShot);
        });

        switchUI(combatView);
        engine.startGame();

        AssetsManager.playMusic("game.mp3", 0.4);
        combatView.updateTurnInfo(true);
    }

    static void main(String[] args) { launch(args); }
}