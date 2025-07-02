package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RoundLogic {

    //region 🧱 Variables

    private GameState gameState;
    private boolean roundEnded;

    //endregion

    //region 🛠️ Constructor

    public RoundLogic(GameState gameState) {
        this.gameState = gameState;
        this.roundEnded = false;
    }

    //endregion

    //region ⚙️ Methods

    /*
     * Play a single round of Liars Dice.
     * (CLI prototype version for now.)
     */

    public void playRound() {
        Scanner scanner = new Scanner(System.in);

        // Roll dice for everyone
        for (Player player : gameState.getPlayers()) {
            player.rollAllDice();
            System.out.println(player.getName() + " rolled: " + player.revealDice());
        }

        while (!roundEnded) {
            Player currentPlayer = gameState.getCurrentPlayer();

            System.out.println("\n--- " + currentPlayer.getName() + "'s turn ---");
            System.out.println("Current call: "
                    + gameState.getCurrentQuantityCalled()
                    + " x " + gameState.getCurrentFaceValueCalled());
            System.out.println("Your dice: " + currentPlayer.revealDice());

            if (!currentPlayer.hasUsedReroll()) {
                System.out.println("Enter your move (e.g. '3 4' for call, 'reroll 0 2', or 'lie'):");
            } else {
                System.out.println("Enter your move (e.g. '3 4' for call, or 'lie'):");
            }

            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("lie")) {
                if (gameState.getCurrentQuantityCalled() == 0) {
                    System.out.println("⚠️ You can't call a lie before any call has been made.");
                    continue;
                }
                resolveLie();
                roundEnded = true;
            } else if (input.startsWith("reroll")) {
                if (currentPlayer.hasUsedReroll()) {
                    System.out.println("⚠️ You already used your reroll this match.");
                } else {
                    handleReroll(currentPlayer, input);
                    currentPlayer.useReroll();
                }
            } else {
                handleNewCall(input, currentPlayer);
            }
        }
    }

    /**
     * Handle rerolling dice for a player.
     */
    private void handleReroll(Player player, String input) {
        String[] parts = input.split(" ");
        List<Integer> indices = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            indices.add(Integer.parseInt(parts[i]));
        }

        player.rerollSelectedDice(indices);
        System.out.println("New dice after reroll: " + player.revealDice());

        System.out.println("Now you must make a higher call (e.g. '3 4'):");

        Scanner scanner = new Scanner(System.in);
        boolean validCall = false;

        while (!validCall) {
            String call = scanner.nextLine().trim();

            if (call.equalsIgnoreCase("lie")) {
                System.out.println("⚠️ You cannot call 'lie' after a reroll. You must make a numeric call.");
                continue;
            }

            String[] partsCall = call.split(" ");
            if (partsCall.length == 2) {
                try {
                    int quantity = Integer.parseInt(partsCall[0]);
                    int faceValue = Integer.parseInt(partsCall[1]);

                    if (quantity > 0 && faceValue >= 1 && faceValue <= 6) {
                        handleNewCall(call, player);
                        validCall = true;
                    } else {
                        System.out.println("⚠️ Invalid call values. Must be > 0 and face value between 1-6.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Invalid input. Please enter two numbers.");
                }
            } else {
                System.out.println("⚠️ You must enter two numbers like '3 4'.");
            }
        }
    }

    /**
     * Handle making a new call.
     */
    private void handleNewCall(String input, Player currentPlayer) {
        try {
            String[] parts = input.split(" ");
            int quantity = Integer.parseInt(parts[0]);
            int faceValue = Integer.parseInt(parts[1]);

            if (quantity > gameState.getTotalDiceCount()) {
                System.out.println("⚠️ Illegal call! You can't call more dice than remain in the game.");
                return;
            }

            if (!gameState.isCallHigher(quantity, faceValue)) {
                System.out.println("⚠️ Illegal call! Must be higher than previous.");
                return;
            }

            gameState.setCurrentCall(quantity, faceValue);
            System.out.println(currentPlayer.getName()
                    + " calls " + quantity + " x " + faceValue);
            gameState.advanceTurn();
        } catch (Exception e) {
            System.out.println("⚠️ Invalid input - Must be higher than previous Call. Please try again.");
        }
    }

    /**
     * Handle accusation of "You LIE!"
     */
    public void resolveLie() {
        int quantity = gameState.getCurrentQuantityCalled();
        int faceValue = gameState.getCurrentFaceValueCalled();

        int actualCount = gameState.getTotalDiceForFace(faceValue);
        System.out.println("Total dice showing " + faceValue + ": " + actualCount);

        Player previousPlayer = getPreviousPlayer();

        // ✅ Check for instant win
        if (isMaxPossibleCall(quantity, faceValue) && actualCount == quantity) {
            System.out.println("🏆 " + previousPlayer.getName()
                    + " made the max possible call and it was TRUE!");
            System.out.println("🎉 " + previousPlayer.getName() + " wins the entire match!");

            // Keep only the winner in the GameState
            List<Player> winnerList = new ArrayList<>();
            winnerList.add(previousPlayer);
            gameState.setPlayers(winnerList);
            return;
        }

        if (actualCount >= quantity) {
            System.out.println("✅ The call was TRUE! Challenger loses 1 die.");
            gameState.getCurrentPlayer().loseDice(1);
        } else {
            System.out.println("❌ The call was FALSE! Previous player loses 2 dice.");
            previousPlayer.loseDice(2);
        }

        gameState.setCurrentCall(0, 0);
        gameState.removeEliminatedPlayers();
    }

    /**
     * Find the player who made the last call.
     */
    private Player getPreviousPlayer() {
        int prevIndex = gameState.getCurrentPlayerIndex() - 1;
        if (prevIndex < 0) {
            prevIndex = gameState.getPlayers().size() - 1;
        }
        return gameState.getPlayers().get(prevIndex);
    }

    private boolean isMaxPossibleCall(int quantity, int faceValue) {
        return quantity == gameState.getTotalDiceCount()
                && faceValue == 6;
    }

    //endregion
}
