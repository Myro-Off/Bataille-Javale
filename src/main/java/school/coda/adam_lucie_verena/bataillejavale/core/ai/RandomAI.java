package school.coda.adam_lucie_verena.bataillejavale.core.ai;

import school.coda.adam_lucie_verena.bataillejavale.core.model.Board;
import school.coda.adam_lucie_verena.bataillejavale.core.model.Coordinate;
import java.util.Random;

/**
 * IA de niveau débutant effectuant des tirs purement aléatoires.
 */
public class RandomAI implements AIStrategy {
    private final Random random = new Random();

    @Override
    public Coordinate chooseTarget(Board enemyBoard) {
        Coordinate target;
        do {
            target = new Coordinate(
                    random.nextInt(enemyBoard.getWidth()),
                    random.nextInt(enemyBoard.getHeight())
            );
        } while (enemyBoard.isAlreadyShot(target));
        return target;
    }

    @Override
    public void informResult(Coordinate lastTarget, boolean hit, boolean sunk) {
        // L'IA aléatoire ne mémorise rien.
    }
}