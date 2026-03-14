package school.coda.adam_lucie_verena.bataillejavale.gui;

import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Référentiel central du design de l'application.
 * Contient l'intégralité des styles et des outils graphiques.
 */
public class Theme {
    // --- COULEURS PRIMAIRES ---
    public static final String HEX_CYAN = "#00d2d3";
    public static final Color CYAN = Color.web(HEX_CYAN);
    public static final Color RED_ALERTE = Color.web("#ff4757");
    public static final Color GREEN_SUCCESS = Color.web("#2ed573");
    public static final Color TEXT_MUTED = Color.web("#94a3b8");
    public static final Color TEXT_DIMMED = Color.web("#64748b");
    public static final Color DIVIDER = Color.web("#334155");
    public static final Color BG_OVERLAY = Color.rgb(2, 6, 23, 0.9);

    // --- COULEURS DÉRIVÉES (GRILLE & UI) ---
    public static final Color GRID_LABEL = CYAN.deriveColor(0, 1, 1, 0.7);
    public static final Color GRID_LINE = CYAN.deriveColor(0, 1, 1, 0.2);
    public static final Color GRID_BG = CYAN.deriveColor(0, 1, 1, 0.1);
    public static final Color GRID_FRAME = CYAN.deriveColor(0, 1, 1, 0.8);
    public static final Color SHIP_INFO = CYAN.deriveColor(0, 1, 1, 0.6);

    // --- STYLES DE PANNEAUX (CSS) ---
    public static final String MAIN_GRADIENT = "-fx-background-color: radial-gradient(center 50% 50%, radius 80%, #1e293b, #020617);";
    public static final String GLASS_PANEL = "-fx-background-color: rgba(15, 23, 42, 0.8); -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 20;";
    public static final String CARD_STYLE = "-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.05); -fx-border-radius: 10;";
    public static final String PREVIEW_HOLDER = "-fx-background-color: rgba(0,0,0,0.2); -fx-background-radius: 15; -fx-border-color: rgba(0,210,211,0.3); -fx-border-radius: 15;";
    public static final String STATS_BOX = "-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 20;";

    // --- STYLES DE BOUTONS ---
    public static final String BTN_NEXT_ACTIVE = "-fx-background-color: " + HEX_CYAN + "; -fx-text-fill: #020617; -fx-background-radius: 8;";
    public static final String BTN_NEXT_DISABLED = "-fx-background-color: #334155; -fx-text-fill: #64748b; -fx-background-radius: 8;";
    public static final String BTN_SECONDARY_NORMAL = "-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-border-color: #475569; -fx-border-radius: 5;";
    public static final String BTN_SECONDARY_HOVER = "-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 5;";

    // --- EFFETS ---
    public static final DropShadow GLOW_CYAN = new DropShadow(15, CYAN);
    public static final DropShadow GLOW_RED = new DropShadow(15, RED_ALERTE);
    public static final DropShadow GLOW_LARGE_CYAN = new DropShadow(20, CYAN);
    public static final DropShadow GLOW_LARGE_RED = new DropShadow(20, RED_ALERTE);
    public static final DropShadow GLOW_SMALL = new DropShadow(10, RED_ALERTE);

    // --- CURSEURS ---
    public static final Cursor CURSOR_CLICK = Cursor.HAND;

    // --- LOGS ---
    public static final Color LOG_HIT = Color.web("#ff4757");
    public static final Color LOG_MISS = Color.web("#94a3b8");
    public static final Color LOG_SUNK = Color.web("#ff9f43");
    public static final Color LOG_SYSTEM = Color.web("#00d2d3");

    public static Font font(double size, FontWeight weight) {
        return Font.font("Verdana", weight, size);
    }

    public static Font mono(double size, FontWeight weight) {
        return Font.font("Monospaced", weight, size);
    }
}