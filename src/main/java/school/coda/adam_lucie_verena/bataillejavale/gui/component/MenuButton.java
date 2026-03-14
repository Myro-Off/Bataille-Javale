package school.coda.adam_lucie_verena.bataillejavale.gui.component;

import javafx.geometry.Pos;
import javafx.scene.effect.Glow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import school.coda.adam_lucie_verena.bataillejavale.gui.Theme;
import school.coda.adam_lucie_verena.bataillejavale.gui.AssetsManager;

import java.util.stream.Collectors;

public class MenuButton extends StackPane {
    private final Polygon bg;
    private final Rectangle glowOverlay;

    public MenuButton(String name, Runnable action) {
        bg = new Polygon(0,0, 260,0, 280,15, 280,45, 20,45, 0,30);
        bg.setFill(new LinearGradient(0,0,1,1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1e293b", 0.6)), new Stop(1, Color.web("#020617", 0.9))));
        bg.setStroke(Theme.CYAN.deriveColor(0, 1, 1, 0.2));
        bg.setStrokeWidth(1.2);

        glowOverlay = new Rectangle(280, 45, Theme.CYAN);
        glowOverlay.setOpacity(0);
        glowOverlay.setClip(new Polygon(0,0, 260,0, 280,15, 280,45, 20,45, 0,30));

        String spacedName = name.toUpperCase().chars().mapToObj(c -> (char)c + " ")
                .collect(Collectors.joining()).trim();

        Text text = new Text(spacedName);
        text.setFill(Color.web("#94a3b8"));
        text.setFont(Theme.font(14, FontWeight.BLACK));

        getChildren().addAll(bg, glowOverlay, text);
        setAlignment(Pos.CENTER);

        setupInteractions(text, action);
    }

    private void setupInteractions(Text text, Runnable action) {
        setOnMouseEntered(_ -> {
            bg.setStroke(Theme.CYAN);
            bg.setStrokeWidth(2);
            setCursor(Theme.CURSOR_CLICK);
            text.setFill(Color.WHITE);
            glowOverlay.setOpacity(0.15);
            this.setEffect(new Glow(0.3));
        });

        setOnMouseExited(_ -> {
            bg.setStroke(Theme.CYAN.deriveColor(0, 1, 1, 0.2));
            bg.setStrokeWidth(1.2);
            text.setFill(Color.web("#94a3b8"));
            glowOverlay.setOpacity(0);
            this.setEffect(null);
            text.setOpacity(1.0);
        });

        setOnMouseClicked(_ -> {
            AssetsManager.playSFX("button.wav", 2);
            action.run();
        });
    }
}