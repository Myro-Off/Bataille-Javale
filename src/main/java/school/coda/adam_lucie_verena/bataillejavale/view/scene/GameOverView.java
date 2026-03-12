package school.coda.adam_lucie_verena.bataillejavale.view.scene;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import school.coda.adam_lucie_verena.bataillejavale.view.component.MenuButton;

public class GameOverView extends StackPane {

    private final Color themeColor;

    public GameOverView(boolean isVictory, int shots, int hits, Runnable onRestart, Runnable onMenu) {
        this.themeColor = isVictory ? Color.web("#00d2d3") : Color.web("#ff4757");
        setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());

        Rectangle bg = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight(), Color.rgb(2, 6, 23, 0.9));

        // --- CONTENEUR PRINCIPAL ---
        VBox root = new VBox(50);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(100));

        // 1. HEADER (Titre + Grade)
        VBox header = buildHeader(isVictory, shots, hits);

        // 2. STATS (Plus larges)
        HBox statsArea = buildStatsArea(shots, hits);

        // 3. ACTIONS
        HBox actions = new HBox(30,
                new MenuButton("REJOUER L'ASSAUT", onRestart),
                new MenuButton("MENU PRINCIPAL", onMenu)
        );
        actions.setAlignment(Pos.CENTER);

        root.getChildren().addAll(header, statsArea, actions);
        getChildren().addAll(bg, root);

        // Animation d'entrée
        root.setOpacity(0);
        root.setScaleX(0.9);
        root.setScaleY(0.9);

        FadeTransition ft = new FadeTransition(Duration.seconds(0.8), root);
        ft.setToValue(1);

        ScaleTransition st = new ScaleTransition(Duration.seconds(0.8), root);
        st.setToX(1); st.setToY(1);

        new ParallelTransition(ft, st).play();
    }

    private VBox buildHeader(boolean isVictory, int shots, int hits) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);

        Text label = new Text(isVictory ? "RAPPORT DE VICTOIRE" : "RAPPORT D'ÉCHEC");
        label.setFill(Color.web("#94a3b8"));
        label.setFont(Font.font("Verdana", 20));

        Text title = new Text(isVictory ? "MISSION ACCOMPLIE" : "FLOTTE DÉMANTELÉE");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 72));
        title.setFill(themeColor);
        title.setEffect(new DropShadow(20, themeColor));

        // Système de Grade
        double acc = shots > 0 ? (double) hits / shots * 100 : 0;
        String rankStr = "E";
        if (isVictory) {
            if (acc > 80) rankStr = "S";
            else if (acc > 60) rankStr = "A";
            else if (acc > 40) rankStr = "B";
            else rankStr = "C";
        }

        Text rankText = new Text("GRADE : " + rankStr);
        rankText.setFont(Font.font("Verdana", FontWeight.BOLD, 32));
        rankText.setFill(Color.WHITE);
        rankText.setOpacity(0.8);

        box.getChildren().addAll(label, title, rankText);
        return box;
    }

    private HBox buildStatsArea(int shots, int hits) {
        HBox box = new HBox(80);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 20;");

        double acc = shots > 0 ? (double) hits / shots * 100 : 0;

        box.getChildren().addAll(
                createLargeStat("PROJECTILES", String.valueOf(shots)),
                createDivider(),
                createLargeStat("IMPACTS", String.valueOf(hits)),
                createDivider(),
                createLargeStat("PRÉCISION", String.format("%.1f%%", acc))
        );

        return box;
    }

    private VBox createLargeStat(String label, String value) {
        VBox v = new VBox(10);
        v.setAlignment(Pos.CENTER);
        Text l = new Text(label); l.setFill(Color.web("#64748b")); l.setFont(Font.font(16));
        Text val = new Text(value); val.setFill(Color.WHITE); val.setFont(Font.font("Monospaced", FontWeight.BOLD, 48));
        v.getChildren().addAll(l, val);
        return v;
    }

    private Line createDivider() {
        Line l = new Line(0, 0, 0, 60);
        l.setStroke(Color.web("#334155"));
        l.setStrokeWidth(2);
        return l;
    }
}