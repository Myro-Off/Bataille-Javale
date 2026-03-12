package school.coda.adam_lucie_verena.bataillejavale.view.audio;
/**
 * Gestionnaire audio centralisé pour Bataille-Javale.
 * =========================================================================================
 * 🔊 TODO : ARCHITECTURE SONORE & RÉGLAGES (SFX / BGM)
 * =========================================================================================
 * OBJECTIF : Finaliser le moteur audio et intégrer la gestion du silence (Mute).
 * ÉTAPES :
 * 1. Ressources : Déposer les .wav (SFX) dans 'assets/sounds' et les .mp3 (BGM) dans 'assets/music'.
 * 2. Logique Mute : Vérifier 'GameConfig.isMuted()' avant de lancer un son.
 * 3. Musiques : Créer des méthodes pour boucler sur les thèmes du menu et du combat.
 * 4. Volume : Configurer le volume global via les réglages de FXGL.
 * =========================================================================================
 */
public class SoundManager {
    /**
     * @param name Nom du fichier sonore.
     */
    public static void playSFX(String name) {}
    /**
     * @param name Nom de la piste musicale.
     */
    public static void playMusic(String name) {}
}