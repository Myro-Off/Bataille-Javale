package school.coda.adam_lucie_verena.bataillejavale.gui.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import school.coda.adam_lucie_verena.bataillejavale.core.model.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Panneau de suivi visuel de l'état d'une flotte (alliée ou ennemie).
 */
public class FleetStatusView extends VBox {

    private final Board board;
    private final String title;
    private final Color titleColor;

    /**
     * @param board Le plateau à suivre.
     * @param title Le titre à afficher en haut du composant.
     * @param titleColor La couleur thématique du titre.
     */
    public FleetStatusView(Board board, String title, Color titleColor) {
        this.board = board;
        this.title = title;
        this.titleColor = titleColor;
        setSpacing(10);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: rgba(15, 23, 42, 0.7); -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.1);");
        update();
    }

    /**
     * Reconstruit la liste des indicateurs de santé des navires.
     */
    public void update() {
        getChildren().clear();
        Text t = new Text(title);
        t.setFill(titleColor);
        t.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        getChildren().add(t);

        Map<ShipType, List<Ship>> grouped = board.getShips().stream()
                .collect(Collectors.groupingBy(Ship::getType));

        for (ShipType type : ShipType.values()) {
            List<Ship> ships = grouped.get(type);
            if (ships == null || ships.isEmpty()) continue;

            FlowPane dots = new FlowPane(5, 5);
            dots.setAlignment(Pos.CENTER_LEFT);
            for (Ship s : ships) {
                Circle dot = new Circle(4, s.isSunk() ? Color.web("#ff4757") : Color.web("#10b981"));
                dots.getChildren().add(dot);
            }

            Text name = new Text(type.getName().toUpperCase());
            name.setFill(Color.GRAY);
            name.setFont(Font.font("Verdana", FontWeight.BOLD, 9));

            getChildren().addAll(name, dots);
        }
    }
}