package school.coda.adam_lucie_verena.bataillejavale.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    // CONSTANTES & ATTRIBUTS
    // ------------------------------------------------------------------------------------------

    private final int width;
    private final int height;

    private final List<Ship> ships = new ArrayList<>();
    private final List<Coordinate> hitShots = new ArrayList<>();
    private final List<Coordinate> missedShots = new ArrayList<>();

    // ------------------------------------------------------------------------------------------
    // CONSTRUCTEUR
    // ------------------------------------------------------------------------------------------

    /**
     * Initialise un nouveau plateau avec des dimensions spécifiques.
     *
     * @param width  Largeur du plateau (colonnes).
     * @param height Hauteur du plateau (lignes).
     */
    public Board(int width, int height) {
        this.width = width;
        this.height = height;
    }

    // ------------------------------------------------------------------------------------------
    // LOGIQUE DE PLACEMENT
    // ------------------------------------------------------------------------------------------

    /**
     * Vérifie si une coordonnée se situe strictement à l'intérieur des limites de la grille.
     *
     * @param coord La coordonnée à tester.
     * @return {@code true} si la coordonnée est valide (dans la grille).
     */
    public boolean isNotWithinBounds(Coordinate coord) {
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
    public boolean canPlaceShip(Ship newShip) {
        for (Coordinate coord : newShip.getOccupiedCoordinates()) {

            // 1. Vérification des bords : Si une coordonnée est HORS limites -> Refus
            if (isNotWithinBounds(coord)) {
                return false;
            }

            // 2. Vérification des collisions : Si une coordonnée est déjà occupée -> Refus
            for (Ship existingShip : ships) {
                if (existingShip.getOccupiedCoordinates().contains(coord)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Ajoute un navire au plateau après validation.
     *
     * @param ship Le navire à poser.
     * @return {@code true} si le placement a réussi.
     */
    public boolean placeShip(Ship ship) {
        if (canPlaceShip(ship)) {
            return ships.add(ship);
        }
        return false;
    }

    /**
     * Tente de placer l'intégralité de la flotte configurée de manière aléatoire.
     * Utilise une stratégie de "Backtracking simplifié" : si un navire ne peut pas être placé,
     * on réinitialise tout le plateau et on recommence jusqu'à réussite ou épuisement des tentatives.
     * * @param shipCounts La carte contenant le nombre de navires à placer pour chaque type.
     */
    public void placeShipsRandomly(Map<ShipType, Integer> shipCounts) {
        Random random = new Random();
        int globalAttempts = 0;
        boolean success = false;

        // On définit des limites pour éviter une boucle infinie sur une grille trop petite
        final int MAX_GLOBAL_ATTEMPTS = 200;
        final int MAX_LOCAL_ATTEMPTS = 100;

        while (!success && globalAttempts < MAX_GLOBAL_ATTEMPTS) {
            this.ships.clear();
            success = true;

            // On parcourt chaque type de navire défini dans la configuration
            for (Map.Entry<ShipType, Integer> entry : shipCounts.entrySet()) {
                ShipType type = entry.getKey();
                int quantity = entry.getValue();

                // On tente de placer le nombre exact de navires demandés pour ce type
                for (int i = 0; i < quantity; i++) {
                    boolean placed = false;
                    int localAttempts = 0;

                    while (!placed && localAttempts < MAX_LOCAL_ATTEMPTS) {
                        int x = random.nextInt(this.width);
                        int y = random.nextInt(this.height);
                        Orientation orientation = random.nextBoolean() ?
                                Orientation.HORIZONTAL : Orientation.VERTICAL;

                        Ship testShip = new Ship(type, new Coordinate(x, y), orientation);

                        // placeShip(testShip) vérifie les collisions et les limites
                        if (placeShip(testShip)) {
                            placed = true;
                        }
                        localAttempts++;
                    }

                    // Si après 100 tentatives locales on n'a pas pu poser CE navire
                    if (!placed) {
                        success = false;
                        break; // On casse la boucle de la flotte pour recommencer à zéro (globalAttempt)
                    }
                }
                if (!success) break;
            }
            globalAttempts++;
        }

        if (!success) {
            System.err.println("[ERREUR TACTIQUE] Impossible de déployer la flotte. Grille trop saturée.");
        }
    }

    // ------------------------------------------------------------------------------------------
    // LOGIQUE DE COMBAT
    // ------------------------------------------------------------------------------------------

    /**
     * Enregistre un tir sur le plateau et identifie s'il y a impact.
     *
     * @param coord La cible du tir.
     * @return {@code true} si un navire a été touché.
     */
    public boolean receiveFire(Coordinate coord) {
        if (isAlreadyShot(coord)) {
            return false;
        }

        shotsHistory.add(coord); // On enregistre le tir dans l'historique global

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
     * Vérifie si une coordonnée a déjà été ciblée.
     */
    public boolean isAlreadyShot(Coordinate coord) {
        return shotsHistory.contains(coord);
    }

    /**
     * Récupère la toute dernière coordonnée visée.
     */
    public Coordinate getLastShotCoordinate() {
        if (shotsHistory.isEmpty()) return null;
        return shotsHistory.getLast();
    }

    /**
     * Détermine si tous les navires présents ont été coulés.
     *
     * @return {@code true} si la défaite est confirmée.
     */
    public boolean allShipsSunk() {
        if (ships.isEmpty()) return false;
        return ships.stream().allMatch(Ship::isSunk);
    }

    // ------------------------------------------------------------------------------------------
    // GETTERS
    // ------------------------------------------------------------------------------------------

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public List<Ship> getShips() { return ships; }
    public List<Coordinate> getHitShots() { return hitShots; }
    public List<Coordinate> getMissedShots() { return missedShots; }
    private final List<Coordinate> shotsHistory = new ArrayList<>();
}