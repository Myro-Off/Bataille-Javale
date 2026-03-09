package school.coda.adam_lucie_verena.bataillejavale.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Gère la grille de jeu, les limites de la carte et la validation des placements.
 * Cette classe supporte des dimensions de grille personnalisées.
 */
public class Board {
    private final int width;
    private final int height;
    private final List<Ship> ships;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.ships = new ArrayList<>();
    }

    /**
     * Vérifie si un point (x, y) est bien à l'intérieur de la carte.
     */
    public boolean isWithinBounds(Coordinate coord) {
        return coord.x() >= 0 && coord.x() < width &&
                coord.y() >= 0 && coord.y() < height;
    }

    /**
     * Valide la possibilité de placer un navire selon deux critères :
     * 1. Le navire ne doit pas sortir des limites de la grille.
     * 2. Le navire ne doit pas chevaucher un navire déjà existant.
     * * @param newShip Le navire à tester.
     * @return {@code true} si le placement est conforme aux règles.
     */
    public boolean canPlaceShip(Ship newShip) {
        // On examine chaque case que le nouveau bateau veut occuper
        for (Coordinate coord : newShip.getOccupiedCoordinates()) {

            // 1. Est-ce qu'il sort de la grille ?
            if (!isWithinBounds(coord)) {
                return false;
            }

            // 2. Est-ce qu'il écrase un autre bateau ?
            for (Ship existingShip : ships) {
                if (existingShip.getOccupiedCoordinates().contains(coord)) {
                    return false;
                }
            }
        }
        // Si on arrive ici, c'est que tout est OK
        return true;
    }

    /**
     * Tente d'ajouter un navire au plateau après vérification des règles de collision.
     * @param ship Le navire à ajouter.
     * @return {@code true} si le navire a été ajouté avec succès.
     */
    public boolean addShip(Ship ship) {
        if (canPlaceShip(ship)) {
            ships.add(ship);
            return true; // Le placement a réussi
        }
        return false; // Le placement a échoué
    }

    public List<Ship> getShips() {
        return ships;
    }
}