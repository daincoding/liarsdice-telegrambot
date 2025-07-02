package game;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    // 📝 This Class knows about the Players, Turns and remembers the current bit
    // Checks if new calls are legal and tells whose turn is next
    // It also calculates how many dice of a given face value exist across all players.

    //region 🧱Variables

    private List<Player> players;
    private int currentPlayerIndex;

    private int currentQuantityCalled;
    private int currentFaceValueCalled;

    //endregion

    //region 🛠️ Constructor

    public GameState(List<Player> players) {
        if  (players == null || players.size() < 2) {
            throw new IllegalArgumentException("There must  be at least 2 players to play.");
        }
        this.players = new ArrayList<>(players);
        this.currentPlayerIndex = 0;
        this.currentQuantityCalled = 0;
        this.currentFaceValueCalled = 0;
    }

    //endregion

    //region ⚙️ Methods

    // -- Checks the current Player Turn
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public void advanceTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    // -- Sets the current Call/Bid of the Player as the one to go above
    public void setCurrentCall(int quantity, int faceValue) {
        this.currentQuantityCalled = quantity;
        this.currentFaceValueCalled = faceValue;
    }

    // -- Check if the proposed call is higher than the current call
    public boolean isCallHigher(int quantity, int faceValue) {
        if (quantity > currentQuantityCalled) {
            return true;
        } else if (quantity == currentQuantityCalled && faceValue > currentFaceValueCalled) {
            return true;
        } else {
            return false;
        }
    }

    public int getTotalDiceForFace(int faceValue) {
        int count  = 0;
        for (Player player : players) {
            List<Integer> dice = player.revealDice();
            for (Integer die : dice) {
                if (die == faceValue) {
                    count++;
                }
            }
        }
        return count;
    }

    public void removeEliminatedPlayers() {
        players.removeIf(Player::isEliminated);
        if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }
    }

    public boolean isGameOver() {
        return players.size() == 1;
    }

    public Player getWinner() {
        if (isGameOver()) {
            return players.getFirst();
        }
        return null;
    }

    public int getTotalDiceCount() {
        int sum = 0;
        for (Player player : players) {
            sum += player.getDiceCount();
        }
        return sum;
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public int getCurrentQuantityCalled() {
        return currentQuantityCalled;
    }

    public int getCurrentFaceValueCalled() {
        return currentFaceValueCalled;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    //endregion

}
