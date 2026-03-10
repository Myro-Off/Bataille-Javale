package school.coda.adam_lucie_verena.bataillejavale.core.data;

import com.almasb.fxgl.logging.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GameStatsDAO {

    private static final Logger log = Logger.get(GameStatsDAO.class);

    public void saveGameResult(int playerId, String result, int shots, int hits) {
        String sql = "INSERT INTO game_stats (player_id, result, shots_fired, hits_landed) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playerId);
            pstmt.setString(2, result);
            pstmt.setInt(3, shots);
            pstmt.setInt(4, hits);

            pstmt.executeUpdate();
            log.info("⚓ Stats enregistrées en base pour le joueur " + playerId);

        } catch (SQLException e) {
            log.warning("🚩 Erreur SQL : " + e.getMessage());
        } catch (Exception e) {
            log.fatal("💥 Erreur système critique : ", e);
        }
    }
}