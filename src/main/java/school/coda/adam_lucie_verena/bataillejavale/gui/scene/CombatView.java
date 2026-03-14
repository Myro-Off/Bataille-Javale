package school.coda.adam_lucie_verena.bataillejavale.gui.scene;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import school.coda.adam_lucie_verena.bataillejavale.gui.Theme;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.*;
import school.coda.adam_lucie_verena.bataillejavale.gui.grid.GameView;
import school.coda.adam_lucie_verena.bataillejavale.gui.vfx.VfxManager;

/**
 * Vue principale orchestrant l'interface de combat et la couche VFX.
 * Utilise un alignement TOP_LEFT fixe pour garantir la cohérence des coordonnées VFX.
 */
public class CombatView extends StackPane {

    private final GameLogView gameLog;
    private final FleetStatusView playerFleet, enemyFleet;
    private final GameView playerGrid, enemyGrid;

    private final Pane vfxOverlay = new Pane();
    private final BorderPane uiLayout = new BorderPane();
    private final VBox notificationLayer = new VBox();

    private final Text playerStats = new Text();
    private final Text enemyStats = new Text();
    private final Text roundLabel = new Text();
    private final VBox turnIndicator = new VBox(5);

    /**
     * Initialise la vue de combat et connecte le gestionnaire de VFX.
     * @param pView Vue joueur.
     * @param eView Vue ennemie.
     * @param log Journal de jeu.
     * @param pFleet Statut flotte joueur.
     * @param eFleet Statut flotte ennemie.
     */
    public CombatView(GameView pView, GameView eView, GameLogView log, FleetStatusView pFleet, FleetStatusView eFleet) {
        this.playerGrid = pView;
        this.enemyGrid = eView;
        this.gameLog = log;
        this.playerFleet = pFleet;
        this.enemyFleet = eFleet;

        this.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        this.setAlignment(Pos.TOP_LEFT);

        vfxOverlay.setMouseTransparent(true);
        vfxOverlay.setPickOnBounds(false);
        vfxOverlay.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());

        VfxManager.setExternalOverlay(vfxOverlay);

        setupLayout();
        this.getChildren().addAll(uiLayout, vfxOverlay);
    }

    /**
     * Organise les composants UI (sidebars et zone centrale).
     */
    private void setupLayout() {
        uiLayout.setStyle(Theme.MAIN_GRADIENT);
        uiLayout.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());

        turnIndicator.setAlignment(Pos.CENTER);
        turnIndicator.setPadding(new Insets(20, 0, 10, 0));
        roundLabel.setFill(Color.GOLD);
        roundLabel.setFont(Theme.font(18, FontWeight.BOLD));
        uiLayout.setTop(turnIndicator);

        VBox left = new VBox(20, playerFleet, playerStats, new SpecialAbilitiesView());
        left.setPadding(new Insets(20)); left.setPrefWidth(280);
        playerStats.setFill(Theme.CYAN); playerStats.setFont(Theme.mono(12, FontWeight.BOLD));
        uiLayout.setLeft(left);

        HBox center = new HBox(60);
        center.setAlignment(Pos.CENTER);
        enemyGrid.autoScale(FXGL.getAppWidth() * 0.45, FXGL.getAppHeight() * 0.7);
        playerGrid.autoScale(FXGL.getAppWidth() * 0.22, FXGL.getAppHeight() * 0.38);
        center.getChildren().addAll(createGridBox("FLOTTE ENNEMIE", enemyGrid), createGridBox("MA FLOTTE", playerGrid));
        uiLayout.setCenter(center);

        VBox right = new VBox(15);
        right.setPadding(new Insets(20)); right.setPrefWidth(320);
        enemyStats.setFill(Theme.RED_ALERTE); enemyStats.setFont(Theme.mono(12, FontWeight.BOLD));
        notificationLayer.setPrefHeight(40); notificationLayer.setAlignment(Pos.CENTER);
        right.getChildren().addAll(enemyFleet, enemyStats, notificationLayer, gameLog);
        uiLayout.setRight(right);
    }

    /**
     * Crée un bloc de grille labellisé enveloppé dans un Group pour respecter le scale.
     * @param title Titre du bloc.
     * @param grid Vue de la grille.
     * @return VBox configurée.
     */
    private VBox createGridBox(String title, GameView grid) {
        Text t = new Text(title); t.setFill(Color.WHITE); t.setFont(Theme.font(14, FontWeight.BOLD));
        Group g = new Group(grid);
        VBox b = new VBox(15, t, g);
        b.setAlignment(Pos.CENTER); b.setFillWidth(false);
        return b;
    }

    /**
     * Affiche une notification temporaire.
     * @param msg Message.
     * @param c Couleur.
     * @param crit True pour une mise en évidence critique.
     */
    public void showCombatNotification(String msg, Color c, boolean crit) {
        Text t = new Text(msg.toUpperCase()); t.setFill(c); t.setFont(Theme.font(crit ? 15 : 12, FontWeight.BOLD));
        notificationLayer.getChildren().setAll(t);
        FadeTransition ft = new FadeTransition(Duration.millis(300), t);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
        PauseTransition p = new PauseTransition(Duration.seconds(2));
        p.setOnFinished(_ -> {
            FadeTransition out = new FadeTransition(Duration.millis(500), t);
            out.setToValue(0); out.setOnFinished(_ -> notificationLayer.getChildren().remove(t));
            out.play();
        });
        p.play();
    }

    /**
     * Met à jour l'indicateur visuel du tour de jeu.
     * @param isPlayer True si c'est au tour du joueur.
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
     * Actualise les statistiques HUD.
     * @param r Manche.
     * @param pS Tirs joueur.
     * @param pH Impacts joueur.
     * @param pA Précision joueur.
     * @param eS Tirs ennemis.
     * @param eH Impacts ennemis.
     * @param eA Précision ennemie.
     */
    public void updateHUD(int r, int pS, int pH, double pA, int eS, int eH, double eA) {
        roundLabel.setText("MANCHE " + r);
        playerStats.setText(String.format("TIRS : %d | IMPACTS : %d\nPRÉCISION : %.1f%%", pS, pH, pA));
        enemyStats.setText(String.format("TIRS : %d | IMPACTS : %d\nPRÉCISION : %.1f%%", eS, eH, eA));
    }

    /** @return Vue de la flotte joueur. */
    public FleetStatusView getPlayerFleet() { return playerFleet; }
    /** @return Vue de la flotte ennemie. */
    public FleetStatusView getEnemyFleet() { return enemyFleet; }
    /** @return Vue du journal de log. */
    public GameLogView getGameLog() { return gameLog; }
}