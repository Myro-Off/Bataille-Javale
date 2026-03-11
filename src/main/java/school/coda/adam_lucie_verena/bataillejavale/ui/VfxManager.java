package school.coda.adam_lucie_verena.bataillejavale.ui;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.particle.ParticleEmitter;
import com.almasb.fxgl.particle.ParticleEmitters;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import school.coda.adam_lucie_verena.bataillejavale.core.model.Coordinate;

/**
 * Gestionnaire d'effets visuels (VFX) pour Bataille-Javale.
 * <p>
 * Cette classe utilitaire centralise la création des animations et des systèmes
 * de particules. Elle permet de séparer la logique de rendu esthétique de la
 * logique de jeu pure.
 * </p>
 */
public class VfxManager {

    /**
     * Déclenche l'effet visuel correspondant au résultat d'un tir.
     * <p>
     * Calcule la position centrale de la cellule ciblée dans l'espace de la scène
     * pour y attacher les particules.
     * </p>
     * @param view   La {@link GameView} sur laquelle le tir a eu lieu (pour le tremblement).
     * @param target La coordonnée de la grille visée.
     * @param isHit  True si le tir a touché un navire, False pour un tir dans l'eau.
     */
    public static void playShotEffect(GameView view, Coordinate target, boolean isHit) {
        // Conversion coordonnée grille -> position scène (centre de la case de 40px)
        Point2D scenePt = view.localToScene(target.x() * 40 + 20, target.y() * 40 + 20);

        if (isHit) {
            spawnExplosion(scenePt.getX(), scenePt.getY());
            shake(view);
        } else {
            spawnSplash(scenePt.getX(), scenePt.getY());
        }
    }

    /**
     * Génère un système de particules d'explosion (feu et étincelles).
     * @param x Position X globale.
     * @param y Position Y globale.
     */
    private static void spawnExplosion(double x, double y) {
        ParticleEmitter emitter = ParticleEmitters.newExplosionEmitter(35);
        emitter.setStartColor(Color.ORANGERED);
        emitter.setEndColor(Color.YELLOW);

        FXGL.entityBuilder()
                .at(x, y)
                .with(new com.almasb.fxgl.particle.ParticleComponent(emitter))
                .with(new com.almasb.fxgl.dsl.components.ExpireCleanComponent(Duration.seconds(1)))
                .buildAndAttach();
    }

    /**
     * Génère un système de particules d'éclaboussure (eau bleue).
     * @param x Position X globale.
     * @param y Position Y globale.
     */
    private static void spawnSplash(double x, double y) {
        ParticleEmitter emitter = ParticleEmitters.newFireEmitter();
        emitter.setStartColor(Color.web("#3498db"));
        emitter.setEndColor(Color.WHITE);
        emitter.setNumParticles(15);

        FXGL.entityBuilder()
                .at(x, y)
                .with(new com.almasb.fxgl.particle.ParticleComponent(emitter))
                .with(new com.almasb.fxgl.dsl.components.ExpireCleanComponent(Duration.seconds(0.8)))
                .buildAndAttach();
    }

    /**
     * Applique un effet de tremblement (Screenshake) horizontal sur un composant.
     * @param node Le nœud graphique qui doit subir la vibration.
     */
    private static void shake(Node node) {
        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(50), new KeyValue(node.translateXProperty(), 10)),
                new KeyFrame(Duration.millis(100), new KeyValue(node.translateXProperty(), -10)),
                new KeyFrame(Duration.millis(150), new KeyValue(node.translateXProperty(), 0))
        );
        tl.play();
    }
}