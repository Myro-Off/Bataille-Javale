package school.coda.adam_lucie_verena.bataillejavale.core.model;

/**
 * Définit les types de navires officiels et leurs caractéristiques techniques.
 * Chaque navire possède une taille fixe exprimée en nombre de cases.
 */
public enum ShipType {
    /** Porte-avions (5 cases) */
    CARRIER(5, "Porte-avions"),
    /** Cuirassé (4 cases) */
    BATTLESHIP(4, "Cuirassé"),
    /** Destroyer (3 cases) */
    DESTROYER(3, "Destroyer"),
    /** Sous-marin (3 cases) */
    SUBMARINE(3, "Sous-marin"),
    /** Patrouilleur (2 cases) */
    PATROL(2, "Patrouilleur");

    private final int size;
    private final String name;

    ShipType(int size, String name) {
        this.size = size;
        this.name = name;
    }

    /** @return Le nombre de cases occupées par le navire */
    public int getSize() { return size; }
    /** @return Le nom du type de navire */
    public String getName() { return name; }
}