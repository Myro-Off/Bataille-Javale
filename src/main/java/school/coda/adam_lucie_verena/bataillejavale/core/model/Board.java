package school.coda.adam_lucie_verena.bataillejavale.core.model;

import java.util.*;

/**
 * Gère l'état logique de la grille de jeu.
 */
public class Board {

    private final int width;
    private final int height;
    private boolean fogActive = false;
    private final List<Ship> ships = new ArrayList<>();
    private final List<Coordinate> hitShots = new ArrayList<>();
    private final List<Coordinate> missedShots = new ArrayList<>();
    private final List<Coordinate> shotsHistory = new ArrayList<>();
    private final Set<Coordinate> meteorImpacts = new HashSet<>();

    /**
     * @param width Largeur de la grille.
     * @param height Hauteur de la grille.
     */
    public Board(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean isNotWithinBounds(Coordinate coord) {
        return coord.x() < 0 || coord.x() >= width ||
                coord.y() < 0 || coord.y() >= height;
    }

    public boolean canPlaceShip(Ship newShip) {
        for (Coordinate coord : newShip.getOccupiedCoordinates()) {
            if (isNotWithinBounds(coord)) return false;
            for (Ship existingShip : ships) {
                if (existingShip.getOccupiedCoordinates().contains(coord)) return false;
            }
        }
        return true;
    }

    public boolean placeShip(Ship ship) {
        if (canPlaceShip(ship)) return ships.add(ship);
        return false;
    }

    public void placeShipsRandomly(Map<ShipType, Integer> shipCounts) {
        Random random = new Random();
        int globalAttempts = 0;
        boolean success = false;
        while (!success && globalAttempts < 200) {
            this.ships.clear();
            success = true;
            for (Map.Entry<ShipType, Integer> entry : shipCounts.entrySet()) {
                for (int i = 0; i < entry.getValue(); i++) {
                    boolean placed = false;
                    int local = 0;
                    while (!placed && local < 100) {
                        Coordinate c = new Coordinate(random.nextInt(width), random.nextInt(height));
                        Orientation o = random.nextBoolean() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
                        Ship s = new Ship(entry.getKey(), c, o);
                        if (placeShip(s)) placed = true;
                        local++;
                    }
                    if (!placed) { success = false; break; }
                }
                if (!success) break;
            }
            globalAttempts++;
        }
    }

    /**
     * Enregistre un tir. Si isSilent est vrai (météore), la case n'est pas ajoutée à l'historique,
     * la rendant ciblable ultérieurement par les joueurs.
     * @param coord Coordonnée visée.
     * @param isSilent Définit si l'impact est environnemental.
     * @return true si un navire est touché.
     */
    public boolean receiveFire(Coordinate coord, boolean isSilent) {
        if (!isSilent && isAlreadyShot(coord)) return false;

        if (!isSilent) {
            shotsHistory.add(coord);
        }

        for (Ship ship : ships) {
            if (ship.isAt(coord)) {
                ship.takeHit(coord);
                if (!hitShots.contains(coord)) hitShots.add(coord);
                return true;
            }
        }

        if (!missedShots.contains(coord)) missedShots.add(coord);
        return false;
    }

    public boolean receiveFire(Coordinate coord) {
        return receiveFire(coord, false);
    }

    /**
     * Vérifie si une case a déjà été ciblée officiellement.
     * @param coord Coordonnée à tester.
     * @return false si le brouillard est actif ou si la case n'est pas dans l'historique joueur.
     */
    public boolean isAlreadyShot(Coordinate coord) {
        if (fogActive) return false;
        return shotsHistory.contains(coord);
    }

    public boolean allShipsSunk() {
        return !ships.isEmpty() && ships.stream().allMatch(Ship::isSunk);
    }

    public void markAsMeteorImpact(Coordinate coord) {
        meteorImpacts.add(coord);
    }

    public boolean isMeteorImpact(Coordinate coord) {
        return meteorImpacts.contains(coord);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public List<Ship> getShips() { return ships; }
    public List<Coordinate> getHitShots() { return hitShots; }
    public List<Coordinate> getMissedShots() { return missedShots; }
    public List<Coordinate> getShotsHistory() { return shotsHistory; }
    public void setFogActive(boolean active) { this.fogActive = active; }
    public boolean isFogActive() { return fogActive; }
}