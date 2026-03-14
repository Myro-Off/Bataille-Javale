package school.coda.adam_lucie_verena.bataillejavale.gui.scene;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import school.coda.adam_lucie_verena.bataillejavale.gui.AssetsManager;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.MenuButton;
import school.coda.adam_lucie_verena.bataillejavale.gui.Theme;

/**
 * Vue de fin de partie présentant les résultats finaux du combat.
 * Affiche les statistiques de tirs, la précision ainsi qu'un grade basé sur la performance.
 */
public class GameOverView extends StackPane {

    private final Color themeColor;

    /**
     * Construit l'écran de fin de partie.
     * @param isVictory État de victoire ou défaite.
     * @param shots Nombre total de projectiles lancés.
     * @param hits Nombre total d'impacts confirmés.
     * @param streak Série de victoires consécutives.
     * @param onRestart Action à exécuter pour recommencer une partie.
     * @param onMenu Action à exécuter pour retourner à l'accueil.
     */
    public GameOverView(boolean isVictory, int shots, int hits, int streak, Runnable onRestart, Runnable onMenu) {
        this.themeColor = isVictory ? Theme.CYAN : Theme.RED_ALERTE;
        setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());

        Rectangle bg = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight(), Theme.BG_OVERLAY);

        VBox root = new VBox(50);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(100));

        VBox header = buildHeader(isVictory, shots, hits);
        HBox statsArea = buildStatsArea(shots, hits, streak);

        HBox actions = new HBox(30,
                new MenuButton("REJOUER", onRestart),
                new MenuButton("MENU PRINCIPAL", onMenu)
        );
        actions.setAlignment(Pos.CENTER);

        root.getChildren().addAll(header, statsArea, actions);
        getChildren().addAll(bg, root);

        animateEntrance(root);
    }

    private VBox buildHeader(boolean isVictory, int shots, int hits) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);

        Text label = new Text(isVictory ? "RAPPORT DE VICTOIRE" : "RAPPORT D'ÉCHEC");
        if (isVictory){
            AssetsManager.stopMusic();
            AssetsManager.playSFX("win.wav", 0.6);
            AssetsManager.playMusic("victory.mp3", 0.6);
        }
        else {
            AssetsManager.playMusic("lose.mp3", 0.6);
        }

        label.setFill(Theme.TEXT_MUTED);
        label.setFont(Theme.font(20, FontWeight.NORMAL));

        Text title = new Text(isVictory ? "MISSION ACCOMPLIE" : "FLOTTE DÉMANTELÉE");
        title.setFont(Theme.font(72, FontWeight.BOLD));
        title.setFill(themeColor);
        title.setEffect(isVictory ? Theme.GLOW_LARGE_CYAN : Theme.GLOW_LARGE_RED);

        double acc = shots > 0 ? (double) hits / shots * 100 : 0;
        String rankStr = calculateRank(isVictory, acc);

        Text rankText = new Text("GRADE : " + rankStr);
        rankText.setFont(Theme.font(32, FontWeight.BOLD));
        rankText.setFill(Color.WHITE);
        rankText.setOpacity(0.8);

        box.getChildren().addAll(label, title, rankText);
        return box;
    }

    private String calculateRank(boolean isVictory, double accuracy) {
        if (!isVictory) return "E";
        if (accuracy > 80) return "S";
        if (accuracy > 60) return "A";
        if (accuracy > 40) return "B";
        if (accuracy > 20) return "C";
        return "E";
    }

    private HBox buildStatsArea(int shots, int hits, int streak) {
        HBox box = new HBox(60);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.setStyle(Theme.STATS_BOX);

        double acc = shots > 0 ? (double) hits / shots * 100 : 0;

        box.getChildren().addAll(
                createLargeStat("PROJECTILES", String.valueOf(shots)),
                createDivider(),
                createLargeStat("IMPACTS", String.valueOf(hits)),
                createDivider(),
                createLargeStat("PRÉCISION", String.format("%.1f%%", acc)),
                createDivider(),
                createStreakStat(streak)
        );

        return box;
    }

    /**
     * Crée un bloc statistique stylisé pour la série de victoires.
     * @param streak Valeur de la série.
     * @return Un conteneur VBox avec un effet visuel de "feu".
     */
    private VBox createStreakStat(int streak) {
        VBox v = new VBox(5);
        v.setAlignment(Pos.CENTER);

        Text l = new Text("SÉRIE");
        l.setFill(Theme.TEXT_DIMMED);
        l.setFont(Theme.font(16, FontWeight.NORMAL));

        HBox valueBox = new HBox(15);
        valueBox.setAlignment(Pos.CENTER);

        Text val = new Text(String.valueOf(streak));
        val.setFont(Theme.mono(48, FontWeight.BOLD));

        SVGPath icon = new SVGPath();
        icon.setContent("M12,2 C12,2 10.5,7 12,11 C13.5,15 17,15 17,21 " +
                "C17,25.5 13,29 8,29 C3,29 -1,25.5 -1,21 " +
                "C-1,15.5 3,11 8,2 C8,2 6.5,7.5 8,11.5 " +
                "C9.5,15.5 12,15.5 12,2 Z");

        icon.setScaleX(1.8);
        icon.setScaleY(1.8);

        if (streak > 2) {
            Color gold = Color.web("#fbc531");
            val.setFill(gold);
            icon.setFill(gold);
            val.setEffect(Theme.GLOW_SMALL);
            icon.setEffect(Theme.GLOW_SMALL);

            ScaleTransition pulse = new ScaleTransition(Duration.seconds(0.6), icon);
            pulse.setFromX(1.8); pulse.setFromY(1.8);
            pulse.setToX(2.1); pulse.setToY(2.1);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.setAutoReverse(true);
            pulse.play();
        } else {
            val.setFill(Color.WHITE);
            icon.setFill(themeColor);
        }

        valueBox.getChildren().addAll(val, icon);
        v.getChildren().addAll(l, valueBox);
        return v;
    }

    private VBox createLargeStat(String label, String value) {
        VBox v = new VBox(10);
        v.setAlignment(Pos.CENTER);
        Text l = new Text(label);
        l.setFill(Theme.TEXT_DIMMED);
        l.setFont(Theme.font(16, FontWeight.NORMAL));

        Text val = new Text(value);
        val.setFill(Color.WHITE);
        val.setFont(Theme.mono(48, FontWeight.BOLD));

        v.getChildren().addAll(l, val);
        return v;
    }

    private Line createDivider() {
        Line l = new Line(0, 0, 0, 60);
        l.setStroke(Theme.DIVIDER);
        l.setStrokeWidth(2);
        return l;
    }

    private void animateEntrance(VBox root) {
        root.setOpacity(0);
        root.setScaleX(0.9);
        root.setScaleY(0.9);

        FadeTransition ft = new FadeTransition(Duration.seconds(0.8), root);
        ft.setToValue(1);

        ScaleTransition st = new ScaleTransition(Duration.seconds(0.8), root);
        st.setToX(1); st.setToY(1);

        new ParallelTransition(ft, st).play();
    }
}