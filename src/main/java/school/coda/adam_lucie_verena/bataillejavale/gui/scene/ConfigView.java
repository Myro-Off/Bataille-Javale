package school.coda.adam_lucie_verena.bataillejavale.gui.scene;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import school.coda.adam_lucie_verena.bataillejavale.core.ai.Difficulty;
import school.coda.adam_lucie_verena.bataillejavale.core.model.GameConfig;
import school.coda.adam_lucie_verena.bataillejavale.core.model.ShipType;
import school.coda.adam_lucie_verena.bataillejavale.gui.Theme;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Interface de configuration tactique permettant de définir les paramètres de la partie.
 * Gère la dimension des grilles, la composition de la flotte, la difficulté de l'IA
 * et la validation des contraintes d'espace.
 */
public class ConfigView extends HBox {
    private static final Integer MAX_SIZE = 26;
    private static final Integer MIN_SIZE = 5;

    private final Pane gridDrawingPane = new Pane();
    private final Map<ShipType, Spinner<Integer>> shipSpinners = new EnumMap<>(ShipType.class);
    private final Text capacityLabel = new Text();
    private Slider sliderWidth, sliderHeight;
    private Label lblWidthText, lblHeightText;
    private Button btnNext;
    private Difficulty selectedDifficulty = Difficulty.NORMAL;

    /**
     * Construit la vue de configuration.
     * @param currentConfig Configuration initiale à afficher.
     * @param onValid Action déclenchée lors de la validation.
     * @param onBack Action déclenchée lors du retour au menu.
     */
    public ConfigView(GameConfig currentConfig, Consumer<GameConfig> onValid, Runnable onBack) {
        this.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        this.setPadding(new Insets(50));
        this.setSpacing(60);
        this.setAlignment(Pos.CENTER);
        this.setStyle(Theme.MAIN_GRADIENT);

        this.getChildren().addAll(buildMapSection(currentConfig, onBack), buildFleetSection(currentConfig, onValid));
        updateCapacity();
    }

    /**
     * Construit la section de réglage des dimensions de la carte.
     * @param config Configuration actuelle.
     * @param onBack Action de retour.
     * @return Conteneur VBox de la section cartographie.
     */
    private VBox buildMapSection(GameConfig config, Runnable onBack) {
        VBox box = createTacticalPanel("SYSTÈME DE CARTOGRAPHIE");

        sliderWidth = createNeonSlider(config.gridWidth());
        sliderHeight = createNeonSlider(config.gridHeight());

        lblWidthText = createStyledLabel("DIMENSION X : " + config.gridWidth());
        lblHeightText = createStyledLabel("DIMENSION Y : " + config.gridHeight());

        StackPane previewContainer = new StackPane(gridDrawingPane);
        previewContainer.setPrefSize(400, 400);
        previewContainer.setStyle(Theme.PREVIEW_HOLDER);

        sliderWidth.valueProperty().addListener((o, old, v) -> {
            lblWidthText.setText("DIMENSION X : " + v.intValue());
            updateGridPreview(v.intValue(), (int)sliderHeight.getValue());
            updateCapacity();
        });

        sliderHeight.valueProperty().addListener((o, old, v) -> {
            lblHeightText.setText("DIMENSION Y : " + v.intValue());
            updateGridPreview((int)sliderWidth.getValue(), v.intValue());
            updateCapacity();
        });

        updateGridPreview(config.gridWidth(), config.gridHeight());

        Button btnBack = new Button("↩ RETOUR AU QG");
        btnBack.setPrefWidth(200);
        btnBack.setCursor(Theme.CURSOR_CLICK);
        btnBack.setStyle(Theme.BTN_SECONDARY_NORMAL);
        btnBack.setOnMouseEntered(_ -> btnBack.setStyle(Theme.BTN_SECONDARY_HOVER));
        btnBack.setOnMouseExited(_ -> btnBack.setStyle(Theme.BTN_SECONDARY_NORMAL));
        btnBack.setOnAction(_ -> onBack.run());

        box.getChildren().addAll(lblWidthText, sliderWidth, lblHeightText, sliderHeight, previewContainer, btnBack);
        return box;
    }

    /**
     * Construit la section dédiée à la flotte et au choix de la doctrine (difficulté).
     * @param config Configuration actuelle.
     * @param onValid Action de validation.
     * @return Conteneur VBox de la section arsenal.
     */
    private VBox buildFleetSection(GameConfig config, Consumer<GameConfig> onValid) {
        VBox box = createTacticalPanel("ARSENAL & DOCTRINE");

        VBox diffBox = new VBox(10);
        HBox diffButtons = new HBox(10);
        ToggleGroup group = new ToggleGroup();

        for (Difficulty d : Difficulty.values()) {
            diffButtons.getChildren().add(createDifficultyButton(d, group, diffButtons));
        }

        diffBox.getChildren().addAll(createStyledLabel("NIVEAU DE L'OPPOSITION :"), diffButtons);

        capacityLabel.setFont(Theme.font(16, FontWeight.BOLD));
        VBox fleetList = new VBox(12);
        for (ShipType type : ShipType.values()) {
            fleetList.getChildren().add(createShipConfigCard(type, config.shipCounts().getOrDefault(type, 1)));
        }

        btnNext = new Button("INITIALISER LA BATAILLE");
        btnNext.setPrefWidth(400);
        btnNext.setMinHeight(60);
        btnNext.setFont(Theme.font(20, FontWeight.BOLD));
        btnNext.setOnAction(_ -> {
            Map<ShipType, Integer> counts = new EnumMap<>(ShipType.class);
            shipSpinners.forEach((type, spinner) -> counts.put(type, spinner.getValue()));
            onValid.accept(new GameConfig((int)sliderWidth.getValue(), (int)sliderHeight.getValue(),
                    selectedDifficulty, false, false, "Amiral", "IA", counts));
        });

        box.getChildren().addAll(diffBox, new Separator(), capacityLabel, fleetList, btnNext);
        return box;
    }

    /**
     * Crée un bouton de sélection de difficulté configuré.
     * @param d La difficulté associée au bouton.
     * @param group Le groupe de sélection unique.
     * @param container Le conteneur parent pour la mise à jour visuelle groupée.
     * @return Un ToggleButton stylisé.
     */
    private ToggleButton createDifficultyButton(Difficulty d, ToggleGroup group, HBox container) {
        ToggleButton tb = new ToggleButton(d.getLabel().toUpperCase());
        tb.setToggleGroup(group);
        tb.setPrefWidth(130);
        tb.setCursor(Theme.CURSOR_CLICK);
        tb.setUserData(d);

        updateDifficultyButtonStyle(tb, d == selectedDifficulty);

        tb.setOnAction(_ -> {
            selectedDifficulty = d;
            container.getChildren().forEach(node -> {
                if (node instanceof ToggleButton btn) {
                    updateDifficultyButtonStyle(btn, btn.isSelected());
                }
            });
        });

        return tb;
    }

    /**
     * Applique le style visuel à un bouton de difficulté selon son état de sélection.
     * @param btn Le bouton à styliser.
     * @param isActive True si le bouton est l'élément sélectionné.
     */
    private void updateDifficultyButtonStyle(ToggleButton btn, boolean isActive) {
        if (isActive) {
            btn.setStyle(Theme.BTN_NEXT_ACTIVE);
            btn.setEffect(Theme.GLOW_CYAN);
        } else {
            btn.setStyle(Theme.BTN_SECONDARY_NORMAL);
            btn.setEffect(null);
        }
    }

    /**
     * Crée un panneau tactique avec un style "glass" et un titre néon.
     * @param titleStr Titre du panneau.
     * @return VBox configurée.
     */
    private VBox createTacticalPanel(String titleStr) {
        VBox box = new VBox(25);
        box.setPadding(new Insets(30));
        box.setAlignment(Pos.TOP_CENTER);
        box.setPrefWidth(550);
        box.setStyle(Theme.GLASS_PANEL);

        Text title = new Text(titleStr);
        title.setFont(Theme.font(24, FontWeight.BOLD));
        title.setFill(Theme.CYAN);
        title.setEffect(Theme.GLOW_CYAN);

        box.getChildren().add(title);
        return box;
    }

    /**
     * Crée un label stylisé aux couleurs du thème.
     * @param text Texte du label.
     * @return Label configuré.
     */
    private Label createStyledLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Theme.TEXT_MUTED);
        l.setFont(Theme.font(12, FontWeight.BOLD));
        return l;
    }

    /**
     * Génère une carte de configuration pour un type de navire spécifique.
     * @param type Type de navire.
     * @param initial Valeur initiale du spinner.
     * @return HBox contenant les infos du navire et le spinner de quantité.
     */
    private HBox createShipConfigCard(ShipType type, int initial) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 20, 12, 20));
        card.setStyle(Theme.CARD_STYLE);

        VBox info = new VBox(2);
        Text name = new Text(type.getName().toUpperCase());
        name.setFill(Color.WHITE);
        name.setFont(Theme.font(14, FontWeight.BOLD));

        Text sizeInfo = new Text("ENCOMBREMENT : " + type.getSize() + " UNITÉS");
        sizeInfo.setFill(Theme.SHIP_INFO);
        sizeInfo.setFont(Theme.font(10, FontWeight.NORMAL));
        info.getChildren().addAll(name, sizeInfo);

        Spinner<Integer> spinner = new Spinner<>(0, 999, initial);
        spinner.setPrefWidth(90);
        spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        spinner.valueProperty().addListener((o, old, v) -> updateCapacity());
        shipSpinners.put(type, spinner);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        card.getChildren().addAll(info, spacer, spinner);
        return card;
    }

    /**
     * Analyse l'occupation spatiale de la flotte et valide la configuration.
     * Bloque le passage à l'étape suivante en cas de surcharge de la grille.
     */
    private void updateCapacity() {
        int w = (int) sliderWidth.getValue();
        int h = (int) sliderHeight.getValue();
        int max = (int) ((w * h) * 0.30);
        int used = shipSpinners.entrySet().stream().mapToInt(e -> e.getKey().getSize() * e.getValue().getValue()).sum();

        boolean isOverloaded = used > max;
        capacityLabel.setText(isOverloaded ? "☢ ALERTE : SURCHARGE FLOTTE (" + used + "/" + max + ")" : "✓ CAPACITÉ OPÉRATIONNELLE (" + used + "/" + max + ")");
        capacityLabel.setFill(isOverloaded ? Theme.RED_ALERTE : Theme.GREEN_SUCCESS);

        btnNext.setDisable(isOverloaded);
        btnNext.setStyle(isOverloaded ? Theme.BTN_NEXT_DISABLED : Theme.BTN_NEXT_ACTIVE);
        btnNext.setEffect(isOverloaded ? null : Theme.GLOW_CYAN);
        btnNext.setCursor(isOverloaded ? javafx.scene.Cursor.DEFAULT : Theme.CURSOR_CLICK);
    }

    /**
     * Crée un Slider personnalisé aux couleurs néon du thème.
     * @param val Valeur par défaut.
     * @return Slider configuré.
     */
    private Slider createNeonSlider(int val) {
        Slider s = new Slider(MIN_SIZE, MAX_SIZE, val);
        s.setMaxWidth(400); s.setMajorTickUnit(1); s.setSnapToTicks(true);
        s.setStyle("-fx-control-inner-background: " + Theme.HEX_CYAN + ";");
        return s;
    }

    /**
     * Redessine la prévisualisation de la grille de manière centrée et contenue.
     * Calcule dynamiquement la taille des cellules pour laisser de la place aux libellés
     * alphanumériques sans déborder du conteneur de 400x400.
     * * @param w Nombre de colonnes (Largeur).
     * @param h Nombre de lignes (Hauteur).
     */
    private void updateGridPreview(int w, int h) {
        gridDrawingPane.getChildren().clear();

        double containerSize = 400.0;
        double labelPaddingLeft = 30.0;
        double labelPaddingTop = 25.0;
        double margin = 10.0;

        double availableWidth = containerSize - labelPaddingLeft - margin;
        double availableHeight = containerSize - labelPaddingTop - margin;

        double cellSize = Math.min(availableWidth / w, availableHeight / h);

        double gridWidth = w * cellSize;
        double gridHeight = h * cellSize;

        Group gridGroup = new Group();

        Rectangle frame = new Rectangle(0, 0, gridWidth, gridHeight);
        frame.setFill(Color.TRANSPARENT);
        frame.setStroke(Theme.GRID_FRAME);
        frame.setStrokeWidth(2);
        gridGroup.getChildren().add(frame);

        for (int x = 0; x < w; x++) {
            Text letter = new Text(String.valueOf((char) ('A' + x)));
            letter.setFill(Theme.GRID_LABEL);
            letter.setFont(Theme.font(10, FontWeight.NORMAL));
            letter.setX(x * cellSize + (cellSize / 2) - 4);
            letter.setY(-8);
            gridGroup.getChildren().add(letter);

            for (int y = 0; y < h; y++) {
                if (x == 0) {
                    Text number = new Text(String.valueOf(y + 1));
                    number.setFill(Theme.GRID_LABEL);
                    number.setFont(Theme.font(10, FontWeight.NORMAL));
                    number.setX(-22);
                    number.setY(y * cellSize + (cellSize / 2) + 4);
                    gridGroup.getChildren().add(number);
                }
                Rectangle r = new Rectangle(x * cellSize, y * cellSize, cellSize, cellSize);
                r.setFill(Theme.GRID_BG);
                r.setStroke(Theme.GRID_LINE);
                gridGroup.getChildren().add(r);
            }
        }

        gridGroup.setTranslateX((containerSize - gridWidth + labelPaddingLeft) / 2.0);
        gridGroup.setTranslateY((containerSize - gridHeight + labelPaddingTop) / 2.0);

        gridDrawingPane.getChildren().add(gridGroup);
    }
}