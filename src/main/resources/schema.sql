-- Suppression des tables si nécessaire
-- DROP TABLE IF EXISTS game_stats;
-- DROP TABLE IF EXISTS players;

-- 1. Création de la table des Joueurs
CREATE TABLE IF NOT EXISTS players (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Création de la table des Statistiques de parties
CREATE TABLE IF NOT EXISTS game_stats (
    id SERIAL PRIMARY KEY,
    player_id INT NOT NULL,
    result VARCHAR(10) CHECK (result IN ('WIN', 'LOSS')),
    shots_fired INT DEFAULT 0,
    hits_landed INT DEFAULT 0,
    game_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Lien avec la table players
    CONSTRAINT fk_player
    FOREIGN KEY(player_id)
    REFERENCES players(id)
    ON DELETE CASCADE
);

-- Index pour accélérer les recherches par nom de joueur
CREATE INDEX IF NOT EXISTS idx_player_username ON players(username);