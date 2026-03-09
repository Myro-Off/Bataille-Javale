package school.coda.adam_lucie_verena.bataillejavale.ui;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Vue du menu principal de l'application.
 * <p>
 * Cette classe utilise un {@link StackPane} pour superposer un fond opaque (dégradé linéaire)
 * et un conteneur vertical {@link VBox} regroupant les boutons de navigation.
 * L'opacité totale garantit que les éléments de jeu en arrière-plan ne sont pas visibles.
 * </p>
 */
public class MainMenuView extends StackPane {

    // ------------------------------------------------------------------------------------------
    // CONSTRUCTEUR
    // ------------------------------------------------------------------------------------------

    /**
     * Initialise le menu principal avec un fond stylisé et les boutons de commande.
     * @param onPlay Action à exécuter lors du clic sur le bouton "JOUER SOLO".
     */
    public MainMenuView(Runnable onPlay) {
        // Configuration de la taille pour recouvrir la fenêtre DisplayGame
        setPrefSize(1000, 600);

        // 1. GÉNÉRATION DU FOND OPAQUE
        Rectangle background = createBackground();

        // 2. CRÉATION DU CONTENU (Titre + Boutons)
        VBox menuContent = createMenuContent(onPlay);

        // 3. EMPILEMENT DES COUCHES
        getChildren().addAll(background, menuContent);
    }

    // ------------------------------------------------------------------------------------------
    // MÉTHODES PRIVÉES : CONSTRUCTION DES COMPOSANTS
    // ------------------------------------------------------------------------------------------

    /**
     * Crée le rectangle de fond avec un dégradé de couleurs "Deep Space".
     * @return Un {@link Rectangle} opaque de 1000x600.
     */
    private Rectangle createBackground() {
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#050514")), // Bleu nuit profond
                new Stop(1, Color.web("#0a0a28"))  // Bleu marine sombre
        );

        Rectangle bg = new Rectangle(1000, 600, gradient);
        bg.setOpacity(1.0);
        return bg;
    }

    /**
     * Assemble le titre du jeu et la liste des boutons d'action.
     * @param onPlay Action de démarrage de partie.
     * @return Un conteneur {@link VBox} centré.
     */
    private VBox createMenuContent(Runnable onPlay) {
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);

        // Configuration du titre principal
        Text title = new Text("BATAILLE JAVALE");
        title.setFont(Font.font("Verdana", 60));
        title.setFill(Color.CYAN);
        title.setStroke(Color.WHITE);
        title.setStrokeWidth(0.5);

        // Instanciation des boutons
        MenuButton btnPlay     = new MenuButton("JOUER SOLO", onPlay);
        MenuButton btnMulti    = new MenuButton("MULTIJOUEUR (Bientôt)", () -> System.out.println("Online coming soon..."));
        MenuButton btnStats    = new MenuButton("CLASSEMENT / STATS", () -> System.out.println("Ouverture du Leaderboard..."));
        MenuButton btnSettings = new MenuButton("OPTIONS", () -> System.out.println("Accès aux réglages..."));
        MenuButton btnQuit     = new MenuButton("QUITTER", () -> FXGL.getGameController().exit());

        content.getChildren().addAll(title, btnPlay, btnMulti, btnStats, btnSettings, btnQuit);
        return content;
    }
}