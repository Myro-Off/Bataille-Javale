package school.coda.adam_lucie_verena.bataillejavale.ui;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;
import school.coda.adam_lucie_verena.bataillejavale.core.engine.GameController;
import school.coda.adam_lucie_verena.bataillejavale.core.data.GameStatsDAO;
import school.coda.adam_lucie_verena.bataillejavale.core.data.PlayerDAO;
import school.coda.adam_lucie_verena.bataillejavale.core.events.GameOverEvent;
import school.coda.adam_lucie_verena.bataillejavale.core.model.*;

/**
 * Orchestrateur principal de l'application Bataille-Javale.
 * <p>
 * Cette classe pilote le moteur {@link GameApplication} et coordonne les transitions
 * entre la logique métier, l'interface utilisateur et la persistance PostgreSQL.
 * </p>
 */
public class DisplayGame extends GameApplication {

    // ------------------------------------------------------------------------------------------
    // CONSTANTES ET ATTRIBUTS
    // ------------------------------------------------------------------------------------------

    /** Taille d'une cellule de la grille en pixels. */
    private static final int CELL_SIZE = 40;

    private Board playerBoard;
    private Board enemyBoard;
    private GameConfig config;
    private GameController controller;
    private int currentPlayerId;

    private GameView playerView;
    private GameView enemyView;

    // ------------------------------------------------------------------------------------------
    // CONFIGURATION ET INITIALISATION
    // ------------------------------------------------------------------------------------------

    /**
     * Configure les réglages globaux du moteur FXGL (Fenêtre, Version, Scène de chargement).
     * @param gameSettings L'objet de configuration du moteur.
     */
    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(1000);
        gameSettings.setHeight(600);
        gameSettings.setTitle("Bataille-Javale-MaSalive");
        gameSettings.setVersion("0.1.5");
        gameSettings.setGameMenuEnabled(false);

        gameSettings.setSceneFactory(new SceneFactory() {
            @NotNull @Override
            public LoadingScene newLoadingScene() { return new CustomLoadingScene(); }
        });
    }

    /**
     * Prépare la logique de jeu et les connexions aux données.
     * <p>
     * Cette phase instancie les plateaux, positionne les navires et initialise
     * le {@link GameController}. Elle gère également l'abonnement au bus d'événements.
     * </p>
     */
    @Override
    protected void initGame() {
        config = GameConfig.createDefault();

        playerBoard = new Board(config.gridWidth(), config.gridHeight());
        enemyBoard = new Board(config.gridWidth(), config.gridHeight());
        playerBoard.placeShipsRandomly();
        enemyBoard.placeShipsRandomly();

        controller = new GameController(playerBoard, enemyBoard);

        // Identification en base de données
        this.currentPlayerId = new PlayerDAO().getOrCreatePlayer(config.player1Name());

        // Écouteur de fin de partie
        FXGL.getEventBus().addEventHandler(GameOverEvent.ANY, event -> handleGameOver(event.isVictory()));

        try { Thread.sleep(800); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    /**
     * Construit l'interface utilisateur graphique.
     */
    @Override
    protected void initUI() {
        FXGL.getGameScene().setCursor(javafx.scene.Cursor.DEFAULT);

        setupPlayerView();
        setupEnemyView();
        setupMainMenu();
    }

    // ------------------------------------------------------------------------------------------
    // MÉTHODES PRIVÉES : CONSTRUCTION DE L'UI
    // ------------------------------------------------------------------------------------------

    /**
     * Initialise et affiche le plateau de jeu du joueur.
     */
    private void setupPlayerView() {
        playerView = new GameView(playerBoard, config);
        playerView.setTranslateX(50);
        playerView.setTranslateY(100);
        playerView.revealShips();

        addLabel("MA FLOTTE", 50, 80, Color.WHITE);
        FXGL.addUINode(playerView);
    }

    /**
     * Initialise le radar ennemi et configure les interactions de tir.
     */
    private void setupEnemyView() {
        enemyView = new GameView(enemyBoard, config);
        enemyView.setTranslateX(550);
        enemyView.setTranslateY(100);

        enemyView.setOnMouseClicked(event -> handlePlayerClick(event.getX(), event.getY()));

        addLabel("RADAR ENNEMI", 550, 80, Color.CYAN);
        FXGL.addUINode(enemyView);
    }

    /**
     * Gère la superposition du menu principal au démarrage.
     */
    private void setupMainMenu() {
        MainMenuView menu = new MainMenuView(() ->
                FXGL.removeUINode(FXGL.getGameScene().getUINodes().getLast())
        );
        FXGL.addUINode(menu);
    }

    /**
     * Utilitaire pour ajouter un texte stylisé sur la scène.
     * * @param content Texte à afficher.
     * @param x       Position horizontale.
     * @param y       Position verticale.
     * @param color   Couleur du texte.
     */
    private void addLabel(String content, double x, double y, Color color) {
        Text label = new Text(content);
        label.setFill(color);
        label.setX(x);
        label.setY(y);
        FXGL.addUINode(label);
    }

    // ------------------------------------------------------------------------------------------
    // LOGIQUE ÉVÉNEMENTIELLE ET FIN DE PARTIE
    // ------------------------------------------------------------------------------------------

    /**
     * Déclenche la séquence de tir du joueur et la riposte automatique de l'IA.
     * @param mouseX Abscisse du clic souris.
     * @param mouseY Ordonnée du clic souris.
     */
    private void handlePlayerClick(double mouseX, double mouseY) {
        if (controller.getCurrentState() != GameState.PLAYER_TURN) return;

        int x = (int) (mouseX / CELL_SIZE);
        int y = (int) (mouseY / CELL_SIZE);
        Coordinate target = new Coordinate(x, y);

        if (enemyBoard.isWithinBounds(target)) return;

        boolean wasAlreadyShot = enemyBoard.getHitShots().contains(target) ||
                enemyBoard.getMissedShots().contains(target);

        if (!wasAlreadyShot) {
            controller.handlePlayerShot(target);
            enemyView.updateDisplay();

            if (enemyBoard.allShipsSunk()) {
                FXGL.getEventBus().fireEvent(new GameOverEvent(true));
                return;
            }

            controller.aiTurn();
            playerView.updateDisplay();

            if (playerBoard.allShipsSunk()) {
                FXGL.getEventBus().fireEvent(new GameOverEvent(false));
            }
        }
    }

    /**
     * Gère la transition finale : calcul des statistiques, sauvegarde DB et affichage du résultat.
     * @param isVictory Résultat de la partie (Vrai pour une victoire).
     */
    private void handleGameOver(boolean isVictory) {
        int shots = enemyBoard.getHitShots().size() + enemyBoard.getMissedShots().size();
        int hits = enemyBoard.getHitShots().size();

        new GameStatsDAO().saveGameResult(currentPlayerId, isVictory ? "WIN" : "LOSS", shots, hits);

        FXGL.addUINode(new GameOverView(isVictory, shots, hits, () -> FXGL.getGameController().startNewGame()));
    }

    /**
     * Point d'entrée principal de l'application.
     * @param args Arguments système.
     */
    static void main(String[] args) { launch(args); }
}