package school.coda.adam_lucie_verena.bataillejavale.gui.component;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import school.coda.adam_lucie_verena.bataillejavale.core.achievement.AchievementType;
import school.coda.adam_lucie_verena.bataillejavale.gui.Theme;
import school.coda.adam_lucie_verena.bataillejavale.gui.AssetsManager;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Système de notification ultra-compact (HUD).
 * Ne prend que l'espace nécessaire, positionné dynamiquement en bas à gauche.
 */
public class NotificationView extends VBox {

    private final Queue<AchievementType> achievementQueue = new LinkedList<>();
    private boolean isDisplaying = false;

    public NotificationView() {
        // Le composant ne capture pas les clics et s'adapte à son contenu
        this.setMouseTransparent(true);
        this.setPickOnBounds(false);

        this.setLayoutX(20);
        this.setLayoutY(FXGL.getAppHeight() - 120);
    }

    public synchronized void showAchievement(AchievementType type) {
        Platform.runLater(() -> {
            achievementQueue.add(type);
            processQueue();
        });
    }

    private void processQueue() {
        if (isDisplaying || achievementQueue.isEmpty()) return;
        isDisplaying = true;
        renderNotification(achievementQueue.poll());
    }

    private void renderNotification(AchievementType type) {
        this.toFront();

        // --- DESIGN DE LA PUCE (CHIP) ---
        HBox banner = new HBox(15);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(10, 25, 10, 15));

        // Largeur fixe compacte (ni trop grand, ni trop petit)
        banner.setPrefWidth(320);

        // Verre dépoli sombre + Bordure Néon à gauche
        banner.setStyle(
                "-fx-background-color: rgba(15, 23, 42, 0.95); " +
                        "-fx-border-color: #00d2d3; " +
                        "-fx-border-width: 0 0 0 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-border-radius: 4;"
        );
        banner.setEffect(new javafx.scene.effect.DropShadow(15, Color.web("#00d2d3", 0.3)));

        // Icône Étoile
        Text icon = new Text("★");
        icon.setFill(Theme.CYAN);
        icon.setFont(Theme.font(24, FontWeight.BOLD));

        // Textes empilés
        VBox textBox = new VBox(2);
        Text title = new Text("SUCCÈS DÉVERROUILLÉ");
        title.setFill(Theme.CYAN);
        title.setFont(Theme.mono(10, FontWeight.BLACK));

        Text name = new Text(type.getName().toUpperCase());
        name.setFill(Color.WHITE);
        name.setFont(Theme.font(14, FontWeight.BOLD));

        textBox.getChildren().addAll(title, name);
        banner.getChildren().addAll(icon, textBox);

        this.getChildren().add(banner);

        // --- DÉCLENCHEMENT DU SON ---
        // Remplace "click.wav" par le nom de ton fichier son de succès si tu en as un spécifique
        try {
            AssetsManager.playSFX("bonus.wav", 1);
        } catch (Exception e) {
            System.err.println("Son de notification introuvable.");
        }

        // --- ANIMATIONS ---
        banner.setTranslateX(-400); // Départ caché à gauche
        banner.setTranslateY(0);
        banner.setOpacity(0);

        // 1. Entrée : Glisse rapide depuis la gauche avec fondu
        ParallelTransition slideIn = new ParallelTransition();
        TranslateTransition txIn = new TranslateTransition(Duration.millis(400), banner);
        txIn.setToX(0);
        txIn.setInterpolator(Interpolator.SPLINE(0.1, 0.9, 0.2, 1.0)); // Freinage très fluide
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), banner);
        fadeIn.setToValue(1.0);
        slideIn.getChildren().addAll(txIn, fadeIn);

        // 2. Pause
        PauseTransition pause = new PauseTransition(Duration.seconds(3.5));

        // 3. Sortie : Tombe vers le bas et s'efface
        ParallelTransition slideOut = new ParallelTransition();
        TranslateTransition tyOut = new TranslateTransition(Duration.millis(400), banner);
        tyOut.setByY(50);
        tyOut.setInterpolator(Interpolator.EASE_IN); // Accélère en tombant
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), banner);
        fadeOut.setToValue(0);
        slideOut.getChildren().addAll(tyOut, fadeOut);

        // Séquence
        SequentialTransition sequence = new SequentialTransition(slideIn, pause, slideOut);
        sequence.setOnFinished(_ -> {
            this.getChildren().remove(banner);
            isDisplaying = false;
            processQueue();
        });

        sequence.play();
    }
}