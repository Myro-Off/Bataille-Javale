package school.coda.adam_lucie_verena.bataillejavale.core.data;

import com.almasb.fxgl.logging.Logger;
import java.sql.*;

/**
 * DAO pour la gestion des joueurs.
 */
public class PlayerDAO {

    private static final Logger log = Logger.get(PlayerDAO.class);

    /**
     * Récupère l'ID d'un joueur par son nom.
     * S'il n'existe pas, il le crée automatiquement.
     */
    public int getOrCreatePlayer(String playerName) {
        String selectSql = "SELECT id FROM players WHERE username = ?";
        String insertSql = "INSERT INTO players (username) VALUES (?) RETURNING id";

        try (Connection conn = DatabaseManager.getConnection()) {

            // 1. Tentative de récupération
            try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                pstmt.setString(1, playerName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    }
                }
            }

            // 2. Création si non trouvé
            log.info("⚓ Nouveau joueur détecté : " + playerName + ". Création du profil...");
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, playerName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    }
                }
            }

        } catch (SQLException e) {
            log.warning("🚩 Erreur SQL lors du GetOrCreatePlayer : " + e.getMessage());
        }
        return -1;
    }
}