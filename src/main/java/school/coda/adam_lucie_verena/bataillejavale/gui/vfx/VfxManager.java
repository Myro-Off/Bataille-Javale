package school.coda.adam_lucie_verena.bataillejavale.gui.vfx;

import javafx.animation.*;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import school.coda.adam_lucie_verena.bataillejavale.core.model.Coordinate;
import school.coda.adam_lucie_verena.bataillejavale.core.model.Ship;
import school.coda.adam_lucie_verena.bataillejavale.gui.grid.GameView;

import java.util.List;

/**
 * Gestionnaire d'effets visuels vectoriels pour le combat naval.
 * Assure la conversion des coordonnées locales en positions globales sur une couche de superposition
 * et gère le cycle de vie des animations de particules (explosions, fumée, éclaboussures).
 */
public class VfxManager {

    private static final int CELL_SIZE = 40;
    private static final int OFFSET = 40;
    private static Pane vfxOverlay;

    /**
     * Définit le conteneur de superposition utilisé pour le rendu des effets.
     * @param overlay Le panneau transparent recouvrant l'interface de jeu.
     */
    public static void setExternalOverlay(Pane overlay) {
        vfxOverlay = overlay;
    }

    /**
     * Déclenche l'effet visuel correspondant à l'issue d'un tir.
     * @param view La vue de la grille ciblée.
     * @param target La coordonnée logique de l'impact.
     * @param isHit True si le tir a touché un navire, false pour un tir dans l'eau.
     */
    public static void playShotEffect(GameView view, Coordinate target, boolean isHit) {
        if (vfxOverlay == null) return;

        double lx = target.x() * CELL_SIZE + OFFSET + (CELL_SIZE / 2.0);
        double ly = target.y() * CELL_SIZE + OFFSET + (CELL_SIZE / 2.0);

        Point2D scenePt = view.localToScene(lx, ly);
        Point2D p = vfxOverlay.sceneToLocal(scenePt);

        double gridScale = view.getLocalToSceneTransform().getMxx();

        if (isHit) {
            spawnExplosion(p.getX(), p.getY(), gridScale, Color.ORANGERED, 30, 1.0);
            shake(view, 5);
        } else {
            spawnSplash(p.getX(), p.getY(), gridScale);
        }
    }

    /**
     * Génère une séquence d'explosions et de fumée sur l'ensemble des coordonnées d'un navire coulé.
     * @param view La vue de la grille contenant le navire.
     * @param ship Le navire venant d'être détruit.
     */
    public static void playSunkEffect(GameView view, Ship ship) {
        if (vfxOverlay == null) return;

        List<Coordinate> coords = ship.getOccupiedCoordinates();
        double gridScale = view.getLocalToSceneTransform().getMxx();

        for (int i = 0; i < coords.size(); i++) {
            final int index = i;
            Coordinate c = coords.get(index);

            PauseTransition delay = new PauseTransition(Duration.millis(index * 200));
            delay.setOnFinished(_ -> {
                double lx = c.x() * CELL_SIZE + OFFSET + (CELL_SIZE / 2.0);
                double ly = c.y() * CELL_SIZE + OFFSET + (CELL_SIZE / 2.0);

                Point2D scenePt = view.localToScene(lx, ly);
                Point2D p = vfxOverlay.sceneToLocal(scenePt);

                if (index == coords.size() - 1) {
                    spawnExplosion(p.getX(), p.getY(), gridScale, Color.ORANGERED, 80, 2.0);
                    shake(view, 20);
                } else {
                    spawnExplosion(p.getX(), p.getY(), gridScale, Color.ORANGERED, 40, 1.5);
                    shake(view, 10);
                }
                spawnSmoke(p.getX(), p.getY(), gridScale);
            });
            delay.play();
        }
    }

    /**
     * Crée une explosion de particules radiales.
     * @param x Position X sur l'overlay.
     * @param y Position Y sur l'overlay.
     * @param scale Facteur d'échelle de la grille.
     * @param color Couleur dominante de l'effet.
     * @param count Nombre de particules à générer.
     * @param power Multiplicateur d'intensité de l'explosion.
     */
    private static void spawnExplosion(double x, double y, double scale, Color color, int count, double power) {
        Group g = new Group();
        g.setLayoutX(x); g.setLayoutY(y);
        g.setScaleX(scale); g.setScaleY(scale);

        Circle core = new Circle(0, 0, 12 * power, color);
        core.setEffect(new DropShadow(20 * power, color));

        ScaleTransition st = new ScaleTransition(Duration.millis(200), core);
        st.setFromX(0.2); st.setFromY(0.2); st.setToX(1.2); st.setToY(1.2);
        FadeTransition ft = new FadeTransition(Duration.millis(300), core);
        ft.setToValue(0);

        g.getChildren().add(core);
        st.play(); ft.play();

        for (int i = 0; i < count; i++) {
            Circle spark = new Circle(0, 0, 2.0 * power, Math.random() > 0.4 ? color : Color.YELLOW);
            double angle = Math.random() * Math.PI * 2;
            double dist = 20 + Math.random() * 50;

            TranslateTransition tt = new TranslateTransition(Duration.millis(500), spark);
            tt.setByX(Math.cos(angle) * dist);
            tt.setByY(Math.sin(angle) * dist);

            FadeTransition f = new FadeTransition(Duration.millis(500), spark);
            f.setToValue(0);

            g.getChildren().add(spark);
            tt.play(); f.play();
        }

        vfxOverlay.getChildren().add(g);
        cleanup(g, 1000);
    }

    /**
     * Génère un nuage de fumée vaporeux s'élevant verticalement.
     * @param x Position X sur l'overlay.
     * @param y Position Y sur l'overlay.
     * @param scale Facteur d'échelle de la grille.
     */
    private static void spawnSmoke(double x, double y, double scale) {
        Group g = new Group();
        g.setLayoutX(x); g.setLayoutY(y);
        g.setScaleX(scale); g.setScaleY(scale);

        for (int i = 0; i < 20; i++) {
            Circle s = new Circle(0, 0, 8 + Math.random() * 7, Color.rgb(40, 40, 40, 0.0));
            s.setEffect(new javafx.scene.effect.GaussianBlur(12));

            s.setTranslateX((Math.random() - 0.5) * 10);
            s.setTranslateY((Math.random() - 0.5) * 10);

            double dur = 3500 + Math.random() * 2000;
            double delay = Math.random() * 600;

            TranslateTransition tt = new TranslateTransition(Duration.millis(dur), s);
            tt.setByY(-100 - Math.random() * 100);
            tt.setByX((Math.random() - 0.5) * 120);
            tt.setDelay(Duration.millis(delay));

            ScaleTransition st = new ScaleTransition(Duration.millis(dur), s);
            st.setFromX(0.2); st.setFromY(0.2);
            st.setToX(3.5); st.setToY(3.5);
            st.setDelay(Duration.millis(delay));

            Timeline fadeTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(s.fillProperty(), Color.rgb(40, 40, 40, 0.0))),
                    new KeyFrame(Duration.millis(dur * 0.2), new KeyValue(s.fillProperty(), Color.rgb(40, 40, 40, 0.4))),
                    new KeyFrame(Duration.millis(dur), new KeyValue(s.fillProperty(), Color.rgb(40, 40, 40, 0.0)))
            );
            fadeTimeline.setDelay(Duration.millis(delay));

            g.getChildren().add(s);
            tt.play(); st.play(); fadeTimeline.play();
        }

        vfxOverlay.getChildren().add(g);
        cleanup(g, 6500);
    }

    /**
     * Crée une animation d'éclaboussure bleue pour les tirs manqués.
     * @param x Position X sur l'overlay.
     * @param y Position Y sur l'overlay.
     * @param scale Facteur d'échelle de la grille.
     */
    private static void spawnSplash(double x, double y, double scale) {
        Group g = new Group();
        g.setLayoutX(x); g.setLayoutY(y);
        g.setScaleX(scale); g.setScaleY(scale);

        for (int i = 0; i < 15; i++) {
            Circle d = new Circle(0, 0, 2.5, Color.web("#3498db"));
            double angle = Math.random() * Math.PI * 2;
            double dist = 15 + Math.random() * 25;

            TranslateTransition tt = new TranslateTransition(Duration.millis(400), d);
            tt.setByX(Math.cos(angle) * dist);
            tt.setByY(Math.sin(angle) * dist);

            FadeTransition ft = new FadeTransition(Duration.millis(400), d);
            ft.setToValue(0);

            g.getChildren().add(d);
            tt.play(); ft.play();
        }
        vfxOverlay.getChildren().add(g);
        cleanup(g, 500);
    }

    /**
     * Supprime un groupe d'effets du conteneur après un délai spécifié.
     * @param g Le groupe à retirer.
     * @param ms Délai en millisecondes avant la suppression.
     */
    private static void cleanup(Group g, int ms) {
        PauseTransition p = new PauseTransition(Duration.millis(ms));
        p.setOnFinished(_ -> vfxOverlay.getChildren().remove(g));
        p.play();
    }

    /**
     * Applique une animation de secousse horizontale sur un nœud.
     * @param node Le composant graphique à animer.
     * @param intensity L'amplitude de la secousse.
     */
    private static void shake(Node node, double intensity) {
        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(50), new KeyValue(node.translateXProperty(), intensity)),
                new KeyFrame(Duration.millis(100), new KeyValue(node.translateXProperty(), -intensity)),
                new KeyFrame(Duration.millis(150), new KeyValue(node.translateXProperty(), 0))
        );
        tl.play();
    }
}