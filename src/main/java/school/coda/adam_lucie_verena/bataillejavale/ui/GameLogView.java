package school.coda.adam_lucie_verena.bataillejavale.ui;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Zone technique réservée au journal de bord (Logs).
 * <p>
 * Cette classe définit l'emplacement visuel du journal dans l'interface de combat.
 * La logique d'affichage et le formatage des messages sont laissés libres pour
 * l'implémentation par le membre de l'équipe responsable de ce module.
 * </p>
 */
public class GameLogView extends ScrollPane {

    private final VBox container;

    /**
     * Initialise la zone de log avec le style graphique du jeu.
     * La structure est prête à recevoir des composants enfants dans le 'container'.
     */
    public GameLogView() {
        this.container = new VBox(8);
        this.container.setPadding(new Insets(15));

        // Style visuel pour marquer l'emplacement dans l'UI
        this.container.setStyle("-fx-background-color: #0f172a;");

        this.setContent(container);
        this.setFitToWidth(true);
        this.setPrefWidth(220);

        // Masquage des barres pour garder un look épuré "Terminal"
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        // Bordure néon à gauche pour la séparation visuelle
        this.setStyle("-fx-background: #0f172a; " +
                "-fx-background-color: #0f172a; " +
                "-fx-border-color: #00d2d3; " +
                "-fx-border-width: 0 0 0 2;");
    }

    /**
     * (version pour que ça compile, implémentation ultérieurement)
     * Point d'entrée pour l'ajout de messages de log.
     * @param message Le contenu textuel de l'événement.
     * @param color   La couleur suggérée pour le texte.
     */
    public void addLog(String message, Color color) {
        // L'implémentation de l'affichage des Text ou Labels se fera ici.
    }
}