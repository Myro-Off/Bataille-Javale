package school.coda.adam_lucie_verena.bataillejavale.controller;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import school.coda.adam_lucie_verena.bataillejavale.core.engine.BattleEngine;
import school.coda.adam_lucie_verena.bataillejavale.core.model.*;
import school.coda.adam_lucie_verena.bataillejavale.view.grid.GameView;
import school.coda.adam_lucie_verena.bataillejavale.view.scene.CombatView;
import school.coda.adam_lucie_verena.bataillejavale.view.vfx.CombatFeedbackManager;
import school.coda.adam_lucie_verena.bataillejavale.view.vfx.VfxManager;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Le chef d'orchestre : il lie le Core et la View.
 * Il gère le timing (pauses) et déclenche les retours visuels/sonores.
 */
public class CombatOrchestrator {

    private final BattleEngine controller;
    private final Board playerBoard, enemyBoard;
    private final GameView playerView, enemyView;
    private final CombatView combatView;
    private final CombatFeedbackManager feedback;

    public CombatOrchestrator(BattleEngine controller, Board pBoard, Board eBoard,
                              GameView pView, GameView eView, CombatView combatView) {
        this.controller = controller;
        this.playerBoard = pBoard;
        this.enemyBoard = eBoard;
        this.playerView = pView;
        this.enemyView = eView;
        this.combatView = combatView;
        this.feedback = new CombatFeedbackManager(combatView.getGameLog());
    }

    public void handlePlayerShot(Coordinate target) {
        if (controller.getCurrentState() != GameState.PLAYER_TURN) return;
        if (enemyBoard.isAlreadyShot(target)) return;

        List<Ship> sunkBefore = enemyBoard.getShips().stream().filter(Ship::isSunk).collect(Collectors.toList());
        boolean hit = controller.handlePlayerShot(target);

        // Mise à jour visuelle
        enemyView.updateDisplay();
        VfxManager.playShotEffect(enemyView, target, hit);

        if (hit) feedback.logHit("COMMANDANT", target);
        else feedback.logMiss("COMMANDANT");

        checkSunkFeedback(enemyBoard, sunkBefore, "ENNEMIE");

        if (controller.getCurrentState() == GameState.AI_TURN) {
            combatView.updateTurnInfo(false);
            triggerAIReprisal();
        }
    }

    private void triggerAIReprisal() {
        PauseTransition pause = new PauseTransition(Duration.millis(800));
        pause.setOnFinished(_ -> {
            List<Ship> sunkBefore = playerBoard.getShips().stream().filter(Ship::isSunk).collect(Collectors.toList());

            // On récupère la coordonnée directement depuis le controller
            Coordinate target = controller.aiTurn();
            if (target == null) return;

            boolean hit = playerBoard.getHitShots().contains(target);

            if (hit) feedback.logHit("ENNEMI", target);
            else feedback.logMiss("ENNEMI");

            playerView.updateDisplay();
            VfxManager.playShotEffect(playerView, target, hit);
            checkSunkFeedback(playerBoard, sunkBefore, "ALLIÉE");

            if (controller.getCurrentState() == GameState.PLAYER_TURN) {
                combatView.updateTurnInfo(true);
                combatView.getFleetStatus().update();
            }
        });
        pause.play();
    }

    private void checkSunkFeedback(Board board, List<Ship> previouslySunk, String team) {
        for (Ship s : board.getShips()) {
            if (s.isSunk() && !previouslySunk.contains(s)) {
                feedback.logSunk(team, s);
            }
        }
    }
}