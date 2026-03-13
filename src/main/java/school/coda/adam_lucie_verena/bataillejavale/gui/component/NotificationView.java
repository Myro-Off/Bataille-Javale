package school.coda.adam_lucie_verena.bataillejavale.gui.component;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import school.coda.adam_lucie_verena.bataillejavale.core.achievement.AchievementType;
import school.coda.adam_lucie_verena.bataillejavale.gui.Theme;

/**
 * Conteneur invisible placé en haut de l'écran pour afficher les succès.
 */
public class NotificationView extends StackPane {
    public NotificationView() {
        this.setPrefWidth(FXGL.getAppWidth());
        // On laisse la hauteur s'adapter ou on met une taille raisonnable
        this.setPrefHeight(200);

        this.setAlignment(Pos.TOP_CENTER);
        this.setPadding(new Insets(50, 0, 0, 0)); // Un peu de marge par rapport au haut

        this.setMouseTransparent(true);
        this.setPickOnBounds(false);
    }

    public void showAchievement(AchievementType type) {
        // Création du bandeau
        System.out.println("[DEBUG VIEW] showAchievement reçu pour : " + type.getName());
        System.out.println("[DEBUG VIEW] Taille actuelle du conteneur : " + getWidth() + "x" + getHeight());
        VBox banner = new VBox(2);
        banner.setAlignment(Pos.CENTER);
        banner.setPadding(new Insets(10, 30, 10, 30));
        banner.setMaxWidth(300);

        // Style "Néon" raccord avec ton thème
        banner.setStyle("-fx-background-color: rgba(2, 6, 23, 0.9); " +
                "-fx-border-color: #00d2d3; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10;");
        banner.setEffect(Theme.GLOW_CYAN);

        Text title = new Text("SUCCÈS DÉVERROUILLÉ");
        title.setFill(Theme.TEXT_MUTED);
        title.setFont(Theme.font(10, FontWeight.BOLD));

        Text name = new Text(type.getName().toUpperCase());
        name.setFill(Color.WHITE);
        name.setFont(Theme.font(16, FontWeight.BOLD));

        banner.getChildren().addAll(title, name);
        this.getChildren().add(banner);
        System.out.println("[DEBUG VIEW] Banner ajoutée aux enfants. Nombre d'enfants : " + getChildren().size());

        // --- ANIMATION ---
        // 1. Apparition (Slide du haut vers le bas)
        banner.setTranslateY(-100);
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(600), banner);
        slideIn.setToY(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);

        // 2. Pause
        PauseTransition pause = new PauseTransition(Duration.seconds(3));

        // 3. Disparition (Fade out)
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), banner);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(_ -> this.getChildren().remove(banner));

        new SequentialTransition(slideIn, pause, fadeOut).play();
    }
}