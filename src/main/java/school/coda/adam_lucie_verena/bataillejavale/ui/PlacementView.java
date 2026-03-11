package school.coda.adam_lucie_verena.bataillejavale.ui;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import school.coda.adam_lucie_verena.bataillejavale.core.model.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Vue de préparation gérant le déploiement stratégique de la flotte.
 * <p>
 * Cette classe permet au joueur de placer ses navires sur la grille avant le combat.
 * Elle supporte le glisser-déposer (Drag & Drop) depuis un inventaire, le déplacement
 * de navires déjà posés, la rotation via la touche 'R' et le placement aléatoire.
 * </p>
 */
public class PlacementView extends Pane {

    private static final String CSS_BG_NAVY = "-fx-background-color: #0f172a;";
    private static final String CSS_INVENTORY_PANEL = "-fx-background-color: rgba(30, 41, 59, 0.8); -fx-background-radius: 20; -fx-border-color: #334155; -fx-border-radius: 20;";
    private static final Color COLOR_ACCENT = Color.web("#00d2d3");
    private static final int CELL_SIZE = 40;

    /** Cache statique pour les textures des navires afin d'éviter des rechargements inutiles. */
    private static final Map<ShipType, Image> TEXTURE_CACHE = new EnumMap<>(ShipType.class);

    private final Board board;
    private final GameView gridView;

    /** Image fantôme suivant le curseur lors du déplacement d'un navire. */
    private final ImageView ghost = new ImageView();
    private Button btnStart;

    /** État de l'inventaire : nombre de navires restants à placer par type. */
    private final Map<ShipType, Integer> stock = new EnumMap<>(ShipType.class);
    private final Map<ShipType, Text> labels = new EnumMap<>(ShipType.class);
    private final Map<ShipType, VBox> cards = new EnumMap<>(ShipType.class);

    private ShipType draggingType = null;
    private Orientation currentOrientation = Orientation.VERTICAL;
    private double lastMouseX, lastMouseY;

    /**
     * Initialise la vue de placement.
     * @param gridView La vue graphique de la grille du joueur.
     * @param board    Le modèle de données du plateau du joueur.
     * @param onStart  Action à exécuter lorsque le joueur valide son déploiement.
     */
    public PlacementView(GameView gridView, Board board, Runnable onStart) {
        this.board = board;
        this.gridView = gridView;

        loadTextures();
        initStock();
        initUI(onStart);
        setupEventListeners();
        checkReady();
    }

    /** Charge les images des navires depuis les ressources si le cache est vide. */
    private void loadTextures() {
        if (!TEXTURE_CACHE.isEmpty()) return;
        for (ShipType t : ShipType.values()) {
            String path = "/assets/textures/" + t.name().toLowerCase() + ".png";
            TEXTURE_CACHE.put(t, new Image(Objects.requireNonNull(getClass().getResource(path)).toExternalForm()));
        }
    }

    /** Initialise le stock de navires (1 de chaque type par défaut). */
    private void initStock() {
        for (ShipType t : ShipType.values()) stock.put(t, 1);
    }

    /** Construit l'agencement visuel de la vue. */
    private void initUI(Runnable onStart) {
        this.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        this.setStyle(CSS_BG_NAVY);

        HBox mainLayout = new HBox(60);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());

        VBox inventory = buildInventory();

        // Conteneur de la grille avec effet de lueur
        StackPane gridPane = new StackPane(gridView);
        gridPane.setStyle("-fx-effect: dropshadow(gaussian, #00d2d3, 20, 0, 0, 0);");

        // Bouclier invisible pour capturer les clics sur la grille de manière propre
        Rectangle glassShield = new Rectangle(board.getWidth() * CELL_SIZE, board.getHeight() * CELL_SIZE, Color.TRANSPARENT);
        gridPane.getChildren().add(glassShield);

        mainLayout.getChildren().addAll(inventory, gridPane);

        btnStart = new Button("DÉPLOYER LA FLOTTE");
        styleButton(btnStart, "#10b981");
        btnStart.setOnAction(_ -> onStart.run());
        inventory.getChildren().add(btnStart);

        // Configuration de l'image fantôme (ghost)
        ghost.setMouseTransparent(true);
        ghost.setOpacity(0.6);
        ghost.setVisible(false);

        this.getChildren().addAll(mainLayout, ghost);
    }

    /**
     * Configure les écouteurs pour la souris et le clavier.
     * Gère notamment la touche 'R' pour la rotation.
     */
    private void setupEventListeners() {
        this.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.R && draggingType != null) rotateGhost();
            });
        });

        this.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (draggingType != null) return;
            handleGridSelection(e);
        });

        this.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (draggingType != null) updateGhost(e.getSceneX(), e.getSceneY());
        });

        this.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (draggingType != null) drop(e.getSceneX(), e.getSceneY());
        });
    }

    /** Permet de récupérer un navire déjà placé sur la grille pour le déplacer. */
    private void handleGridSelection(MouseEvent e) {
        Point2D p = gridView.sceneToLocal(e.getSceneX(), e.getSceneY());
        if (gridView.getBoundsInLocal().contains(p)) {
            int cx = (int)(p.getX() / CELL_SIZE);
            int cy = (int)(p.getY() / CELL_SIZE);
            Coordinate target = new Coordinate(cx, cy);

            board.getShips().stream()
                    .filter(s -> s.getOccupiedCoordinates().contains(target))
                    .findFirst().ifPresent(s -> {
                        board.getShips().remove(s);
                        incrementStock(s.getType());
                        gridView.updateDisplay();
                        startDrag(s.getType(), s.getOrientation(), e.getSceneX(), e.getSceneY());
                    });
        }
    }

    /** * Initialise l'état visuel et logique du glisser-déposer.
     */
    private void startDrag(ShipType type, Orientation o, double x, double y) {
        draggingType = type;
        currentOrientation = o;
        ghost.setImage(TEXTURE_CACHE.get(type));
        ghost.setFitWidth(CELL_SIZE);
        ghost.setFitHeight(CELL_SIZE * type.getSize());
        ghost.setRotate(o == Orientation.VERTICAL ? 0 : 90);
        ghost.setVisible(true);
        updateGhost(x, y);
    }

    /**
     * * Tente de poser le navire sur la grille aux coordonnées pointées.
     * En cas d'échec (hors limites ou collision), le navire retourne dans l'inventaire.
     */
    private void drop(double sx, double sy) {
        Point2D p = gridView.sceneToLocal(sx, sy);

        // Calcul du décalage pour centrer le navire sur la souris selon sa taille et orientation
        double wOffset = (currentOrientation == Orientation.VERTICAL ? 1 : draggingType.getSize()) * (CELL_SIZE / 2.0);
        double hOffset = (currentOrientation == Orientation.VERTICAL ? draggingType.getSize() : 1) * (CELL_SIZE / 2.0);

        int cx = (int) Math.round((p.getX() - wOffset) / CELL_SIZE);
        int cy = (int) Math.round((p.getY() - hOffset) / CELL_SIZE);

        Ship s = new Ship(draggingType, new Coordinate(cx, cy), currentOrientation);

        if (board.canPlaceShip(s)) {
            board.placeShip(s);
            decrementStock(draggingType);
            gridView.updateDisplay();
        } else {
            refreshCard(draggingType);
        }

        ghost.setVisible(false);
        draggingType = null;
        checkReady();
    }

    /** Construit le panneau d'inventaire "ARSENAL". */
    private VBox buildInventory() {
        VBox inv = new VBox(20);
        inv.setPrefWidth(300);
        inv.setPadding(new Insets(30));
        inv.setStyle(CSS_INVENTORY_PANEL);

        Text title = new Text("ARSENAL");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 22));

        VBox list = new VBox(15);
        for (ShipType type : ShipType.values()) list.getChildren().add(createCard(type));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button btnRandom = new Button("PLACEMENT ALÉATOIRE");
        styleButton(btnRandom, "#f39c12");
        btnRandom.setOnAction(_ -> applyRandomPlacement());

        inv.getChildren().addAll(title, list, spacer, btnRandom);
        return inv;
    }

    /** Vide le plateau et place tous les navires aléatoirement. */
    private void applyRandomPlacement() {
        this.setMouseTransparent(true);
        board.getShips().clear();
        board.placeShipsRandomly();
        gridView.updateDisplay();

        for (ShipType type : ShipType.values()) {
            stock.put(type, 0);
            refreshCard(type);
        }
        this.setMouseTransparent(false);
        checkReady();
    }

    /** Crée une carte de sélection pour un type de navire dans l'inventaire. */
    private VBox createCard(ShipType type) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10; -fx-cursor: hand;");

        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);

        ImageView img = new ImageView(TEXTURE_CACHE.get(type));
        img.setFitHeight(30); img.setPreserveRatio(true);

        Text count = new Text("x" + stock.get(type));
        count.setFill(COLOR_ACCENT);
        count.setFont(Font.font("System", FontWeight.BOLD, 14));

        row.getChildren().addAll(img, new Text(type.getName().toUpperCase()), new Spacer(), count);
        card.getChildren().add(row);

        card.setOnMousePressed(e -> {
            if (stock.get(type) > 0 && draggingType == null) {
                startDrag(type, Orientation.VERTICAL, e.getSceneX(), e.getSceneY());
            }
        });

        labels.put(type, count);
        cards.put(type, card);
        return card;
    }

    private void incrementStock(ShipType t) {
        stock.put(t, stock.get(t) + 1);
        refreshCard(t);
        checkReady();
    }

    private void decrementStock(ShipType t) {
        stock.put(t, stock.get(t) - 1);
        refreshCard(t);
        checkReady();
    }

    /** Met à jour visuellement le compteur et l'opacité d'une carte d'inventaire. */
    private void refreshCard(ShipType t) {
        labels.get(t).setText("x" + stock.get(t));
        cards.get(t).setOpacity(stock.get(t) > 0 ? 1 : 0.3);
    }

    /** Met à jour la position de l'image fantôme selon la souris. */
    private void updateGhost(double x, double y) {
        lastMouseX = x; lastMouseY = y;
        ghost.setTranslateX(x - ghost.getBoundsInLocal().getWidth()/2);
        ghost.setTranslateY(y - ghost.getBoundsInLocal().getHeight()/2);
    }

    /** Bascule l'orientation du navire en cours de déplacement. */
    private void rotateGhost() {
        currentOrientation = (currentOrientation == Orientation.VERTICAL) ? Orientation.HORIZONTAL : Orientation.VERTICAL;
        ghost.setRotate(currentOrientation == Orientation.VERTICAL ? 0 : 90);
        updateGhost(lastMouseX, lastMouseY);
    }

    /** Vérifie si tous les navires sont placés pour activer le bouton de démarrage. */
    private void checkReady() {
        boolean ok = stock.values().stream().allMatch(v -> v == 0);
        btnStart.setDisable(!ok);
        btnStart.setOpacity(ok ? 1 : 0.5);
    }

    private void styleButton(Button b, String hexColor) {
        b.setPrefWidth(240);
        b.setStyle("-fx-background-color: " + hexColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 10;");
    }

    /** Classe utilitaire pour créer des espaces flexibles dans les layouts HBox/VBox. */
    private static class Spacer extends Region {
        public Spacer() { HBox.setHgrow(this, Priority.ALWAYS); VBox.setVgrow(this, Priority.ALWAYS); }
    }
}