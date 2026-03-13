package school.coda.adam_lucie_verena.bataillejavale.gui.audio;

import javafx.scene.media.AudioClip;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private static final Map<String, AudioClip> SOUND_CACHE = new HashMap<>();

    public static void playSFX(String name) {
        try {
            AudioClip sound = SOUND_CACHE.computeIfAbsent(name, k -> {
                var resource = SoundManager.class.getResource("/assets/sounds/" + k);

                if (resource == null) {
                    throw new RuntimeException("Fichier introuvable : /assets/sounds/" + k);
                }

                return new AudioClip(resource.toExternalForm());
            });

            if (sound != null) {
                sound.setVolume(1.0);
                sound.play();
            }
        } catch (Exception e) {
            System.err.println("Erreur SoundManager : " + e.getMessage());
        }
    }

    public static void playMusic(String name) {
        try {
            AudioClip sound = SOUND_CACHE.computeIfAbsent(name, k -> {
                var resource = SoundManager.class.getResource("/assets/music/" + k);

                if (resource == null) {
                    throw new RuntimeException("Fichier introuvable : /assets/music/" + k);
                }

                return new AudioClip(resource.toExternalForm());
            });

            if (sound != null) {
                sound.setVolume(0.8);
                sound.play();
            }
        } catch (Exception e) {
            System.err.println("Erreur SoundManager : " + e.getMessage());
        }
    }

    public static void stopMusic() {
        com.almasb.fxgl.dsl.FXGL.getAudioPlayer().stopAllMusic();
    }
}