package school.coda.adam_lucie_verena.bataillejavale.gui.scene;

import com.almasb.fxgl.dsl.FXGL;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import school.coda.adam_lucie_verena.bataillejavale.gui.AssetsManager;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.MenuButton;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Interface de configuration tactique "Command Center".
 * Gère la persistance complète des réglages de mission.
 */
public class ConfigView extends StackPane {

    private static final int MIN_SIZE = 5;
    private static final String SPINNER_STYLE = "-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #06b6d4; -fx-border-radius: 5;";

    private final Pane gridDrawingPane = new Pane();
    private final Map<ShipType, Spinner<Integer>> shipSpinners = new EnumMap<>(ShipType.class);
    private final Map<String, CheckBox> eventToggles = new HashMap<>();
    private final Map<String, Slider> eventSliders = new HashMap<>();

    private Slider sliderWidth, sliderHeight;
    private Label lblWidthText, lblHeightText;
    private Difficulty selectedDifficulty;
    private final BooleanProperty isSalveMode = new SimpleBooleanProperty(false);
    private Text difficultyDesc, modeDesc;
    private final Text capacityLabel = new Text();
    private Button btnNext;

    private final CheckBox cbCustomFleet = new CheckBox("PERSONNALISER LE NOMBRE DE NAVIRES");
    private final CheckBox cbEnableEvents = new CheckBox("ACTIVER LES ÉVÉNEMENTS ALÉATOIRES");
    private final CheckBox cbSpecialAbilities = new CheckBox("ACTIVER LES CAPACITÉS SPÉCIALES");
    private final VBox statsPanel = new VBox(10);
    private Spinner<Integer> apocRoundSpinner;

    /**
     * @param currentConfig Configuration à charger pour restauration.
     * @param onValid       Action lors du lancement de la bataille.
     * @param onBack        Action de retour au menu principal.
     */
    //                             🚨 pourrait être mieux nommé. Ex. onLaunchBattle
    public ConfigView(GameConfig currentConfig, Consumer<GameConfig> onValid, Runnable onBack) {
        this.selectedDifficulty = currentConfig.difficulty();
        this.isSalveMode.set(currentConfig.isSalveMode());

        this.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        this.setStyle(Theme.MAIN_GRADIENT);

        // ScrollPane transparent sans barre visible
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox mainContainer = new VBox(50);
        mainContainer.setPadding(new Insets(50));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // Restauration des états
        this.cbCustomFleet.setSelected(currentConfig.isCustomFleet());
        this.cbEnableEvents.setSelected(currentConfig.eventsEnabled());
        this.cbSpecialAbilities.setSelected(currentConfig.abilitiesEnabled());

        FlowPane topSection = new FlowPane(60, 40);
        topSection.setAlignment(Pos.CENTER);
        topSection.getChildren().addAll(buildMapSection(currentConfig, onBack), buildFleetSection(currentConfig, onValid));

        mainContainer.getChildren().addAll(topSection, buildMissionSettingsSection(currentConfig));
        scroll.setContent(mainContainer);
        this.getChildren().add(scroll);

        updateCapacity();
        calculatePercentages();
    }

    private VBox buildMapSection(GameConfig config, Runnable onBack) {
        VBox box = createTacticalPanel("SYSTÈME DE CARTOGRAPHIE");
        box.setPrefWidth(550);

        sliderWidth = createNeonSlider(config.gridWidth());
        sliderHeight = createNeonSlider(config.gridHeight());
        lblWidthText = createStyledLabel("DIMENSION X : " + config.gridWidth());
        lblHeightText = createStyledLabel("DIMENSION Y : " + config.gridHeight());

        sliderWidth.valueProperty().addListener((o, old, v) -> {
            lblWidthText.setText("DIMENSION X : " + v.intValue());
            updateGridPreview(v.intValue(), (int) sliderHeight.getValue());
            updateCapacity();
        });

        sliderHeight.valueProperty().addListener((o, old, v) -> {
            lblHeightText.setText("DIMENSION Y : " + v.intValue());
            updateGridPreview((int) sliderWidth.getValue(), v.intValue());
            updateCapacity();
        });

        Button btnResetGrid = new Button("RÉINITIALISER TAILLE (10x10)");
        styleResetButton(btnResetGrid);
        btnResetGrid.setOnAction(_ -> {
            sliderWidth.setValue(10);
            sliderHeight.setValue(10);
        });

        updateGridPreview(config.gridWidth(), config.gridHeight());
        box.getChildren().addAll(lblWidthText, sliderWidth, lblHeightText, sliderHeight, btnResetGrid,
                new StackPane(gridDrawingPane) {{
                    setPrefSize(400, 400);
                    setStyle(Theme.PREVIEW_HOLDER);
                }},
                new MenuButton("RETOUR AU QG", onBack));
        return box;
    }
    //                                   🚨 pourrait être mieux nommé. Ex. onLaunchBattle
    private VBox buildFleetSection(GameConfig config, Consumer<GameConfig> onValid) {
        VBox box = createTacticalPanel("ARSENAL & DOCTRINE");
        box.setPrefWidth(550);

        HBox diffButtons = new HBox(10);
        ToggleGroup dg = new ToggleGroup();
        for (Difficulty d : Difficulty.values()) diffButtons.getChildren().add(createDifficultyButton(d, dg));
        difficultyDesc = createDescText();
        updateDifficultyDescription();

        HBox modeButtons = new HBox(10);
        ToggleGroup mg = new ToggleGroup();
        modeButtons.getChildren().addAll(createModeButton("CLASSIQUE", false, mg), createModeButton("SALVE", true, mg));
        modeDesc = createDescText();
        updateModeDescription();

        cbCustomFleet.setTextFill(Color.WHITE);
        cbCustomFleet.setFont(Theme.font(14, FontWeight.BOLD));
        VBox fleetList = new VBox(12);
        for (ShipType type : ShipType.values())
            fleetList.getChildren().add(createShipConfigCard(type, config.shipCounts().getOrDefault(type, 1)));

        fleetList.visibleProperty().bind(cbCustomFleet.selectedProperty());
        fleetList.managedProperty().bind(fleetList.visibleProperty());
        capacityLabel.visibleProperty().bind(cbCustomFleet.selectedProperty());
        capacityLabel.managedProperty().bind(capacityLabel.visibleProperty());

        Button btnResetFleet = new Button("RÉINITIALISER FLOTTE");
        styleResetButton(btnResetFleet);
        btnResetFleet.visibleProperty().bind(cbCustomFleet.selectedProperty());
        btnResetFleet.managedProperty().bind(btnResetFleet.visibleProperty());
        btnResetFleet.setOnAction(_ -> shipSpinners.values().forEach(s -> s.getValueFactory().setValue(1)));

        btnNext = new Button("INITIALISER LA BATAILLE");
        btnNext.setPrefWidth(400);
        btnNext.setMinHeight(60);
        btnNext.setFont(Theme.font(20, FontWeight.BOLD));
        btnNext.setOnAction(_ -> handleGameLaunch(onValid));

        box.getChildren().addAll(createStyledLabel("NIVEAU IA :"), diffButtons, difficultyDesc,
                createStyledLabel("DOCTRINE :"), modeButtons, modeDesc,
                new Separator(), cbCustomFleet, capacityLabel, fleetList, btnResetFleet, btnNext);
        return box;
    }

    private VBox buildMissionSettingsSection(GameConfig config) {
        VBox box = createTacticalPanel("PARAMÈTRES DES ÉVÉNEMENTS");
        box.setMaxWidth(1160);

        cbSpecialAbilities.visibleProperty().bind(isSalveMode.not());
        cbSpecialAbilities.managedProperty().bind(cbSpecialAbilities.visibleProperty());

        HBox coreLayout = new HBox(50);
        coreLayout.setAlignment(Pos.TOP_CENTER);
        coreLayout.visibleProperty().bind(cbEnableEvents.selectedProperty());
        coreLayout.managedProperty().bind(coreLayout.visibleProperty());

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(15);
        int r = 0;
        addEventRow(grid, "Rien ne se passe", config, r++, "Stabilité totale du secteur.");
        addEventRow(grid, "Brouillage", config, r++, "Brouille les logs radar.");
        addEventRow(grid, "Pluie de météores", config, r++, "Dégâts orbitaux aléatoires.");
        addEventRow(grid, "Ravitaillement", config, r++, "Recharge les capacités spéciales.");
        addEventRow(grid, "Ravitaillement gratuit", config, r++, "Bonus logistique sans perte de tour.");
        addEventRow(grid, "Blocage capacité", config, r++, "Désactive temporairement les compétences.");
        addEventRow(grid, "Salve boostée", config, r++, "Augmente le quota de tirs (Mode Salve).");

        VBox statsBox = new VBox(15, createStyledLabel("PROBABILITÉS RÉELLES (BASE 100%)"), statsPanel);
        statsBox.setPadding(new Insets(20));
        statsBox.setStyle("-fx-background-color: rgba(15, 23, 42, 0.7); -fx-border-color: #334155; -fx-border-radius: 10;");
        statsBox.setMinWidth(350);

        coreLayout.getChildren().addAll(grid, statsBox);

        apocRoundSpinner = new Spinner<>(2, 100, config.apocalypseRound());
        apocRoundSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        apocRoundSpinner.setStyle(SPINNER_STYLE);

        CheckBox toggleApoc = new CheckBox("Activer") {{
            setSelected(config.isEventActive("Apocalypse"));
            textFillProperty().set(Color.WHITE);
        }};
        eventToggles.put("Apocalypse", toggleApoc);

        HBox apocBox = new HBox(20, createStyledLabel("DÉBUT APOCALYPSE (MANCHE) :"), toggleApoc, apocRoundSpinner);
        apocBox.setAlignment(Pos.CENTER);
        apocBox.visibleProperty().bind(cbEnableEvents.selectedProperty());
        apocBox.managedProperty().bind(apocBox.visibleProperty());

        Button btnResetEvents = new Button("RÉINITIALISER LES ÉVÉNEMENTS");
        styleResetButton(btnResetEvents);
        btnResetEvents.setOnAction(_ -> resetEvents());

        box.getChildren().addAll(cbEnableEvents, cbSpecialAbilities, new Separator(), coreLayout, apocBox, btnResetEvents);
        return box;
    }

    /**
     * Collecte les données de l'interface et déclenche le callback de lancement.
     */
    private void handleGameLaunch(Consumer<GameConfig> onValid) {
        AssetsManager.playSFX("button_init.wav", 1.0);

        Map<ShipType, Integer> counts = new EnumMap<>(ShipType.class);
        shipSpinners.forEach((t, s) -> counts.put(t, s.getValue()));

        Map<String, Integer> weights = new HashMap<>();
        Map<String, Boolean> toggles = new HashMap<>();

        eventSliders.forEach((name, s) -> weights.put(name, (int) s.getValue()));
        eventToggles.forEach((name, cb) -> toggles.put(name, cb.isSelected()));

        // 🚨 Pourrait être mieux nommé
        // Ex. onLaunchBattle
        onValid.accept(new GameConfig(
                (int) sliderWidth.getValue(), (int) sliderHeight.getValue(),
                selectedDifficulty, isSalveMode.get(), cbEnableEvents.isSelected(),
                cbSpecialAbilities.isSelected(), cbCustomFleet.isSelected(),
                weights, toggles, apocRoundSpinner.getValue(), "Amiral", "IA", counts
        ));
    }

    private void addEventRow(GridPane grid, String name, GameConfig config, int row, String desc) {
        CheckBox cb = new CheckBox() {{
            setSelected(config.isEventActive(name));
            if (name.equals("Rien ne se passe")) setDisable(true);
        }};
        Slider s = new Slider(0, 100, config.getWeight(name));
        s.setPrefWidth(150);
        Label val = new Label((int) s.getValue() + " pts");
        val.setTextFill(Theme.CYAN);
        val.setPrefWidth(50);

        s.valueProperty().addListener((o, old, v) -> {
            val.setText(v.intValue() + " pts");
            calculatePercentages();
        });
        cb.selectedProperty().addListener((o, old, v) -> calculatePercentages());
        s.disableProperty().bind(cb.selectedProperty().not());

        eventToggles.put(name, cb);
        eventSliders.put(name, s);

        HBox controlBox = new HBox(15, cb, new VBox(2, createStyledLabel(name.toUpperCase()), new Text(desc) {{
            setFill(Theme.TEXT_MUTED);
            setFont(Theme.font(10, FontWeight.NORMAL));
        }}));
        controlBox.setAlignment(Pos.CENTER_LEFT);

        if (name.equals("Salve boostée")) controlBox.visibleProperty().bind(isSalveMode);
        else if (name.contains("capacité") || name.contains("Ravitaillement"))
            controlBox.visibleProperty().bind(isSalveMode.not().and(cbSpecialAbilities.selectedProperty()));

        controlBox.managedProperty().bind(controlBox.visibleProperty());
        s.visibleProperty().bind(controlBox.visibleProperty());
        s.managedProperty().bind(s.visibleProperty());
        val.visibleProperty().bind(controlBox.visibleProperty());
        val.managedProperty().bind(val.visibleProperty());

        grid.addRow(row, controlBox, s, val);
    }

    private void calculatePercentages() {
        statsPanel.getChildren().clear();
        double total = eventSliders.entrySet().stream()
                .filter(e -> eventToggles.get(e.getKey()).isSelected() && e.getValue().isVisible())
                .mapToDouble(e -> e.getValue().getValue()).sum();

        if (total == 0) {
            addStatLine("RIEN NE SE PASSE (Sécurité)", 99.0, Color.GRAY);
        } else {
            double factor = 99.0 / total;
            eventSliders.forEach((name, s) -> {
                if (eventToggles.get(name).isSelected() && s.isVisible()) {
                    double pct = s.getValue() * factor;
                    if (pct > 0) addStatLine(name, pct, Color.WHITE);
                }
            });
        }
        addStatLine("JOUR ENSOLEILLÉ (Fixe)", 1.0, Color.GOLD);
    }

    private void addStatLine(String name, double pct, Color color) {
        statsPanel.getChildren().add(new Text(String.format("• %s : %.1f%%", name.toUpperCase(), pct)) {{
            setFill(color);
            setFont(Theme.mono(12, FontWeight.BOLD));
        }});
    }

    private void updateGridPreview(int w, int h) {
        gridDrawingPane.getChildren().clear();
        double cellSize = Math.min(360.0 / w, 365.0 / h);
        Group group = new Group();
        group.getChildren().add(new Rectangle(w * cellSize, h * cellSize) {{
            setFill(Color.TRANSPARENT);
            setStroke(Theme.GRID_FRAME);
            setStrokeWidth(2);
        }});

        for (int x = 0; x < w; x++) {
            int finalX = x;
            group.getChildren().add(new Text(String.valueOf((char) ('A' + finalX))) {{
                setFill(Theme.GRID_LABEL);
                setFont(Theme.font(10, FontWeight.NORMAL));
                setX(finalX * cellSize + (cellSize / 2) - 4);
                setY(-8);
            }});
            for (int y = 0; y < h; y++) {
                if (x == 0) {
                    int finalY = y;
                    group.getChildren().add(new Text(String.valueOf(finalY + 1)) {{
                        setFill(Theme.GRID_LABEL);
                        setFont(Theme.font(10, FontWeight.NORMAL));
                        setX(-22);
                        setY(finalY * cellSize + (cellSize / 2) + 4);
                    }});
                }
                group.getChildren().add(new Rectangle(x * cellSize, y * cellSize, cellSize, cellSize) {{
                    setFill(Theme.GRID_BG);
                    setStroke(Theme.GRID_LINE);
                }});
            }
        }
        group.setTranslateX((400 - w * cellSize + 30) / 2.0);
        group.setTranslateY((400 - h * cellSize + 25) / 2.0);
        gridDrawingPane.getChildren().add(group);
    }

    private ToggleButton createModeButton(String label, boolean isSalve, ToggleGroup g) {
        return new ToggleButton(label) {{
            setToggleGroup(g);
            setPrefWidth(180);
            if (isSalve == isSalveMode.get()) {
                setSelected(true);
                updateButtonStyle(this, true);
            }
            setOnAction(_ -> {
                AssetsManager.playSFX("button.wav", 1.0);
                isSalveMode.set(isSalve);
                updateModeDescription();
                g.getToggles().forEach(t -> updateButtonStyle((ToggleButton) t, t.isSelected()));
            });
        }};
    }

    private ToggleButton createDifficultyButton(Difficulty d, ToggleGroup g) {
        return new ToggleButton(d.getLabel().toUpperCase()) {{
            setToggleGroup(g);
            setPrefWidth(120);
            if (d == selectedDifficulty) {
                setSelected(true);
                updateButtonStyle(this, true);
            }
            setOnAction(_ -> {
                AssetsManager.playSFX("button.wav", 1.0);
                selectedDifficulty = d;
                updateDifficultyDescription();
                g.getToggles().forEach(t -> updateButtonStyle((ToggleButton) t, t.isSelected()));
            });
        }};
    }

    private HBox createShipConfigCard(ShipType type, int initial) {
        Spinner<Integer> s = new Spinner<>(0, 10, initial) {{
            setPrefWidth(100);
            getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
            setStyle(SPINNER_STYLE);
            valueProperty().addListener((o, old, v) -> updateCapacity());
        }};
        shipSpinners.put(type, s);

        Text name = new Text(type.getName().toUpperCase());
        name.setFill(Color.WHITE);
        name.setFont(Theme.font(14, FontWeight.BOLD));

        Text sizeInfo = new Text(String.format("[%d %s]",
                type.getSize(),
                type.getSize() > 1 ? "CASES" : "CASE"));
        sizeInfo.setFill(Theme.CYAN);
        sizeInfo.setFont(Theme.font(10, FontWeight.NORMAL));
        sizeInfo.setOpacity(0.8);

        HBox infoBox = new HBox(10, name, sizeInfo);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        return new HBox(15, infoBox, spacer, s) {{
            setAlignment(Pos.CENTER_LEFT);
            setPadding(new Insets(10, 20, 10, 20));
            setStyle(Theme.CARD_STYLE);
        }};
    }

    private void updateModeDescription() {
        modeDesc.setText(isSalveMode.get() ? "MODE SALVE : Tirs multiples. [!] Pas de ravitaillement ni capacités." : "MODE CLASSIQUE : Tour par tour. [✓] Ravitaillement et capacités disponibles.");
    }

    private void updateDifficultyDescription() {
        // 🚨 Les ternaires imbriquées sont compliquées à lire
        difficultyDesc.setText(selectedDifficulty == Difficulty.EASY ? "RECRUE : L'IA tire au hasard." : selectedDifficulty == Difficulty.NORMAL ? "TACTICIEN : L'IA traque après impact." : "LÉGENDE : Analyse probabiliste avancée.");
        // 💡 Comme elles s'appuient sur des valeurs d'enum, une switch expression est souvent plus lisible
        // Ex.
        // String difficultyDescription = switch (selectedDifficulty) {
        //     case EASY -> "RECRUE : L'IA tire au hasard.";
        //     case NORMAL -> "TACTICIEN : L'IA traque après impact.";
        //     default -> "LÉGENDE : Analyse probabiliste avancée.";
        // };
        // difficultyDesc.setText(difficultyDescription);


    }

    private void updateCapacity() {
        int w = (int) sliderWidth.getValue(), h = (int) sliderHeight.getValue(), max = (int) ((w * h) * 0.30), used = shipSpinners.entrySet().stream().mapToInt(e -> e.getKey().getSize() * e.getValue().getValue()).sum();
        boolean inv = used > max || used == 0;
        capacityLabel.setText(used == 0 ? "⚠ ARSENAL VIDE" : (inv ? "☢ SURCHARGE (" + used + "/" + max + ")" : "✓ OPÉRATIONNEL (" + used + "/" + max + ")"));
        capacityLabel.setFill(inv ? Theme.RED_ALERTE : Theme.GREEN_SUCCESS);
        btnNext.setDisable(inv);
    }

    private void resetEvents() {
        eventSliders.forEach((k, v) -> v.setValue(k.equals("Rien ne se passe") ? 80 : k.equals("Brouillage") ? 20 : (k.contains("capacité") || k.equals("Salve boostée")) ? 10 : 5));
        eventToggles.values().forEach(cb -> cb.setSelected(true));
        apocRoundSpinner.getValueFactory().setValue(30);
        calculatePercentages();
    }

    private void updateButtonStyle(ToggleButton b, boolean a) {
        b.setStyle(a ? Theme.BTN_NEXT_ACTIVE : Theme.BTN_SECONDARY_NORMAL);
        b.setEffect(a ? Theme.GLOW_CYAN : null);
    }

    private VBox createTacticalPanel(String t) {
        return new VBox(25, new Text(t) {{
            setFont(Theme.font(24, FontWeight.BOLD));
            setFill(Theme.CYAN);
            setEffect(Theme.GLOW_CYAN);
        }}) {{
            setPadding(new Insets(30));
            setAlignment(Pos.TOP_CENTER);
            setStyle(Theme.GLASS_PANEL);
        }};
    }

    private Label createStyledLabel(String t) {
        return new Label(t) {{
            setTextFill(Theme.TEXT_MUTED);
            setFont(Theme.font(12, FontWeight.BOLD));
        }};
    }

    private Text createDescText() {
        return new Text() {{
            setFill(Theme.TEXT_MUTED);
            setFont(Theme.font(11, FontWeight.NORMAL));
            setWrappingWidth(450);
        }};
    }

    private Slider createNeonSlider(int val) {
        return new Slider(MIN_SIZE, 26, val) {{
            setMaxWidth(400);
            setStyle("-fx-control-inner-background: #06b6d4;");
        }};
    }

    private void styleResetButton(Button b) {
        b.setPrefWidth(240);
        b.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-font-weight: bold;");
    }
}