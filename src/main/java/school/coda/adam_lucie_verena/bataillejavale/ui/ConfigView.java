package school.coda.adam_lucie_verena.bataillejavale.ui;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import school.coda.adam_lucie_verena.bataillejavale.core.ai.Difficulty;
import school.coda.adam_lucie_verena.bataillejavale.core.model.GameConfig;
import school.coda.adam_lucie_verena.bataillejavale.core.model.ShipType;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Vue de configuration de la partie permettant de définir les paramètres de la carte et de la flotte.
 * Intègre un système de validation dynamique pour garantir que la flotte n'occupe pas plus de 30% de la grille.
 */
public class ConfigView extends HBox {

    private static final String NEON_CYAN = "#00d2d3";
    private static final Integer MAX_SIZE = 26;
    private static final Integer MIN_SIZE = 5;
    private final Pane gridDrawingPane = new Pane();
    private final Map<ShipType, Spinner<Integer>> shipSpinners = new EnumMap<>(ShipType.class);
    private final Text capacityLabel = new Text();

    private Slider sliderWidth, sliderHeight;
    private Label lblWidthText, lblHeightText;

    /**
     * Initialise l'interface de configuration avec les réglages par défaut ou actuels.
     * @param currentConfig La configuration de référence pour remplir les champs.
     * @param onValid Callback exécuté lorsque l'utilisateur valide ses choix.
     */
    public ConfigView(GameConfig currentConfig, Consumer<GameConfig> onValid) {
        this.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        this.setPadding(new Insets(40));
        this.setSpacing(80);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 70%, #0f172a, #020617);");

        VBox leftCol = buildMapSection(currentConfig);
        VBox rightCol = buildFleetSection(currentConfig, onValid);

        this.getChildren().addAll(leftCol, rightCol);
        updateCapacity();
    }

    /**
     * Construit la section de gauche dédiée aux dimensions du plateau de jeu.
     * @param config Configuration actuelle.
     * @return Conteneur VBox de la section cartographie.
     */
    private VBox buildMapSection(GameConfig config) {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(500);

        Text title = new Text("CARTOGRAPHIE");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 28));
        title.setFill(Color.web(NEON_CYAN));

        sliderWidth = createNeonSlider(config.gridWidth());
        sliderHeight = createNeonSlider(config.gridHeight());

        lblWidthText = new Label("LARGEUR : " + config.gridWidth());
        lblWidthText.setTextFill(Color.WHITE);
        lblWidthText.setFont(Font.font("Verdana", FontWeight.BOLD, 14));

        lblHeightText = new Label("HAUTEUR : " + config.gridHeight());
        lblHeightText.setTextFill(Color.WHITE);
        lblHeightText.setFont(Font.font("Verdana", FontWeight.BOLD, 14));

        StackPane previewContainer = new StackPane(gridDrawingPane);
        previewContainer.setPrefSize(400, 400);
        previewContainer.setMinSize(400, 400);

        sliderWidth.valueProperty().addListener((o, old, v) -> {
            lblWidthText.setText("LARGEUR : " + v.intValue());
            updateGridPreview(v.intValue(), (int)sliderHeight.getValue());
            updateCapacity();
        });

        sliderHeight.valueProperty().addListener((o, old, v) -> {
            lblHeightText.setText("HAUTEUR : " + v.intValue());
            updateGridPreview((int)sliderWidth.getValue(), v.intValue());
            updateCapacity();
        });

        updateGridPreview(config.gridWidth(), config.gridHeight());

        box.getChildren().addAll(title, lblWidthText, sliderWidth, lblHeightText, sliderHeight, previewContainer);
        return box;
    }

    /**
     * Construit la section de droite dédiée à la sélection de la flotte et à la validation.
     * @param config Configuration actuelle.
     * @param onValid Callback de validation.
     * @return Conteneur VBox de la section arsenal.
     */
    private VBox buildFleetSection(GameConfig config, Consumer<GameConfig> onValid) {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(500);

        Text title = new Text("ARSENAL DE GUERRE");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 28));
        title.setFill(Color.web(NEON_CYAN));

        capacityLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));

        VBox fleetList = new VBox(10);
        for (ShipType type : ShipType.values()) {
            fleetList.getChildren().add(createShipConfigCard(type, config.shipCounts().getOrDefault(type, 1)));
        }

        Button btnNext = new Button("VALIDER LA STRATÉGIE");
        btnNext.setPrefWidth(400);
        btnNext.setMinHeight(50);
        btnNext.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        btnNext.setStyle("-fx-background-color: " + NEON_CYAN + "; -fx-text-fill: black; -fx-background-radius: 5; -fx-cursor: hand;");

        btnNext.setOnAction(_ -> {
            Map<ShipType, Integer> counts = new EnumMap<>(ShipType.class);
            shipSpinners.forEach((type, spinner) -> counts.put(type, spinner.getValue()));
            onValid.accept(new GameConfig((int)sliderWidth.getValue(), (int)sliderHeight.getValue(), Difficulty.NORMALE, false, false, "Joueur 1", "IA", counts));
        });

        box.getChildren().addAll(title, capacityLabel, fleetList, btnNext);
        return box;
    }

    /**
     * Calcule et met à jour l'indicateur de capacité de la grille.
     * Ajuste dynamiquement le maximum des spinners pour éviter de dépasser la limite d'occupation.
     */
    private void updateCapacity() {
        int w = (int) sliderWidth.getValue();
        int h = (int) sliderHeight.getValue();
        int max = (int) ((w * h) * 0.30);
        int used = shipSpinners.entrySet().stream().mapToInt(e -> e.getKey().getSize() * e.getValue().getValue()).sum();
        int remaining = max - used;

        capacityLabel.setText("CAPACITÉ : " + used + " / " + max + " CASES");
        capacityLabel.setFill(remaining <= 0 ? Color.ORANGERED : Color.LIME);

        shipSpinners.forEach((type, spinner) -> {
            SpinnerValueFactory.IntegerSpinnerValueFactory factory = (SpinnerValueFactory.IntegerSpinnerValueFactory) spinner.getValueFactory();
            factory.setMax(spinner.getValue() + (Math.max(0, remaining) / type.getSize()));
        });
    }

    /**
     * Crée un élément de contrôle pour un type de navire spécifique.
     * @param type Type de navire concerné.
     * @param initial Quantité initiale affichée.
     * @return Conteneur HBox représentant la ligne de configuration du navire.
     */
    private HBox createShipConfigCard(ShipType type, int initial) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 20, 10, 20));
        card.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8;");
        Text name = new Text(type.getName().toUpperCase());
        name.setFill(Color.WHITE);
        name.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
        Spinner<Integer> spinner = new Spinner<>(0, 999, initial);
        spinner.setPrefWidth(80);
        spinner.valueProperty().addListener((o, old, v) -> updateCapacity());
        shipSpinners.put(type, spinner);
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        card.getChildren().addAll(name, spacer, spinner);
        return card;
    }

    /**
     * Met à jour le dessin de prévisualisation de la grille.
     * @param w Largeur de la grille à dessiner.
     * @param h Hauteur de la grille à dessiner.
     */
    private void updateGridPreview(int w, int h) {
        gridDrawingPane.getChildren().clear();
        double offset = 30;
        double availableSize = 400.0 - offset - 10;
        double cellSize = Math.min(availableSize / w, availableSize / h);
        double gW = w * cellSize; double gH = h * cellSize;

        Group gridGroup = new Group();
        Rectangle frame = new Rectangle(0, 0, gW, gH);
        frame.setFill(Color.TRANSPARENT);
        frame.setStroke(Color.web(NEON_CYAN, 0.8));
        frame.setStrokeWidth(2);
        gridGroup.getChildren().add(frame);

        for (int x = 0; x < w; x++) {
            Text letter = new Text(String.valueOf((char)('A' + x)));
            letter.setFill(Color.web(NEON_CYAN, 0.7)); letter.setFont(Font.font("Verdana", 10));
            letter.setX(x * cellSize + (cellSize/2) - 3); letter.setY(-5);
            gridGroup.getChildren().add(letter);
            for (int y = 0; y < h; y++) {
                if (x == 0) {
                    Text number = new Text(String.valueOf(y + 1));
                    number.setFill(Color.web(NEON_CYAN, 0.7)); number.setFont(Font.font("Verdana", 10));
                    number.setX(-20); number.setY(y * cellSize + (cellSize/2) + 3);
                    gridGroup.getChildren().add(number);
                }
                Rectangle r = new Rectangle(x * cellSize, y * cellSize, cellSize, cellSize);
                r.setFill(Color.web(NEON_CYAN, 0.1)); r.setStroke(Color.web(NEON_CYAN, 0.2));
                gridGroup.getChildren().add(r);
            }
        }
        gridGroup.setTranslateX((400 - gW) / 2.0 + (offset / 2));
        gridGroup.setTranslateY((400 - gH) / 2.0 + (offset / 2));
        gridDrawingPane.getChildren().add(gridGroup);
    }

    /**
     * Crée un curseur stylisé pour les réglages numériques.
     *
     * @param val Valeur initiale.
     * @return Instance de Slider configurée.
     */
    private Slider createNeonSlider(int val) {
        Slider s = new Slider(ConfigView.MIN_SIZE, ConfigView.MAX_SIZE, val);
        s.setMaxWidth(400); s.setMajorTickUnit(1); s.setSnapToTicks(true);
        return s;
    }
}