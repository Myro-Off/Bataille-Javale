package school.coda.adam_lucie_verena.bataillejavale.gui.component;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Composant graphique affichant le journal de bord de la partie.
 */
public class GameLogView extends ScrollPane {

    private final VBox container;

    /**
     * Initialise la console de bord avec un style visuel translucide et des dimensions fixes.
     * Configure l'auto-scroll sur les changements de hauteur du contenu.
     */
    public GameLogView() {
        this.container = new VBox(6);
        this.container.setPadding(new Insets(12, 12, 12, 12));
        this.container.setStyle("-fx-background-color: transparent;");

        this.setContent(container);
        this.setFitToWidth(true);

        this.setPrefHeight(600);
        this.setMinHeight(400);

        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.NEVER);

        this.setStyle("-fx-background: transparent; " +
                "-fx-background-color: rgba(15, 23, 42, 0.4); " +
                "-fx-border-color: rgba(0, 210, 211, 0.2); " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5;");

        this.container.heightProperty().addListener((obs, old, newVal) -> this.setVvalue(1.0));
    }

    /**
     * Ajoute une nouvelle entrée textuelle au journal avec un formatage monospacé.
     * @param message Le texte décrivant l'événement de jeu.
     * @param color La couleur thématique appliquée au texte.
     */
    public void addLog(String message, Color color) {
        Text logEntry = new Text(message);
        logEntry.setFill(color);
        logEntry.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        logEntry.wrappingWidthProperty().bind(this.widthProperty().subtract(30));

        this.container.getChildren().add(logEntry);
    }
}