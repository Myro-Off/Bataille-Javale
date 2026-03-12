package school.coda.adam_lucie_verena.bataillejavale.view.component;

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
 * Panneau de suivi de l'état de la flotte ennemie.
 * <p>
 * Affiche des indicateurs visuels (points) pour chaque navire,
 * avec un retour à la ligne automatique pour la lisibilité.
 * </p>
 */
public class FleetStatusView extends VBox {

    private final Board enemyBoard;

    public FleetStatusView(Board enemyBoard) {
        this.enemyBoard = enemyBoard;
        setSpacing(15);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: rgba(15, 23, 42, 0.7); -fx-background-radius: 10; -fx-border-color: #1e293b;");
        update();
    }

    /**
     * Rafraîchit les indicateurs de santé.
     */
    public void update() {
        getChildren().clear();

        Text title = new Text("SUIVI FLOTTE ENNEMIE");
        title.setFill(Color.web("#00d2d3"));
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        getChildren().add(title);

        Map<ShipType, List<Ship>> grouped = enemyBoard.getShips().stream()
                .collect(Collectors.groupingBy(Ship::getType));

        for (ShipType type : ShipType.values()) {
            List<Ship> ships = grouped.get(type);
            if (ships == null || ships.isEmpty()) continue;

            VBox typeBox = new VBox(5);
            Text name = new Text(type.getName().toUpperCase());
            name.setFill(Color.LIGHTGRAY);
            name.setFont(Font.font("Verdana", FontWeight.BOLD, 10));

            FlowPane dotsContainer = new FlowPane(8, 8);
            dotsContainer.setPrefWrapLength(120);
            dotsContainer.setAlignment(Pos.CENTER_LEFT);

            for (Ship s : ships) {
                Circle dot = new Circle(5, s.isSunk() ? Color.web("#ff4757") : Color.web("#10b981"));
                dotsContainer.getChildren().add(dot);
            }

            typeBox.getChildren().addAll(name, dotsContainer);
            getChildren().add(typeBox);
        }
    }
}