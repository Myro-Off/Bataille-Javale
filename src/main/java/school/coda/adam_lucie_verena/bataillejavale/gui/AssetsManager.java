package school.coda.adam_lucie_verena.bataillejavale.gui;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.audio.Music;
import com.almasb.fxgl.texture.Texture;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire audio centralisé pour la Bataille-Javale.
 * Gère le cache des textures, le mixage des bruitages et de la musique
 * en appliquant un coefficient Master (Slider) sur les volumes locaux (Code).
 */
// 💡 La classe ne contient que des attributs et méthodes statiques.
// Elle peut être rendue final
public class AssetsManager {

    private static Music currentMusic;
    private static final Map<String, Texture> TEXTURE_CACHE = new HashMap<>();

    private static boolean musicMuted = false;
    private static boolean sfxMuted = false;

    private static double masterMusicVol = 0.3;
    private static double masterSfxVol = 0.5;

    /** Stocke le volume local de la musique actuelle pour recalculer le mixage lors des changements de slider. */
    private static double currentLocalMusicVol = 1.0;

    /**
     * Charge ou récupère une texture depuis le cache.
     * @param name Nom du fichier image.
     * @return L'objet Texture correspondant.
     */
    public static Texture loadTexture(String name) {
        return TEXTURE_CACHE.computeIfAbsent(name, k -> FXGL.getAssetLoader().loadTexture(k));
    }

    /**
     * Joue un effet sonore ponctuel.
     * Le volume final est le produit du Master SFX et du volume local spécifié.
     * @param name Nom du fichier audio.
     * @param localVolume Intensité propre à l'action (0.0 à 1.0).
     */
    public static void playSFX(String name, double localVolume) {
        double finalVol = sfxMuted ? 0 : (masterSfxVol * localVolume);
        FXGL.getSettings().setGlobalSoundVolume(finalVol);
        FXGL.play(name);
    }

    /**
     * Définit le niveau de volume principal pour les bruitages.
     * @param volume Niveau Master SFX (0.0 à 1.0).
     */
    public static void setSFXVolume(double volume) {
        masterSfxVol = volume;
        if (volume > 0) sfxMuted = false;
        FXGL.getSettings().setGlobalSoundVolume(sfxMuted ? 0 : masterSfxVol);
    }

    /**
     * Alterne l'état muet des bruitages.
     * @return True si les bruitages sont désormais coupés.
     */
    public static boolean toggleSFXMute() {
        sfxMuted = !sfxMuted;
        FXGL.getSettings().setGlobalSoundVolume(sfxMuted ? 0 : masterSfxVol);
        return sfxMuted;
    }

    /**
     * Lance une musique d'ambiance en boucle.
     * Mémorise le volume local pour assurer la cohérence du mixage avec le slider.
     * @param name Nom du fichier musical.
     * @param localVolume Intensité souhaitée pour ce morceau (0.0 à 1.0).
     */
    public static void playMusic(String name, double localVolume) {
        stopMusic();
        currentLocalMusicVol = localVolume;
        currentMusic = FXGL.getAssetLoader().loadMusic(name);

        double finalVol = musicMuted ? 0 : (masterMusicVol * currentLocalMusicVol);
        FXGL.getSettings().setGlobalMusicVolume(finalVol);

        FXGL.getAudioPlayer().loopMusic(currentMusic);
    }

    /**
     * Arrête la musique en cours de lecture.
     */
    public static void stopMusic() {
        if (currentMusic != null) {
            FXGL.getAudioPlayer().stopMusic(currentMusic);
            currentMusic = null;
        }
    }

    /**
     * Définit le niveau de volume principal de la musique.
     * Recalcule instantanément le volume global en respectant le volume local du morceau actuel.
     * @param volume Niveau Master Musique (0.0 à 1.0).
     */
    public static void setMusicVolume(double volume) {
        masterMusicVol = volume;
        if (volume > 0) musicMuted = false;

        double finalVol = musicMuted ? 0 : (masterMusicVol * currentLocalMusicVol);
        FXGL.getSettings().setGlobalMusicVolume(finalVol);
    }

    /**
     * Alterne l'état muet de la musique.
     * @return True si la musique est désormais coupée.
     */
    public static boolean toggleMusicMute() {
        musicMuted = !musicMuted;
        double finalVol = musicMuted ? 0 : (masterMusicVol * currentLocalMusicVol);
        FXGL.getSettings().setGlobalMusicVolume(finalVol);
        return musicMuted;
    }

    /** @return True si la musique est coupée. */
    public static boolean isMusicMuted() { return musicMuted; }

    /** @return True si les bruitages sont coupés. */
    public static boolean isSfxMuted() { return sfxMuted; }

    /** @return Le volume Master de la musique. */
    public static double getMusicVolume() { return masterMusicVol; }

    /** @return Le volume Master des bruitages. */
    public static double getSfxVolume() { return masterSfxVol; }
}