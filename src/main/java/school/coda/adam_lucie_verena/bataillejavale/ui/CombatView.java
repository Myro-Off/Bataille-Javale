package school.coda.adam_lucie_verena.bataillejavale.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

/**
 * Centre de commandement tactique et vue principale du combat.
 * <p>
 * Cette classe organise l'interface globale durant la phase de jeu active.
 * Elle utilise un {@link BorderPane} pour structurer les différents modules :
 * <ul>
 * <li><b>TOP :</b> Indicateur de tour et bannière d'état.</li>
 * <li><b>LEFT :</b> Capacités spéciales et suivi des succès (Achievements).</li>
 * <li><b>CENTER :</b> Les deux grilles de combat (Joueur vs Ennemi).</li>
 * <li><b>RIGHT :</b> Statut de la flotte et journal de bord (Logs).</li>
 * </ul>
 * </p>
 */
public class CombatView extends BorderPane {

    private final GameView playerView;
    private final GameView enemyView;
    private final GameLogView gameLog;
    private final FleetStatusView fleetStatus;

    /** Conteneur vertical pour l'affichage dynamique du tour actuel. */
    private final VBox turnIndicator = new VBox(5);

    /**
     * Initialise la vue de combat avec tous les composants nécessaires.
     * @param pView   La vue de la grille du joueur.
     * @param eView   La vue de la grille ennemie.
     * @param log     L'instance du journal de bord.
     * @param status  L'instance du statut de la flotte.
     */
    public CombatView(GameView pView, GameView eView, GameLogView log, FleetStatusView status) {
        this.playerView = pView;
        this.enemyView = eView;
        this.gameLog = log;
        this.fleetStatus = status;

        setupLayout();
        // Style : Fond bleu nuit profond pour une immersion tactique
        setStyle("-fx-background-color: #020617;");
    }

    /**
     * Configure l'agencement spatial des composants dans le BorderPane.
     * <p>
     * Gère notamment l'encapsulation du journal de bord dans un ScrollPane
     * pour garantir que la zone de texte ne déforme pas l'interface globale.
     * </p>
     */
    private void setupLayout() {
        // --- TOP : Bannière d'information sur le tour ---
        turnIndicator.setAlignment(Pos.CENTER);
        turnIndicator.setPadding(new Insets(15));
        setTop(turnIndicator);

        // --- LEFT : Sidebar des aptitudes et trophées ---
        VBox leftSidebar = new VBox(30, new SpecialAbilitiesView(), new AchievementTrackerView());
        leftSidebar.setPadding(new Insets(20));
        leftSidebar.setPrefWidth(250);
        setLeft(leftSidebar);

        // --- CENTER : Zone d'engagement (Grilles face à face) ---
        HBox grids = new HBox(40, playerView, enemyView);
        grids.setAlignment(Pos.CENTER);
        setCenter(grids);

        // --- RIGHT : Surveillance de la flotte et flux de données (Logs) ---
        VBox rightSidebar = new VBox(20);
        rightSidebar.setPadding(new Insets(20));
        rightSidebar.setPrefWidth(280);

        /*
         * ScrollPane configuré pour contenir le module de logs.
         * Assure que le log reste dans sa boîte de 400px de haut.
         */
        ScrollPane scroll = new ScrollPane(gameLog);
        scroll.setPrefHeight(400);
        scroll.setFitToWidth(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: #1e293b;");

        // Mécanisme d'Auto-scroll : Force le défilement vers le bas à chaque ajout de log
        gameLog.heightProperty().addListener((obs, old, newVal) -> scroll.setVvalue(1.0));

        rightSidebar.getChildren().addAll(fleetStatus, scroll);
        setRight(rightSidebar);
    }

    /**
     * Met à jour l'affichage de la bannière supérieure selon le tour.
     * @param isPlayerTurn True si c'est au joueur de cliquer, False pour l'IA.
     */
    public void updateTurnInfo(boolean isPlayerTurn) {
        turnIndicator.getChildren().clear();

        // Texte principal avec code couleur (Cyan pour le joueur, Rouge pour l'ennemi)
        Text t = new Text(isPlayerTurn ? "VOTRE TOUR" : "TOUR ENNEMI");
        t.setFill(isPlayerTurn ? Color.web("#00d2d3") : Color.web("#ff4757"));
        t.setFont(Font.font("Verdana", FontWeight.BOLD, 24));

        // Rappel de l'ordre de passage
        Text p = new Text("Prochains : " + (isPlayerTurn ? "Ennemi > Vous" : "Vous > Ennemi"));
        p.setFill(Color.GRAY);

        turnIndicator.getChildren().addAll(t, p);
    }

    /** @return L'instance gérant l'état de la flotte ennemie. */
    public FleetStatusView getFleetStatus() { return fleetStatus; }

    /** @return L'instance gérant le journal des événements. */
    public GameLogView getGameLog() { return gameLog; }
}