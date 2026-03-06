package school.coda.adam_lucie_venera.bataillejavale.ui;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import javafx.scene.Cursor;

import static com.almasb.fxgl.dsl.FXGL.getGameScene;

public class DisplayGame extends GameApplication {

    @Override
    /*
    * Configuration de la fenêtre d'affichage
    * */
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(800);
        gameSettings.setHeight(600);
        gameSettings.setTitle("Bataille-Javale-MaSalive");
        gameSettings.setVersion("0.0.1");
    }

    @Override
    /*
    * Configuration du menu principale
    * */
    protected void initUI() {
        super.initUI();
        getGameScene().getRoot().setCursor(Cursor.DEFAULT);
        // à compléter
    }

    /*
    * Lancement de la fenêtre sur Adam pour le faire chier (pour l'instant)
    * */
    static void main(String[] args){
        launch(args);
    }
}
