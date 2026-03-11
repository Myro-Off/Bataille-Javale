package school.coda.adam_lucie_verena.bataillejavale.ui;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.*;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import school.coda.adam_lucie_verena.bataillejavale.core.model.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Composant graphique représentant la grille tactique (Joueur ou Ennemi).
 * <p>
 * Cette vue gère le rendu multi-couches du champ de bataille :
 * <ul>
 * <li><b>GridLayer :</b> Le fond de grille avec les cellules.</li>
 * <li><b>ShipsLayer :</b> Les textures des navires (visibles pour le joueur, cachées pour l'ennemi sauf si coulés).</li>
 * <li><b>MarkersLayer :</b> Les indicateurs de tirs (impacts et ratés) avec animations.</li>
 * </ul>
 * </p>
 */
public class GameView extends Pane {

    private static final int CELL_SIZE = 40;
    private final Board board;
    private final boolean isPlayerView;

    private final Group gridLayer = new Group();
    private final Group shipsLayer = new Group();
    private final Group markersLayer = new Group();

    /**
     * Ensemble des coordonnées ayant déjà joué leur animation d'apparition.
     * Évite de rejouer l'animation à chaque appel de {@link #updateDisplay()}.
     */
    private final Set<Coordinate> animatedCoords = new HashSet<>();

    /**
     * Initialise une nouvelle vue de grille.
     * @param board        Le modèle de plateau à observer.
     * @param isPlayerView True si c'est la grille du joueur (affiche les navires dès le début).
     */
    public GameView(Board board, boolean isPlayerView) {
        this.board = board;
        this.isPlayerView = isPlayerView;

        // Configuration de la taille fixe pour éviter tout décalage de mise en page
        int w = board.getWidth() * CELL_SIZE;
        int h = board.getHeight() * CELL_SIZE;
        setMinWidth(w); setMinHeight(h);
        setPrefSize(w, h);
        setMaxSize(w, h);

        this.getChildren().addAll(gridLayer, shipsLayer, markersLayer);
        generateGrid();
        updateDisplay();
    }

    /**
     * Dessine la structure de base de la grille (rectangles bleus sombres).
     */
    private void generateGrid() {
        gridLayer.getChildren().clear();
        for (int x = 0; x < board.getWidth(); x++) {
            for (int y = 0; y < board.getHeight(); y++) {
                Rectangle cell = new Rectangle(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                cell.setFill(Color.web("#0f172a", 0.6));
                cell.setStroke(Color.web("#00d2d3", 0.15));
                gridLayer.getChildren().add(cell);
            }
        }
    }

    /**
     * Rafraîchit l'intégralité des éléments dynamiques (navires et tirs).
     * <p>
     * Cette méthode reconstruit les couches de navires et de marqueurs en fonction
     * de l'état actuel du {@link Board}.
     * </p>
     */
    public void updateDisplay() {
        markersLayer.getChildren().clear();
        shipsLayer.getChildren().clear();

        // Affichage des navires
        for (Ship ship : board.getShips()) {
            if (isPlayerView || ship.isSunk()) drawShipTexture(ship);
        }

        // Affichage des tirs ratés
        for (Coordinate miss : board.getMissedShots()) {
            addMissMarker(miss.x(), miss.y(), animatedCoords.add(miss));
        }

        // Affichage des impacts réussis
        for (Coordinate hit : board.getHitShots()) {
            addHitMarker(hit.x(), hit.y(), animatedCoords.add(hit));
        }
    }

    /**
     * Ajoute un indicateur visuel de tir raté (effet de "splash" dans l'eau).
     * @param x       Coordonnée X sur la grille.
     * @param y       Coordonnée Y sur la grille.
     * @param animate True pour jouer l'animation de mise à l'échelle.
     */
    private void addMissMarker(int x, int y, boolean animate) {
        double center = CELL_SIZE / 2.0;
        Circle splash = new Circle(center, center, 3, Color.WHITE);
        Circle ripple1 = new Circle(center, center, CELL_SIZE * 0.2, Color.TRANSPARENT);
        ripple1.setStroke(Color.web("#7ed6df"));
        ripple1.setStrokeWidth(1.5);

        Group missIcon = new Group(ripple1, splash);
        missIcon.setLayoutX(x * CELL_SIZE);
        missIcon.setLayoutY(y * CELL_SIZE);
        markersLayer.getChildren().add(missIcon);

        if (animate) {
            FXGL.animationBuilder().duration(Duration.seconds(0.4))
                    .scale(missIcon).from(new Point2D(0.6, 0.6)).to(new Point2D(1, 1)).buildAndPlay();
        }
    }

    /**
     * Ajoute un indicateur visuel d'impact (centre incandescent et lueur).
     * @param x       Coordonnée X sur la grille.
     * @param y       Coordonnée Y sur la grille.
     * @param animate True pour déclencher l'animation d'étincelles.
     */
    private void addHitMarker(int x, int y, boolean animate) {
        double center = CELL_SIZE / 2.0;
        double radius = CELL_SIZE * 0.4;

        RadialGradient grad = new RadialGradient(0, 0, center, center, radius, false, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#fbc531")), new Stop(0.3, Color.ORANGERED), new Stop(1.0, Color.TRANSPARENT));

        Circle glowingCenter = new Circle(center, center, radius * 0.8, grad);
        DropShadow glow = new DropShadow(15, Color.rgb(255, 140, 0, 0.5));
        glowingCenter.setEffect(glow);

        glowingCenter.setLayoutX(x * CELL_SIZE);
        glowingCenter.setLayoutY(y * CELL_SIZE);
        markersLayer.getChildren().add(glowingCenter);

        if (animate) {
            animateSparks(x, y, center);
        }
    }

    /**
     * Génère une petite explosion de particules (étincelles) au point d'impact.
     */
    private void animateSparks(int x, int y, double center) {
        for (int i = 0; i < 8; i++) {
            Circle spark = new Circle(1.5, Math.random() > 0.5 ? Color.YELLOW : Color.ORANGERED);
            spark.setLayoutX((x * CELL_SIZE) + center);
            spark.setLayoutY((y * CELL_SIZE) + center);
            markersLayer.getChildren().add(spark);

            double angle = Math.random() * 2 * Math.PI;
            double dist = 10 + Math.random() * 20;

            TranslateTransition move = new TranslateTransition(Duration.seconds(0.4), spark);
            move.setByX(Math.cos(angle) * dist); move.setByY(Math.sin(angle) * dist);

            FadeTransition fade = new FadeTransition(Duration.seconds(0.4), spark);
            fade.setToValue(0);

            ParallelTransition pt = new ParallelTransition(move, fade);
            pt.setOnFinished(_ -> markersLayer.getChildren().remove(spark));
            pt.play();
        }
    }

    /**
     * Calcule la position et dessine la texture d'un navire.
     * <p>
     * Gère automatiquement la rotation à 90° pour les navires horizontaux.
     * Si le navire est coulé, un filtre rouge semi-transparent est appliqué.
     * </p>
     * @param s Le navire à dessiner.
     */
    private void drawShipTexture(Ship s) {
        String path = "/assets/textures/" + s.getType().name().toLowerCase() + ".png";
        ImageView iv = new ImageView(new Image(Objects.requireNonNull(getClass().getResource(path)).toExternalForm()));
        iv.setFitWidth(CELL_SIZE);
        iv.setFitHeight(CELL_SIZE * s.getType().getSize());

        double x = s.getOccupiedCoordinates().getFirst().x() * CELL_SIZE;
        double y = s.getOccupiedCoordinates().getFirst().y() * CELL_SIZE;

        if (s.getOrientation() == Orientation.HORIZONTAL) {
            iv.setRotate(90);
            /*
             * En JavaFX, la rotation se fait sur le centre.
             * On calcule 'diff' pour recentrer l'image après rotation car sa boîte englobante change.
             */
            double diff = (iv.getFitHeight() - iv.getFitWidth()) / 2.0;
            iv.setLayoutX(x + diff); iv.setLayoutY(y - diff);
        } else {
            iv.setLayoutX(x); iv.setLayoutY(y);
        }

        if (s.isSunk()) {
            // Effet visuel : Teinte rouge pour les navires détruits
            javafx.scene.effect.ColorInput redTint = new javafx.scene.effect.ColorInput(
                    0, 0, iv.getFitWidth(), iv.getFitHeight(), Color.RED
            );
            javafx.scene.effect.Blend blend = new javafx.scene.effect.Blend(
                    javafx.scene.effect.BlendMode.SRC_ATOP,
                    null,
                    redTint
            );
            redTint.setPaint(new Color(1, 0, 0, 0.6));
            iv.setEffect(blend);
        }
        shipsLayer.getChildren().add(iv);
    }

    /**
     * Convertit des coordonnées pixels (clic) en coordonnées grille (0-9).
     * @param localX Position X en pixels.
     * @param localY Position Y en pixels.
     * @return Un objet {@link Coordinate} correspondant.
     */
    public Coordinate getGridCoordinate(double localX, double localY) {
        return new Coordinate((int) (localX / CELL_SIZE), (int) (localY / CELL_SIZE));
    }
}