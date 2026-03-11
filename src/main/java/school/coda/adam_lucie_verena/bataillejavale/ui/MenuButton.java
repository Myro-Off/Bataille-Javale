package school.coda.adam_lucie_verena.bataillejavale.ui;

import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.util.stream.Collectors;

/**
 * Composant de bouton personnalisé pour les menus du jeu.
 * <p>
 * Ce bouton utilise un style "Néon-Cyber" avec des dégradés de bleu ardoise,
 * des bordures cyan et des effets de lueur (glow) lors du survol.
 * Il gère nativement ses propres animations de mise à l'échelle (zoom).
 * </p>
 */
public class MenuButton extends StackPane {

    /** Couleur cyan néon signature utilisée pour les bordures et les effets. */
    private static final Color CYAN_NEON = Color.web("#00d2d3");

    /**
     * Crée un nouveau bouton de menu stylisé.
     * * @param name   Le texte à afficher sur le bouton.
     * @param action La fonction (Runnable) à exécuter lors du clic.
     */
    public MenuButton(String name, Runnable action) {
        // Fond du bouton : un rectangle avec des coins arrondis et un dégradé subtil
        Rectangle bg = new Rectangle(280, 45);
        bg.setArcWidth(10);
        bg.setArcHeight(10);
        bg.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1e293b", 0.8)),
                new Stop(1, Color.web("#0f172a", 0.9))));

        // Bordure cyan semi-transparente par défaut
        bg.setStroke(CYAN_NEON.deriveColor(0, 1, 1, 0.4));
        bg.setStrokeWidth(1.5);

        /*
          Transformation du texte :
          Convertit "JOUER" en "J O U E R" pour un style plus "Interface de commande".
         */
        String spacedName = name.toUpperCase().chars()
                .mapToObj(c -> (char)c + " ")
                .collect(Collectors.joining()).trim();

        Text text = new Text(spacedName);
        text.setFill(Color.WHITE);
        text.setFont(Font.font("Verdana", FontWeight.BOLD, 14));

        // Assemblage des composants
        getChildren().addAll(bg, text);
        setAlignment(Pos.CENTER);

        // Initialisation des comportements interactifs
        setupInteractions(bg, text, action);
    }

    /**
     * Configure les animations et les changements d'état lors des interactions souris.
     * * @param bg     Le rectangle de fond à animer.
     * @param text   Le texte dont la couleur change.
     * @param action L'action à déclencher au clic.
     */
    private void setupInteractions(Rectangle bg, Text text, Runnable action) {
        // Animation de zoom fluide
        ScaleTransition st = new ScaleTransition(Duration.millis(150), this);

        // Effet de lueur externe
        DropShadow glow = new DropShadow(15, CYAN_NEON);

        /*
         * État : Survol (Hover)
         * Augmente la taille, change la couleur du texte et active la lueur.
         */
        setOnMouseEntered(_ -> {
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
            bg.setStroke(CYAN_NEON);
            bg.setEffect(glow);
            text.setFill(CYAN_NEON);
        });

        /*
         * État : Sortie (Normal)
         * Réinitialise l'apparence du bouton.
         */
        setOnMouseExited(_ -> {
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
            bg.setStroke(CYAN_NEON.deriveColor(0, 1, 1, 0.4));
            bg.setEffect(null);
            text.setFill(Color.WHITE);
        });

        /*
         * État : Clic
         * Exécute la logique métier passée en paramètre.
         */
        setOnMouseClicked(_ -> action.run());
    }
}