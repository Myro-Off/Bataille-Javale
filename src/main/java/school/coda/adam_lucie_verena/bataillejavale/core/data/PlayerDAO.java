package school.coda.adam_lucie_verena.bataillejavale.core.data;

import java.sql.*;

public class PlayerDAO {
    private final DatabaseManager dbManager;

    public PlayerDAO() {
        this.dbManager = new DatabaseManager();
    }

    /**
     * Récupère l'ID d'un joueur par son pseudo.
     * S'il n'existe pas, il le crée automatiquement.
     */
    public int getOrCreatePlayer(String username) {
        String selectSql = "SELECT id FROM players WHERE username = ?";
        String insertSql = "INSERT INTO players (username) VALUES (?) RETURNING id";

        try (Connection conn = dbManager.getConnection()) {
            // 1. On cherche si le joueur existe déjà
            try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) return rs.getInt("id");
            }

            // 2. S'il n'existe pas, on le crée
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) return rs.getInt("id");
            }

        } catch (Exception e) {
            System.err.println("Erreur Database : " + e.getMessage());
        }
        return -1; // En cas d'erreur fatale
    }
}