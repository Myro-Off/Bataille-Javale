package school.coda.adam_lucie_verena.bataillejavale.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import com.almasb.fxgl.dsl.FXGL;

/**
 * Vue de configuration des paramètres tactiques avant le début de la partie.
 * <p>
 * Cette classe représente l'écran intermédiaire où le joueur peut valider
 * ses choix avant de passer à la phase de placement des navires.
 * Elle utilise un style visuel "Cyber/Néon" cohérent avec le reste de l'application.
 * </p>
 */
public class ConfigView extends VBox {

    /**
     * Construit l'interface de configuration.
     * @param onNext Action à exécuter (généralement fournie par l'App) pour
     * déclencher la transition vers l'écran de placement.
     */
    public ConfigView(Runnable onNext) {
        // Configuration du conteneur principal (VBox)
        this.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        this.setAlignment(Pos.CENTER);
        this.setSpacing(40);

        // Background avec un dégradé radial pour un effet de profondeur
        this.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 70%, #2c3e50, #000000);");

        // Titre de la vue avec effet de lueur néon
        Text title = new Text("PARAMÈTRES TACTIQUES");
        title.setFont(Font.font("Verdana", 36));
        title.setFill(Color.web("#00d2d3")); // Bleu néon
        title.setStyle("-fx-effect: dropshadow(gaussian, #00d2d3, 15, 0.5, 0, 0);");

        // Bouton de validation stylisé (Bordures néon, fond transparent)
        Button btnNext = new Button("VALIDER ET PLACER LA FLOTTE");
        btnNext.setFont(Font.font("Verdana", 18));
        btnNext.setStyle("-fx-background-color: transparent; -fx-border-color: #00d2d3; -fx-text-fill: #00d2d3; -fx-border-width: 2; -fx-padding: 15 30;");

        /*
         * Gestion des événements de survol (Hover) pour l'interactivité.
         */
        btnNext.setOnMouseEntered(_ -> btnNext.setStyle("-fx-background-color: #00d2d3; -fx-text-fill: black; -fx-padding: 15 30;"));

        btnNext.setOnMouseExited(_ -> btnNext.setStyle("-fx-background-color: transparent; -fx-border-color: #00d2d3; -fx-text-fill: #00d2d3; -fx-border-width: 2; -fx-padding: 15 30;"));

        /*
         * Action lors du clic sur le bouton.
         * Exécute le Runnable passé en paramètre du constructeur.
         */
        btnNext.setOnAction(_ -> onNext.run());

        // Ajout des composants au layout
        this.getChildren().addAll(title, btnNext);
    }
}