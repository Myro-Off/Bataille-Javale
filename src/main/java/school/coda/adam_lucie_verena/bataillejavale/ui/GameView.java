package school.coda.adam_lucie_verena.bataillejavale.ui;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.*;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import school.coda.adam_lucie_verena.bataillejavale.core.model.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class GameView extends Pane {

    private static final int CELL_SIZE = 40;
    private static final int OFFSET = 40;

    private final Board board;
    private final boolean isPlayerView;

    private final Group contentGroup = new Group();
    private final Group gridLayer = new Group();
    private final Group shipsLayer = new Group();
    private final Group markersLayer = new Group();

    private final Rectangle highlightRow = new Rectangle();
    private final Rectangle highlightCol = new Rectangle();
    private final Text hoverCoord = new Text();

    private final Set<Coordinate> animatedCoords = new HashSet<>();

    /**
     * Initialise la vue tactique et configure la hiérarchie visuelle.
     * @param board Le plateau de jeu associé.
     * @param isPlayerView Définit si les navires non coulés doivent être visibles.
     */
    public GameView(Board board, boolean isPlayerView) {
        this.board = board;
        this.isPlayerView = isPlayerView;

        double w = board.getWidth() * CELL_SIZE + OFFSET;
        double h = board.getHeight() * CELL_SIZE + OFFSET;

        setMinWidth(w); setMinHeight(h);
        setPrefSize(w, h);
        setMaxSize(w, h);

        contentGroup.getChildren().addAll(gridLayer, highlightRow, highlightCol, shipsLayer, markersLayer);
        this.getChildren().addAll(contentGroup, hoverCoord);

        initHoverEffects();
        generateGrid();
        updateDisplay();
    }

    /**
     * Génère graphiquement les cellules de la grille et les libellés (A-Z, 1-N).
     */
    private void generateGrid() {
        gridLayer.getChildren().clear();
        Font coordFont = Font.font("Verdana", FontWeight.BOLD, 12);

        for (int x = 0; x < board.getWidth(); x++) {
            Text tLetter = new Text(String.valueOf((char) ('A' + x)));
            tLetter.setFill(Color.web("#00d2d3", 0.7));
            tLetter.setFont(coordFont);
            tLetter.setX(x * CELL_SIZE + OFFSET + 15);
            tLetter.setY(25);
            gridLayer.getChildren().add(tLetter);

            for (int y = 0; y < board.getHeight(); y++) {
                if (x == 0) {
                    Text tNum = new Text(String.valueOf(y + 1));
                    tNum.setFill(Color.web("#00d2d3", 0.7));
                    tNum.setFont(coordFont);
                    tNum.setX(10);
                    tNum.setY(y * CELL_SIZE + OFFSET + 25);
                    gridLayer.getChildren().add(tNum);
                }

                Rectangle cell = new Rectangle(x * CELL_SIZE + OFFSET, y * CELL_SIZE + OFFSET, CELL_SIZE, CELL_SIZE);
                cell.setFill(Color.web("#0f172a", 0.6));
                cell.setStroke(Color.web("#00d2d3", 0.2));
                gridLayer.getChildren().add(cell);
            }
        }
    }

    /**
     * Initialise les détecteurs de survol pour l'affichage du crosshair et des coordonnées.
     */
    private void initHoverEffects() {
        highlightRow.setFill(Color.web("#00d2d3", 0.15));
        highlightCol.setFill(Color.web("#00d2d3", 0.15));
        highlightRow.setMouseTransparent(true);
        highlightCol.setMouseTransparent(true);
        highlightRow.setVisible(false);
        highlightCol.setVisible(false);

        highlightRow.setManaged(false);
        highlightCol.setManaged(false);
        hoverCoord.setManaged(false);

        hoverCoord.setFill(Color.WHITE);
        hoverCoord.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        hoverCoord.setMouseTransparent(true);
        hoverCoord.setVisible(false);

        this.setOnMouseMoved(e -> {
            if (e.getX() >= OFFSET && e.getY() >= OFFSET) {
                Coordinate c = getGridCoordinate(e.getX(), e.getY());

                if (!board.isNotWithinBounds(c)) {
                    highlightRow.setVisible(true);
                    highlightCol.setVisible(true);
                    hoverCoord.setVisible(true);

                    highlightRow.setX(OFFSET);
                    highlightRow.setY(c.y() * CELL_SIZE + OFFSET);
                    highlightRow.setWidth(board.getWidth() * CELL_SIZE);
                    highlightRow.setHeight(CELL_SIZE);

                    highlightCol.setX(c.x() * CELL_SIZE + OFFSET);
                    highlightCol.setY(OFFSET);
                    highlightCol.setWidth(CELL_SIZE);
                    highlightCol.setHeight(board.getHeight() * CELL_SIZE);

                    hoverCoord.setText((char)('A' + c.x()) + " : " + (c.y() + 1));

                    double maxW = board.getWidth() * CELL_SIZE + OFFSET;
                    if (e.getX() + 70 > maxW) {
                        hoverCoord.setX(e.getX() - 60);
                    } else {
                        hoverCoord.setX(e.getX() + 15);
                    }

                    if (e.getY() - 15 < 15) {
                        hoverCoord.setY(e.getY() + 25);
                    } else {
                        hoverCoord.setY(e.getY() - 15);
                    }
                } else {
                    hideHighlights();
                }
            } else {
                hideHighlights();
            }
        });

        this.setOnMouseExited(_ -> hideHighlights());
    }

    /**
     * Réinitialise la visibilité des éléments de survol.
     */
    private void hideHighlights() {
        highlightRow.setVisible(false);
        highlightCol.setVisible(false);
        hoverCoord.setVisible(false);
    }

    /**
     * Applique un facteur d'échelle à la vue pour s'adapter au conteneur parent.
     * @param maxWidth Largeur maximale autorisée.
     * @param maxHeight Hauteur maximale autorisée.
     */
    public void autoScale(double maxWidth, double maxHeight) {
        double w = board.getWidth() * CELL_SIZE + OFFSET;
        double h = board.getHeight() * CELL_SIZE + OFFSET;
        double scale = Math.min(1.0, Math.min(maxWidth / w, maxHeight / h));
        this.getTransforms().setAll(new Scale(scale, scale, 0, 0));
    }

    /**
     * Convertit une position en pixels sur la vue en une coordonnée logique de grille.
     * @param localX Position X en pixels.
     * @param localY Position Y en pixels.
     * @return La coordonnée correspondante sur le plateau.
     */
    public Coordinate getGridCoordinate(double localX, double localY) {
        int gx = (int) ((localX - OFFSET) / CELL_SIZE);
        int gy = (int) ((localY - OFFSET) / CELL_SIZE);
        return new Coordinate(gx, gy);
    }

    /**
     * Synchronise l'affichage avec l'état actuel du plateau (navires et tirs).
     */
    public void updateDisplay() {
        markersLayer.getChildren().clear();
        shipsLayer.getChildren().clear();

        for (Ship ship : board.getShips()) {
            if (isPlayerView || ship.isSunk()) drawShipTexture(ship);
        }

        for (Coordinate miss : board.getMissedShots()) {
            addMissMarker(miss.x(), miss.y(), animatedCoords.add(miss));
        }

        for (Coordinate hit : board.getHitShots()) {
            boolean isNew = animatedCoords.add(hit);
            addHitMarker(hit.x(), hit.y(), isNew);
            if (isNew) shakeGrid();
        }
    }

    /**
     * Crée et anime un marqueur visuel pour un tir manqué.
     * @param x Coordonnée X de la grille.
     * @param y Coordonnée Y de la grille.
     * @param animate Indique si l'animation d'apparition doit être jouée.
     */
    private void addMissMarker(int x, int y, boolean animate) {
        double c = CELL_SIZE / 2.0;
        Circle splash = new Circle(c, c, 3, Color.WHITE);
        Circle ripple = new Circle(c, c, CELL_SIZE * 0.2, Color.TRANSPARENT);
        ripple.setStroke(Color.web("#7ed6df"));
        ripple.setStrokeWidth(1.5);

        Group g = new Group(ripple, splash);
        g.setLayoutX(x * CELL_SIZE + OFFSET);
        g.setLayoutY(y * CELL_SIZE + OFFSET);
        markersLayer.getChildren().add(g);

        if (animate) {
            FXGL.animationBuilder().duration(Duration.seconds(0.4))
                    .scale(g).from(new Point2D(0.6, 0.6)).to(new Point2D(1, 1)).buildAndPlay();
        }
    }

    /**
     * Crée un marqueur visuel pour un tir réussi.
     * @param x Coordonnée X de la grille.
     * @param y Coordonnée Y de la grille.
     * @param animate Indique si l'animation d'impact doit être déclenchée.
     */
    private void addHitMarker(int x, int y, boolean animate) {
        double center = CELL_SIZE / 2.0;
        double radius = CELL_SIZE * 0.4;
        RadialGradient grad = new RadialGradient(0, 0, center, center, radius, false,
                javafx.scene.paint.CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#fbc531")), new Stop(0.3, Color.ORANGERED), new Stop(1.0, Color.TRANSPARENT));

        Circle glowingCenter = new Circle(center, center, radius * 0.8, grad);
        glowingCenter.setEffect(new DropShadow(15, Color.rgb(255, 140, 0, 0.5)));

        glowingCenter.setLayoutX(x * CELL_SIZE + OFFSET);
        glowingCenter.setLayoutY(y * CELL_SIZE + OFFSET);
        markersLayer.getChildren().add(glowingCenter);

        if (animate) animateSparks(x, y, center);
    }

    /**
     * Génère une animation de particules d'étincelles lors d'un impact.
     * @param x Coordonnée X.
     * @param y Coordonnée Y.
     * @param center Décalage vers le centre de la cellule.
     */
    private void animateSparks(int x, int y, double center) {
        for (int i = 0; i < 8; i++) {
            Circle spark = new Circle(1.5, Math.random() > 0.5 ? Color.YELLOW : Color.ORANGERED);
            spark.setLayoutX((x * CELL_SIZE) + OFFSET + center);
            spark.setLayoutY((y * CELL_SIZE) + OFFSET + center);
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
     * Déclenche une secousse visuelle rapide sur l'ensemble des calques de contenu.
     */
    private void shakeGrid() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), contentGroup);
        tt.setFromX(0);
        tt.setByX(4);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.play();
    }

    /**
     * Dessine la texture d'un navire et applique des effets selon son état (ex: coulé).
     * @param s Le navire à représenter.
     */
    private void drawShipTexture(Ship s) {
        String path = "/assets/textures/" + s.getType().name().toLowerCase() + ".png";
        ImageView iv = new ImageView(new Image(Objects.requireNonNull(getClass().getResource(path)).toExternalForm()));
        iv.setFitWidth(CELL_SIZE);
        iv.setFitHeight(CELL_SIZE * s.getType().getSize());

        Coordinate start = s.getOccupiedCoordinates().getFirst();
        double x = start.x() * CELL_SIZE + OFFSET;
        double y = start.y() * CELL_SIZE + OFFSET;

        if (s.getOrientation() == Orientation.HORIZONTAL) {
            iv.setRotate(90);
            double diff = (iv.getFitHeight() - iv.getFitWidth()) / 2.0;
            iv.setLayoutX(x + diff);
            iv.setLayoutY(y - diff);
        } else {
            iv.setLayoutX(x);
            iv.setLayoutY(y);
        }

        if (s.isSunk()) {
            ColorInput redTint = new ColorInput(
                    0, 0, iv.getFitWidth(), iv.getFitHeight(), new Color(1, 0, 0, 0.6)
            );
            Blend blend = new Blend(BlendMode.SRC_ATOP, null, redTint);
            iv.setEffect(blend);
        }
        shipsLayer.getChildren().add(iv);
    }
}