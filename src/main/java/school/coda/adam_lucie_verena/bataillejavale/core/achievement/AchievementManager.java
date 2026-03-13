package school.coda.adam_lucie_verena.bataillejavale.core.achievement;

import javafx.application.Platform;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.NotificationView;

import java.io.*;
import java.util.*;

/**
 * Gère la progression du joueur, le déblocage des succès et la persistance des données.
 * Les données sont stockées dans le dossier utilisateur pour survivre aux redémarrages.
 */
public class AchievementManager {

    private static final String SAVE_FILE = System.getProperty("user.home") + File.separator + ".bataille_javale_stats.dat";

    private final Set<String> unlockedAchievements = new HashSet<>();
    private int totalGamesPlayed = 0;
    private int totalWins = 0;

    private NotificationView notificationView;

    /**
     * Initialise le gestionnaire et tente de charger les données existantes.
     */
    public AchievementManager() {
        loadData();
    }

    /**
     * Enregistre la vue de notification pour l'affichage des alertes.
     */
    public void setNotificationView(NotificationView view) {
        this.notificationView = view;
    }

    /**
     * Analyse les résultats d'une partie terminée pour débloquer d'éventuels succès.
     * @param won True si le joueur a gagné.
     * @param rounds Nombre de manches écoulées.
     */
    public void onGameEnd(boolean won, int rounds) {
        totalGamesPlayed++;
        System.out.println("youhou");

        if (won) {
            totalWins++;
            System.out.println("j'ai gagné");
            unlock(AchievementType.FIRST_WIN);

            if (rounds < 36) {
                System.out.println("j'ai fait moins de 36 rounds");
                unlock(AchievementType.FAST_WIN);
            }
        }

        // Succès basés sur le cumul de parties
        if (totalGamesPlayed >= 10) unlock(AchievementType.VETERAN_10);
//        if (totalGamesPlayed >= 50) unlock(AchievementType.VETERAN_50);
//        if (totalGamesPlayed >= 100) unlock(AchievementType.VETERAN_100);

        saveData();
    }

    /**
     * Débloque manuellement un succès (ex: Konami Code).
     */
    public void unlock(AchievementType type) {
        System.out.println("Vérification succès : " + type);
        if (unlockedAchievements == null) {
            System.out.println("ERREUR : unlocked est NULL !");
            return;
        }
        if (unlockedAchievements.contains(type.name())) {
            unlockedAchievements.add(type.name());
            saveData();

            // Notification visuelle sur le thread UI
            System.out.println("ça jsp");
            if (notificationView != null) {
                Platform.runLater(() -> notificationView.showAchievement(type));
            }
        }
    }

    /**
     * Sauvegarde les statistiques et les succès dans un fichier binaire.
     */
    private void saveData() {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(SAVE_FILE))) {
            dos.writeInt(totalGamesPlayed);
            dos.writeInt(totalWins);
            dos.writeInt(unlockedAchievements.size());
            for (String id : unlockedAchievements) {
                dos.writeUTF(id);
            }
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde achievements : " + e.getMessage());
        }
    }

    /**
     * Charge les données depuis le fichier utilisateur.
     */
    private void loadData() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            totalGamesPlayed = dis.readInt();
            totalWins = dis.readInt();
            int size = dis.readInt();
            for (int i = 0; i < size; i++) {
                unlockedAchievements.add(dis.readUTF());
            }
        } catch (IOException e) {
            System.err.println("Erreur chargement achievements : " + e.getMessage());
        }
    }

    // --- Getters pour l'écran de statistiques ---
    public int getTotalGamesPlayed() { return totalGamesPlayed; }
    public int getTotalWins() { return totalWins; }
    public boolean isUnlocked(AchievementType type) { return unlockedAchievements.contains(type.name()); }
}