package school.coda.adam_lucie_verena.bataillejavale.ui;

import com.almasb.fxgl.app.scene.LoadingScene;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Scène de chargement personnalisée pour le projet Bataille Navale.
 * Elle remplace l'écran par défaut pour offrir une immersion immédiate avec un radar animé
 * et l'affichage des crédits de l'équipe.
 */
public class CustomLoadingScene extends LoadingScene {

    /**
     * Construit l'interface graphique de l'écran de chargement.
     * Organise les éléments visuels (fond, titre, sonar, crédits) et lance les animations.
     */
    public CustomLoadingScene() {
        int width = getAppWidth();
        int height = getAppHeight();

        // ——— 1. FOND DÉGRADÉ ———
        // Crée une ambiance "abyssale" avec un dégradé du bleu marine vers le noir.
        LinearGradient oceanDeeper = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#001a33")),
                new Stop(1, Color.web("#000000"))
        );
        Rectangle background = new Rectangle(width, height, oceanDeeper);

        // ——— 2. STRUCTURE DE BASE ———
        // Utilisation d'un StackPane pour superposer les éléments sur le fond.
        StackPane root = new StackPane();
        root.setPrefSize(width, height);
        root.getChildren().add(background);

        // Utilisation d'une VBox pour aligner verticalement le contenu central.
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        // ——— 3. TITRE ———
        Text title = new Text("LA BATAILLE JAVALE");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 50));
        title.setFill(Color.WHITE);
        title.setEffect(new DropShadow(15, Color.BLACK));

        // ——— 4. LE SONAR (ANIMATION) ———
        Group sonarGroup = new Group();

        // Le cercle extérieur du radar (statique).
        Circle radarScreen = new Circle(0, 0, 60, Color.TRANSPARENT);
        radarScreen.setStroke(Color.web("#00ff00"));
        radarScreen.setStrokeWidth(2);

        // Le faisceau tournant. On l'isole dans un groupe avec des bordures invisibles
        // pour stabiliser son point de pivot lors de la rotation.
        Group beamGroup = new Group();
        Circle invisibleBounds = new Circle(0, 0, 60, Color.TRANSPARENT);
        Arc radarBeam = new Arc(0, 0, 60, 60, 0, 60);
        radarBeam.setType(ArcType.ROUND);
        radarBeam.setFill(Color.web("#00ff00", 0.4));

        beamGroup.getChildren().addAll(invisibleBounds, radarBeam);
        sonarGroup.getChildren().addAll(radarScreen, beamGroup);

        // Configuration de la rotation continue du faisceau.
        RotateTransition beamSweep = new RotateTransition(Duration.seconds(2), beamGroup);
        beamSweep.setByAngle(360);
        beamSweep.setInterpolator(Interpolator.LINEAR);
        beamSweep.setCycleCount(Animation.INDEFINITE);
        beamSweep.play();

        // ——— 5. TEXTE DE STATUT ———
        Text loadingText = new Text("Analyse des secteurs maritimes...");
        loadingText.setFont(Font.font("Verdana", 18));
        loadingText.setFill(Color.web("#00ff00"));

        // Animation de clignotement.
        FadeTransition blink = new FadeTransition(Duration.seconds(1), loadingText);
        blink.setFromValue(1.0);
        blink.setToValue(0.3);
        blink.setCycleCount(Animation.INDEFINITE);
        blink.setAutoReverse(true);
        blink.play();

        // ——— 6. CRÉDITS ———
        // Affiche les noms des développeurs (les goats).
        Text credits = new Text("Développé par Adam, Lucie et Verena");
        credits.setFont(Font.font("Verdana", 16));
        credits.setFill(Color.GRAY);
        VBox.setMargin(credits, new javafx.geometry.Insets(50, 0, 0, 0));

        // ——— 7. ASSEMBLAGE FINAL ———
        content.getChildren().addAll(title, sonarGroup, loadingText, credits);
        root.getChildren().add(content);
        getContentRoot().getChildren().add(root);
    }
}