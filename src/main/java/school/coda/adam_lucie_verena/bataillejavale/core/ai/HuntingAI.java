package school.coda.adam_lucie_verena.bataillejavale.core.ai;

import school.coda.adam_lucie_verena.bataillejavale.core.model.Board;
import school.coda.adam_lucie_verena.bataillejavale.core.model.Coordinate;
import java.util.*;

/**
 * IA de niveau normal utilisant l'algorithme de traque (Hunt and Target).
 */
public class HuntingAI implements AIStrategy {
    private final Random random = new Random();
    private final Stack<Coordinate> targetStack = new Stack<>();

    @Override
    public Coordinate chooseTarget(Board enemyBoard) {
        while (!targetStack.isEmpty()) {
            Coordinate potential = targetStack.pop();
            if (!enemyBoard.isAlreadyShot(potential) && !enemyBoard.isNotWithinBounds(potential)) {
                return potential;
            }
        }

        Coordinate target;
        do {
            target = new Coordinate(random.nextInt(enemyBoard.getWidth()), random.nextInt(enemyBoard.getHeight()));
        } while (enemyBoard.isAlreadyShot(target));
        return target;
    }

    @Override
    public void informResult(Coordinate lastTarget, boolean hit, boolean sunk) {
        if (hit && !sunk) {
            int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            List<Coordinate> neighbors = new ArrayList<>();
            for (int[] d : dirs) {
                neighbors.add(new Coordinate(lastTarget.x() + d[0], lastTarget.y() + d[1]));
            }
            Collections.shuffle(neighbors);
            targetStack.addAll(neighbors);
        }
        if (sunk) {
            targetStack.clear();
        }
    }
}