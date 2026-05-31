package school.coda.adam_lucie_verena.bataillejavale.core.model;

import school.coda.adam_lucie_verena.bataillejavale.core.ai.Difficulty;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Registre de configuration de la mission.
 * Contient l'intégralité des paramètres structurels, doctrinaux et environnementaux.
 */
public record GameConfig(
        int gridWidth,
        int gridHeight,
        Difficulty difficulty,
        boolean isSalveMode,
        boolean eventsEnabled,
        boolean abilitiesEnabled,
        boolean isCustomFleet,
        Map<String, Integer> eventWeights,
        Map<String, Boolean> eventToggles,
        int apocalypseRound,
        String player1Name,
        String player2Name,
        Map<ShipType, Integer> shipCounts
) {
    /**
     * @return Une configuration standard (10x10, IA Normale, Événements actifs).
     */
    public static GameConfig createDefault() {
        Map<ShipType, Integer> ships = new EnumMap<>(ShipType.class);
        for (ShipType s : ShipType.values()) ships.put(s, 1);

        Map<String, Integer> weights = new HashMap<>();
        Map<String, Boolean> toggles = new HashMap<>();
        String[] events = {"Rien ne se passe", "Brouillage", "Pluie de météores", "Ravitaillement",
                "Ravitaillement gratuit", "Blocage capacité", "Salve boostée", "Apocalypse"};

        for (String e : events) {
            // 🚨 Pas très facile à comprendre
            weights.put(e, e.equals("Rien ne se passe") ? 80 : e.equals("Brouillage") ? 20 : 5);
            // 💡 Pourrait être plus lisible avec un switch ?. Ex.
            // switch (e) {
            //     case "Rien ne se passe" -> weights.put(e, 80);
            //     case "Brouillage" -> weights.put(e, 20);
            //     default -> weights.put(e, 5);
            // }
            toggles.put(e, true);
        }

        return new GameConfig(10, 10, Difficulty.NORMAL, false, true, true,
                false, weights, toggles, 30, "Amiral", "IA", ships);
    }

    /** @return Le poids configuré pour un événement ou 0 par défaut. */
    public int getWeight(String name) { return eventWeights.getOrDefault(name, 0); }

    /** @return Vrai si l'événement est activé manuellement dans les options. */
    public boolean isEventActive(String name) { return eventToggles.getOrDefault(name, true); }
}