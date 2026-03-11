package school.coda.adam_lucie_verena.bataillejavale.ui;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import javafx.scene.paint.Color;

public class AchievementTrackerView extends VBox {
    public AchievementTrackerView() {
        setSpacing(10);
        Text t = new Text("SUCCÈS ÉPINGLÉ");
        t.setFill(Color.web("#f1c40f"));
        t.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        getChildren().add(t);
        // Espace vide pour l'achievement futur
    }
}