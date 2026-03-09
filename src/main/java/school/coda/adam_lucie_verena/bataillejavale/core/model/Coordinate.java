package school.coda.adam_lucie_verena.bataillejavale.core.model;

/**
 * Représente un point immuable dans un espace à deux dimensions (x, y).
 * Ce point sert de référence pour le placement des navires et la visée des tirs.
 * @param x La position sur l'axe horizontal (colonne).
 * @param y La position sur l'axe vertical (ligne).
 */
public record Coordinate(int x, int y) {
}