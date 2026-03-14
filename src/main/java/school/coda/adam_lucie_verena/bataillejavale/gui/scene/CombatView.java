package school.coda.adam_lucie_verena.bataillejavale.gui.scene;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import school.coda.adam_lucie_verena.bataillejavale.core.model.GameConfig;
import school.coda.adam_lucie_verena.bataillejavale.gui.Theme;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.*;
import school.coda.adam_lucie_verena.bataillejavale.gui.grid.GameView;
import school.coda.adam_lucie_verena.bataillejavale.gui.vfx.VfxManager;

/**
 * Vue principale orchestrant l'interface graphique du combat naval.
 * <p>Cette classe gère la disposition spatiale des grilles (joueur et ennemi),
 * le journal de combat, ainsi que la couche d'effets visuels (VFX). Elle adapte
 * dynamiquement son contenu selon le mode de jeu : en mode 'Salve', les composants
 * liés aux capacités spéciales sont totalement omis de l'affichage.</p>
 * <p>L'alignement est fixé sur {@code TOP_LEFT} pour garantir une superposition
 * parfaite de l'overlay VFX avec les coordonnées de la scène FXGL.</p>
 */
public class CombatView extends StackPane {

    private final GameLogView gameLog;
    private final FleetStatusView playerFleet, enemyFleet;
    private final GameView playerGrid, enemyGrid;
    private final GameConfig config;

    private final Pane vfxOverlay = new Pane();
    private final BorderPane uiLayout = new BorderPane();
    private final VBox notificationLayer = new VBox();

    private final Text playerStats = new Text();
    private final Text enemyStats = new Text();
    private final Text roundLabel = new Text();
    private final VBox turnIndicator = new VBox(5);

    /**
     * Initialise la scène de combat et configure les couches d'interface.
     * * @param pView  Instance de la vue de la grille du joueur.
     * @param eView  Instance de la vue de la grille ennemie (radar).
     * @param log    Journal des événements de combat.
     * @param pFleet Composant affichant l'état des navires alliés.
     * @param eFleet Composant affichant l'état des navires détectés chez l'ennemi.
     * @param config Configuration actuelle utilisée pour filtrer les éléments d'interface.
     */
    public CombatView(GameView pView, GameView eView, GameLogView log,
                      FleetStatusView pFleet, FleetStatusView eFleet, GameConfig config) {
        this.playerGrid = pView;
        this.enemyGrid = eView;
        this.gameLog = log;
        this.playerFleet = pFleet;
        this.enemyFleet = eFleet;
        this.config = config;

        this.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        this.setAlignment(Pos.TOP_LEFT);

        // Configuration de la couche VFX (transparente aux clics)
        vfxOverlay.setMouseTransparent(true);
        vfxOverlay.setPickOnBounds(false);
        vfxOverlay.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        VfxManager.setExternalOverlay(vfxOverlay);

        setupLayout();
        this.getChildren().addAll(uiLayout, vfxOverlay);
    }

    /**
     * Organise les différents panneaux de l'interface utilisateur.
     * Applique le mode "furtif" (masquage des capacités) si le mode Salve est actif.
     */
    private void setupLayout() {
        uiLayout.setStyle(Theme.MAIN_GRADIENT);
        uiLayout.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());

        // Zone Supérieure : Indicateur de tour et Manche
        turnIndicator.setAlignment(Pos.CENTER);
        turnIndicator.setPadding(new Insets(20, 0, 10, 0));
        roundLabel.setFill(Color.GOLD);
        roundLabel.setFont(Theme.font(18, FontWeight.BOLD));
        uiLayout.setTop(turnIndicator);

        // Sidebar Gauche : Infos Joueur et Capacités (Conditionnel)
        VBox left = new VBox(25);
        left.setPadding(new Insets(20));
        left.setPrefWidth(280);
        playerStats.setFill(Theme.CYAN);
        playerStats.setFont(Theme.mono(12, FontWeight.BOLD));
        left.getChildren().addAll(playerFleet, playerStats);

        if (!config.isSalveMode()) {
            left.getChildren().add(new SpecialAbilitiesView());
        }
        uiLayout.setLeft(left);

        // Zone Centrale : Les deux champs de bataille
        HBox center = new HBox(60);
        center.setAlignment(Pos.CENTER);
        enemyGrid.autoScale(FXGL.getAppWidth() * 0.45, FXGL.getAppHeight() * 0.7);
        playerGrid.autoScale(FXGL.getAppWidth() * 0.22, FXGL.getAppHeight() * 0.38);
        center.getChildren().addAll(createGridBox("RADAR TACTIQUE ENNEMI", enemyGrid),
                createGridBox("ÉTAT-MAJOR : MA FLOTTE", playerGrid));
        uiLayout.setCenter(center);

        // Sidebar Droite : Stats Ennemies, Notifications et Logs
        VBox right = new VBox(15);
        right.setPadding(new Insets(20));
        right.setPrefWidth(320);
        enemyStats.setFill(Theme.RED_ALERTE);
        enemyStats.setFont(Theme.mono(12, FontWeight.BOLD));
        notificationLayer.setPrefHeight(40);
        notificationLayer.setAlignment(Pos.CENTER);
        right.getChildren().addAll(enemyFleet, enemyStats, notificationLayer, gameLog);
        uiLayout.setRight(right);
    }

    /**
     * Encapsule une grille de jeu avec son titre dans un conteneur stylisé.
     * * @param title Label affiché au-dessus de la grille.
     * @param grid  L'objet GameView à afficher.
     * @return Une VBox centrée contenant le titre et la grille.
     */
    private VBox createGridBox(String title, GameView grid) {
        Text t = new Text(title);
        t.setFill(Color.WHITE);
        t.setFont(Theme.font(14, FontWeight.BOLD));
        Group g = new Group(grid);
        VBox b = new VBox(15, t, g);
        b.setAlignment(Pos.CENTER);
        b.setFillWidth(false);
        return b;
    }

    /**
     * Déclenche une notification visuelle temporaire dans le HUD.
     * * @param msg  Message à afficher.
     * @param c    Couleur thématique de l'alerte.
     * @param crit Si vrai, applique une mise en évidence critique (taille augmentée).
     */
    public void showCombatNotification(String msg, Color c, boolean crit) {
        Text t = new Text(msg.toUpperCase());
        t.setFill(c);
        t.setFont(Theme.font(crit ? 15 : 12, FontWeight.BOLD));
        notificationLayer.getChildren().setAll(t);

        FadeTransition ft = new FadeTransition(Duration.millis(300), t);
        ft.setFromValue(0); ft.setToValue(1); ft.play();

        PauseTransition p = new PauseTransition(Duration.seconds(2.5));
        p.setOnFinished(_ -> {
            FadeTransition out = new FadeTransition(Duration.millis(500), t);
            out.setToValue(0);
            out.setOnFinished(_ -> notificationLayer.getChildren().remove(t));
            out.play();
        });
        p.play();
    }

    /**
     * Met à jour l'indicateur de tour principal avec un effet de lueur néon.
     * * @param isPlayer Définit si le message doit être "VOTRE TOUR" ou "TOUR ENNEMI".
     */
    public void updateTurnInfo(boolean isPlayer) {
        turnIndicator.getChildren().clear();
        Text t = new Text(isPlayer ? "VOTRE TOUR" : "TOUR ENNEMI");
        t.setFill(isPlayer ? Theme.CYAN : Theme.RED_ALERTE);
        t.setFont(Theme.font(52, FontWeight.BOLD));
        t.setEffect(isPlayer ? Theme.GLOW_LARGE_CYAN : Theme.GLOW_LARGE_RED);
        turnIndicator.getChildren().addAll(t, roundLabel);
    }

    /**
     * Bascule l'interface dans le mode visuel 'Apocalypse'.
     * Ajoute une alerte clignotante et modifie le fond d'écran vers un dégradé radial sombre.
     */
    public void activateApocalypseTheme() {
        Text alert = new Text("☢ APOCALYPSE : PLUIE CRITIQUE EN COURS ☢");
        alert.setFill(Color.web("#ff4757"));
        alert.setFont(Theme.font(28, FontWeight.BLACK));
        alert.setEffect(new javafx.scene.effect.Glow(0.8));
        alert.setTranslateY(50);

        Timeline blink = new Timeline(
                new KeyFrame(Duration.seconds(0.5), new KeyValue(alert.opacityProperty(), 0.2)),
                new KeyFrame(Duration.seconds(1), new KeyValue(alert.opacityProperty(), 1.0))
        );
        blink.setCycleCount(Animation.INDEFINITE);
        blink.setAutoReverse(true);
        blink.play();

        Platform.runLater(() -> {
            StackPane.setAlignment(alert, Pos.TOP_CENTER);
            StackPane.setMargin(alert, new Insets(80, 0, 0, 0));
            if (!this.getChildren().contains(alert)) {
                this.getChildren().add(alert);
            }
        });

        this.setStyle(this.getStyle() + "-fx-background-color: radial-gradient(center 50% 50%, radius 100%, #2f0a0a, #050505);");
    }

    /**
     * Actualise les données du HUD (Heads-Up Display).
     * * @param r   Numéro de la manche actuelle.
     * @param pS  Nombre total de tirs effectués par le joueur.
     * @param pH  Nombre total d'impacts réussis par le joueur.
     * @param pA  Pourcentage de précision du joueur.
     * @param eS  Nombre total de tirs effectués par l'IA.
     * @param eH  Nombre total d'impacts réussis par l'IA.
     * @param eA  Pourcentage de précision de l'IA.
     */
    public void updateHUD(int r, int pS, int pH, double pA, int eS, int eH, double eA) {
        roundLabel.setText("MANCHE " + r);
        playerStats.setText(String.format("TIRS : %d | IMPACTS : %d\nPRÉCISION : %.1f%%", pS, pH, pA));
        enemyStats.setText(String.format("TIRS : %d | IMPACTS : %d\nPRÉCISION : %.1f%%", eS, eH, eA));
    }

    // --- Accesseurs ---

    /** @return La vue d'état de la flotte alliée. */
    public FleetStatusView getPlayerFleet() { return playerFleet; }
    /** @return La vue d'état de la flotte ennemie. */
    public FleetStatusView getEnemyFleet() { return enemyFleet; }
    /** @return Le journal de bord du combat. */
    public GameLogView getGameLog() { return gameLog; }
}