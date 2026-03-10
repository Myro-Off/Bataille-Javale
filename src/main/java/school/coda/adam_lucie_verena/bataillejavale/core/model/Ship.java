package school.coda.adam_lucie_verena.bataillejavale.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Modèle représentant un navire de guerre positionné sur la grille.
 * <p>
 * Un navire est défini par son type (déterminant sa taille), sa coordonnée d'origine
 * et son orientation. Il gère dynamiquement ses segments occupés et son état d'intégrité
 * (nombre d'impacts reçus).
 * </p>
 */
public class Ship {

    // ------------------------------------------------------------------------------------------
    // ATTRIBUTS
    // ------------------------------------------------------------------------------------------

    /** Le modèle technique du navire (Porte-avions, Croiseur, etc.). */
    private final ShipType type;
    /** Le point d'ancrage initial (la "proue") du navire sur la grille. */
    private final Coordinate startCoordinate;
    /** L'axe de déploiement (Horizontal ou Vertical). */
    private final Orientation orientation;
    /** Liste exhaustive des coordonnées occupées par le navire. */
    private final List<Coordinate> occupiedCoordinates;
    /** Liste des coordonnées ayant subi un impact de tir. */
    private final List<Coordinate> hits;

    // ------------------------------------------------------------------------------------------
    // CONSTRUCTEUR
    // ------------------------------------------------------------------------------------------

    /**
     * Construit un navire et calcule immédiatement son emprise spatiale sur la grille.
     *
     * @param type            Le type de navire définissant sa longueur.
     * @param startCoordinate La coordonnée de départ.
     * @param orientation     Le sens de déploiement.
     */
    public Ship(ShipType type, Coordinate startCoordinate, Orientation orientation) {
        this.type = type;
        this.startCoordinate = startCoordinate;
        this.orientation = orientation;
        this.occupiedCoordinates = new ArrayList<>();
        this.hits = new ArrayList<>();

        // Calcul automatique des segments lors de l'instanciation
        calculateOccupiedCoordinates();
    }

    // ------------------------------------------------------------------------------------------
    // LOGIQUE INTERNE
    // ------------------------------------------------------------------------------------------

    /**
     * Calcule et stocke l'ensemble des coordonnées occupées par le navire
     * en fonction de son point de départ, de sa taille et de son orientation.
     */
    private void calculateOccupiedCoordinates() {
        for (int i = 0; i < type.getSize(); i++) {
            int currentX = startCoordinate.x() + (orientation == Orientation.HORIZONTAL ? i : 0);
            int currentY = startCoordinate.y() + (orientation == Orientation.VERTICAL ? i : 0);
            occupiedCoordinates.add(new Coordinate(currentX, currentY));
        }
    }

    // ------------------------------------------------------------------------------------------
    // MÉTHODES DE JEU (LOGIQUE MÉTIER)
    // ------------------------------------------------------------------------------------------

    /**
     * Détermine si le navire occupe une coordonnée spécifique.
     *
     * @param coord La coordonnée à vérifier.
     * @return {@code true} si la case fait partie du navire.
     */
    public boolean isAt(Coordinate coord) {
        return occupiedCoordinates.contains(coord);
    }

    /**
     * Enregistre un impact sur le navire si la cible est valide.
     * <p>
     * Un impact n'est comptabilisé que si la coordonnée appartient au navire
     * et n'a pas déjà été touchée précédemment.
     * </p>
     *
     * @param coord La coordonnée visée par le tir.
     */
    public void takeHit(Coordinate coord) {
        if (isAt(coord) && !hits.contains(coord)) {
            hits.add(coord);
        }
    }

    /**
     * Vérifie l'état de destruction du navire.
     *
     * @return {@code true} si le nombre d'impacts est égal à la taille du navire.
     */
    public boolean isSunk() {
        return hits.size() >= type.getSize();
    }

    // ------------------------------------------------------------------------------------------
    // ACCESSEURS (GETTERS)
    // ------------------------------------------------------------------------------------------

    /** @return L'orientation (HORIZONTAL ou VERTICAL) pour l'affichage des textures */
    public Orientation getOrientation() {
        return orientation;
    }
    /** @return La liste des coordonnées constituant le corps du navire. */
    public List<Coordinate> getOccupiedCoordinates() {
        return occupiedCoordinates;
    }
    /** @return Le type (classe) du navire. */
    public ShipType getType() {
        return type;
    }
}