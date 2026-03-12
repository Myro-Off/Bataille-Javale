package school.coda.adam_lucie_verena.bataillejavale.ui;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Overlay de fin de partie affichant le débriefing de la mission.
 * Présente les statistiques de tir et le verdict final avec des transitions fluides.
 */
public class GameOverView extends StackPane {

    /**
     * Construit l'écran de fin de partie et initialise les animations d'entrée.
     * @param isVictory Indique si le joueur a remporté la victoire.
     * @param shots     Nombre total de projectiles lancés durant la partie.
     * @param hits      Nombre d'impacts confirmés sur les navires ennemis.
     * @param onRestart Action à exécuter pour relancer une nouvelle session de jeu.
     */
    public GameOverView(boolean isVictory, int shots, int hits, Runnable onRestart) {
        setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());

        Rectangle bg = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight());
        bg.setFill(new RadialGradient(0, 0, 0.5, 0.5, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(15, 23, 42, 0.8)),
                new Stop(1, Color.rgb(2, 6, 23, 0.95))));

        VBox content = buildContent(isVictory, shots, hits, onRestart);
        content.setMaxSize(600, 450);
        content.setStyle("-fx-background-color: rgba(30, 41, 59, 0.9); " +
                "-fx-background-radius: 20; " +
                "-fx-border-color: " + (isVictory ? "#f1c40f" : "#ff4757") + "; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 20;");

        getChildren().addAll(bg, content);

        FadeTransition ft = new FadeTransition(Duration.seconds(0.5), this);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        ScaleTransition st = new ScaleTransition(Duration.seconds(0.4), content);
        st.setFromX(0.5);
        st.setFromY(0.5);
        st.setToX(1);
        st.setToY(1);
        st.play();
    }

    /**
     * Génère la hiérarchie visuelle des éléments du rapport (Titre, Statistiques, Boutons).
     * @param isVictory État de victoire.
     * @param shots Nombre de tirs.
     * @param hits Nombre d'impacts.
     * @param onRestart Callback de redémarrage.
     * @return Le conteneur VBox structurant le contenu.
     */
    private VBox buildContent(boolean isVictory, int shots, int hits, Runnable onRestart) {
        VBox v = new VBox(30);
        v.setAlignment(Pos.CENTER);

        Text title = new Text(isVictory ? "VICTOIRE CONFIRMÉE" : "MISSION ÉCHOUÉE");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 48));
        title.setFill(isVictory ? Color.GOLD : Color.web("#ff4757"));

        double acc = shots > 0 ? (double) hits / shots * 100 : 0;

        VBox statsBox = new VBox(10);
        statsBox.setAlignment(Pos.CENTER);

        Text t1 = createStatText("PROJECTILES LANCÉS : " + shots);
        Text t2 = createStatText("IMPACTS CONFIRMÉS : " + hits);
        Text t3 = createStatText(String.format("RATIO DE PRÉCISION : %.1f%%", acc));
        t3.setFill(acc > 50 ? Color.LIME : Color.ORANGE);

        statsBox.getChildren().addAll(t1, t2, t3);

        HBox buttons = new HBox(20,
                new MenuButton("REJOUER", onRestart),
                new MenuButton("QUITTER", () -> FXGL.getGameController().gotoMainMenu())
        );
        buttons.setAlignment(Pos.CENTER);

        v.getChildren().addAll(title, statsBox, buttons);
        return v;
    }

    /**
     * Crée un élément textuel stylisé pour l'affichage des statistiques.
     * @param content Le message à afficher.
     * @return Une instance de Text configurée.
     */
    private Text createStatText(String content) {
        Text t = new Text(content);
        t.setFill(Color.LIGHTGRAY);
        t.setFont(Font.font("Monospaced", 18));
        return t;
    }
}