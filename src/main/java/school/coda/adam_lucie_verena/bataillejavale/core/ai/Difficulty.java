package school.coda.adam_lucie_verena.bataillejavale.core.ai;

/**
 * Définit les niveaux d'intelligence et de stratégie pour l'adversaire informatique.
 * Ce réglage influence l'algorithme de sélection des cibles utilisé par l'IA.
 */
public enum Difficulty {

    /**
     * Mode Débutant : L'IA sélectionne des coordonnées de manière totalement aléatoire.
     * Elle ne possède aucune mémoire des coups précédents.
     */
    DEBUTANT("Débutant"),
    /**
     * Mode Normal : L'IA utilise l'algorithme "Hunt & Target".
     * Elle cherche au hasard, mais s'acharne sur les cases adjacentes dès qu'elle touche un navire.
     */
    NORMALE("Normale"),
    /**
     * Mode Expert : L'IA utilise des calculs de probabilités et analyse la taille des navires
     * restants pour optimiser ses chances de coup au but.
     */
    EXPERT("Expert");

    private final String label;

    /**
     * Constructeur de l'énumération.
     * @param label Le nom lisible du niveau de difficulté pour l'affichage UI.
     */
    Difficulty(String label) {
        this.label = label;
    }

    /**
     * Retourne le libellé associé à la difficulté.
     * @return Le nom de la difficulté (ex: "Normale").
     */
    public String getLabel() {
        return label;
    }
}