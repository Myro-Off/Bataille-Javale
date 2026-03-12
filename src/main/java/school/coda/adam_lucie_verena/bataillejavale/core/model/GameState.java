package school.coda.adam_lucie_verena.bataillejavale.core.model;

/**
 * Définit les différents états possibles du cycle de vie d'une partie.
 */
public enum GameState {
    /** Le joueur humain doit effectuer son tir. */
    PLAYER_TURN,

    /** L'ordinateur calcule et effectue sa riposte. */
    AI_TURN,

    /** La partie est terminée (victoire ou défaite). */
    GAME_OVER
}