package school.coda.adam_lucie_verena.bataillejavale.view.component;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import javafx.scene.paint.Color;

public class SpecialAbilitiesView extends VBox {
    public SpecialAbilitiesView() {
        setSpacing(10);
        Text t = new Text("CAPACITÉS SPÉCIALES");
        t.setFill(Color.web("#00d2d3"));
        t.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        getChildren().add(t);
        // Espace vide pour le moment (Mode Salve, etc.)
    }
}