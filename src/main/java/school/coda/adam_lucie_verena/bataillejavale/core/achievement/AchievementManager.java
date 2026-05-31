package school.coda.adam_lucie_verena.bataillejavale.core.achievement;

import javafx.application.Platform;
import school.coda.adam_lucie_verena.bataillejavale.core.ai.Difficulty;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.NotificationView;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Gestionnaire central des succès et des statistiques persistantes.
 */
public class AchievementManager {

    private static final String SAVE_FILE = System.getProperty("user.home") + File.separator + ".bataille_javale_stats.dat";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Map<String, String> unlockedAchievements = new HashMap<>();
    private int totalGamesPlayed = 0;
    private int totalWins = 0;
    private int currentWinStreak = 0;
    private int currentHitStreak = 0;
    private int maxHitStreak = 0;

    // 💡 AchievementManager pourrait être découplé de la GUI
    // grâce à l'event-bus (voir plus bas)
    // Si on décide d'implémenter une TUI, on n'aurait pas de raison d'appeler NotificationView
    private NotificationView notificationView;

    public AchievementManager() {
        loadData();
    }

    public void setNotificationView(NotificationView view) {
        this.notificationView = view;
    }

    /**
     * Traite les résultats d'une partie.
     */
    public void onGameEnd(boolean won, int rounds, int shots, int hits, Difficulty difficulty) {
        totalGamesPlayed++;

        if (won) {
            totalWins++;
            currentWinStreak++;

            if (currentWinStreak >= 3) unlock(AchievementType.STREAK_3);
            if (currentWinStreak >= 5) unlock(AchievementType.STREAK_5);

            unlock(AchievementType.FIRST_WIN);
            if (rounds < 36) unlock(AchievementType.FAST_WIN);

            double accuracy = shots > 0 ? (double) hits / shots * 100 : 0;
            checkGradeAchievements(accuracy);

            switch (difficulty) {
                case EASY -> unlock(AchievementType.WIN_EASY);
                case NORMAL -> unlock(AchievementType.WIN_NORMAL);
                case EXPERT -> unlock(AchievementType.WIN_EXPERT);
            }
        } else {
            currentWinStreak = 0;
        }

        if (totalGamesPlayed >= 10) unlock(AchievementType.VETERAN_10);
        if (totalGamesPlayed >= 50) unlock(AchievementType.VETERAN_50);
        if (totalGamesPlayed >= 100) unlock(AchievementType.VETERAN_100);

        saveData();
    }

    public void unlockKonamiCode() { unlock(AchievementType.KONAMI_CODE); }
    public void onWinSalve() { unlock(AchievementType.WIN_SALVE); }
    public void onTuiComplete() { unlock(AchievementType.TUI_MASTER); }

    public void trackHitStreak(boolean hit) {
        if (hit) {
            currentHitStreak++;
            if (currentHitStreak > maxHitStreak) maxHitStreak = currentHitStreak;
            if (currentHitStreak >= 5) unlock(AchievementType.SHARP_SHOOTER);
            else if (currentHitStreak >= 2) unlock(AchievementType.BEGINNER_SHOOTER);
        } else {
            currentHitStreak = 0;
        }
    }

    public void resetMidGameStats() {
        this.currentHitStreak = 0;
    }

    private void checkGradeAchievements(double accuracy) {
        if (accuracy >= 80) unlock(AchievementType.GRADE_S);
        if (accuracy >= 60) unlock(AchievementType.GRADE_A);
        if (accuracy >= 40) unlock(AchievementType.GRADE_B);
        if (accuracy >= 20) unlock(AchievementType.GRADE_C);
    }

    public void onMeteorSunkShip() { unlock(AchievementType.METEOR_KILL); }
    public void unlockSunnyDay() { unlock(AchievementType.SUNNY_DAY); }

    public void unlock(AchievementType type) {
        if (!unlockedAchievements.containsKey(type.name())) {
            unlockedAchievements.put(type.name(), LocalDateTime.now().format(DATE_FORMATTER));
            saveData();
            // 💡 Ici, on pourrait utiliser L'event bus
            // FXGL.getEventBus().fireEvent(new AchievementUnlockedEvent(type));
            // Voir du côté de BatailleJavaleApp::initUI() pour l'initialisation de l'event handler
            if (notificationView != null) {
                Platform.runLater(() -> notificationView.showAchievement(type));
            }
        }
    }

    public int getAchievementProgress(AchievementType type) {
        if (isUnlocked(type)) return type.getTargetValue();
        return switch (type) {
            case VETERAN_10, VETERAN_50, VETERAN_100 -> totalGamesPlayed;
            case STREAK_3, STREAK_5 -> currentWinStreak;
            case SHARP_SHOOTER, BEGINNER_SHOOTER -> maxHitStreak;
            default -> 0;
        };
    }

    private void saveData() {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(SAVE_FILE))) {
            dos.writeInt(totalGamesPlayed);
            dos.writeInt(totalWins);
            dos.writeInt(currentWinStreak);
            dos.writeInt(maxHitStreak);
            dos.writeInt(unlockedAchievements.size());
            for (Map.Entry<String, String> entry : unlockedAchievements.entrySet()) {
                dos.writeUTF(entry.getKey());
                dos.writeUTF(entry.getValue());
            }
        } catch (IOException e) {
            System.err.println("Save error: " + e.getMessage());
        }
    }

    private void loadData() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return;
        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            totalGamesPlayed = dis.readInt();
            totalWins = dis.readInt();
            currentWinStreak = dis.readInt();
            maxHitStreak = dis.readInt();
            int size = dis.readInt();
            for (int i = 0; i < size; i++) {
                unlockedAchievements.put(dis.readUTF(), dis.readUTF());
            }
        } catch (IOException e) {
            System.err.println("Load error: " + e.getMessage());
        }
    }

    public boolean isUnlocked(AchievementType type) { return unlockedAchievements.containsKey(type.name()); }
    public String getUnlockDate(AchievementType type) { return unlockedAchievements.getOrDefault(type.name(), "NON DÉFINIE"); }
    public int getCurrentStreak() { return currentWinStreak; }
}