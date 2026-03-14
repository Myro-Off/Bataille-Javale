package school.coda.adam_lucie_verena.bataillejavale.gui.component;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import school.coda.adam_lucie_verena.bataillejavale.gui.AssetsManager;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Overlay de contrôle global regroupant les réglages audio et les options d'affichage.
 * Ce composant reste persistant au premier plan de l'application.
 */
public class SettingsControlOverlay extends HBox {

    private static final String SVG_MUSIC = "M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z";
    private static final String SVG_SFX = "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z";
    private static final String SVG_MUTE = "M19.1 4.93l-1.41-1.41L12 9.17l-5.69-5.65L4.9 4.93 10.59 10.5l-5.69 5.66 1.41 1.41L12 11.92l5.69 5.65 1.41-1.41-5.69-5.66z";

    // Icônes pour le Plein Écran
    private static final String SVG_FULLSCREEN = "M7 14H5v5h5v-2H7v-3zm-2-4h2V7h3V5H5v5zm12 7h-3v2h5v-5h-2v3zM14 5v2h3v3h2V5h-5z";
    private static final String SVG_WINDOWED = "M5 16h3v3h2v-5H5v2zm3-8H5v2h5V5H8v3zm6 11h2v-3h3v-2h-5v5zm2-11V5h-2v5h5V8h-3z";

    public SettingsControlOverlay() {
        setSpacing(20);
        setPadding(new Insets(15));
        setAlignment(Pos.TOP_RIGHT);
        setPickOnBounds(false);

        // Bouton de contrôle du Plein Écran
        Button btnFullscreen = createFullscreenButton();

        // Contrôles Volume
        VolumeControl musicCtrl = new VolumeControl(SVG_MUSIC, AssetsManager::getMusicVolume, AssetsManager::setMusicVolume, AssetsManager::toggleMusicMute, AssetsManager::isMusicMuted);
        VolumeControl sfxCtrl = new VolumeControl(SVG_SFX, AssetsManager::getSfxVolume, AssetsManager::setSFXVolume, AssetsManager::toggleSFXMute, AssetsManager::isSfxMuted);

        getChildren().addAll(btnFullscreen, musicCtrl, sfxCtrl);
    }

    /**
     * Crée le bouton permettant de basculer entre le mode fenêtré et le plein écran.
     */
    private Button createFullscreenButton() {
        Button btn = new Button();
        SVGPath icon = new SVGPath();
        btn.setGraphic(icon);
        styleButton(btn);

        // Mise à jour initiale de l'icône
        updateFullscreenIcon(icon, FXGL.getPrimaryStage().isFullScreen());

        btn.setOnAction(_ -> {
            AssetsManager.playSFX("button.wav", 1.0);
            boolean isFull = !FXGL.getPrimaryStage().isFullScreen();
            FXGL.getPrimaryStage().setFullScreen(isFull);
            updateFullscreenIcon(icon, isFull);
        });

        return btn;
    }

    private void updateFullscreenIcon(SVGPath icon, boolean isFullscreen) {
        icon.setContent(isFullscreen ? SVG_WINDOWED : SVG_FULLSCREEN);
        icon.setFill(Color.WHITE);
    }

    private static void styleButton(Button b) {
        b.setPrefSize(44, 44);
        b.setStyle("-fx-background-color: rgba(15, 23, 42, 0.9); -fx-background-radius: 12; " +
                "-fx-border-color: #00d2d3; -fx-border-width: 1.5; -fx-border-radius: 12; -fx-cursor: hand;");
    }

    /**
     * Classe interne gérant l'affichage d'un bouton de volume et son slider vertical escamotable.
     */
    private static class VolumeControl extends VBox {
        private final Button btn;
        private final Slider slider;
        private final String normalIcon;

        public VolumeControl(String iconPath, Supplier<Double> volGetter, Consumer<Double> volSetter, Supplier<Boolean> muteToggler, Supplier<Boolean> muteGetter) {
            this.normalIcon = iconPath;
            setAlignment(Pos.TOP_CENTER);
            setSpacing(10);

            btn = new Button();
            SVGPath icon = new SVGPath();
            btn.setGraphic(icon);
            styleButton(btn);

            slider = new Slider(0, 1, volGetter.get());
            slider.setOrientation(Orientation.VERTICAL);
            slider.setPrefHeight(100);
            slider.setVisible(false);
            slider.setManaged(false);
            styleSlider(slider);

            updateUI(muteGetter.get());

            slider.valueProperty().addListener((obs, old, val) -> {
                double v = val.doubleValue();
                volSetter.accept(v);
                if (v > 0 && muteGetter.get()) muteToggler.get();
                updateUI(muteGetter.get() || v == 0);
            });

            btn.setOnAction(_ -> {
                boolean muted = muteToggler.get();
                updateUI(muted);
                if (!muted) slider.setValue(volGetter.get());
            });

            this.setOnMouseEntered(_ -> { slider.setVisible(true); slider.setManaged(true); });
            this.setOnMouseExited(_ -> { slider.setVisible(false); slider.setManaged(false); });

            getChildren().addAll(btn, slider);
        }

        private void updateUI(boolean isMuted) {
            SVGPath p = (SVGPath) btn.getGraphic();
            p.setContent(isMuted ? SVG_MUTE : normalIcon);
            p.setFill(isMuted ? Color.web("#ff4757") : Color.WHITE);
            btn.setStyle(btn.getStyle() + "-fx-border-color: " + (isMuted ? "#ff4757" : "#00d2d3") + ";");
        }

        private void styleSlider(Slider s) {
            s.setStyle("-fx-control-inner-background: rgba(15, 23, 42, 0.9); -fx-cursor: hand;");
        }
    }
}