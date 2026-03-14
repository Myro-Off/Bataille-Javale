package school.coda.adam_lucie_verena.bataillejavale.gui.scene;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import school.coda.adam_lucie_verena.bataillejavale.core.achievement.AchievementManager;
import school.coda.adam_lucie_verena.bataillejavale.core.achievement.AchievementType;
import school.coda.adam_lucie_verena.bataillejavale.gui.Theme;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.MenuButton;

/**
 * Vue du registre de carrière affichant les succès et la progression.
 * <p>
 * Optimisée pour la fluidité avec un rendu conditionnel des barres de progression
 * et un système de segments dynamiques.
 * </p>
 *
 * @author Adam & Lucie
 */
public class AchievementMenuView extends StackPane {

    /**
     * Initialise la vue avec les données de carrière.
     * @param manager Source des données.
     * @param onBack Retour au menu.
     */
    public AchievementMenuView(AchievementManager manager, Runnable onBack) {
        this.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        this.setStyle("-fx-background-color: #020617;");

        VBox root = new VBox(30);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(FXGL.getAppHeight() * 0.05, 0, 0, 0));

        VBox header = createGlobalHeader(manager);
        header.maxWidthProperty().bind(this.widthProperty().multiply(0.65));

        VBox list = new VBox(15);
        list.setAlignment(Pos.TOP_CENTER);
        list.setPadding(new Insets(10, 20, 10, 20));

        for (AchievementType type : AchievementType.values()) {
            HBox card = createAchievementCard(type, manager);
            card.maxWidthProperty().bind(header.maxWidthProperty());
            list.getChildren().add(card);
        }

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.prefHeightProperty().bind(this.heightProperty().multiply(0.68));
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

        MenuButton btnBack = new MenuButton("RETOUR AU QG", onBack);

        root.getChildren().addAll(header, scroll, btnBack);
        getChildren().add(root);
    }

    private VBox createGlobalHeader(AchievementManager manager) {
        VBox header = new VBox(15);
        header.setAlignment(Pos.CENTER);

        long unlocked = 0;
        for(AchievementType t : AchievementType.values()) if(manager.isUnlocked(t)) unlocked++;
        int total = AchievementType.values().length;
        double progress = (double) unlocked / total;

        Text title = new Text("ARCHIVES TACTIQUES");
        title.setFont(Theme.font(36, FontWeight.BLACK));
        title.setFill(Color.WHITE);

        HBox stats = new HBox(25);
        stats.setAlignment(Pos.CENTER);
        Text perc = new Text((int)(progress * 100) + "%");
        perc.setFill(Theme.CYAN);
        perc.setFont(Theme.mono(24, FontWeight.BOLD));
        Text rat = new Text(unlocked + " / " + total + " DOSSIERS COMPLÉTÉS");
        rat.setFill(Theme.TEXT_MUTED);
        rat.setFont(Theme.mono(16, FontWeight.NORMAL));
        stats.getChildren().addAll(perc, rat);

        HBox bar = createSegmentedBar(unlocked, total, Theme.CYAN, 25);
        bar.prefWidthProperty().bind(header.widthProperty());

        header.getChildren().addAll(title, stats, bar);
        return header;
    }

    private HBox createAchievementCard(AchievementType type, AchievementManager manager) {
        boolean isUnlocked = manager.isUnlocked(type);
        int current = manager.getAchievementProgress(type);
        int target = type.getTargetValue();

        HBox card = new HBox(25);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: rgba(30, 41, 59, 0.4); " +
                "-fx-border-color: " + (isUnlocked ? "#00d2d3" : "#1e293b") + "; " +
                "-fx-border-width: 0 0 0 6; -fx-background-radius: 0 5 5 0;");

        VBox info = new VBox(8);
        HBox.setHgrow(info, Priority.ALWAYS);

        HBox titleLine = new HBox(15);
        titleLine.setAlignment(Pos.BASELINE_LEFT);
        Text name = new Text(type.getName().toUpperCase());
        name.setFill(isUnlocked ? Color.WHITE : Theme.TEXT_DIMMED);
        name.setFont(Theme.font(18, FontWeight.BOLD));
        titleLine.getChildren().add(name);

        if (isUnlocked) {
            Text date = new Text("[" + manager.getUnlockDate(type) + "]");
            date.setFill(Theme.CYAN);
            date.setFont(Theme.mono(10, FontWeight.NORMAL));
            titleLine.getChildren().add(date);
        }

        Text desc = new Text(type.getDescription());
        desc.setFill(isUnlocked ? Theme.TEXT_MUTED : Theme.TEXT_DIMMED);
        desc.setFont(Theme.font(13, FontWeight.NORMAL));
        desc.wrappingWidthProperty().bind(card.widthProperty().multiply(0.7));

        info.getChildren().addAll(titleLine, desc);

        if (!isUnlocked) {
            HBox miniBar = createSegmentedBar(current, target, Theme.CYAN, Math.min(target, 30));
            miniBar.setMaxWidth(300);
            info.getChildren().add(miniBar);
        }

        Text status = new Text(isUnlocked ? "ARCHIVÉ" : current + " / " + target);
        status.setFill(isUnlocked ? Theme.CYAN : Theme.TEXT_DIMMED);
        status.setFont(Theme.mono(12, FontWeight.BLACK));

        card.getChildren().addAll(info, status);
        return card;
    }

    private HBox createSegmentedBar(long current, int target, Color color, int numSegments) {
        HBox container = new HBox(3);
        container.setAlignment(Pos.CENTER_LEFT);
        double progressRatio = (double) current / target;

        for (int i = 0; i < numSegments; i++) {
            Rectangle r = new Rectangle(0, 10);
            r.widthProperty().bind(container.widthProperty().divide(numSegments).subtract(3));
            boolean active = (i < progressRatio * numSegments);
            r.setFill(active ? color : Color.web("#1e293b"));
            if (active) r.setEffect(new javafx.scene.effect.Bloom(0.5));
            container.getChildren().add(r);
        }
        return container;
    }
}