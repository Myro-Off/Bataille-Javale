package school.coda.adam_lucie_verena.bataillejavale.core.model;

/**
 * Définit les différents types de ravitaillements (bonus) disponibles dans le jeu.
 * Chaque bonus possède un poids utilisé pour le calcul du "Drop Rate" et un nombre de munitions.
 */
public enum SupplyType {

    /** Torpille à fragmentation : Attaque de zone puissante. */
    FRAGMENTED_TORPEDO("Torpille à fragmentation", 50, 1),
    /** Tir fumigène : Cache une zone de la grille adverse. */
    SMOKE_SHOT("Tir fumigène", 50, 2),
    /** Raid aérien : Frappe aléatoire sur plusieurs cases. */
    AIR_RAID("Raid aérien", 40, 1),
    /** Bombe banane : Projectile rebondissant ou à effet spécial. */
    BANANA_BOMB("Bombe banane", 55, 1);

    private final String label;
    private final int weight;
    private final int ammoCount;

    /**
     * Constructeur d'un type de ravitaillement.
     * @param label Nom affiché dans l'interface.
     * @param weight Poids pour le calcul des probabilités (Drop Rate).
     * @param ammoCount Nombre de munitions accordées lors de l'obtention.
     */
    SupplyType(String label, int weight, int ammoCount) {
        this.label = label;
        this.weight = weight;
        this.ammoCount = ammoCount;
    }

    /** @return Le nom lisible du bonus. */
    public String getLabel() { return label; }
    /** @return Le poids pour le tirage aléatoire. */
    public int getWeight() { return weight; }
    /** @return Le nombre de tirs autorisés avec ce bonus. */
    public int getAmmoCount() { return ammoCount; }
}