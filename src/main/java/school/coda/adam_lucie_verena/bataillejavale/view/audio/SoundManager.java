package school.coda.adam_lucie_verena.bataillejavale.view.audio;

import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.media.AudioClip;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private static final Map<String, AudioClip> SOUND_CACHE = new HashMap<>();

    public static void playSFX(String name) {
        try {
            AudioClip sound = SOUND_CACHE.computeIfAbsent(name, k -> {
                // On cherche le fichier dans le dossier resources/assets/sounds/
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
                // On cherche le fichier dans le dossier resources/assets/sounds/
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
        // Pour la musique (fichiers longs), on utilise généralement FXGL directement
        //FXGL.loopBGM(name);
    }

    public static void stopMusic() {
        // FXGL.getAudioPlayer() permet d'accéder aux contrôles globaux
        com.almasb.fxgl.dsl.FXGL.getAudioPlayer().stopAllMusic();
    }
}