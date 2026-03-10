package school.coda.adam_lucie_verena.bataillejavale.core.model;

public enum AchievementType {
}
/* =========================================================================================
 * 🎖️ TODO : LE PANTHÉON DES MARINS (ACHIEVEMENTS)
 * =========================================================================================
 * OBJECTIF : Créer un dictionnaire de défis pour récompenser le joueur.
 *
 * ÉTAPES :
 * 1. Créer un Enum 'AchievementType' avec les champs : Title, Description, TargetValue.
 * 2. Ajouter des constantes comme :
 * - FIRST_BLOOD ("Premier Sang", "Couler votre premier navire", 1)
 * - SNIPER      ("Tireur d'élite", "Atteindre 75% de précision", 75)
 * - VETERAN     ("Vétéran", "Jouer 10 parties au total", 10)
 *
 * PISTES :
 * - L'Enum est le meilleur endroit pour centraliser ces règles immuables.
 * - @see PlayerDAO : Il faudra sûrement une table 'player_achievements' en SQL !
 * ========================================================================================= */