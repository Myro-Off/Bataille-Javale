package school.coda.adam_lucie_verena.bataillejavale.core.ai;

import school.coda.adam_lucie_verena.bataillejavale.core.model.Board;
import school.coda.adam_lucie_verena.bataillejavale.core.model.Coordinate;
import school.coda.adam_lucie_verena.bataillejavale.core.model.Ship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 💡 C'est bien d'expliquer et mentionner le nom de la technique utilisée (Heatmap)
// Je reste sur ma faim. J'aurais aimé avoir des URL avec @see vers les ressources utilisées.
/**
 * Intelligence artificielle de niveau Expert utilisant une carte de probabilités (Heatmap).
 * L'algorithme calcule pour chaque case le nombre de configurations de navires
 * statistiquement possibles afin de cibler la zone de plus haute densité.
 */
public class TacticalAI implements AIStrategy {

    /**
     * Calcule la meilleure cible en générant une matrice de probabilités.
     * @param enemyBoard Le plateau du joueur à analyser.
     * @return La coordonnée présentant la probabilité d'impact la plus élevée.
     */
    @Override
    public Coordinate chooseTarget(Board enemyBoard) {
        int width = enemyBoard.getWidth();
        int height = enemyBoard.getHeight();
        double[][] heatmap = new double[width][height];

        List<Integer> remainingLengths = enemyBoard.getShips().stream()
                .filter(s -> !s.isSunk())
                .map(s -> s.getType().getSize())
                .toList();

        for (int length : remainingLengths) {
            calculateShipProbability(enemyBoard, heatmap, length);
        }

        return findBestCoordinate(enemyBoard, heatmap);
    }

    /**
     * Parcourt le plateau pour simuler tous les placements horizontaux et verticaux
     * possibles pour une longueur de navire donnée.
     */
    private void calculateShipProbability(Board board, double[][] heatmap, int length) {
        for (int x = 0; x < board.getWidth(); x++) {
            for (int y = 0; y < board.getHeight(); y++) {
                if (canFit(board, x, y, length, true)) {
                    applyWeight(board, heatmap, x, y, length, true);
                }
                if (canFit(board, x, y, length, false)) {
                    applyWeight(board, heatmap, x, y, length, false);
                }
            }
        }
    }

    /**
     * Vérifie si un navire d'une certaine longueur peut théoriquement être placé
     * à partir d'un point sans traverser une case déjà identifiée comme "Manqué".
     */
    private boolean canFit(Board board, int x, int y, int length, boolean horizontal) {
        for (int i = 0; i < length; i++) {
            int curX = horizontal ? x + i : x;
            int curY = horizontal ? y : y + i;

            if (board.isNotWithinBounds(new Coordinate(curX, curY))) return false;

            Coordinate c = new Coordinate(curX, curY);
            if (board.getMissedShots().contains(c)) return false;

            boolean isSunkPart = board.getShips().stream()
                    .filter(Ship::isSunk)
                    .anyMatch(s -> s.isAt(c));
            if (isSunkPart) return false;
        }
        return true;
    }

    /**
     * Incrémente la valeur des cases dans la heatmap.
     * Si la configuration traverse un tir réussi (Hit) non coulé, le poids est
     * massivement augmenté pour forcer la traque.
     */
    private void applyWeight(Board board, double[][] heatmap, int x, int y, int length, boolean horizontal) {
        int hitCount = 0;
        for (int i = 0; i < length; i++) {
            Coordinate c = new Coordinate(horizontal ? x + i : x, horizontal ? y : y + i);
            if (board.getHitShots().contains(c)) hitCount++;
        }

        double weight = Math.pow(10, hitCount);

        for (int i = 0; i < length; i++) {
            int curX = horizontal ? x + i : x;
            int curY = horizontal ? y : y + i;
            heatmap[curX][curY] += weight;
        }
    }

    /**
     * Identifie la coordonnée ayant le score le plus élevé parmi les cases non encore visées.
     */
    private Coordinate findBestCoordinate(Board board, double[][] heatmap) {
        double maxProb = -1;
        List<Coordinate> candidates = new ArrayList<>();

        for (int x = 0; x < board.getWidth(); x++) {
            for (int y = 0; y < board.getHeight(); y++) {
                Coordinate c = new Coordinate(x, y);
                if (board.isAlreadyShot(c)) continue;

                if (heatmap[x][y] > maxProb) {
                    maxProb = heatmap[x][y];
                    candidates.clear();
                    candidates.add(c);
                } else if (heatmap[x][y] == maxProb) {
                    candidates.add(c);
                }
            }
        }

        Collections.shuffle(candidates);
        return candidates.isEmpty() ? new Coordinate(0, 0) : candidates.getFirst();
    }

    @Override
    public void informResult(Coordinate lastTarget, boolean hit, boolean sunk) {
    }
}