package school.coda.adam_lucie_verena.bataillejavale.gui.scene;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import school.coda.adam_lucie_verena.bataillejavale.gui.Theme;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.MenuButton;

public class MainMenuView extends StackPane {

    private final Pane parallaxLayer = new Pane();

    public MainMenuView(Runnable onPlay, Runnable onStats) {
        setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());

        // 1. Fond Néon Profond
        Rectangle bg = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight(),
                new RadialGradient(0, 0, 0.5, 0.5, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#0a192f")), new Stop(1, Color.web("#020617"))));

        // 2. Grille de fond animée
        Node grid = createAnimatedGrid();

        // 3. Layer de Parallaxe (Les particules du message précédent)
        parallaxLayer.setMouseTransparent(true);
        setupParallax();

        // 4. Crédits Développeurs (Adam & Lucie)
        VBox credits = createCredits();

        // 5. Contenu Principal
        VBox ui = new VBox(60, createTitle(), createMenu(onPlay, onStats));
        ui.setAlignment(Pos.CENTER);

        getChildren().addAll(bg, grid, parallaxLayer, credits, ui);
    }

    private Node createAnimatedGrid() {
        Pane grid = new Pane();
        grid.setOpacity(0.1);
        for (int i = 0; i < FXGL.getAppWidth(); i += 50) {
            Line l = new Line(i, 0, i, FXGL.getAppHeight());
            l.setStroke(Theme.CYAN);
            grid.getChildren().add(l);
        }
        for (int i = 0; i < FXGL.getAppHeight(); i += 50) {
            Line l = new Line(0, i, FXGL.getAppWidth(), i);
            l.setStroke(Theme.CYAN);
            grid.getChildren().add(l);
        }
        // Animation de défilement de la grille
        TranslateTransition tt = new TranslateTransition(Duration.seconds(10), grid);
        tt.setByY(-50);
        tt.setInterpolator(Interpolator.LINEAR);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.play();
        return grid;
    }

    private void setupParallax() {
        this.setOnMouseMoved(e -> {
            double x = (e.getX() - (double) FXGL.getAppWidth() /2) / 50;
            double y = (e.getY() - (double) FXGL.getAppHeight() / 2) / 50;
            parallaxLayer.setTranslateX(x);
            parallaxLayer.setTranslateY(y);
        });
    }

    private VBox createCredits() {
        VBox box = new VBox(2);
        box.setPadding(new Insets(30));

        Text auth = new Text("AUTHORIZED ARCHITECTS:");
        auth.setFont(Theme.mono(10, FontWeight.BOLD));
        auth.setFill(Theme.CYAN);
        auth.setOpacity(0.5);

        Text dev = new Text("ADAM // LUCIE");
        dev.setFont(Theme.mono(14, FontWeight.BLACK));
        dev.setFill(Color.WHITE);
        dev.setEffect(new Glow(0.8));

        box.getChildren().addAll(auth, dev);
        StackPane.setAlignment(box, Pos.BOTTOM_RIGHT);
        return box;
    }

    private Node createTitle() {
        VBox box = new VBox(-10);
        box.setAlignment(Pos.CENTER);
        Text t1 = new Text("BATAILLE");
        t1.setFont(Theme.font(100, FontWeight.BLACK));
        t1.setFill(Color.WHITE);
        Text t2 = new Text("JAVALE");
        t2.setFont(Theme.font(100, FontWeight.BLACK));
        t2.setFill(Theme.CYAN);
        t2.setEffect(new Glow(1.0));
        box.getChildren().addAll(t1, t2);
        return box;
    }

    private VBox createMenu(Runnable onPlay, Runnable onStats) {
        VBox menu = new VBox(25,
                new MenuButton("Lancer l'assaut", onPlay),
                new MenuButton("Archives Trophées", onStats),
                new MenuButton("Quitter", () -> System.exit(0))
        );
        menu.setAlignment(Pos.CENTER);
        return menu;
    }
}