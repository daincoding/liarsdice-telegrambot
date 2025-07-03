package telegram;

import game.GameState;
import game.RoundLogic;

public class GameSession {

    private GameState gameState;
    private RoundLogic roundLogic;

    public GameSession(GameState gameState, RoundLogic roundLogic) {
        this.gameState = gameState;
        this.roundLogic = roundLogic;
    }

    public GameState getGameState() {
        return gameState;
    }

    public RoundLogic getRoundLogic() {
        return roundLogic;
    }
}