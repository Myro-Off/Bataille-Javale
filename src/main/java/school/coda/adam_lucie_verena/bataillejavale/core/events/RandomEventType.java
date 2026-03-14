package school.coda.adam_lucie_verena.bataillejavale.core.events;

public enum RandomEventType {
    RIEN("Rien ne se passe", "Le calme avant la tempête.", 80),
    BROUILLAGE("Brouillage radar", "Grille radar indisponible pendant 2 tours !", 20),
    METEORES("Pluie de météores", "Des météorites s'abattent sur les deux flottes !", 5),
    CAPACITES_BLOQUEES("Capacités bloquées", "Seuls les tirs standards sont possibles pendant 2 tours.", 10),
    RAVITAILLEMENT_GRATUIT("Ravitaillement gratuit", "Chaque joueur reçoit un ravitaillement immédiat !", 5),
    JOURNEE_ENSOLEILLEE("Juste une journée ensoleillée", "Le soleil brille, tout le monde est récompensé !", 0), // Poids géré à part
    SALVE_BOOSTEE("Salve boostée", "La prochaine salve utilisera le nombre de vaisseaux initial !", 10),
    APOCALYPSE("Apocalypse", "Tour 30 : La fin des temps a commencé.", 0);

    private final String title;
    private final String description;
    private final int defaultWeight;

    RandomEventType(String title, String description, int weight) {
        this.title = title;
        this.description = description;
        this.defaultWeight = weight;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getDefaultWeight() { return defaultWeight; }
}