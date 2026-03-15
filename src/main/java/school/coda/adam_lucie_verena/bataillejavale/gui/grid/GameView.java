package school.coda.adam_lucie_verena.bataillejavale.gui.grid;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.*;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
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
import school.coda.adam_lucie_verena.bataillejavale.gui.AssetsManager;
import school.coda.adam_lucie_verena.bataillejavale.gui.Theme;
import java.util.HashSet;
import java.util.Set;

/**
 * Composant graphique représentant la grille de bataille navale.
 * Gère le rendu des navires, des impacts et des effets environnementaux (brouillage, apocalypse).
 */
public class GameView extends Pane {

    private static final int CELL_SIZE = 40;
    private static final int OFFSET = 40;

    private final Board board;
    private final boolean isPlayerView;
    private boolean isJammed = false;

    /** Historique des coordonnées ciblées spécifiquement durant la phase de brouillage actuelle. */
    private final Set<Coordinate> jammedSessionShots = new HashSet<>();

    private final Group contentGroup = new Group();
    private final Group gridLayer = new Group();
    private final Group shipsLayer = new Group();
    private final Group markersLayer = new Group();

    private final Rectangle highlightRow = new Rectangle();
    private final Rectangle highlightCol = new Rectangle();
    private final Text hoverCoord = new Text();

    private final Set<Coordinate> animatedCoords = new HashSet<>();

    /**
     * Construit une nouvelle vue de grille.
     * @param board Le modèle de plateau associé.
     * @param isPlayerView Définit s'il s'agit de la vue du joueur (vaisseaux visibles) ou de l'ennemi.
     */
    public GameView(Board board, boolean isPlayerView) {
        this.board = board;
        this.isPlayerView = isPlayerView;

        double w = board.getWidth() * CELL_SIZE + OFFSET;
        double h = board.getHeight() * CELL_SIZE + OFFSET;

        setMinWidth(w); setMinHeight(h);
        setPrefSize(w, h); setMaxSize(w, h);
        this.setClip(new Rectangle(w, h));

        contentGroup.getChildren().addAll(gridLayer, highlightRow, highlightCol, shipsLayer, markersLayer);
        this.getChildren().addAll(contentGroup, hoverCoord);

        initHoverEffects();
        generateGrid();
        updateDisplay();
    }

    /**
     * Active ou désactive l'état de brouillage radar sur la grille.
     * Le passage à l'état actif réinitialise la session de tirs occultés.
     * @param jammed True pour activer le brouillage, False pour restaurer la vision.
     */
    public void setJammed(boolean jammed) {
        if (isPlayerView) return;

        this.isJammed = jammed;

        if (jammed) {
            jammedSessionShots.clear();
        }

        updateDisplay();
    }

    /**
     * Enregistre une tentative de tir durant une phase de brouillage.
     * Permet d'afficher un marqueur d'incertitude même si la case a déjà été ciblée.
     * @param c Coordonnée ciblée.
     */
    public void registerJammedClick(Coordinate c) {
        if (isJammed && c != null) {
            jammedSessionShots.add(c);
            updateDisplay();
        }
    }

    /**
     * Vérifie si le radar est actuellement sous l'effet d'un brouillage.
     * * @return True si le brouillage est actif.
     */
    public boolean isJammed() {
        return isJammed;
    }

    /**
     * Actualise l'intégralité du rendu graphique en fonction de l'état actuel du moteur.
     */
    public void updateDisplay() {
        shipsLayer.getChildren().clear();
        markersLayer.getChildren().clear();

        if (isJammed) {
            applyGlitchEffect();

            // Rendu exclusif des marqueurs d'incertitude de la session actuelle
            for (Coordinate coord : jammedSessionShots) {
                drawUnknownMarker(coord);
            }
            return;
        }

        // Restauration de l'état normal
        if (this.getEffect() instanceof javafx.scene.effect.ColorAdjust) {
            this.setEffect(null);
        }

        for (Ship ship : board.getShips()) {
            if (isPlayerView || ship.isSunk()) {
                drawShipTexture(ship);
            }
        }

        renderNormalMarkers();
    }

    /**
     * Applique les marqueurs d'impact standards (Hits/Miss) et les indicateurs météorologiques.
     */
    private void renderNormalMarkers() {
        for (Coordinate miss : board.getMissedShots()) {
            if (!isPlayerView && board.isMeteorImpact(miss) && !board.getShotsHistory().contains(miss)) {
                continue;
            }
            addMissMarker(miss.x(), miss.y(), animatedCoords.add(miss));
            if (isPlayerView && board.isMeteorImpact(miss)) {
                addMeteorIndicator(miss.x(), miss.y());
            }
        }

        for (Coordinate hit : board.getHitShots()) {
            if (!isPlayerView && board.isMeteorImpact(hit) && !board.getShotsHistory().contains(hit)) {
                continue;
            }
            boolean isNew = animatedCoords.add(hit);
            addHitMarker(hit.x(), hit.y(), isNew);
            if (isNew) shakeGrid();
            if (isPlayerView && board.isMeteorImpact(hit)) {
                addMeteorIndicator(hit.x(), hit.y());
            }
        }
    }

    /**
     * Dessine un marqueur d'incertitude (point d'interrogation) sur la grille.
     * @param c Coordonnée de la cellule.
     */
    private void drawUnknownMarker(Coordinate c) {
        Text qMark = new Text("?");
        qMark.setFill(Color.YELLOW);
        qMark.setFont(Theme.mono(22, FontWeight.BOLD));
        qMark.setX(c.x() * CELL_SIZE + OFFSET + 14);
        qMark.setY(c.y() * CELL_SIZE + OFFSET + 28);
        markersLayer.getChildren().add(qMark);
    }

    /**
     * Applique un effet de distorsion visuelle simulant un dysfonctionnement électronique.
     */
    private void applyGlitchEffect() {
        javafx.scene.effect.ColorAdjust glitch = new javafx.scene.effect.ColorAdjust();
        glitch.setContrast(0.2);
        glitch.setHue(-0.05);
        this.setEffect(glitch);
    }

    /**
     * Récupère la texture du navire via l'AssetsManager et l'ajoute au calque dédié.
     * @param s Modèle du navire à dessiner.
     */
    private void drawShipTexture(Ship s) {
        String fileName = s.getType().name().toLowerCase() + ".png";
        ImageView iv = AssetsManager.loadTexture(fileName);

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
            ColorInput redTint = new ColorInput(0, 0, iv.getFitWidth(), iv.getFitHeight(), Theme.RED_ALERTE.deriveColor(0, 1, 1, 0.6));
            iv.setEffect(new Blend(BlendMode.SRC_ATOP, null, redTint));
        }

        shipsLayer.getChildren().add(iv);
    }

    /**
     * Génère dynamiquement la grille visuelle et les étiquettes de coordonnées.
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
     * Active le thème visuel apocalyptique sur la grille.
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
     * Ajoute un marqueur de tir manqué avec animation de cercle d'eau.
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
     * Ajoute un marqueur de tir réussi avec gradient radial incandescent.
     */
    private void addHitMarker(int x, int y, boolean animate) {
        double center = CELL_SIZE / 2.0;
        double radius = CELL_SIZE * 0.4;
        RadialGradient grad = new RadialGradient(0, 0, center, center, radius, false, CycleMethod.NO_CYCLE, new Stop(0.0, Color.web("#fbc531")), new Stop(0.3, Color.ORANGERED), new Stop(1.0, Color.TRANSPARENT));
        Circle glowingCenter = new Circle(center, center, radius * 0.8, grad);
        glowingCenter.setEffect(Theme.GLOW_SMALL);
        glowingCenter.setLayoutX(x * CELL_SIZE + OFFSET); glowingCenter.setLayoutY(y * CELL_SIZE + OFFSET);
        markersLayer.getChildren().add(glowingCenter);
        if (animate) animateSparks(x, y, center);
    }

    /**
     * Déclenche une animation de particules d'étincelles à une position donnée.
     */
    private void animateSparks(int x, int y, double center) {
        for (int i = 0; i < 6; i++) {
            Circle spark = new Circle(1.5, Math.random() > 0.5 ? Color.YELLOW : Color.ORANGERED);
            spark.setLayoutX((x * CELL_SIZE) + OFFSET + center);
            spark.setLayoutY((y * CELL_SIZE) + OFFSET + center);
            markersLayer.getChildren().add(spark);
            TranslateTransition move = new TranslateTransition(Duration.seconds(0.4), spark);
            double angle = Math.random() * 2 * Math.PI; double dist = 10 + Math.random() * 20;
            move.setByX(Math.cos(angle) * dist); move.setByY(Math.sin(angle) * dist);
            FadeTransition fade = new FadeTransition(Duration.seconds(0.4), spark); fade.setToValue(0);
            ParallelTransition pt = new ParallelTransition(move, fade);
            pt.setOnFinished(_ -> markersLayer.getChildren().remove(spark)); pt.play();
        }
    }

    /**
     * Produit un effet de secousse sismique sur le contenu de la grille.
     */
    private void shakeGrid() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), contentGroup);
        tt.setFromX(0); tt.setByX(3); tt.setCycleCount(4); tt.setAutoReverse(true); tt.play();
    }

    /**
     * Ajuste l'échelle de la grille pour tenir dans les dimensions spécifiées.
     */
    public void autoScale(double maxWidth, double maxHeight) {
        double scale = Math.min(1.0, Math.min(maxWidth / getPrefWidth(), maxHeight / getPrefHeight()));
        this.getTransforms().setAll(new Scale(scale, scale, 0, 0));
    }

    /**
     * Affiche un indicateur visuel de combustion suite à un impact de météore.
     */
    private void addMeteorIndicator(int x, int y) {
        Circle flame = new Circle(4, Color.ORANGERED);
        flame.setLayoutX(x * CELL_SIZE + OFFSET + CELL_SIZE - 10);
        flame.setLayoutY(y * CELL_SIZE + OFFSET + 10);
        markersLayer.getChildren().add(flame);
    }

    /**
     * Initialise les effets visuels de survol (réticule et étiquette de coordonnées).
     */
    private void initHoverEffects() {
        highlightRow.setFill(Theme.CYAN.deriveColor(0, 1, 1, 0.15));
        highlightCol.setFill(Theme.CYAN.deriveColor(0, 1, 1, 0.15));
        highlightRow.setMouseTransparent(true); highlightCol.setMouseTransparent(true);
        hoverCoord.setFill(Color.WHITE); hoverCoord.setFont(Theme.mono(16, FontWeight.BOLD));
        hoverCoord.setMouseTransparent(true);
        hideHighlights();
        this.setOnMouseMoved(e -> {
            Coordinate c = getGridCoordinate(e.getX(), e.getY());
            if (c != null) {
                highlightRow.setVisible(true); highlightCol.setVisible(true); hoverCoord.setVisible(true);
                highlightRow.setY(c.y() * CELL_SIZE + OFFSET); highlightRow.setX(OFFSET);
                highlightRow.setWidth(board.getWidth() * CELL_SIZE); highlightRow.setHeight(CELL_SIZE);
                highlightCol.setX(c.x() * CELL_SIZE + OFFSET); highlightCol.setY(OFFSET);
                highlightCol.setWidth(CELL_SIZE); highlightCol.setHeight(board.getHeight() * CELL_SIZE);
                hoverCoord.setText((char) ('A' + c.x()) + " : " + (c.y() + 1));
                hoverCoord.setX(e.getX() + 15); hoverCoord.setY(e.getY() - 15);
            } else { hideHighlights(); }
        });
        this.setOnMouseExited(_ -> hideHighlights());
    }

    /**
     * Masque les éléments visuels d'aide au ciblage.
     */
    private void hideHighlights() {
        highlightRow.setVisible(false); highlightCol.setVisible(false); hoverCoord.setVisible(false);
    }

    /**
     * Traduit une position de souris locale en coordonnées logiques de grille.
     */
    public Coordinate getGridCoordinate(double localX, double localY) {
        double xInGrid = localX - OFFSET; double yInGrid = localY - OFFSET;
        if (xInGrid < 0 || yInGrid < 0 || xInGrid >= board.getWidth() * CELL_SIZE || yInGrid >= board.getHeight() * CELL_SIZE) return null;
        return new Coordinate((int) (xInGrid / CELL_SIZE), (int) (yInGrid / CELL_SIZE));
    }
}