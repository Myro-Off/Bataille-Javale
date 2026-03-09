package school.coda.adam_lucie_verena.bataillejavale.core.model;

import school.coda.adam_lucie_verena.bataillejavale.core.ai.Difficulty;

/**
 * Stocke la configuration complète d'une session de jeu.
 * Cet objet est créé dans le menu principal et utilisé pour initialiser
 * le plateau et l'intelligence artificielle.
 * @param gridWidth       Largeur de la grille de jeu (ex: 10).
 * @param gridHeight      Hauteur de la grille de jeu (ex: 10).
 * @param difficulty      Niveau de l'IA (Débutant, Normal, Expert).
 * @param isMultiplayer   Vrai si la partie se joue en réseau, Faux pour le mode Solo.
 * @param supplyEnabled   Active ou désactive l'apparition des bonus/ravitaillements.
 * @param player1Name     Nom du premier joueur.
 * @param player2Name     Nom du second joueur (ou nom de l'IA).
 */
public record GameConfig(
        int gridWidth,
        int gridHeight,
        Difficulty difficulty,
        boolean isMultiplayer,
        boolean supplyEnabled,
        String player1Name,
        String player2Name
) {
    /**
     * Crée une configuration par défaut (Grille 10x10, Difficulté Normale, Solo).
     * Utile pour tester le jeu rapidement sans passer par le menu.
     */
    public static GameConfig createDefault() {
        return new GameConfig(
                10, 10,
                Difficulty.NORMALE,
                false,
                true,
                "Joueur 1",
                "Ordinateur"
        );
    }
}