package school.coda.adam_lucie_verena.bataillejavale.gui.component;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import school.coda.adam_lucie_verena.bataillejavale.core.achievement.AchievementType;
import school.coda.adam_lucie_verena.bataillejavale.core.events.RandomEventType;
import school.coda.adam_lucie_verena.bataillejavale.gui.Theme;
import school.coda.adam_lucie_verena.bataillejavale.gui.AssetsManager;

import java.util.*;

/**
 * HUD de notifications gérant les succès (pile gauche) et les événements (barre basse).
 */
public class NotificationView extends StackPane {

    private final VBox achievementContainer = new VBox(10);
    private final HBox eventContainer = new HBox(15);

    private final Queue<AchievementType> achievementQueue = new LinkedList<>();
    private final Map<RandomEventType, Node> activeEventNodes = new HashMap<>();
    private int visibleAchievements = 0;

    public NotificationView() {
        this.setMouseTransparent(true);
        this.setPickOnBounds(false);
        this.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());

        achievementContainer.setPadding(new Insets(0, 0, 150, 20));
        achievementContainer.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(achievementContainer, Pos.BOTTOM_LEFT);

        eventContainer.setPadding(new Insets(0, 0, 30, 0));
        eventContainer.setMaxHeight(Region.USE_PREF_SIZE);
        eventContainer.setAlignment(Pos.BOTTOM_CENTER);
        StackPane.setAlignment(eventContainer, Pos.BOTTOM_CENTER);

        this.getChildren().addAll(achievementContainer, eventContainer);
    }

    public synchronized void showAchievement(AchievementType type) {
        Platform.runLater(() -> {
            achievementQueue.add(type);
            processAchievementQueue();
        });
    }

    private void processAchievementQueue() {
        if (visibleAchievements >= 3 || achievementQueue.isEmpty()) return;
        visibleAchievements++;
        renderAchievement(achievementQueue.poll());
    }

    private void renderAchievement(AchievementType type) {
        HBox banner = createBanner(type.getName().toUpperCase(), "★ SUCCÈS DÉBLOQUÉ", "#00d2d3");
        achievementContainer.getChildren().addFirst(banner);

        AssetsManager.playSFX("bonus.wav", 1.0);
        animateIn(banner, -400, 0);

        PauseTransition pause = new PauseTransition(Duration.seconds(4));
        pause.setOnFinished(_ -> animateOut(banner, () -> {
            achievementContainer.getChildren().remove(banner);
            visibleAchievements--;
            processAchievementQueue();
        }));
        pause.play();
    }

    // --- GESTION DES ÉVÉNEMENTS ---

    public void showEvent(RandomEventType type) {
        // On ne montre pas de notification pour l'Apocalypse (gérée par l'alerte rouge)
        if (type == RandomEventType.APOCALYPSE) return;

        Platform.runLater(() -> {
            if (activeEventNodes.containsKey(type)) return;

            String color = getEventColor(type);
            HBox eventBox = createBanner(type.getTitle().toUpperCase(), "ALERTE SYSTÈME", color);

            // --- STYLE PLUS AGRESSIF ---
            eventBox.setPrefWidth(350);
            eventBox.setStyle(eventBox.getStyle() + "-fx-border-width: 2 2 2 6;"); // Bordure plus épaisse

            activeEventNodes.put(type, eventBox);
            eventContainer.getChildren().add(eventBox);

            AssetsManager.playSFX("error.wav", 1.0);
            animateIn(eventBox, 0, 100);
        });
    }

    public void hideEvent(RandomEventType type) {
        Platform.runLater(() -> {
            Node node = activeEventNodes.remove(type);
            if (node != null) {
                eventContainer.getChildren().remove(node);
            }
        });
    }

    private String getEventColor(RandomEventType type) {
        return switch (type) {
            case RAVITAILLEMENT_GRATUIT, JOURNEE_ENSOLEILLEE, SALVE_BOOSTEE -> "#2ecc71"; // VERT
            case APOCALYPSE, METEORES -> "#e74c3c"; // ROUGE
            default -> "#f39c12"; // ORANGE
        };
    }

    // --- UTILITAIRES ---

    private HBox createBanner(String nameStr, String titleStr, String colorHex) {
        HBox banner = getHBox(colorHex);

        VBox texts = new VBox(1);
        texts.setAlignment(Pos.CENTER_LEFT);

        Text t = new Text(titleStr);
        t.setFill(Color.web(colorHex));
        t.setFont(Theme.mono(9, FontWeight.BLACK));

        Text n = new Text(nameStr);
        n.setFill(Color.WHITE);
        n.setFont(Theme.font(12, FontWeight.BOLD));

        texts.getChildren().addAll(t, n);
        banner.getChildren().add(texts);

        return banner;
    }

    @NotNull
    private static HBox getHBox(String colorHex) {
        HBox banner = new HBox(12);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(10, 15, 10, 10));

        banner.setMinHeight(55);
        banner.setMaxHeight(55);

        banner.setStyle(
                "-fx-background-color: rgba(15, 23, 42, 0.95); " +
                        "-fx-border-color: " + colorHex + "; " +
                        "-fx-border-width: 0 0 0 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-border-radius: 4;"
        );
        return banner;
    }

    private void animateIn(Node node, double fromX, double fromY) {
        node.setOpacity(0);
        TranslateTransition tt = new TranslateTransition(Duration.millis(400), node);
        if (fromX != 0) { tt.setFromX(fromX); tt.setToX(0); }
        if (fromY != 0) { tt.setFromY(fromY); tt.setToY(0); }
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setToValue(1);
        new ParallelTransition(tt, ft).play();
    }

    private void animateOut(Node node, Runnable onFinished) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setToValue(0);
        ft.setOnFinished(_ -> onFinished.run());
        ft.play();
    }

    public void clearEvents() {
        Platform.runLater(() -> {
            activeEventNodes.clear();
            eventContainer.getChildren().clear();
        });
    }
}