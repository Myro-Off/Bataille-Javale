package school.coda.adam_lucie_verena.bataillejavale.core.events;

import school.coda.adam_lucie_verena.bataillejavale.core.engine.BattleEngine;
import school.coda.adam_lucie_verena.bataillejavale.core.model.GameConfig;
import java.util.*;
import java.util.function.Consumer;

/**
 * Gestionnaire des événements aléatoires.
 * Utilise les poids et les états d'activation définis dans la configuration de la mission.
 */
public class RandomEventManager {
    private final BattleEngine engine;
    private final GameConfig config;
    private final List<RandomEvent> activeEvents = new ArrayList<>();
    private final Random random = new Random();

    private Consumer<RandomEventType> onEventTriggered;
    private Consumer<RandomEventType> onEventEnded;

    public RandomEventManager(BattleEngine engine, GameConfig config) {
        this.engine = engine;
        this.config = config;
    }

    public void setOnEventTriggered(Consumer<RandomEventType> handler) { this.onEventTriggered = handler; }
    public void setOnEventEnded(Consumer<RandomEventType> handler) { this.onEventEnded = handler; }

    /**
     * Met à jour la durée des événements et tente d'en déclencher un nouveau.
     */
    public void update() {
        activeEvents.removeIf(event -> {
            event.decreaseDuration();
            if (event.isExpired()) {
                event.remove(engine);
                if (onEventEnded != null) onEventEnded.accept(event.getType());
                return true;
            }
            return false;
        });

        // On ne déclenche rien si les événements sont désactivés globalement
        if (config.eventsEnabled()) {
            triggerNewEvent();
        }
    }

    private void triggerNewEvent() {
        int round = engine.getRoundNumber();
        RandomEventType selected;

        // --- RÈGLE APOCALYPSE ---
        if (round >= config.apocalypseRound() && config.isEventActive("Apocalypse")) {
            selected = RandomEventType.METEORES;
            if (round == config.apocalypseRound() && onEventTriggered != null) {
                onEventTriggered.accept(RandomEventType.APOCALYPSE);
            }
        } else {
            selected = calculateWeightedSelection();
        }

        if (selected == RandomEventType.RIEN) return;

        RandomEvent newEvent = switch (selected) {
            case BROUILLAGE -> new RadarJammingEvent(2);
            case METEORES -> new MeteorRainEvent();
            case CAPACITES_BLOQUEES -> new BlockedAbilitiesEvent(2);
            case RAVITAILLEMENT_GRATUIT -> new FreeSupplyEvent();
            case JOURNEE_ENSOLEILLEE -> new SunnyDayEvent();
            case SALVE_BOOSTEE -> new BoostedVolleyEvent();
            default -> null;
        };

        if (newEvent != null) {
            // Évite les doublons d'événements persistants
            if (activeEvents.stream().anyMatch(e -> e.getType() == newEvent.getType())) return;

            activeEvents.add(newEvent);
            newEvent.apply(engine);
            if (onEventTriggered != null) onEventTriggered.accept(selected);
        }
    }

    /**
     * Sélectionne un événement en respectant les poids et les cases cochées.
     * Garantit 1% fixe pour le Jour Ensoleillé.
     */
    private RandomEventType calculateWeightedSelection() {
        // 1. Priorité absolue au Jour Ensoleillé (1%)
        if (random.nextDouble() * 100.0 < 1.0) {
            return RandomEventType.JOURNEE_ENSOLEILLEE;
        }

        // 2. Construction de la table des poids actifs
        Map<RandomEventType, Integer> activeWeights = new HashMap<>();

        // Événements permanents (si cochés)
        addIfActive(activeWeights, RandomEventType.RIEN, "Rien ne se passe");
        addIfActive(activeWeights, RandomEventType.BROUILLAGE, "Brouillage");
        addIfActive(activeWeights, RandomEventType.METEORES, "Pluie de météores");

        // Événements liés aux capacités
        if (config.abilitiesEnabled() && !config.isSalveMode()) {
            addIfActive(activeWeights, RandomEventType.RAVITAILLEMENT_GRATUIT, "Ravitaillement gratuit");
            addIfActive(activeWeights, RandomEventType.CAPACITES_BLOQUEES, "Blocage capacité");
        }

        // Événements liés au mode Salve
        if (config.isSalveMode()) {
            addIfActive(activeWeights, RandomEventType.SALVE_BOOSTEE, "Salve boostée");
        }

        int totalWeight = activeWeights.values().stream().mapToInt(Integer::intValue).sum();

        // Sécurité anti-zéro (99% restant devient RIEN)
        if (totalWeight <= 0) return RandomEventType.RIEN;

        // 3. Tirage pondéré
        int draw = random.nextInt(totalWeight);
        int currentSum = 0;

        for (Map.Entry<RandomEventType, Integer> entry : activeWeights.entrySet()) {
            currentSum += entry.getValue();
            if (draw < currentSum) return entry.getKey();
        }

        return RandomEventType.RIEN;
    }

    /**
     * Utilitaire pour n'ajouter un poids que si la case est cochée dans l'UI.
     */
    private void addIfActive(Map<RandomEventType, Integer> map, RandomEventType type, String configName) {
        if (config.isEventActive(configName)) {
            map.put(type, config.getWeight(configName));
        }
    }

    public List<RandomEvent> getActiveEvents() { return activeEvents; }
}