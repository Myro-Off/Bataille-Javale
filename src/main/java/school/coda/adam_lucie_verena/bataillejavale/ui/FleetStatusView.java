package school.coda.adam_lucie_verena.bataillejavale.ui;

import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import school.coda.adam_lucie_verena.bataillejavale.core.model.*;

/**
 * Composant d'interface affichant l'état de santé en temps réel de la flotte ennemie.
 * <p>
 * Cette vue agit comme un tableau de bord tactique. Elle liste tous les navires
 * adverses, indique leur taille, et change leur apparence visuelle (barré/rouge)
 * lorsqu'ils sont confirmés comme coulés.
 * </p>
 */
public class FleetStatusView extends VBox {

    /** Référence au plateau ennemi pour surveiller l'état des navires. */
    private final Board enemyBoard;

    /**
     * Initialise le panneau de statut de la flotte.
     * @param enemyBoard Le plateau de l'adversaire à observer.
     */
    public FleetStatusView(Board enemyBoard) {
        this.enemyBoard = enemyBoard;

        // Configuration du layout
        setSpacing(10);
        setPadding(new Insets(20));

        // Style semi-transparent pour s'intégrer à l'ambiance "centre de commande"
        setStyle("-fx-background-color: rgba(30, 41, 59, 0.5); -fx-background-radius: 10;");

        update();
    }

    /**
     * Rafraîchit l'affichage de la liste des navires.
     * <p>
     * Cette méthode doit être appelée après chaque tir réussi pour mettre à jour
     * visuellement la progression du joueur. Elle parcourt la liste des navires
     * du modèle et applique les styles suivants :
     * </p>
     * <ul>
     * <li><b>En vie :</b> Texte gris clair, affichage de la taille.</li>
     * <li><b>Coulé :</b> Texte rouge corail, nom barré.</li>
     * </ul>
     */
    public void update() {
        // Nettoyage des anciens éléments avant reconstruction
        getChildren().clear();

        // Titre de la section avec la couleur accentuée du jeu
        Text title = new Text("CIBLES RESTANTES");
        title.setFill(Color.web("#00d2d3"));
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        getChildren().add(title);

        /*
         * Génération dynamique de la liste des navires basée sur le Board ennemi.
         */
        for (Ship ship : enemyBoard.getShips()) {
            Text name = new Text(ship.getType().getName() + " [" + ship.getType().getSize() + "]");

            if (ship.isSunk()) {
                name.setFill(Color.web("#ff4757"));
                name.setStrikethrough(true);
            } else {
                name.setFill(Color.LIGHTGRAY);
            }

            getChildren().add(name);
        }
    }
}