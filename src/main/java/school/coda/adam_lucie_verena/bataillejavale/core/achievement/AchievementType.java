package school.coda.adam_lucie_verena.bataillejavale.core.achievement;

/**
 * Liste exhaustive des succès déverrouillables et leurs objectifs numériques.
 */
public enum AchievementType {

    // --- SUCCÈS DE BASE ---
    FIRST_WIN("PREMIER SANG", "Remportez votre première victoire navale.", 1),
    FAST_WIN("BLITZKRIEG", "Victoire éclair en moins de 36 tours.", 1),

    // --- SUCCÈS DE LONGÉVITÉ ---
    VETERAN_10("VÉTÉRAN", "Disputez un total de 10 batailles.", 10),
    VETERAN_50("AMIRAL", "Disputez un total de 50 batailles.", 50),
    VETERAN_100("VIEUX LOUP DE MER", "Disputez un total de 100 batailles.", 100),

    // --- SUCCÈS DE SÉRIE DE VICTOIRES (STREAK) ---
    STREAK_3("TRIPLE MENACE", "Enchaînez 3 victoires consécutives.", 3),
    STREAK_5("INARRÊTABLE", "Enchaînez 5 victoires consécutives.", 5),

    // --- SUCCÈS DE PRÉCISION (GRADES) ---
    GRADE_S("TIREUR D'ÉLITE", "Atteignez un grade de précision S (80%+).", 1),
    GRADE_A("OFFICIER DE TIR", "Atteignez un grade de précision A (60%+).", 1),
    GRADE_B("ARTILLEUR", "Atteignez un grade de précision B (40%+).", 1),
    GRADE_C("APPRENTI", "Atteignez un grade de précision C (20%+).", 1),

    // --- SUCCÈS VICTOIRE CONTRE IA ---
    WIN_EASY("RECRUE", "Battez l'IA en mode débutant.", 1),
    WIN_NORMAL("TACTICIEN", "Battez l'IA en mode normal.", 1),
    WIN_EXPERT("LÉGENDE DES MERS", "Battez l'IA en mode expert.", 1),

    // --- SUCCÈS SÉRIE DE TOUCHÉS ---
    SHARP_SHOOTER("FINE GÂCHETTE", "Réussissez 5 tirs consécutifs sur l'ennemi.", 5),
    BEGINNER_SHOOTER("BON DÉBUT", "Réussissez 2 tirs consécutifs sur l'ennemi.", 2);

    private final String name;
    private final String description;
    private final int targetValue;

    AchievementType(String name, String description, int targetValue) {
        this.name = name;
        this.description = description;
        this.targetValue = targetValue;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getTargetValue() { return targetValue; }
}