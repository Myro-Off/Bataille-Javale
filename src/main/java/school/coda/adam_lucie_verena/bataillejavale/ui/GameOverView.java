package school.coda.adam_lucie_verena.bataillejavale.ui;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Superposition (Overlay) affichée lors de la fin d'une partie.
 * <p>
 * Cette vue assombrit l'écran de jeu et présente au joueur son résultat
 * (Victoire ou Défaite) ainsi que ses statistiques de précision de tir.
 * </p>
 */
public class GameOverView extends StackPane {

    // ------------------------------------------------------------------------------------------
    // CONSTRUCTEUR
    // ------------------------------------------------------------------------------------------

    /**
     * Construit l'écran de fin de partie avec les statistiques de performance.
     *
     * @param isVictory {@code true} si le joueur a gagné la partie.
     * @param shots     Nombre total de tirs effectués par le joueur.
     * @param hits      Nombre de tirs ayant touché un navire ennemi.
     * @param onRestart Action de rappel pour réinitialiser et relancer une partie.
     */
    public GameOverView(boolean isVictory, int shots, int hits, Runnable onRestart) {
        // Configuration de la taille pour couvrir l'intégralité de la scène
        setPrefSize(1000, 600);

        // 1. GÉNÉRATION DU FOND
        // Un rectangle noir semi-transparent pour détacher le menu du jeu en arrière-plan
        Rectangle background = new Rectangle(1000, 600, Color.rgb(0, 0, 0, 0.85));

        // 2. CRÉATION DU CONTENU
        VBox content = createContent(isVictory, shots, hits, onRestart);

        // 3. EMPILEMENT
        getChildren().addAll(background, content);
    }

    // ------------------------------------------------------------------------------------------
    // MÉTHODES PRIVÉES : CONSTRUCTION DE L'INTERFACE
    // ------------------------------------------------------------------------------------------

    /**
     * Assemble les éléments textuels et les boutons de navigation.
     *
     * @param isVictory Résultat de la partie.
     * @param shots     Total des tirs.
     * @param hits      Tirs réussis.
     * @param onRestart Action de redémarrage.
     * @return Un conteneur {@link VBox} centré et stylisé.
     */
    private VBox createContent(boolean isVictory, int shots, int hits, Runnable onRestart) {
        VBox container = new VBox(25);
        container.setAlignment(Pos.CENTER);

        // Titre principal (Or pour la victoire, Rouge pour la défaite)
        Text title = new Text(isVictory ? "VICTOIRE NAVALE !" : "FLOTTE COULÉE...");
        title.setFont(Font.font("Verdana", 60));
        title.setFill(isVictory ? Color.GOLD : Color.RED);

        // Calcul et affichage de la précision
        double accuracy = shots > 0 ? (double) hits / shots * 100 : 0;
        Text stats = new Text(String.format("Précision : %.1f%% (%d touches / %d tirs)",
                accuracy, hits, shots));
        stats.setFill(Color.WHITE);
        stats.setFont(Font.font(20));

        // Boutons d'action
        MenuButton btnRetry = new MenuButton("REJOUER", onRestart);
        MenuButton btnMenu  = new MenuButton("MENU PRINCIPAL", () ->
                FXGL.getGameController().gotoMainMenu()
        );

        container.getChildren().addAll(title, stats, btnRetry, btnMenu);
        return container;
    }
}