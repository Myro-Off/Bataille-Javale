package school.coda.adam_lucie_verena.bataillejavale.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Modèle représentant le plateau de jeu (la grille).
 * <p>
 * Cette classe gère l'état spatial de la partie : placement des navires,
 * détection des collisions, historique des tirs et vérification de l'état
 * global de la flotte (victoire/défaite).
 * </p>
 */
public class Board {

    // ------------------------------------------------------------------------------------------
    // ATTRIBUTS
    // ------------------------------------------------------------------------------------------

    /** Largeur de la grille (nombre de colonnes). */
    private final int width;
    /** Hauteur de la grille (nombre de lignes). */
    private final int height;
    /** Liste des navires actuellement positionnés sur le plateau. */
    private final List<Ship> ships;
    /** Historique des coordonnées où un tir a touché un navire. */
    private final List<Coordinate> hitShots;
    /** Historique des coordonnées où un tir a fini dans l'eau. */
    private final List<Coordinate> missedShots;

    // ------------------------------------------------------------------------------------------
    // CONSTRUCTEUR
    // ------------------------------------------------------------------------------------------

    /**
     * Initialise un nouveau plateau avec des dimensions spécifiques.
     *
     * @param width  Largeur du plateau.
     * @param height Hauteur du plateau.
     */
    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.ships = new ArrayList<>();
        this.hitShots = new ArrayList<>();
        this.missedShots = new ArrayList<>();
    }

    // ------------------------------------------------------------------------------------------
    // LOGIQUE DE PLACEMENT
    // ------------------------------------------------------------------------------------------

    /**
     * Vérifie si une coordonnée se situe à l'intérieur des limites de la grille.
     *
     * @param coord La coordonnée à tester.
     * @return {@code true} si la coordonnée est valide.
     */
    public boolean isWithinBounds(Coordinate coord) {
        return coord.x() < 0 || coord.x() >= width ||
                coord.y() < 0 || coord.y() >= height;
    }

    /**
     * Valide si un navire peut être placé sans sortir de la grille ni entrer
     * en collision avec un navire existant.
     *
     * @param newShip Le navire à tester.
     * @return {@code true} si le placement est autorisé.
     */
    private boolean canPlaceShip(Ship newShip) {
        for (Coordinate coord : newShip.getOccupiedCoordinates()) {
            // 1. Vérification des bords
            if (isWithinBounds(coord)) {
                return false;
            }

            // 2. Vérification des collisions
            for (Ship existingShip : ships) {
                if (existingShip.getOccupiedCoordinates().contains(coord)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Ajoute un navire au plateau après validation de sa position.
     *
     * @param ship Le navire à ajouter.
     * @return {@code true} si l'ajout a réussi.
     */
    private boolean addShip(Ship ship) {
        if (canPlaceShip(ship)) {
            ships.add(ship);
            return true;
        }
        return false;
    }

    /**
     * Remplit le plateau en plaçant aléatoirement une flotte standard.
     * <p>
     * Utilise une boucle de sécurité pour garantir que chaque type de navire
     * finit par trouver une place libre.
     * </p>
     */
    public void placeShipsRandomly() {
        Random random = new Random();

        for (ShipType type : ShipType.values()) {
            boolean placed = false;
            while (!placed) {
                int x = random.nextInt(width);
                int y = random.nextInt(height);
                Orientation orientation = random.nextBoolean() ?
                        Orientation.HORIZONTAL : Orientation.VERTICAL;

                Ship testShip = new Ship(type, new Coordinate(x, y), orientation);

                if (canPlaceShip(testShip)) {
                    this.addShip(testShip);
                    placed = true;
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------
    // LOGIQUE DE COMBAT
    // ------------------------------------------------------------------------------------------

    /**
     * Traite l'impact d'un tir sur le plateau.
     * <p>
     * Enregistre la coordonnée dans l'historique approprié et met à jour
     * l'état de santé du navire touché, le cas échéant.
     * </p>
     *
     * @param coord La cible du tir.
     * @return {@code true} si un navire a été touché.
     */
    public boolean receiveFire(Coordinate coord) {
        // Empêcher de gaspiller un tir sur une case déjà visée
        if (hitShots.contains(coord) || missedShots.contains(coord)) {
            return false;
        }

        for (Ship ship : ships) {
            if (ship.isAt(coord)) {
                ship.takeHit(coord);
                hitShots.add(coord);
                return true;
            }
        }

        missedShots.add(coord);
        return false;
    }

    /**
     * Vérifie si la flotte entière a été neutralisée.
     *
     * @return {@code true} si tous les navires sont coulés.
     */
    public boolean allShipsSunk() {
        return !ships.isEmpty() && ships.stream().allMatch(Ship::isSunk);
    }

    // ------------------------------------------------------------------------------------------
    // ACCESSEURS (GETTERS)
    // ------------------------------------------------------------------------------------------

    /** @return Largeur de la grille. */
    public int getWidth() { return width; }
    /** @return Hauteur de la grille. */
    public int getHeight() { return height; }
    /** @return Liste des navires sur le plateau. */
    public List<Ship> getShips() { return ships; }
    /** @return Liste des tirs ayant réussi. */
    public List<Coordinate> getHitShots() { return hitShots; }
    /** @return Liste des tirs ayant échoué. */
    public List<Coordinate> getMissedShots() { return missedShots; }
}