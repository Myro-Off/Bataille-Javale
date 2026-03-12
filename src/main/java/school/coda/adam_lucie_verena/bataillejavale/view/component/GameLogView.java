package school.coda.adam_lucie_verena.bataillejavale.view.component;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Composant graphique affichant l'historique des actions de la partie (journal de bord).
 * Permet de suivre les événements de combat de manière textuelle.
 */
public class GameLogView extends ScrollPane {

    private final VBox container;

    /**
     * Initialise la vue du journal avec un défilement vertical et un style néon.
     */
    public GameLogView() {
        this.container = new VBox(8);
        this.container.setPadding(new Insets(15));
        this.container.setStyle("-fx-background-color: #0f172a;");

        this.setContent(container);
        this.setFitToWidth(true);
        this.setPrefWidth(220);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        this.setStyle("-fx-background: #0f172a; " +
                "-fx-background-color: #0f172a; " +
                "-fx-border-color: #00d2d3; " +
                "-fx-border-width: 0 0 0 2;");
    }

    /**
     * Ajoute une nouvelle entrée au journal avec une couleur spécifique.
     * @param message Le texte à afficher dans le log.
     * @param color La couleur appliquée au texte (ex: rouge pour les dégâts, vert pour les succès).
     */
    public void addLog(String message, Color color) {
        Text logEntry = new Text(">" + message);
        logEntry.setFill(color);
        logEntry.setFont(Font.font("Monospaced", FontWeight.BOLD, 13));
        logEntry.setWrappingWidth(this.getPrefWidth() - 30);
        this.container.getChildren().add(logEntry);

        this.setVvalue(1.0);
    }
}