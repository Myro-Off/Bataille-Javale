package school.coda.adam_lucie_verena.bataillejavale.ui;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import org.jetbrains.annotations.NotNull;

/**
 * Point d'entrée principal de l'application Bataille Navale.
 * Cette classe orchestre le cycle de vie du jeu, de l'initialisation des paramètres
 * à la gestion de l'interface utilisateur et de la logique métier.
 */
public class DisplayGame extends GameApplication {

    /**
     * Configure les réglages globaux du moteur de jeu avant son lancement.
     * Définit les propriétés de la fenêtre, les versions, et enregistre la scène de chargement personnalisée.
     * @param gameSettings L'objet de configuration fourni par FXGL.
     */
    @Override
    protected void initSettings(GameSettings gameSettings) {
        // Autorise le basculement en mode plein écran via les raccourcis système
        gameSettings.setFullScreenAllowed(true);
        // Permet à l'utilisateur de redimensionner manuellement la fenêtre
        gameSettings.setManualResizeEnabled(true);
        // Définit le titre affiché dans la barre de titre de la fenêtre
        gameSettings.setTitle("Bataille-Javale-MaSalive");
        // Version actuelle du projet pour le suivi du développement
        gameSettings.setVersion("0.0.1");
        // Désactive le menu système par défaut de FXGL pour utiliser notre propre interface
        gameSettings.setGameMenuEnabled(false);

        // Enregistre la fabrique de scènes pour injecter notre écran de chargement personnalisé
        gameSettings.setSceneFactory(new SceneFactory() {
            @NotNull
            @Override
            public LoadingScene newLoadingScene() {
                return new CustomLoadingScene();
            }
        });
    }

    /**
     * Initialise les éléments de l'interface utilisateur (boutons, menus, overlays).
     * Cette méthode est appelée sur le thread principal de JavaFX.
     */
    @Override
    protected void initUI() {
        // Force l'utilisation du curseur standard du système sur la scène de jeu
        FXGL.getGameScene().setCursor(javafx.scene.Cursor.DEFAULT);
        super.initUI();
        // TODO: Ajouter ici le menu principal et les composants graphiques
    }

    /**
     * Initialise la logique de jeu, les entités et les données.
     * S'exécute en arrière-plan pendant l'affichage de la LoadingScene.
     */
    @Override
    protected void initGame() {
        try {
            // Temporisation artificielle pour permettre de visualiser l'écran de chargement
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // Enregistre une erreur fatale dans les logs FXGL si le thread est interrompu
            com.almasb.fxgl.logging.Logger.get(DisplayGame.class).fatal("Erreur de chargement", e);
            // Restaure le statut d'interruption du thread (bonne pratique Java)
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Méthode de lancement standard de l'application Java.
     * @param args Arguments de la ligne de commande.
     */
    public static void main(String[] args){
        launch(args);
    }
}