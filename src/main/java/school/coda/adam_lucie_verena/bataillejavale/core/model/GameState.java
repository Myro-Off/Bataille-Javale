package school.coda.adam_lucie_verena.bataillejavale.core.model;

import school.coda.adam_lucie_verena.bataillejavale.core.engine.GameController;

/**
 * Définit les différents états possibles du cycle de vie d'une partie.
 * <p>
 * Cette énumération sert de base à la machine à états pilotée par le
 * {@link GameController}.
 * </p>
 */
public enum GameState {

    // ------------------------------------------------------------------------------------------
    // ÉTATS DE JEU ACTIFS
    // ------------------------------------------------------------------------------------------

    /** Le joueur humain attend de sélectionner une cible sur le radar ennemi. */
    PLAYER_TURN,
    /** L'intelligence artificielle calcule sa cible et exécute son tir. */
    AI_TURN,

    // ------------------------------------------------------------------------------------------
    // ÉTATS DE FIN DE PARTIE
    // ------------------------------------------------------------------------------------------

    /** Condition de victoire : tous les navires adverses ont été coulés. */
    WON,
    /** Condition de défaite : la flotte du joueur a été entièrement détruite. */
    LOST
}