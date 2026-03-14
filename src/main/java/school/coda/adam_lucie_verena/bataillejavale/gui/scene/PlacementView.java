package school.coda.adam_lucie_verena.bataillejavale.gui.scene;

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
import javafx.scene.transform.Scale;
import school.coda.adam_lucie_verena.bataillejavale.core.model.*;
import school.coda.adam_lucie_verena.bataillejavale.gui.AssetsManager;
import school.coda.adam_lucie_verena.bataillejavale.gui.grid.GameView;

import java.util.EnumMap;
import java.util.Map;

/**
 * Vue interactive permettant au joueur de positionner ses navires sur la grille avant le combat.
 * Gère le drag-and-drop, la rotation des navires et la validation du placement.
 */
public class PlacementView extends Pane {

    private static final String CSS_NAVY = "-fx-background-color: #020617;";
    private static final String CSS_PANEL = "-fx-background-color: rgba(30, 41, 59, 0.8); -fx-background-radius: 20; -fx-border-color: #334155; -fx-border-radius: 20;";
    private static final int CELL_SIZE = 40;
    private static final Map<ShipType, Image> TEXTURE_CACHE = new EnumMap<>(ShipType.class);

    private final Board board;
    private final GameView gridView;
    private final Map<ShipType, Integer> stock = new EnumMap<>(ShipType.class);
    private final Map<ShipType, Integer> initialCounts;
    private final Map<ShipType, Text> labels = new EnumMap<>(ShipType.class);
    private final Map<ShipType, VBox> cards = new EnumMap<>(ShipType.class);

    private final ImageView ghost = new ImageView();
    private ShipType draggingType = null;
    private Orientation currentOrientation = Orientation.VERTICAL;
    private Button btnStart;
    private double lastMouseX, lastMouseY;

    /**
     * Constructeur de la vue de placement.
     * @param gridView Composant graphique de la grille.
     * @param board Modèle logique du plateau.
     * @param counts Quantité initiale de chaque type de navire.
     * @param onBack Action de retour au menu.
     * @param onStart Action de lancement de la bataille.
     */
    public PlacementView(GameView gridView, Board board, Map<ShipType, Integer> counts, Runnable onBack, Runnable onStart) {
        this.board = board;
        this.gridView = gridView;
        this.initialCounts = new EnumMap<>(counts);
        this.stock.putAll(counts);

        loadTextures();
        initUI(onBack, onStart);
        setupEventListeners();
        checkReady();
    }

    /**
     * Charge les images des navires dans un cache statique si ce n'est pas déjà fait.
     */
    private void loadTextures() {
        if (!TEXTURE_CACHE.isEmpty()) return;
        for (ShipType t : ShipType.values()) {
            Image img = AssetsManager.loadTexture(t.name().toLowerCase() + ".png").getImage();
            TEXTURE_CACHE.put(t, img);
        }
    }

    /**
     * Initialise la mise en page globale de la vue.
     * @param onBack Callback pour le bouton retour.
     * @param onStart Callback pour le bouton de lancement.
     */
    private void initUI(Runnable onBack, Runnable onStart) {
        this.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        this.setStyle(CSS_NAVY);

        BorderPane root = new BorderPane();
        root.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());

        VBox inv = buildInventory(onBack, onStart);
        root.setLeft(inv);

        gridView.autoScale(FXGL.getAppWidth() - 400, FXGL.getAppHeight() - 100);
        StackPane gridPane = new StackPane(gridView);
        gridPane.setAlignment(Pos.CENTER);

        Rectangle glassShield = new Rectangle();
        glassShield.widthProperty().bind(gridView.widthProperty());
        glassShield.heightProperty().bind(gridView.heightProperty());
        glassShield.setFill(Color.TRANSPARENT);
        gridPane.getChildren().add(glassShield);

        root.setCenter(gridPane);

        ghost.setMouseTransparent(true);
        ghost.setOpacity(0.6);
        ghost.setVisible(false);

        this.getChildren().addAll(root, ghost);
    }

    /**
     * Construit le panneau latéral contenant l'arsenal et les outils de gestion.
     * @param onBack Action de retour.
     * @param onStart Action de déploiement.
     * @return Le conteneur VBox de l'inventaire.
     */
    private VBox buildInventory(Runnable onBack, Runnable onStart) {
        VBox inv = new VBox(20);
        inv.setPrefWidth(300);
        inv.setPadding(new Insets(30));
        inv.setStyle(CSS_PANEL);

        Text title = new Text("ARSENAL");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 22));

        VBox list = new VBox(15);
        for (ShipType type : ShipType.values()) {
            if (initialCounts.getOrDefault(type, 0) > 0) {
                list.getChildren().add(createCard(type));
            }
        }

        VBox helpBox = buildHelpPanel();

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button btnRandom = new Button("PLACEMENT ALÉATOIRE");
        styleButton(btnRandom, "#f39c12");
        btnRandom.setOnAction(_ -> {
            applyRandomPlacement();
            AssetsManager.playSFX("random-button.wav", 0.6);
        });

        Button btnReset = new Button("RÉINITIALISER");
        styleButton(btnReset, "#ff4757");
        btnReset.setOnAction(_ -> {
                resetPlacement();
                AssetsManager.playSFX("reset.wav", 1);
        });

        Button bb = new Button("RETOUR CONFIG");
        styleButton(bb, "#64748b");
        bb.setOnAction(_ -> {
            AssetsManager.playSFX("button.wav", 1);
            onBack.run();
        });

        btnStart = new Button("DÉPLOYER LA FLOTTE");
        styleButton(btnStart, "#10b981");
        btnStart.setOnAction(_ -> {
            AssetsManager.playSFX("good.wav", 1);
            onStart.run();
        });

        inv.getChildren().addAll(title, list, helpBox, spacer, btnRandom, btnReset, bb, btnStart);
        return inv;
    }

    /**
     * Crée un panneau d'instructions visuelles pour les commandes de placement.
     * @return Un VBox contenant les textes d'aide stylisés.
     */
    private VBox buildHelpPanel() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(15, 0, 0, 0));
        box.setStyle("-fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1 0 0 0;");

        Text helpTitle = new Text("COMMANDES");
        helpTitle.setFill(Color.web("#94a3b8"));
        helpTitle.setFont(Font.font("System", FontWeight.BOLD, 12));

        HBox dragInfo = createHelpRow("🖱️", "Drag & Drop pour placer");
        HBox rotateInfo = createHelpRow("⌨️ [R]", "Rotation du navire");

        box.getChildren().addAll(helpTitle, dragInfo, rotateInfo);
        return box;
    }

    /**
     * Utilitaire pour créer une ligne d'aide avec une icône/touche et une description.
     * @param key Le symbole ou la touche.
     * @param desc La description de l'action.
     * @return Un HBox horizontal d'information.
     */
    private HBox createHelpRow(String key, String desc) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Text tKey = new Text(key);
        tKey.setFill(Color.web("#00d2d3"));
        tKey.setFont(Font.font("System", FontWeight.BOLD, 13));

        Text tDesc = new Text(desc);
        tDesc.setFill(Color.web("#cbd5e1"));
        tDesc.setFont(Font.font("System", 12));

        row.getChildren().addAll(tKey, tDesc);
        return row;
    }

    /**
     * Crée une carte interactive pour un type de navire donné.
     * @param type Le type de navire concerné.
     * @return Le composant visuel de la carte.
     */
    private VBox createCard(ShipType type) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10; -fx-cursor: hand;");

        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);

        ImageView img = new ImageView(TEXTURE_CACHE.get(type));
        img.setFitHeight(30); img.setPreserveRatio(true);

        Text count = new Text("x" + stock.get(type));
        count.setFill(Color.web("#00d2d3"));
        count.setFont(Font.font("System", FontWeight.BOLD, 14));

        Text n = new Text(type.getName().toUpperCase());
        n.setFill(Color.WHITE);

        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        row.getChildren().addAll(img, n, s, count);
        card.getChildren().add(row);

        card.setOnMousePressed(e -> {
            if (stock.get(type) > 0 && draggingType == null) {
                AssetsManager.playSFX("button.wav", 1);
                startDrag(type, Orientation.VERTICAL, e.getSceneX(), e.getSceneY());
            }
        });

        labels.put(type, count);
        cards.put(type, card);
        return card;
    }

    /**
     * Initialise l'état de glissement (drag) pour un navire.
     * @param type Type du navire sélectionné.
     * @param o Orientation initiale.
     * @param x Coordonnée X de la souris.
     * @param y Coordonnée Y de la souris.
     */
    private void startDrag(ShipType type, Orientation o, double x, double y) {
        draggingType = type;
        currentOrientation = o;
        ghost.setImage(TEXTURE_CACHE.get(type));

        double scale = gridView.getTransforms().isEmpty() ? 1.0 : ((Scale)gridView.getTransforms().getFirst()).getX();

        ghost.setFitWidth(CELL_SIZE * scale);
        ghost.setFitHeight(CELL_SIZE * type.getSize() * scale);
        ghost.setRotate(o == Orientation.VERTICAL ? 0 : 90);
        ghost.setVisible(true);
        updateGhost(x, y);
    }

    /**
     * Met à jour la position de l'image fantôme selon la position du curseur.
     * @param sceneX Position X dans la scène.
     * @param sceneY Position Y dans la scène.
     */
    private void updateGhost(double sceneX, double sceneY) {
        lastMouseX = sceneX;
        lastMouseY = sceneY;
        Point2D localMouse = this.sceneToLocal(sceneX, sceneY);
        ghost.setTranslateX(localMouse.getX() - ghost.getBoundsInLocal().getWidth() / 2.0);
        ghost.setTranslateY(localMouse.getY() - ghost.getBoundsInLocal().getHeight() / 2.0);
    }

    /**
     * Alterne l'orientation du navire actuellement en cours de placement.
     */
    private void rotateGhost() {
        currentOrientation = (currentOrientation == Orientation.VERTICAL) ? Orientation.HORIZONTAL : Orientation.VERTICAL;
        ghost.setRotate(currentOrientation == Orientation.VERTICAL ? 0 : 90);
        AssetsManager.playSFX("button.wav", 1);
        updateGhost(lastMouseX, lastMouseY);
    }

    /**
     * Tente de déposer le navire sur la grille aux coordonnées indiquées.
     * @param sx Coordonnée X de relâchement.
     * @param sy Coordonnée Y de relâchement.
     */
    private void drop(double sx, double sy) {
        Point2D p = gridView.sceneToLocal(sx, sy);
        double wOffset = (currentOrientation == Orientation.VERTICAL ? 1 : draggingType.getSize()) * (CELL_SIZE / 2.0);
        double hOffset = (currentOrientation == Orientation.VERTICAL ? draggingType.getSize() : 1) * (CELL_SIZE / 2.0);

        int cx = (int) Math.round((p.getX() - 40 - wOffset) / CELL_SIZE);
        int cy = (int) Math.round((p.getY() - 40 - hOffset) / CELL_SIZE);

        Ship s = new Ship(draggingType, new Coordinate(cx, cy), currentOrientation);

        if (board.canPlaceShip(s)) {
            board.placeShip(s);
            decrementStock(draggingType);
            gridView.updateDisplay();
            AssetsManager.playSFX("button.wav", 1.5);
        } else {
            refreshCard(draggingType);
            AssetsManager.playSFX("error.wav", 1);
        }

        ghost.setVisible(false);
        draggingType = null;
        checkReady();
    }

    /**
     * Configure les écouteurs de scène pour les touches clavier et les filtres de souris.
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

    /**
     * Gère la sélection d'un navire déjà positionné sur la grille pour permettre son déplacement.
     * @param e Événement de souris.
     */
    private void handleGridSelection(MouseEvent e) {
        Point2D p = gridView.sceneToLocal(e.getSceneX(), e.getSceneY());
        Coordinate target = gridView.getGridCoordinate(p.getX(), p.getY());

        board.getShips().stream()
                .filter(s -> s.getOccupiedCoordinates().contains(target))
                .findFirst().ifPresent(s -> {
                    AssetsManager.playSFX("button.wav", 1);
                    board.getShips().remove(s);
                    incrementStock(s.getType());
                    gridView.updateDisplay();
                    startDrag(s.getType(), s.getOrientation(), e.getSceneX(), e.getSceneY());
                });
    }

    /**
     * Augmente le stock disponible pour un type de navire donné.
     * @param t Type du navire.
     */
    private void incrementStock(ShipType t) {
        stock.put(t, stock.get(t) + 1);
        refreshCard(t);
        checkReady();
    }

    /**
     * Diminue le stock disponible pour un type de navire donné.
     * @param t Type du navire.
     */
    private void decrementStock(ShipType t) {
        stock.put(t, stock.get(t) - 1);
        refreshCard(t);
        checkReady();
    }

    /**
     * Met à jour l'affichage de la carte d'inventaire d'un navire (compteur et opacité).
     * @param t Type du navire à rafraîchir.
     */
    private void refreshCard(ShipType t) {
        if (labels.containsKey(t)) {
            labels.get(t).setText("x" + stock.get(t));
            cards.get(t).setOpacity(stock.get(t) > 0 ? 1 : 0.3);
        }
    }

    /**
     * Vide le plateau et restaure les quantités initiales de navires.
     */
    private void resetPlacement() {
        board.getShips().clear();
        stock.putAll(initialCounts);
        stock.keySet().forEach(this::refreshCard);
        gridView.updateDisplay();
        checkReady();
    }

    /**
     * Réinitialise le placement et positionne tous les navires de manière aléatoire.
     */
    private void applyRandomPlacement() {
        resetPlacement();
        board.placeShipsRandomly(initialCounts);
        gridView.updateDisplay();
        stock.keySet().forEach(t -> { stock.put(t, 0); refreshCard(t); });
        checkReady();
    }

    /**
     * Vérifie si l'arsenal est vide pour activer ou désactiver le bouton de démarrage.
     */
    private void checkReady() {
        boolean ok = stock.values().stream().allMatch(v -> v == 0);
        btnStart.setDisable(!ok);
        btnStart.setOpacity(ok ? 1 : 0.5);
    }

    private void styleButton(Button b, String hex) {
        b.setPrefWidth(240);
        b.setStyle("-fx-background-color: " + hex + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 10;");
    }
}