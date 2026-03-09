package school.coda.adam_lucie_verena.bataillejavale.core.model;

/**
 * Définit les directions possibles pour le placement d'un navire sur la grille.
 * Cette énumération est utilisée par la classe {@link Ship} pour calculer
 * l'ensemble des coordonnées occupées.
 */
public enum Orientation {
    /** Déploiement vers la droite.
     * Les cases sont ajoutées en augmentant la valeur de X.
     */
    HORIZONTAL,
    /** Déploiement vers le bas.
     * Les cases sont ajoutées en augmentant la valeur de Y.
     */
    VERTICAL
}