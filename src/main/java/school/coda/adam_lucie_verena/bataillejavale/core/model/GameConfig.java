package school.coda.adam_lucie_verena.bataillejavale.core.model;

import school.coda.adam_lucie_verena.bataillejavale.core.ai.Difficulty;
import java.util.Map;

/**
 * Conteneur de données pour la configuration d'une partie.
 */
public record GameConfig(
        int gridWidth,
        int gridHeight,
        Difficulty difficulty,
        boolean isSinglePlayer,
        boolean isMuted,
        String player1Name,
        String player2Name,
        Map<ShipType, Integer> shipCounts
) {
    /** Création d'une configuration par défaut pour le premier lancement. */
    public static GameConfig createDefault() {
        return new GameConfig(10, 10, Difficulty.NORMALE, true, false, "Joueur 1", "IA",
                Map.of(ShipType.CARRIER, 1, ShipType.BATTLESHIP, 1, ShipType.DESTROYER, 1, ShipType.SUBMARINE, 1, ShipType.PATROL, 1));
    }
}