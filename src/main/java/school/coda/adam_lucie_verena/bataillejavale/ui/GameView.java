package school.coda.adam_lucie_verena.bataillejavale.ui;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import school.coda.adam_lucie_verena.bataillejavale.core.model.Board;
import school.coda.adam_lucie_verena.bataillejavale.core.model.Coordinate;
import school.coda.adam_lucie_verena.bataillejavale.core.model.GameConfig;
import school.coda.adam_lucie_verena.bataillejavale.core.model.Ship;
import school.coda.adam_lucie_verena.bataillejavale.core.model.Orientation;
import javafx.scene.shape.Circle;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.effect.DropShadow;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Gère l'affichage graphique de la grille de jeu.
 * Utilise un système de calques pour superposer l'eau, les navires et les impacts.
 */
public class GameView extends Group {

    private static final int CELL_SIZE = 40;
    private final Board board;
    private final GameConfig config;
    private final Rectangle[][] cells;

    // Calques organisés par profondeur (Z-index)
    private final Group gridLayer = new Group();
    private final Group shipsLayer = new Group();
    private final Group markersLayer = new Group();

    /** Mémorise les tirs déjà animés pour éviter de relancer les effets au rafraîchissement. */
    private final Set<Coordinate> animatedCoords = new HashSet<>();

    public GameView(Board board, GameConfig config) {
        this.board = board;
        this.config = config;
        this.cells = new Rectangle[config.gridWidth()][config.gridHeight()];

        // L'ordre d'ajout définit la superposition : markersLayer sera toujours au-dessus.
        this.getChildren().addAll(gridLayer, shipsLayer, markersLayer);
        this.generateGrid();
    }

    /**
     * Initialise le fond bleu de la grille.
     */
    private void generateGrid() {
        for (int x = 0; x < config.gridWidth(); x++) {
            for (int y = 0; y < config.gridHeight(); y++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                cell.setX(x * CELL_SIZE);
                cell.setY(y * CELL_SIZE);
                cell.setFill(Color.web("#2b6da3"));
                cell.setStroke(Color.WHITE);
                cell.setStrokeWidth(0.5);

                cells[x][y] = cell;
                gridLayer.getChildren().add(cell);
            }
        }
    }

    /**
     * Synchronise la vue avec l'état du modèle.
     * Vide et reconstruit le calque des marqueurs pour maintenir la fluidité.
     */
    public void updateDisplay() {
        markersLayer.getChildren().clear();

        for (Coordinate miss : board.getMissedShots()) {
            cells[miss.x()][miss.y()].setFill(Color.web("#1a4a73"));
            addMissMarker(miss.x(), miss.y(), animatedCoords.add(miss));
        }

        for (Coordinate hit : board.getHitShots()) {
            addHitMarker(hit.x(), hit.y(), animatedCoords.add(hit));
        }
    }

    /**
     * Ajoute un effet visuel d'onde pour un tir manqué.
     */
    private void addMissMarker(int x, int y, boolean shouldAnimate) {
        double centerX = CELL_SIZE / 2.0;
        double centerY = CELL_SIZE / 2.0;

        Circle splash = new Circle(centerX, centerY, 3, Color.WHITE);
        Circle ripple1 = new Circle(centerX, centerY, CELL_SIZE * 0.2, Color.TRANSPARENT);
        ripple1.setStroke(Color.web("#7ed6df"));
        ripple1.setStrokeWidth(1.5);

        Circle ripple2 = new Circle(centerX, centerY, CELL_SIZE * 0.35, Color.TRANSPARENT);
        ripple2.setStroke(Color.web("#e0f7fa"));
        ripple2.setStrokeWidth(1);
        ripple2.setOpacity(0.6);

        Group missIcon = new Group(ripple2, ripple1, splash);
        missIcon.setTranslateX(x * CELL_SIZE);
        missIcon.setTranslateY(y * CELL_SIZE);

        markersLayer.getChildren().add(missIcon);

        if (shouldAnimate) {
            FXGL.animationBuilder()
                    .duration(Duration.seconds(0.4))
                    .scale(missIcon)
                    .from(new Point2D(0.6, 0.6))
                    .to(new Point2D(1.0, 1.0))
                    .buildAndPlay();
        }
    }

    /**
     * Ajoute un marqueur d'impact enflammé et génère des particules d'étincelles.
     */
    private void addHitMarker(int x, int y, boolean shouldAnimate) {
        double centerX = CELL_SIZE / 2.0;
        double centerY = CELL_SIZE / 2.0;
        double radius = CELL_SIZE * 0.4;

        Circle ring = new Circle(centerX, centerY, radius, Color.TRANSPARENT);
        ring.setStroke(Color.ORANGERED);
        ring.setStrokeWidth(2);

        RadialGradient fireGradient = new RadialGradient(
                0, 0, centerX, centerY, radius, false, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#fbc531")),
                new Stop(0.3, Color.ORANGERED),
                new Stop(0.8, Color.RED),
                new Stop(1.0, Color.TRANSPARENT)
        );
        Circle glowingCenter = new Circle(centerX, centerY, radius * 0.8, fireGradient);

        Group impactIcon = new Group(glowingCenter, ring);

        // Effet de lueur subtil pour l'impact
        DropShadow subtleGlow = new DropShadow();
        subtleGlow.setColor(Color.rgb(255, 140, 0, 0.4));
        subtleGlow.setRadius(15);

        impactIcon.setEffect(subtleGlow);
        impactIcon.setTranslateX(x * CELL_SIZE);
        impactIcon.setTranslateY(y * CELL_SIZE);

        markersLayer.getChildren().add(impactIcon);

        if (shouldAnimate) {
            FXGL.animationBuilder()
                    .duration(Duration.seconds(0.3))
                    .scale(impactIcon)
                    .from(new Point2D(0.4, 0.4))
                    .to(new Point2D(1.0, 1.0))
                    .buildAndPlay();

            // Génération d'étincelles JavaFX natives
            for (int i = 0; i < 8; i++) {
                Circle spark = new Circle(1.5, Math.random() > 0.5 ? Color.YELLOW : Color.ORANGERED);
                spark.setTranslateX((x * CELL_SIZE) + centerX);
                spark.setTranslateY((y * CELL_SIZE) + centerY);

                markersLayer.getChildren().add(spark);

                double angle = Math.random() * 2 * Math.PI;
                double distance = 10 + Math.random() * 20;

                TranslateTransition move = new TranslateTransition(Duration.seconds(0.4), spark);
                move.setByX(Math.cos(angle) * distance);
                move.setByY(Math.sin(angle) * distance);

                FadeTransition fade = new FadeTransition(Duration.seconds(0.4), spark);
                fade.setToValue(0);

                ParallelTransition explosion = new ParallelTransition(move, fade);
                explosion.setOnFinished(_ -> markersLayer.getChildren().remove(spark));
                explosion.play();
            }
        }
    }

    /**
     * Affiche tous les navires sur le plateau (utilisé pour la flotte joueur ou la révélation finale).
     */
    public void revealShips() {
        shipsLayer.getChildren().clear();
        board.getShips().forEach(this::addShipTexture);
    }

    /**
     * Charge la texture d'un navire et applique la correction mathématique de rotation.
     */
    private void addShipTexture(Ship ship) {
        String imageName = ship.getType().name().toLowerCase() + ".png";
        String path = "/assets/textures/" + imageName;
        URL url = getClass().getResource(path);

        if (url == null) return;

        Image image = new Image(url.toExternalForm());
        ImageView imageView = new ImageView(image);

        int shipLength = ship.getOccupiedCoordinates().size();
        imageView.setFitWidth(CELL_SIZE);
        imageView.setFitHeight(CELL_SIZE * shipLength);
        imageView.setPreserveRatio(false);

        double x = ship.getOccupiedCoordinates().getFirst().x() * CELL_SIZE;
        double y = ship.getOccupiedCoordinates().getFirst().y() * CELL_SIZE;

        // Rotation avec compensation du pivot pour rester aligné sur la grille
        if (ship.getOrientation() == Orientation.HORIZONTAL) {
            imageView.setRotate(90);
            double diff = (imageView.getFitHeight() - imageView.getFitWidth()) / 2.0;
            imageView.setTranslateX(x + diff);
            imageView.setTranslateY(y - diff);
        } else {
            imageView.setTranslateX(x);
            imageView.setTranslateY(y);
        }

        shipsLayer.getChildren().add(imageView);
    }

    public int getCellSize() {
        return CELL_SIZE;
    }
}