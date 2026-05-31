package school.coda.adam_lucie_verena.bataillejavale.controller;

import javafx.animation.PauseTransition;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import school.coda.adam_lucie_verena.bataillejavale.core.engine.BattleEngine;
import school.coda.adam_lucie_verena.bataillejavale.core.model.GameState;
import school.coda.adam_lucie_verena.bataillejavale.core.events.RandomEventType;
import school.coda.adam_lucie_verena.bataillejavale.core.model.*;
import school.coda.adam_lucie_verena.bataillejavale.gui.Theme;
import school.coda.adam_lucie_verena.bataillejavale.gui.component.NotificationView;
import school.coda.adam_lucie_verena.bataillejavale.gui.grid.GameView;
import school.coda.adam_lucie_verena.bataillejavale.gui.scene.CombatView;
import school.coda.adam_lucie_verena.bataillejavale.gui.vfx.CombatFeedbackManager;
import school.coda.adam_lucie_verena.bataillejavale.gui.vfx.VfxManager;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrateur pilotant les interactions entre le moteur de jeu et l'interface graphique.
 * Gère le séquençage des tirs (salves), les effets visuels de guerre électronique (brouillage)
 * et la mise à jour des informations de combat en temps réel.
 */
public class CombatOrchestrator {

    // 👍 private final
    private final BattleEngine controller;
    private final Board playerBoard, enemyBoard;
    private final GameView playerView, enemyView;
    private final CombatView combatView;
    private final CombatFeedbackManager feedback;
    private final NotificationView notificationView;

    // 👍 inversion de contrôle par constructeur

    /**
     * Initialise l'orchestrateur et lie les événements du moteur à l'interface.
     */
    public CombatOrchestrator(BattleEngine controller, Board pBoard, Board eBoard,
                              GameView pView, GameView eView, CombatView combatView,
                              NotificationView notificationView) {
        this.controller = controller;
        this.playerBoard = pBoard; // 👈 pas besoin d'abréger les noms d'arguments
        this.enemyBoard = eBoard; // 👈 pas besoin d'abréger les noms d'arguments
        this.playerView = pView; // 👈 pas besoin d'abréger les noms d'arguments
        this.enemyView = eView; // 👈 pas besoin d'abréger les noms d'arguments
        this.combatView = combatView;
        this.notificationView = notificationView;
        this.feedback = new CombatFeedbackManager(combatView.getGameLog());

        this.controller.getEventManager().setOnEventTriggered(this::handleRandomEvent);
        this.controller.getEventManager().setOnEventEnded(this::handleEventEnded);

        feedback.logRound(controller.getRoundNumber());
        updateUI();
    }

    /**
     * Traite le déclenchement d'un événement aléatoire et applique les effets visuels associés.
     */
    private void handleRandomEvent(RandomEventType type) {
        this.notificationView.showEvent(type);
        feedback.logEvent("☢ " + type.getTitle().toUpperCase(), Color.web(getHexForType(type)));

        if (type == RandomEventType.APOCALYPSE) {
            combatView.activateApocalypseTheme();
            playerView.applyApocalypseEffect();
            enemyView.applyApocalypseEffect();
        }

        if (type == RandomEventType.BROUILLAGE) {
            // 🚨 Demeter law / 💩 Feature envy
            // Ex. combatView.blurLog();
            combatView.getGameLog().setEffect(new GaussianBlur(12));
        }

        playerView.updateDisplay();
        enemyView.updateDisplay();

        if (type == RandomEventType.METEORES) {
            controller.getLastPlayerMeteors().forEach(c -> VfxManager.playShotEffect(playerView, c, true));
        }
    }

    /**
     * Nettoie les effets visuels lorsque l'événement aléatoire prend fin.
     */
    private void handleEventEnded(RandomEventType type) {
        this.notificationView.hideEvent(type);
        if (type == RandomEventType.BROUILLAGE) {
            // 🚨 Demeter law / 💩 Feature envy
            // Ex. combatView.unBlurLog();
            combatView.getGameLog().setEffect(null);
        }
        playerView.updateDisplay();
        enemyView.updateDisplay();
    }

    /**
     * Gère la logique de tir du joueur, incluant la gestion du mode Salve.
     */
    public void handlePlayerShot(Coordinate target) {
        if (target == null || controller.getCurrentState() != GameState.PLAYER_TURN || enemyBoard.isAlreadyShot(target)) return;

        // 🚨 Demeter law : enemyBoard.getSunkShips();
        // 💡 Formater les streams ligne par ligne peut aider à la lecture
        List<Ship> sunkBefore = enemyBoard.getShips().stream()
                .filter(Ship::isSunk)
                .collect(Collectors.toList());

        boolean hit = controller.handlePlayerShot(target);
        // 🚨 getNewlySunkShip pourrait retourner un optional
        // Optional<Ship> newlySunk
        Ship newlySunk = getNewlySunkShip(enemyBoard, sunkBefore);

        enemyView.updateDisplay();

        if (!enemyBoard.isFogActive()) {
            VfxManager.playShotEffect(enemyView, target, hit);
        }

        if (newlySunk != null) {
            VfxManager.playSunkEffect(enemyView, newlySunk);
            feedback.logSunk("ENNEMIE", newlySunk);
            combatView.showCombatNotification("NAVIRE ENNEMI COULÉ !", Theme.CYAN, true);
        } else if (hit) {
            feedback.logHit("CMD", target);
            combatView.showCombatNotification("IMPACT CONFIRMÉ", Theme.CYAN, false);
        } else {
            feedback.logMiss("CMD", target);
        }

        updateUI();

        if (controller.getCurrentState() == GameState.AI_TURN) {
            combatView.updateTurnInfo(false);
            triggerAIReprisal();
        } else if (controller.getCurrentState() != GameState.GAME_OVER) {
            int remaining = controller.getShotsAllowed() - controller.getShotsFiredThisTurn();
            combatView.showCombatNotification("SALVE : " + remaining + " TIRS RESTANTS", Theme.CYAN, false);
        } else {
            cleanupEndGame();
        }
    }

    /**
     * Déclenche une riposte automatique de l'IA, capable d'enchaîner plusieurs tirs en mode Salve.
     */
    private void triggerAIReprisal() {
        if (controller.getCurrentState() != GameState.AI_TURN) return;

        PauseTransition pause = new PauseTransition(Duration.millis(800));
        pause.setOnFinished(_ -> {
            // 🚨 Demeter law
            // 💩 Code smell : Feature envy
            // Voir : https://refactoring.guru/fr/smells/feature-envy
            // playerBoard.getSunkShips();
            // 💡 Formater les streams ligne par ligne peut aider à la lecture
            List<Ship> sunkBefore = playerBoard.getShips().stream()
                    .filter(Ship::isSunk)
                    .collect(Collectors.toList());
            // 🚨 controller.aiTurn(); pourrait retourner un optional
            Coordinate target = controller.aiTurn();

            if (target != null) {
                // 🚨 Demeter
                // 💩 Code smell : Feature envy
                // playerBoard.wasShotAt(target);
                boolean hit = playerBoard.getHitShots().contains(target);

                // 💩 Code smell : Feature envy
                // Ship newlySunk = playerBoard.getNewlySunkShip(sunkBefore);
                Ship newlySunk = getNewlySunkShip(playerBoard, sunkBefore);

                playerView.updateDisplay();
                VfxManager.playShotEffect(playerView, target, hit);

                if (newlySunk != null) {
                    VfxManager.playSunkEffect(playerView, newlySunk);
                    feedback.logSunk("ALLIÉE", newlySunk);
                    combatView.showCombatNotification("UNITÉ ALLIÉE PERDUE !", Theme.RED_ALERTE, true);
                } else if (hit) {
                    feedback.logHit("CPU", target);
                    combatView.showCombatNotification("NAVIRE ALLIÉ TOUCHÉ !", Theme.RED_ALERTE, false);
                } else {
                    feedback.logMiss("CPU", target);
                }
            }

            if (controller.getCurrentState() == GameState.AI_TURN) {
                triggerAIReprisal(); // On relance pour le tir suivant de la salve
            } else if (controller.getCurrentState() == GameState.GAME_OVER) {
                cleanupEndGame();
            } else {
                checkRoundTransition();
            }
            updateUI();
        });
        pause.play();
    }

    /**
     * Désactive les effets de brouillage et nettoie les notifications lors de la fin de partie.
     */
    private void cleanupEndGame() {
        // 🚨 Demeter law / 💩 Feature envy
        // Ex. combatView.unBlurLog();
        combatView.getGameLog().setEffect(null);
        notificationView.clearEvents();
        playerView.updateDisplay();
        enemyView.updateDisplay();
    }

    private void checkRoundTransition() {
        if (controller.getCurrentState() == GameState.PLAYER_TURN) {
            feedback.logRound(controller.getRoundNumber());
            combatView.updateTurnInfo(true);
        }
    }

    private void updateUI() {
        combatView.getEnemyFleet().update();
        combatView.getPlayerFleet().update();
        combatView.updateHUD(
                controller.getRoundNumber(),
                controller.getTotalPlayerShots(), controller.getTotalPlayerHits(), controller.getPlayerAccuracy(),
                controller.getTotalEnemyShots(), controller.getTotalEnemyHits(), controller.getEnemyAccuracy()
        );
    }

    // 🚨 Pourrait retourner un Optional<Ship>
    // 🚨 Pourrait être déplacée dans la classe Board
    private Ship getNewlySunkShip(Board board, List<Ship> previouslySunk) {
        return board.getShips().stream()
                .filter(s -> s.isSunk() && !previouslySunk.contains(s))
                .findFirst().orElse(null);
    }

    private String getHexForType(RandomEventType type) {
        return switch (type) {
            case APOCALYPSE, METEORES -> "#e74c3c";
            case RAVITAILLEMENT_GRATUIT, JOURNEE_ENSOLEILLEE -> "#2ecc71";
            default -> "#f39c12";
        };
    }
}