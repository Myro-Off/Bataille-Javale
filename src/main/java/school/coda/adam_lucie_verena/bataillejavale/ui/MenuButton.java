package school.coda.adam_lucie_verena.bataillejavale.ui;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Bouton personnalisé interactif pour les menus du jeu.
 * <p>
 * Ce composant combine un {@link Rectangle} pour le fond et un {@link Text} pour le libellé.
 * Il intègre nativement des effets visuels lors du survol (Hover) pour améliorer
 * l'expérience utilisateur.
 * </p>
 */
public class MenuButton extends StackPane {

    // ------------------------------------------------------------------------------------------
    // CONSTANTES VISUELLES
    // ------------------------------------------------------------------------------------------

    private static final double WIDTH = 250;
    private static final double HEIGHT = 40;
    private static final double FONT_SIZE = 18;

    // ------------------------------------------------------------------------------------------
    // CONSTRUCTEUR
    // ------------------------------------------------------------------------------------------

    /**
     * Crée un bouton avec un texte spécifique et définit l'action à exécuter.
     *
     * @param name   Le texte affiché sur le bouton.
     * @param action La fonction {@link Runnable} appelée lors du clic.
     */
    public MenuButton(String name, Runnable action) {
        // 1. DÉFINITION DU FOND
        Rectangle bg = new Rectangle(WIDTH, HEIGHT);
        bg.setOpacity(0.6);
        bg.setFill(Color.BLACK);
        bg.setStroke(Color.CYAN);

        // 2. DÉFINITION DU TEXTE
        Text text = new Text(name);
        text.setFill(Color.WHITE);
        text.setFont(Font.font("Verdana", FONT_SIZE));

        // 3. CONFIGURATION DU CONTENEUR
        setAlignment(Pos.CENTER);
        getChildren().addAll(bg, text);

        // 4. GESTION DES ÉVÉNEMENTS
        setupInteractions(bg, text, action);
    }

    // ------------------------------------------------------------------------------------------
    // MÉTHODES PRIVÉES : INTERACTIONS
    // ------------------------------------------------------------------------------------------

    /**
     * Configure les gestionnaires d'événements pour le survol et le clic.
     *
     * @param bg     Le rectangle de fond à modifier.
     * @param text   Le texte à modifier.
     * @param action L'action à déclencher au clic.
     */
    private void setupInteractions(Rectangle bg, Text text, Runnable action) {
        // Effet au survol (Entrée de souris)
        setOnMouseEntered(_ -> {
            bg.setFill(Color.CYAN);
            text.setFill(Color.BLACK);
        });

        // Retour à l'état normal (Sortie de souris)
        setOnMouseExited(_ -> {
            bg.setFill(Color.BLACK);
            text.setFill(Color.WHITE);
        });

        // Exécution de l'action définie
        setOnMouseClicked(_ -> action.run());
    }
}