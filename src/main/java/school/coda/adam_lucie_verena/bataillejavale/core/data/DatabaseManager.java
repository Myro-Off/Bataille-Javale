package school.coda.adam_lucie_verena.bataillejavale.core.data;

import com.almasb.fxgl.logging.Logger;
import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestionnaire central de la connectivité à la base de données PostgreSQL.
 * <p>
 * Cette classe utilise la bibliothèque {@link Dotenv} pour charger les identifiants
 * de connexion depuis un fichier {@code .env} externe, garantissant la sécurité
 * des informations sensibles (URL, utilisateur, mot de passe).
 * </p>
 */
public class DatabaseManager {

    // ------------------------------------------------------------------------------------------
    // CONSTANTES ET CONFIGURATION
    // ------------------------------------------------------------------------------------------

    /** Logger officiel pour le suivi des opérations de base de données. */
    private static final Logger log = Logger.get(DatabaseManager.class);
    /** Chargeur de variables d'environnement. */
    private static final Dotenv dotenv = Dotenv.load();
    /** Point d'accès (JDBC URL) à la base de données PostgreSQL. */
    private static final String URL = dotenv.get("DB_URL");
    /** Identifiant de l'utilisateur de la base de données. */
    private static final String USER = dotenv.get("DB_USER");
    /** Mot de passe associé à l'utilisateur. */
    private static final String PASS = dotenv.get("DB_PASS");

    // ------------------------------------------------------------------------------------------
    // GESTION DE LA CONNEXION
    // ------------------------------------------------------------------------------------------

    /**
     * Établit et retourne une nouvelle connexion active à PostgreSQL.
     * <p>
     * Cette méthode s'assure du chargement du driver JDBC avant de tenter
     * l'authentification auprès du serveur.
     * </p>
     *
     * @return Une instance de {@link Connection} active.
     * @throws SQLException Si l'URL est incorrecte ou si l'accès est refusé.
     */
    public Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            log.fatal("Driver PostgreSQL (JAR) introuvable dans le classpath !");
            throw new SQLException("Impossible de charger le driver JDBC", e);
        }
    }

    /**
     * Vérifie la viabilité de la connexion (Utile lors du démarrage de l'application).
     * <p>
     * En cas de succès, un log de niveau INFO est généré. En cas d'échec,
     * un avertissement est envoyé au logger.
     * </p>
     */
    public void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                log.info("Connexion établie avec succès à la base de données : " + URL);
            }
        } catch (SQLException e) {
            log.warning("Échec de la connexion test à la base de données : " + e.getMessage());
        }
    }
}