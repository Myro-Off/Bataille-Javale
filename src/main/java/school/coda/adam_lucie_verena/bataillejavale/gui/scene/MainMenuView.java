package school.coda.adam_lucie_verena.bataillejavale.gui.scene;

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
import school.coda.adam_lucie_verena.bataillejavale.gui.audio.SoundManager;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.MenuButton;

/**
 * Vue principale représentant le menu d'accueil du jeu.
 * Gère l'affichage du titre, des boutons de navigation et l'ambiance sonore initiale.
 */
public class MainMenuView extends StackPane {

    /**
     * Initialise le menu principal avec son fond, son contenu et lance la musique d'ambiance.
     * @param onPlay Action à exécuter lors du clic sur le bouton de jeu solo.
     */
    public MainMenuView(Runnable onPlay) {
        SoundManager.playMusic("musique_de_fond.wav");
        this.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());

        Rectangle background = createBackground();
        VBox menuContent = createMenuContent(onPlay);
        //VBox muteBox = createBoxMuet();

        getChildren().addAll(background, menuContent/*, muteBox*/);
    }

    /**
     * Crée le rectangle de fond avec un dégradé linéaire sombre pour l'esthétique "Deep Sea".
     * @return Un Rectangle aux dimensions de l'application avec un dégradé de bleu nuit.
     */
    private Rectangle createBackground() {
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#050514")),
                new Stop(1, Color.web("#0a0a28"))
        );

        Rectangle bg = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight(), gradient);
        bg.setOpacity(1.0);
        return bg;
    }

    /**
     * Méthode de création de l'interface de contrôle du volume (actuellement désactivée).
     * @return Une VBox contenant les options de gestion du son.
     */
//    private VBox createBoxMuet() {
//        VBox right = new VBox(5);
//        right.setAlignment(Pos.TOP_RIGHT);
//        StackPane.setAlignment(right, Pos.TOP_RIGHT);
//
//        right.setPickOnBounds(false);
//        right.setPadding(new javafx.geometry.Insets(15));
//
//        javafx.scene.control.MenuButton btnMuet = new javafx.scene.control.MenuButton("SON");
//
//        MenuItem item1 = new MenuItem("Couper le son");
//        item1.setOnAction(e -> SoundManager.stopMusic());
//
//        btnMuet.getItems().add(item1);
//        right.getChildren().add(btnMuet);
//        return right;
//    }

    /**
     * Génère le titre stylisé et la liste des boutons de navigation du menu.
     * @param onPlay Action liée au bouton "Jouer Solo".
     * @return Une VBox centrée contenant les éléments interactifs du menu.
     */
    private VBox createMenuContent(Runnable onPlay) {
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);

        Text title = new Text("BATAILLE JAVALE");
        title.setFont(Font.font("Verdana", 60));
        title.setFill(Color.CYAN);
        title.setStroke(Color.WHITE);
        title.setStrokeWidth(0.5);

        MenuButton btnPlay     = new MenuButton("JOUER SOLO", onPlay);
        MenuButton btnMulti    = new MenuButton("MULTIJOUEUR", () -> System.out.println("Online coming soon..."));
        MenuButton btnStats    = new MenuButton("CLASSEMENT / STATS", () -> System.out.println("Ouverture du Leaderboard..."));
        MenuButton btnSettings = new MenuButton("OPTIONS", () -> System.out.println("Accès aux réglages..."));
        MenuButton btnQuit     = new MenuButton("QUITTER", () -> FXGL.getGameController().exit());

        content.getChildren().addAll(title, btnPlay, btnMulti, btnStats, btnSettings, btnQuit);
        return content;
    }
}