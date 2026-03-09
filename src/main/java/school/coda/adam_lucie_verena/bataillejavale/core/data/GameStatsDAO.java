package school.coda.adam_lucie_verena.bataillejavale.core.data;

import com.almasb.fxgl.logging.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Data Access Object (DAO) gérant la persistance des statistiques de fin de partie.
 * <p>
 * Cette classe assure la liaison entre la logique de jeu et la table {@code game_stats}
 * de PostgreSQL. Elle permet d'archiver les performances (précision, tirs, résultat)
 * pour établir des classements ultérieurs.
 * </p>
 */
public class GameStatsDAO {

    // ------------------------------------------------------------------------------------------
    // ATTRIBUTS
    // ------------------------------------------------------------------------------------------

    /** Logger officiel de FXGL pour le suivi des opérations de statistiques. */
    private static final Logger log = Logger.get(GameStatsDAO.class);
    /** Gestionnaire de connexion à la base de données. */
    private final DatabaseManager dbManager = new DatabaseManager();

    // ------------------------------------------------------------------------------------------
    // MÉTHODES DE PERSISTANCE
    // ------------------------------------------------------------------------------------------

    /**
     * Enregistre le bilan final d'une partie dans la base de données.
     * <p>
     * Utilise une requête {@code INSERT} pour sauvegarder le résultat, le volume de tirs
     * et le taux de réussite du joueur identifié.
     * </p>
     *
     * @param playerId  L'identifiant unique du joueur (clé étrangère {@code players.id}).
     * @param result    Le résultat de la partie (ex: "WIN" ou "LOSS").
     * @param shots     Nombre total de projectiles tirés.
     * @param hits      Nombre de projectiles ayant atteint un navire.
     */
    public void saveGameResult(int playerId, String result, int shots, int hits) {
        String sql = "INSERT INTO game_stats (player_id, result, shots_fired, hits_landed) VALUES (?, ?, ?, ?)";

        // Utilisation du try-with-resources pour fermer automatiquement la connexion et le statement
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playerId);
            pstmt.setString(2, result);
            pstmt.setInt(3, shots);
            pstmt.setInt(4, hits);

            pstmt.executeUpdate();
            log.info("Statistiques sauvegardées avec succès pour le joueur ID: " + playerId);

        } catch (SQLException e) {
            log.warning("Échec de la sauvegarde des statistiques SQL : " + e.getMessage());
        } catch (Exception e) {
            log.fatal("Erreur système inattendue lors de l'accès aux statistiques", e);
        }
    }
}