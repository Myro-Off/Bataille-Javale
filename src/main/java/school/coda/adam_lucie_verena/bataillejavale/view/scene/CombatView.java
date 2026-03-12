package school.coda.adam_lucie_verena.bataillejavale.view.scene;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import school.coda.adam_lucie_verena.bataillejavale.view.component.AchievementTrackerView;
import school.coda.adam_lucie_verena.bataillejavale.view.component.FleetStatusView;
import school.coda.adam_lucie_verena.bataillejavale.view.component.SpecialAbilitiesView;
import school.coda.adam_lucie_verena.bataillejavale.view.component.GameLogView;
import school.coda.adam_lucie_verena.bataillejavale.view.grid.GameView;

/**
 * Vue principale de l'interface de combat coordonnant l'affichage des grilles tactiques,
 * du journal de bord et de l'état des flottes.
 */
public class CombatView extends BorderPane {

    private final GameView playerView;
    private final GameView enemyView;
    private final GameLogView gameLog;
    private final FleetStatusView fleetStatus;
    private final VBox turnIndicator = new VBox(5);

    /**
     * Construit la vue de combat et configure les dimensions initiales de l'application.
     * @param pView Vue de la grille du joueur.
     * @param eView Vue de la grille ennemie.
     * @param log Composant de journalisation des événements.
     * @param status Composant affichant l'état des navires.
     */
    public CombatView(GameView pView, GameView eView, GameLogView log, FleetStatusView status) {
        this.playerView = pView;
        this.enemyView = eView;
        this.gameLog = log;
        this.fleetStatus = status;

        this.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());

        setupLayout();
        setStyle("-fx-background-color: #020617;");
    }

    /**
     * Organise la structure spatiale de l'interface (Haut, Gauche, Centre, Droite)
     * et applique les mises à l'échelle dynamiques des grilles de jeu.
     */
    private void setupLayout() {
        turnIndicator.setAlignment(Pos.CENTER);
        turnIndicator.setPadding(new Insets(10));
        setTop(turnIndicator);

        VBox leftSidebar = new VBox(20, new SpecialAbilitiesView(), new AchievementTrackerView());
        leftSidebar.setPadding(new Insets(20));
        leftSidebar.setPrefWidth(220);
        setLeft(leftSidebar);

        HBox mainLayout = new HBox(40);
        mainLayout.setAlignment(Pos.CENTER);

        double availableW = FXGL.getAppWidth() - 620;
        double availableH = FXGL.getAppHeight() - 150;

        enemyView.autoScale(availableW * 0.7, availableH);
        playerView.autoScale(availableW * 0.25, availableH * 0.4);

        mainLayout.getChildren().addAll(new Group(enemyView), new Group(playerView));
        setCenter(mainLayout);

        VBox rightSidebar = new VBox(15);
        rightSidebar.setPadding(new Insets(20));
        rightSidebar.setPrefWidth(300);

        fleetStatus.setPrefHeight(250);
        ScrollPane scroll = new ScrollPane(gameLog);
        scroll.setPrefHeight(400);
        scroll.setFitToWidth(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: #1e293b;");

        gameLog.heightProperty().addListener((obs, old, newVal) -> scroll.setVvalue(1.0));

        rightSidebar.getChildren().addAll(fleetStatus, scroll);
        setRight(rightSidebar);
    }

    /**
     * Met à jour l'affichage de l'indicateur de tour en changeant le texte et la couleur.
     * @param isPlayerTurn Vrai si c'est au tour du joueur, faux s'il s'agit de l'ennemi.
     */
    public void updateTurnInfo(boolean isPlayerTurn) {
        turnIndicator.getChildren().clear();
        Text t = new Text(isPlayerTurn ? "VOTRE TOUR" : "TOUR ENNEMI");
        t.setFill(isPlayerTurn ? Color.web("#00d2d3") : Color.web("#ff4757"));
        t.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
        turnIndicator.getChildren().add(t);
    }

    /**
     * @return Le composant affichant l'état de santé de la flotte.
     */
    public FleetStatusView getFleetStatus() { return fleetStatus; }

    /**
     * @return Le composant de journalisation des actions.
     */
    public GameLogView getGameLog() { return gameLog; }
}