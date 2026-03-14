package school.coda.adam_lucie_verena.bataillejavale.gui.grid;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.*;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import school.coda.adam_lucie_verena.bataillejavale.core.model.*;
import school.coda.adam_lucie_verena.bataillejavale.gui.Theme;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Vue tactique représentant une grille de bataille navale.
 * Gère le rendu dynamique des navires, les marqueurs d'impact complexes (splash, sparks)
 * et assure la stabilité du layout via un masque de collision (Clip).
 */
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
     * Initialise la vue tactique, configure les calques et verrouille les dimensions.
     * @param board Le plateau de jeu contenant les données métier.
     * @param isPlayerView Définit si le brouillard de guerre est désactivé.
     */
    public GameView(Board board, boolean isPlayerView) {
        this.board = board;
        this.isPlayerView = isPlayerView;

        double w = board.getWidth() * CELL_SIZE + OFFSET;
        double h = board.getHeight() * CELL_SIZE + OFFSET;

        setMinWidth(w); setMinHeight(h);
        setPrefSize(w, h); setMaxSize(w, h);

        // Verrouille la taille pour empêcher le texte de survol ou les particules de déformer l'interface
        this.setClip(new Rectangle(w, h));

        contentGroup.getChildren().addAll(gridLayer, highlightRow, highlightCol, shipsLayer, markersLayer);
        this.getChildren().addAll(contentGroup, hoverCoord);

        initHoverEffects();
        generateGrid();
        updateDisplay();
    }

    /**
     * Génère graphiquement les cellules de la grille et les libellés alphanumériques.
     * L'opacité des lignes s'adapte à la densité du plateau.
     */
    private void generateGrid() {
        gridLayer.getChildren().clear();
        double lineOpacity = (board.getWidth() > 15 || board.getHeight() > 15) ? 0.2 : 0.5;

        for (int x = 0; x < board.getWidth(); x++) {
            Text tLetter = new Text(String.valueOf((char) ('A' + x)));
            tLetter.setFill(Theme.GRID_LABEL);
            tLetter.setFont(Theme.font(12, FontWeight.BOLD));
            tLetter.setX(x * CELL_SIZE + OFFSET + 15);
            tLetter.setY(25);
            gridLayer.getChildren().add(tLetter);

            for (int y = 0; y < board.getHeight(); y++) {
                if (x == 0) {
                    Text tNum = new Text(String.valueOf(y + 1));
                    tNum.setFill(Theme.GRID_LABEL);
                    tNum.setFont(Theme.font(12, FontWeight.BOLD));
                    tNum.setX(10);
                    tNum.setY(y * CELL_SIZE + OFFSET + 25);
                    gridLayer.getChildren().add(tNum);
                }
                Rectangle cell = new Rectangle(x * CELL_SIZE + OFFSET, y * CELL_SIZE + OFFSET, CELL_SIZE, CELL_SIZE);
                cell.setFill(Theme.GRID_BG);
                cell.setStroke(Theme.GRID_LINE.deriveColor(0, 1, 1, lineOpacity));
                gridLayer.getChildren().add(cell);
            }
        }
    }

    /**
     * Configure le réticule de sélection et les coordonnées de survol.
     * Le texte de coordonnées est bridé pour ne jamais sortir des limites de la grille.
     */
    private void initHoverEffects() {
        highlightRow.setFill(Theme.CYAN.deriveColor(0, 1, 1, 0.15));
        highlightCol.setFill(Theme.CYAN.deriveColor(0, 1, 1, 0.15));
        highlightRow.setMouseTransparent(true);
        highlightCol.setMouseTransparent(true);

        hoverCoord.setFill(Color.WHITE);
        hoverCoord.setFont(Theme.mono(16, FontWeight.BOLD));
        hoverCoord.setMouseTransparent(true);

        hideHighlights();

        this.setOnMouseMoved(e -> {
            Coordinate c = getGridCoordinate(e.getX(), e.getY());
            if (c != null) {
                highlightRow.setVisible(true); highlightCol.setVisible(true); hoverCoord.setVisible(true);
                highlightRow.setY(c.y() * CELL_SIZE + OFFSET);
                highlightRow.setX(OFFSET);
                highlightRow.setWidth(board.getWidth() * CELL_SIZE);
                highlightRow.setHeight(CELL_SIZE);
                highlightCol.setX(c.x() * CELL_SIZE + OFFSET);
                highlightCol.setY(OFFSET);
                highlightCol.setWidth(CELL_SIZE);
                highlightCol.setHeight(board.getHeight() * CELL_SIZE);

                hoverCoord.setText((char) ('A' + c.x()) + " : " + (c.y() + 1));

                double tX = e.getX() + 15;
                double tY = e.getY() - 15;
                if (tX + 75 > getWidth()) tX = e.getX() - 80;
                if (tY - 20 < 0) tY = e.getY() + 30;

                hoverCoord.setX(tX); hoverCoord.setY(tY);
            } else {
                hideHighlights();
            }
        });
        this.setOnMouseExited(_ -> hideHighlights());
    }

    /**
     * Masque les aides visuelles de sélection.
     */
    private void hideHighlights() {
        highlightRow.setVisible(false);
        highlightCol.setVisible(false);
        hoverCoord.setVisible(false);

        if (getCursor() != javafx.scene.Cursor.DEFAULT) {
            setCursor(javafx.scene.Cursor.DEFAULT);
        }
    }

    /**
     * Convertit une position pixel en coordonnée logique de grille.
     * @param localX Position X.
     * @param localY Position Y.
     * @return Coordinate ou null si hors zone.
     */
    public Coordinate getGridCoordinate(double localX, double localY) {
        double xInGrid = localX - OFFSET;
        double yInGrid = localY - OFFSET;
        if (xInGrid < 0 || yInGrid < 0 || xInGrid >= board.getWidth() * CELL_SIZE || yInGrid >= board.getHeight() * CELL_SIZE) return null;
        return new Coordinate((int) (xInGrid / CELL_SIZE), (int) (yInGrid / CELL_SIZE));
    }

    /**
     * Actualise le rendu des navires et des impacts selon l'état du Board.
     */
    public void updateDisplay() {
        markersLayer.getChildren().clear();
        shipsLayer.getChildren().clear();

        // État de brouillage : Uniquement sur le radar ennemi si le fog est actif
        boolean isJammed = !isPlayerView && board.isFogActive();

        if (isJammed) {
            applyGlitchEffect();
        } else {
            this.setEffect(null); // On retire l'effet si le brouillage est fini
        }

        // 1. RENDU DES NAVIRES
        // Si brouillé, on ne dessine RIEN (on cache même les épaves déjà connues)
        if (!isJammed) {
            for (Ship ship : board.getShips()) {
                if (isPlayerView || ship.isSunk()) drawShipTexture(ship);
            }
        }

        // 2. RENDU DES MARQUEURS (TIRS)
        if (isJammed) {
            for (Coordinate coord : board.getShotsHistory()) {
                drawUnknownMarker(coord);
            }
        } else {
            renderNormalMarkers();
        }
    }

    /**
     * Ajoute un indicateur visuel de débris enflammés pour signaler un impact de météore.
     */
    private void addMeteorIndicator(int x, int y) {
        double size = 8;
        Circle flame = new Circle(size / 2.0, Color.ORANGERED);
        flame.setStroke(Color.YELLOW);
        flame.setStrokeWidth(1);
        flame.setEffect(new javafx.scene.effect.Glow(0.8));

        // Positionnement en haut à droite de la case
        flame.setLayoutX(x * CELL_SIZE + OFFSET + CELL_SIZE - 10);
        flame.setLayoutY(y * CELL_SIZE + OFFSET + 10);

        markersLayer.getChildren().add(flame);

        // Petite animation de scintillement
        Timeline flash = new Timeline(
                new KeyFrame(Duration.seconds(0.5), new KeyValue(flame.opacityProperty(), 0.4)),
                new KeyFrame(Duration.seconds(1.0), new KeyValue(flame.opacityProperty(), 1.0))
        );
        flash.setCycleCount(Animation.INDEFINITE);
        flash.setAutoReverse(true);
        flash.play();
    }

    /**
     * Active l'apparence visuelle de l'Apocalypse sur la grille.
     */
    public void applyApocalypseEffect() {
        gridLayer.getChildren().forEach(node -> {
            if (node instanceof Rectangle rect) {
                rect.setStroke(Color.web("#ff4757", 0.8));
                rect.setStrokeWidth(1.2);
            }
        });

        javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow(30, Color.RED);
        ds.setSpread(0.2);
        this.setEffect(ds);
    }

    /**
     * Marqueur de tir manqué : Point blanc et onde de choc (ripple).
     * @param x Colonne.
     * @param y Ligne.
     * @param animate True pour déclencher l'animation d'apparition.
     */
    private void addMissMarker(int x, int y, boolean animate) {
        double c = CELL_SIZE / 2.0;
        Circle splash = new Circle(c, c, 3, Color.WHITE);
        Circle ripple = new Circle(c, c, CELL_SIZE * 0.2, Color.TRANSPARENT);
        ripple.setStroke(Color.web("#7ed6df")); ripple.setStrokeWidth(1.5);
        Group g = new Group(ripple, splash);
        g.setLayoutX(x * CELL_SIZE + OFFSET); g.setLayoutY(y * CELL_SIZE + OFFSET);
        markersLayer.getChildren().add(g);
        if (animate) FXGL.animationBuilder().duration(Duration.seconds(0.4)).scale(g).from(new Point2D(0.6, 0.6)).to(new Point2D(1, 1)).buildAndPlay();
    }

    /**
     * Marqueur de tir réussi : Halo radial incandescent.
     * @param x Colonne.
     * @param y Ligne.
     * @param animate True pour déclencher les étincelles.
     */
    private void addHitMarker(int x, int y, boolean animate) {
        double center = CELL_SIZE / 2.0;
        double radius = CELL_SIZE * 0.4;
        RadialGradient grad = new RadialGradient(0, 0, center, center, radius, false, CycleMethod.NO_CYCLE, new Stop(0.0, Color.web("#fbc531")), new Stop(0.3, Color.ORANGERED), new Stop(1.0, Color.TRANSPARENT));
        Circle glowingCenter = new Circle(center, center, radius * 0.8, grad);
        glowingCenter.setEffect(Theme.GLOW_SMALL);
        glowingCenter.setLayoutX(x * CELL_SIZE + OFFSET); glowingCenter.setLayoutY(y * CELL_SIZE + OFFSET);
        markersLayer.getChildren().add(glowingCenter);
        if (animate && !board.isFogActive()) animateSparks(x, y, center);
    }

    /**
     * Génère une pluie d'étincelles à l'impact.
     * @param x Colonne.
     * @param y Ligne.
     * @param center Point d'origine central.
     */
    private void animateSparks(int x, int y, double center) {
        for (int i = 0; i < 8; i++) {
            Circle spark = new Circle(1.5, Math.random() > 0.5 ? Color.YELLOW : Color.ORANGERED);
            spark.setLayoutX((x * CELL_SIZE) + OFFSET + center);
            spark.setLayoutY((y * CELL_SIZE) + OFFSET + center);
            markersLayer.getChildren().add(spark);
            TranslateTransition move = new TranslateTransition(Duration.seconds(0.4), spark);
            double angle = Math.random() * 2 * Math.PI; double dist = 10 + Math.random() * 20;
            move.setByX(Math.cos(angle) * dist); move.setByY(Math.sin(angle) * dist);
            FadeTransition fade = new FadeTransition(Duration.seconds(0.4), spark);
            fade.setToValue(0);
            ParallelTransition pt = new ParallelTransition(move, fade);
            pt.setOnFinished(_ -> markersLayer.getChildren().remove(spark));
            pt.play();
        }
    }

    /**
     * Secousse visuelle de la grille.
     */
    private void shakeGrid() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), contentGroup);
        tt.setFromX(0); tt.setByX(4); tt.setCycleCount(4); tt.setAutoReverse(true); tt.play();
    }

    /**
     * Dessine la texture du navire et applique une teinte rouge s'il est coulé.
     * @param s Le navire.
     */
    private void drawShipTexture(Ship s) {
        String path = "/assets/textures/" + s.getType().name().toLowerCase() + ".png";
        ImageView iv = new ImageView(new Image(Objects.requireNonNull(getClass().getResource(path)).toExternalForm()));
        iv.setFitWidth(CELL_SIZE); iv.setFitHeight(CELL_SIZE * s.getType().getSize());
        Coordinate start = s.getOccupiedCoordinates().getFirst();
        double x = start.x() * CELL_SIZE + OFFSET; double y = start.y() * CELL_SIZE + OFFSET;
        if (s.getOrientation() == Orientation.HORIZONTAL) {
            iv.setRotate(90);
            double diff = (iv.getFitHeight() - iv.getFitWidth()) / 2.0;
            iv.setLayoutX(x + diff); iv.setLayoutY(y - diff);
        } else {
            iv.setLayoutX(x); iv.setLayoutY(y);
        }
        if (s.isSunk()) {
            ColorInput redTint = new ColorInput(0, 0, iv.getFitWidth(), iv.getFitHeight(), Theme.RED_ALERTE.deriveColor(0, 1, 1, 0.6));
            iv.setEffect(new Blend(BlendMode.SRC_ATOP, null, redTint));
        }
        shipsLayer.getChildren().add(iv);
    }

    /**
     * Adapte dynamiquement l'échelle de la vue.
     * @param maxWidth Largeur cible.
     * @param maxHeight Hauteur cible.
     */
    public void autoScale(double maxWidth, double maxHeight) {
        double scale = Math.min(1.0, Math.min(maxWidth / getPrefWidth(), maxHeight / getPrefHeight()));
        this.getTransforms().setAll(new Scale(scale, scale, 0, 0));
    }

    /**
     * Affiche un point d'interrogation sur une case ciblée durant le brouillage.
     */
    private void drawUnknownMarker(Coordinate c) {
        Text qMark = new Text("?");
        qMark.setFill(Theme.CYAN);
        qMark.setFont(Theme.mono(22, FontWeight.BOLD));

        // Centrage approximatif dans la case
        qMark.setX(c.x() * CELL_SIZE + OFFSET + 14);
        qMark.setY(c.y() * CELL_SIZE + OFFSET + 28);

        // Petite animation de flottement pour le côté "bug"
        Timeline pulse = new Timeline(
                new KeyFrame(Duration.seconds(0.5), new KeyValue(qMark.opacityProperty(), 0.3)),
                new KeyFrame(Duration.seconds(1.0), new KeyValue(qMark.opacityProperty(), 1.0))
        );
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        markersLayer.getChildren().add(qMark);
    }

    /**
     * Applique un effet visuel de "bug" ou de distorsion sur la grille.
     */
    private void applyGlitchEffect() {
        javafx.scene.effect.ColorAdjust glitch = new javafx.scene.effect.ColorAdjust();
        glitch.setContrast(0.3);
        glitch.setHue(-0.05);
        glitch.setSaturation(-0.2);
        this.setEffect(glitch);
    }

    /**
     * Encapsule la logique de rendu normal (ton code précédent).
     */
    private void renderNormalMarkers() {
        for (Coordinate miss : board.getMissedShots()) {
            if (!isPlayerView && board.isMeteorImpact(miss) && !board.getShotsHistory().contains(miss)) continue;
            addMissMarker(miss.x(), miss.y(), animatedCoords.add(miss));
            if (isPlayerView && board.isMeteorImpact(miss)) addMeteorIndicator(miss.x(), miss.y());
        }

        for (Coordinate hit : board.getHitShots()) {
            if (!isPlayerView && board.isMeteorImpact(hit) && !board.getShotsHistory().contains(hit)) continue;
            boolean isNew = animatedCoords.add(hit);
            addHitMarker(hit.x(), hit.y(), isNew);
            if (isNew) shakeGrid();
            if (isPlayerView && board.isMeteorImpact(hit)) addMeteorIndicator(hit.x(), hit.y());
        }
    }
}