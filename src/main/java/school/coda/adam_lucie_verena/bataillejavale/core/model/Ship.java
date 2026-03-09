package school.coda.adam_lucie_verena.bataillejavale.core.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Représente un navire concret positionné sur la grille de jeu.
 * Cette classe gère le calcul de sa zone d'occupation et son état de santé.
 */
public class Ship {
    private final ShipType type;
    private final Coordinate startCoordinate;
    private final Orientation orientation;
    private final Set<Coordinate> occupiedCoordinates;
    private int hitsReceived;

    /**
     * Crée un navire et calcule instantanément toutes les cases qu'il occupe.
     * * @param type Le modèle du navire (définit sa taille).
     * @param startCoordinate La position de la première case du navire.
     * @param orientation Le sens de déploiement (Horizontal ou Vertical).
     */
    public Ship(ShipType type, Coordinate startCoordinate, Orientation orientation) {
        this.type = type;
        this.startCoordinate = startCoordinate;
        this.orientation = orientation;
        this.occupiedCoordinates = new HashSet<>();
        this.hitsReceived = 0;

        calculateOccupiedCoordinates();
    }

    /**
     * Remplit l'ensemble des coordonnées occupées en fonction de la taille du navire.
     */
    private void calculateOccupiedCoordinates() {
        for (int i = 0; i < type.getSize(); i++) {
            int currentX = startCoordinate.x() + (orientation == Orientation.HORIZONTAL ? i : 0);
            int currentY = startCoordinate.y() + (orientation == Orientation.VERTICAL ? i : 0);
            occupiedCoordinates.add(new Coordinate(currentX, currentY));
        }
    }

    /**
     * Détermine si un tir ennemi à une position donnée impacte ce navire.
     * @param target La coordonnée ciblée par l'attaquant.
     * @return true si le navire est touché sur l'une de ses cases.
     */
    public boolean isHit(Coordinate target) {
        if (occupiedCoordinates.contains(target)) {
            hitsReceived++;
            return true;
        }
        return false;
    }

    /**
     * Vérifie si le navire a été totalement détruit.
     * @return true si le nombre de touches est égal à la taille du navire.
     */
    public boolean isSunk() {
        return hitsReceived >= type.getSize();
    }

    // --- ACCESSEURS (GETTERS) ---

    /** @return Le type de navire (ex: Porte-avions, Destroyer). */
    public ShipType getType() { return type; }

    /** @return La coordonnée de départ du navire (utile pour l'affichage graphique). */
    public Coordinate getStartCoordinate() { return startCoordinate; }

    /** @return L'orientation choisie lors du placement. */
    public Orientation getOrientation() { return orientation; }

    /** @return L'ensemble des cases (x,y) actuellement couvertes par le navire. */
    public Set<Coordinate> getOccupiedCoordinates() { return occupiedCoordinates; }

    /** @return Le nombre total d'impacts reçus par ce navire. */
    public int getHitsReceived() { return hitsReceived; }
}