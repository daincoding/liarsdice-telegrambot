package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RoundLogic {

    //region 🧱 Variables

    private final GameState gameState;
    private boolean roundEnded;

    //endregion

    //region 🛠️ Constructor

    public RoundLogic(GameState gameState) {
        this.gameState = gameState;
        this.roundEnded = false;
    }

    //endregion

    //region ⚙️ Methods

    // 📝 Game Start
    public void playRound() {
        Scanner scanner = new Scanner(System.in);

        while (!roundEnded) {
            Player currentPlayer = gameState.getCurrentPlayer();

            if (currentPlayer instanceof BotPlayer) {
                handleBotTurn((BotPlayer) currentPlayer);
            } else {
                handleHumanTurn(currentPlayer, scanner);
            }
        }
    }

    // 📝 Bot Turn
    private void handleBotTurn(BotPlayer botPlayer) {
        int currentQuantity = gameState.getCurrentQuantityCalled();
        int currentFace = gameState.getCurrentFaceValueCalled();

        boolean callLie = false;

        if (currentQuantity > 0) {
            callLie = botPlayer.shouldCallLie(
                    currentQuantity,
                    currentFace,
                    gameState.getTotalDiceCount()
            );
        }

        if (callLie) {
            System.out.println("🤖 Bot shouts: YOU LIE!");
            resolveLie();
        } else {
            if (!botPlayer.hasUsedReroll() && botPlayer.shouldReroll()) {
                System.out.println("🤖 Bot decides to reroll!");
                List<Integer> indices = botPlayer.chooseDiceToReroll();
                botPlayer.rerollSelectedDice(indices);
                botPlayer.useReroll();
//                System.out.println("🤖 Bot's new dice: " + botPlayer.revealDice());
            }

            String call = botPlayer.decideNextCall(
                    currentQuantity,
                    currentFace,
                    gameState.getTotalDiceCount()
            );

            System.out.println("🤖 Bot calls: " + call);
            handleNewCall(call, botPlayer);
        }
    }

    // 📝 Human Turn
    private void handleHumanTurn(Player player, Scanner scanner) {
        System.out.println("\n--- " + player.getName() + "'s turn ---");
        System.out.println("Current call: "
                + gameState.getCurrentQuantityCalled()
                + " x " + gameState.getCurrentFaceValueCalled());
        System.out.println("Your dice: " + player.revealDice());

        if (!player.hasUsedReroll()) {
            System.out.println("Enter your move (e.g. '3 4' for call, 'reroll 0 2', or 'lie'):");
        } else {
            System.out.println("Enter your move (e.g. '3 4' for call, or 'lie'):");
        }

        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("lie")) {
            if (gameState.getCurrentQuantityCalled() == 0) {
                System.out.println("⚠️ You can't call a lie before any call has been made.");
                return;
            }
            resolveLie();
        } else if (input.startsWith("reroll")) {
            if (player.hasUsedReroll()) {
                System.out.println("⚠️ You already used your reroll this match.");
            } else {
                handleReroll(player, input, scanner);
                player.useReroll();
            }
        } else {
            handleNewCall(input, player);
        }
    }

    // 📝 Handle rerolling dice for a player.
    private void handleReroll(Player player, String input, Scanner scanner) {
        String[] parts = input.split(" ");
        List<Integer> indices = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            indices.add(Integer.parseInt(parts[i]));
        }

        player.rerollSelectedDice(indices);
        System.out.println("New dice after reroll: " + player.revealDice());

        System.out.println("Now you must make a higher call (e.g. '3 4'):");

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

    // 📝 Handle making a new call

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

    // 📝 Resolve the Lie
    public void resolveLie() {
        System.out.println("\n=== REVEALING ALL DICE ===");
        for (Player player : gameState.getPlayers()) {
            System.out.println(player.getName() + "'s dice: " + player.revealDice());
        }
        System.out.println("=========================\n");

        int quantity = gameState.getCurrentQuantityCalled();
        int faceValue = gameState.getCurrentFaceValueCalled();

        int actualCount = gameState.getTotalDiceForFace(faceValue);
        System.out.println("Total dice showing " + faceValue + ": " + actualCount);

        Player previousPlayer = getPreviousPlayer();

        if (isMaxPossibleCall(quantity, faceValue) && actualCount == quantity) {
            System.out.println("🏆 " + previousPlayer.getName()
                    + " made the max possible call and it was TRUE!");
            System.out.println("🎉 " + previousPlayer.getName() + " wins the entire match! THIS IS SO RARE");

            List<Player> winnerList = new ArrayList<>();
            winnerList.add(previousPlayer);
            gameState.setPlayers(winnerList);
            roundEnded = true;
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
        roundEnded = true;
    }

    // 📝 Who made the last call
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