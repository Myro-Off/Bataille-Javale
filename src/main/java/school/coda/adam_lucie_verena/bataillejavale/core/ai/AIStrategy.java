package school.coda.adam_lucie_verena.bataillejavale.core.ai;

import school.coda.adam_lucie_verena.bataillejavale.core.model.Board;
import school.coda.adam_lucie_verena.bataillejavale.core.model.Coordinate;

// 👍 Utilisation pertinente des interfaces et du pattern Stratégie
/**
 * Interface définissant le contrat de stratégie pour l'intelligence artificielle.
 * Elle permet d'implémenter différentes logiques de tir (aléatoire, tactique, etc.)
 * en fonction du niveau de difficulté choisi.
 * @see <a href="https://refactoring.guru/fr/design-patterns/strategy">Strategy Design Pattern</a>
 */
public interface AIStrategy {

    /**
     * Détermine la prochaine coordonnée à cibler sur le plateau adverse.
     * Cette méthode est le "cerveau" de l'IA qui sera appelé à chaque tour de l'ordinateur.
     * @param enemyBoard Le plateau de l'adversaire (joueur humain) pour analyser les coups précédents.
     * @return La {@link Coordinate} choisie par l'IA pour son prochain tir.
     */
    // 💡 Pourrait retourner un Optional<Coordinate> plutôt que null pour clarifier le contrat
    // Et forcer l'appelant de gérer explicitement quand ça arrive
    Coordinate chooseTarget(Board enemyBoard);

    /**
     * Permet d'informer l'IA du résultat de son dernier tir.
     * Indispensable pour les IA de niveau "Normale" ou "Expert" afin de mémoriser
     * si un navire a été touché ou coulé.
     * @param lastTarget La dernière coordonnée visée.
     * @param hit true si le tir a touché un navire.
     * @param sunk true si le tir a coulé le navire.
     */
    void informResult(Coordinate lastTarget, boolean hit, boolean sunk);
}