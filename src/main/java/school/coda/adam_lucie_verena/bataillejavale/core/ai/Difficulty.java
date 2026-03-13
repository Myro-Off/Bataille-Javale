package school.coda.adam_lucie_verena.bataillejavale.core.ai;

import java.util.function.Supplier;

/**
 * Définit les niveaux d'intelligence et de stratégie pour l'adversaire informatique.
 * Ce réglage influence l'algorithme de sélection des cibles utilisé par l'IA.
 */
public enum Difficulty {

    /**
     * Mode Débutant : L'IA sélectionne des coordonnées de manière totalement aléatoire.
     * Elle ne possède aucune mémoire des coups précédents.
     */
    EASY("Débutant", RandomAI::new),
    /**
     * Mode Normal : L'IA utilise l'algorithme "Hunt & Target".
     * Elle cherche au hasard, mais s'acharne sur les cases adjacentes dès qu'elle touche un navire.
     */
    NORMAL("Normale", HuntingAI::new),
    /**
     * Mode Expert : L'IA utilise des calculs de probabilités et analyse la taille des navires
     * restants pour optimiser ses chances de coup au but.
     */
    EXPERT("Expert", TacticalAI::new);

    private final String label;
    private final Supplier<AIStrategy> aiStrategyFactory;

    /**
     * Constructeur de l'énumération.
     *
     * @param label Le nom lisible du niveau de difficulté pour l'affichage UI.
     */
    Difficulty(String label, Supplier<AIStrategy> aiStrategyFactory) {
        this.label = label;
        this.aiStrategyFactory = aiStrategyFactory;
    }

    /**
     * Retourne le libellé associé à la difficulté.
     *
     * @return Le nom de la difficulté (ex: "Normale").
     */
    public String getLabel() {
        return label;
    }

    public AIStrategy createAiStrategy() {
        return aiStrategyFactory.get();
    }
}