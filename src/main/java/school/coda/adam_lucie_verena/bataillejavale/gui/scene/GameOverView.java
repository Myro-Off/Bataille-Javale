package school.coda.adam_lucie_verena.bataillejavale.gui.scene;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import school.coda.adam_lucie_verena.bataillejavale.gui.AssetsManager;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.MenuButton;
import school.coda.adam_lucie_verena.bataillejavale.gui.Theme;

/**
 * Ecran de fin de partie affichant le rapport de mission, les statistiques et le grade obtenu.
 */
public class GameOverView extends StackPane {

    private final Color themeColor;

    /**
     * Initialise la vue de fin de partie avec les résultats du combat.
     * @param isVictory Indique si le joueur a gagné.
     * @param shots Nombre total de tirs effectués.
     * @param hits Nombre total de tirs réussis.
     * @param streak Série de victoires actuelle.
     * @param onRestart Action pour relancer une partie.
     * @param onMenu Action pour retourner au menu principal.
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
        actions.setOnMouseClicked(_ -> AssetsManager.playMusic("mainmenu.mp3", 0.2));

        root.getChildren().addAll(header, statsArea, actions);
        getChildren().addAll(bg, root);

        animateEntrance(root);
    }

    /**
     * Construit l'en-tête contenant le titre de mission et le grade.
     */
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

    /**
     * Détermine le grade en fonction du résultat et de la précision.
     */
    private String calculateRank(boolean isVictory, double accuracy) {
        if (!isVictory) return "E";
        if (accuracy > 80) return "S";
        if (accuracy > 60) return "A";
        if (accuracy > 40) return "B";
        if (accuracy > 20) return "C";
        return "E";
    }

    /**
     * Construit la zone centrale affichant les compteurs statistiques.
     */
    private HBox buildStatsArea(int shots, int hits, int streak) {
        HBox box = new HBox(80);
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
                createLargeStat("SERIE", String.valueOf(streak))
        );

        return box;
    }

    /**
     * Crée un bloc statistique individuel.
     */
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

    /**
     * Crée une ligne de séparation verticale pour les statistiques.
     */
    private Line createDivider() {
        Line l = new Line(0, 0, 0, 60);
        l.setStroke(Theme.DIVIDER);
        l.setStrokeWidth(2);
        return l;
    }

    /**
     * Exécute les transitions d'opacité et d'échelle à l'apparition de la vue.
     */
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