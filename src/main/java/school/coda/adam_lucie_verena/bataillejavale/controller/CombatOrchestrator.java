package school.coda.adam_lucie_verena.bataillejavale.controller;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import school.coda.adam_lucie_verena.bataillejavale.core.engine.BattleEngine;
import school.coda.adam_lucie_verena.bataillejavale.core.model.*;
import school.coda.adam_lucie_verena.bataillejavale.gui.Theme;
import school.coda.adam_lucie_verena.bataillejavale.gui.grid.GameView;
import school.coda.adam_lucie_verena.bataillejavale.gui.scene.CombatView;
import school.coda.adam_lucie_verena.bataillejavale.gui.vfx.CombatFeedbackManager;
import school.coda.adam_lucie_verena.bataillejavale.gui.vfx.VfxManager;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrateur principal du combat naval.
 * Cette classe assure la liaison entre le moteur de jeu (logique métier) et les vues (interface graphique).
 * Elle gère le séquençage des tours, les feedbacks sonores/visuels et la mise à jour du HUD.
 */
public class CombatOrchestrator {

    private final BattleEngine controller;
    private final Board playerBoard, enemyBoard;
    private final GameView playerView, enemyView;
    private final CombatView combatView;
    private final CombatFeedbackManager feedback;

    /**
     * Initialise l'orchestrateur avec tous les composants nécessaires au combat.
     * * @param controller Le moteur logique de la bataille.
     * @param pBoard Le plateau de données du joueur.
     * @param eBoard Le plateau de données de l'intelligence artificielle.
     * @param pView La vue graphique de la grille du joueur.
     * @param eView La vue graphique de la grille ennemie.
     * @param combatView La scène globale de combat.
     */
    public CombatOrchestrator(BattleEngine controller, Board pBoard, Board eBoard,
                              GameView pView, GameView eView, CombatView combatView) {
        this.controller = controller;
        this.playerBoard = pBoard;
        this.enemyBoard = eBoard;
        this.playerView = pView;
        this.enemyView = eView;
        this.combatView = combatView;
        this.feedback = new CombatFeedbackManager(combatView.getGameLog());

        feedback.logRound(controller.getRoundNumber());
        updateUI();
    }

    /**
     * Traite l'intention de tir du joueur sur une coordonnée cible.
     * Gère la vérification du tour, l'impact visuel, la détection des navires coulés
     * et la transition vers le tour de l'intelligence artificielle.
     * * @param target La coordonnée logique ciblée par le joueur.
     */
    public void handlePlayerShot(Coordinate target) {
        if (target == null || controller.getCurrentState() != GameState.PLAYER_TURN || enemyBoard.isAlreadyShot(target)) return;

        List<Ship> sunkBefore = enemyBoard.getShips().stream().filter(Ship::isSunk).collect(Collectors.toList());
        boolean hit = controller.handlePlayerShot(target);
        Ship newlySunk = getNewlySunkShip(enemyBoard, sunkBefore);

        enemyView.updateDisplay();
        VfxManager.playShotEffect(enemyView, target, hit);

        if (newlySunk != null) {
            VfxManager.playSunkEffect(enemyView, newlySunk);
            feedback.logSunk("ENNEMIE", newlySunk);
            combatView.showCombatNotification("NAVIRÉ ENNEMI COULÉ !", Theme.CYAN, true);
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
        }
    }

    /**
     * Déclenche la riposte de l'intelligence artificielle après un court délai de simulation.
     * Gère l'application des dégâts sur le plateau du joueur et réinitialise le tour de jeu.
     */
    private void triggerAIReprisal() {
        PauseTransition pause = new PauseTransition(Duration.millis(1000));
        pause.setOnFinished(_ -> {
            List<Ship> sunkBefore = playerBoard.getShips().stream().filter(Ship::isSunk).collect(Collectors.toList());
            Coordinate target = controller.aiTurn();
            if (target == null) return;

            boolean hit = playerBoard.getHitShots().contains(target);
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

            if (controller.getCurrentState() == GameState.PLAYER_TURN) {
                controller.nextRound();
                feedback.logRound(controller.getRoundNumber());
                combatView.updateTurnInfo(true);
            }
            updateUI();
        });
        pause.play();
    }

    /**
     * Actualise l'ensemble des éléments de l'interface utilisateur.
     * Met à jour l'état visuel des flottes et les statistiques de précision dans le HUD.
     */
    private void updateUI() {
        combatView.getEnemyFleet().update();
        combatView.getPlayerFleet().update();
        combatView.updateHUD(
                controller.getRoundNumber(),
                controller.getTotalPlayerShots(), controller.getTotalPlayerHits(), controller.getPlayerAccuracy(),
                controller.getTotalEnemyShots(), controller.getTotalEnemyHits(), controller.getEnemyAccuracy()
        );
    }

    /**
     * Identifie si un navire vient d'être coulé suite au dernier tir effectué.
     * * @param board Le plateau sur lequel vérifier l'état des navires.
     * @param previouslySunk La liste des navires qui étaient déjà coulés avant le tir.
     * @return Le navire nouvellement coulé, ou null si aucun nouveau navire n'est détruit.
     */
    private Ship getNewlySunkShip(Board board, List<Ship> previouslySunk) {
        return board.getShips().stream()
                .filter(s -> s.isSunk() && !previouslySunk.contains(s))
                .findFirst().orElse(null);
    }
}