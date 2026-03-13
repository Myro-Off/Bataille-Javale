package school.coda.adam_lucie_verena.bataillejavale.gui.scene;

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
 * Scène de chargement immersive remplaçant l'écran par défaut de FXGL.
 * <p>
 * Elle présente une ambiance sous-marine avec un radar actif (sonar) et
 * affiche les crédits de l'équipe de développement.
 * </p>
 */
public class CustomLoadingScene extends LoadingScene {

    // ------------------------------------------------------------------------------------------
    // CONSTANTES VISUELLES
    // ------------------------------------------------------------------------------------------

    private static final Color RADAR_GREEN = Color.web("#00ff00");
    private static final String FONT_FAMILY = "Verdana";
    private static final double RADAR_RADIUS = 60.0;

    // ------------------------------------------------------------------------------------------
    // CONSTRUCTEUR
    // ------------------------------------------------------------------------------------------

    /**
     * Initialise l'interface et lance les animations du sonar et du texte.
     */
    public CustomLoadingScene() {
        int width = getAppWidth();
        int height = getAppHeight();

        // 1. FOND ET STRUCTURE
        StackPane root = new StackPane();
        root.setPrefSize(width, height);
        root.getChildren().add(createBackground(width, height));

        VBox content = new VBox(25);
        content.setAlignment(Pos.CENTER);

        // 2. ASSEMBLAGE DES COMPOSANTS
        content.getChildren().addAll(
                createTitle(),
                createSonar(),
                createLoadingText(),
                createCredits()
        );

        root.getChildren().add(content);
        getContentRoot().getChildren().add(root);
    }

    // ------------------------------------------------------------------------------------------
    // MÉTHODES PRIVÉES : CONSTRUCTION DES COMPOSANTS
    // ------------------------------------------------------------------------------------------

    /**
     * Crée le fond dégradé évoquant les abysses.
     */
    private Rectangle createBackground(int w, int h) {
        LinearGradient oceanDeeper = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#001a33")),
                new Stop(1, Color.web("#000000"))
        );
        return new Rectangle(w, h, oceanDeeper);
    }

    /**
     * Crée le titre principal avec un effet d'ombre portée.
     */
    private Text createTitle() {
        Text title = new Text("LA BATAILLE JAVALE");
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 50));
        title.setFill(Color.WHITE);
        title.setEffect(new DropShadow(15, Color.BLACK));
        return title;
    }

    /**
     * Construit le sonar animé (Cercle statique + Faisceau tournant).
     */
    private Group createSonar() {
        Group sonarGroup = new Group();

        // Cercle extérieur du radar
        Circle radarScreen = new Circle(0, 0, RADAR_RADIUS, Color.TRANSPARENT);
        radarScreen.setStroke(RADAR_GREEN);
        radarScreen.setStrokeWidth(2);

        // Faisceau (Arc tournant)
        Group beamGroup = new Group();
        Circle invisibleBounds = new Circle(0, 0, RADAR_RADIUS, Color.TRANSPARENT);
        Arc radarBeam = new Arc(0, 0, RADAR_RADIUS, RADAR_RADIUS, 0, 60);
        radarBeam.setType(ArcType.ROUND);
        radarBeam.setFill(RADAR_GREEN.deriveColor(0, 1, 1, 0.4));

        beamGroup.getChildren().addAll(invisibleBounds, radarBeam);

        // Animation de rotation
        RotateTransition beamSweep = new RotateTransition(Duration.seconds(2), beamGroup);
        beamSweep.setByAngle(360);
        beamSweep.setInterpolator(Interpolator.LINEAR);
        beamSweep.setCycleCount(Animation.INDEFINITE);
        beamSweep.play();

        sonarGroup.getChildren().addAll(radarScreen, beamGroup);
        return sonarGroup;
    }

    /**
     * Crée le texte de statut avec animation de clignotement.
     */
    private Text createLoadingText() {
        Text loadingText = new Text("Analyse des secteurs maritimes...");
        loadingText.setFont(Font.font(FONT_FAMILY, 18));
        loadingText.setFill(RADAR_GREEN);

        FadeTransition blink = new FadeTransition(Duration.seconds(1), loadingText);
        blink.setFromValue(1.0);
        blink.setToValue(0.3);
        blink.setCycleCount(Animation.INDEFINITE);
        blink.setAutoReverse(true);
        blink.play();

        return loadingText;
    }

    /**
     * Crée la mention des auteurs.
     */
    private Text createCredits() {
        Text credits = new Text("Développé par Adam, Lucie et Verena");
        credits.setFont(Font.font(FONT_FAMILY, 16));
        credits.setFill(Color.GRAY);
        return credits;
    }
}