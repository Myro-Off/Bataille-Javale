package school.coda.adam_lucie_verena.bataillejavale.view.scene;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Pos;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import school.coda.adam_lucie_verena.bataillejavale.view.audio.SoundManager;
import school.coda.adam_lucie_verena.bataillejavale.view.component.MenuButton;

/**
 * Vue du menu principal de l'application.
 */
public class MainMenuView extends StackPane {

    // ------------------------------------------------------------------------------------------
    // CONSTRUCTEUR
    // ------------------------------------------------------------------------------------------

    public MainMenuView(Runnable onPlay) {
        SoundManager.playMusic("musique_de_fond.wav");
        this.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());

        // 1. GÉNÉRATION DU FOND OPAQUE
        Rectangle background = createBackground();

        // 2. CRÉATION DU CONTENU (Titre + Boutons)
        VBox menuContent = createMenuContent(onPlay);
        //VBox muteBox = createBoxMuet();

        // 3. EMPILEMENT DES COUCHES
        // L'ordre est crucial : muteBox est au-dessus mais "transparente" aux clics
        getChildren().addAll(background, menuContent/*, muteBox*/);
    }

    // ------------------------------------------------------------------------------------------
    // MÉTHODES PRIVÉES : CONSTRUCTION DES COMPOSANTS
    // ------------------------------------------------------------------------------------------

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

//    private VBox createBoxMuet() {
//        VBox right = new VBox(5);
//        right.setAlignment(Pos.TOP_RIGHT);
//        StackPane.setAlignment(right, Pos.TOP_RIGHT);
//
//        // Empêche la VBox de bloquer les boutons du dessous
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

    /* =========================================================================================
     * 📖 TODO : LE REGISTRE DE L'AMIRAUTÉ (UI MENU)
     * ========================================================================================= */
}