package school.coda.adam_lucie_verena.bataillejavale.core.data;

import com.almasb.fxgl.logging.Logger;
import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// 🚨 Code pas encore utilisé
/**
 * Gestionnaire de connectivité PostgreSQL.
 */
public class DatabaseManager {

    private static final Logger log = Logger.get(DatabaseManager.class);
    private static final Dotenv dotenv;

    // Variables de configuration
    private static final String URL;
    private static final String USER;
    private static final String PASS;

    static {
        // Chargement sécurisé du .env
        dotenv = Dotenv.configure().ignoreIfMissing().load();

        // Récupération des valeurs avec des valeurs par défaut pour le Docker
        URL = dotenv.get("DB_URL", "jdbc:postgresql://localhost:5432/bataille_javale");
        USER = dotenv.get("DB_USER", "postgres");
        PASS = dotenv.get("DB_PASS", "AVerySecurePassword");
    }

    /**
     * @return Une connexion active.
     */
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            log.fatal("Erreur de connexion SQL : " + e.getMessage());
            throw e;
        }
    }

    /**
     * Teste la connexion au démarrage.
     */
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                log.info("⚓ Connexion validée vers : " + URL);
            }
        } catch (SQLException e) {
            log.warning("🚩 Base de données injoignable. Vérifiez Docker !");
        }
    }
}