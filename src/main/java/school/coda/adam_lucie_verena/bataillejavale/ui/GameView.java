package school.coda.adam_lucie_verena.bataillejavale.ui;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import school.coda.adam_lucie_verena.bataillejavale.core.model.Board;
import school.coda.adam_lucie_verena.bataillejavale.core.model.Coordinate;
import school.coda.adam_lucie_verena.bataillejavale.core.model.GameConfig;
import school.coda.adam_lucie_verena.bataillejavale.core.model.Ship;

/**
 * Composant graphique représentant une grille de bataille navale sous forme de {@link GridPane}.
 * <p>
 * Cette classe assure la liaison visuelle entre le modèle de données {@link Board} et
 * l'interface utilisateur. Elle gère l'affichage des cellules, des navires et des impacts
 * de tirs (réussis ou manqués).
 * </p>
 */
public class GameView extends GridPane {

    // ------------------------------------------------------------------------------------------
    // CONSTANTES ET ATTRIBUTS
    // ------------------------------------------------------------------------------------------

    /** Taille fixe d'une cellule de la grille en pixels. */
    private static final int CELL_SIZE = 40;
    /** Référence vers le plateau de données associé à cette vue. */
    private final Board board;
    /** Configuration de la partie pour récupérer les dimensions de la grille. */
    private final GameConfig config;
    /** Matrice de rectangles JavaFX permettant d'accéder directement à chaque cellule visuelle. */
    private final Rectangle[][] cells;

    // ------------------------------------------------------------------------------------------
    // CONSTRUCTEUR
    // ------------------------------------------------------------------------------------------

    /**
     * Initialise la vue de la grille et génère les composants graphiques initiaux.
     *
     * @param board  Le modèle {@link Board} contenant l'état des navires et des tirs.
     * @param config La configuration {@link GameConfig} définissant la taille du plateau.
     */
    public GameView(Board board, GameConfig config) {
        this.board = board;
        this.config = config;
        this.cells = new Rectangle[config.gridWidth()][config.gridHeight()];
        this.generateGrid();
    }

    // ------------------------------------------------------------------------------------------
    // MÉTHODES PRIVÉES : GÉNÉRATION GRAPHIQUE
    // ------------------------------------------------------------------------------------------

    /**
     * Construit dynamiquement la grille de rectangles dans le {@link GridPane}.
     * <p>
     * Chaque cellule est stockée dans la matrice {@code cells} pour permettre des
     * mises à jour ultérieures sans reconstruire toute l'interface.
     * </p>
     */
    private void generateGrid() {
        for (int x = 0; x < config.gridWidth(); x++) {
            for (int y = 0; y < config.gridHeight(); y++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);

                // Style par défaut : Bleu mer avec bordure blanche fine
                cell.setFill(Color.web("#2b6da3"));
                cell.setStroke(Color.WHITE);
                cell.setStrokeWidth(0.5);

                // Stockage pour accès rapide par coordonnées (x, y)
                cells[x][y] = cell;

                // Ajout au GridPane (Paramètres : Node, Colonne, Ligne)
                this.add(cell, x, y);
            }
        }
    }

    private void addShipTextures(Ship ship){
        String imageName = ship.getType().name().toLowerCase() +".svg";
        int positionPixel = getOccupiedCoordinates() * getCellSize();
    }

    // ------------------------------------------------------------------------------------------
    // MÉTHODES PUBLIQUES : MISE À JOUR DE L'AFFICHAGE
    // ------------------------------------------------------------------------------------------

    /**
     * Synchronise l'état visuel de la grille avec les données de tirs du plateau.
     * <p>
     * Les impacts réussis sont colorés en rouge, tandis que les tirs manqués
     * adoptent une teinte sombre avec une opacité réduite.
     * </p>
     */
    public void updateDisplay() {
        // 1. Coloration des tirs réussis (Hits)
        for (Coordinate hit : board.getHitShots()) {
            cells[hit.x()][hit.y()].setFill(Color.RED);
        }

        // 2. Coloration des tirs manqués (Misses)
        for (Coordinate miss : board.getMissedShots()) {
            cells[miss.x()][miss.y()].setFill(Color.web("#1a4a73"));
            cells[miss.x()][miss.y()].setOpacity(0.5);
        }
    }

    /**
     * Affiche l'emplacement des navires sur la grille.
     * <p>
     * Méthode utilisée pour la "Flotte Joueur" ou
     * pour révéler le plateau adverse à la fin de la partie.
     * </p>
     */
    public void revealShips() {
        board.getShips().forEach(this::addShipTextures);
    }


    // ------------------------------------------------------------------------------------------
    // GETTERS
    // ------------------------------------------------------------------------------------------

    /**
     * @return La taille d'une cellule en pixels.
     */
    public int getCellSize() {
        return CELL_SIZE;
    }
}