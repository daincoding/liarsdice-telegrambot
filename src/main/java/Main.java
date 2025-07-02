import game.*;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.println("🎲 Welcome to Liars Dice!");

        Scanner scanner = new Scanner(System.in);

        // Create two players manually
        System.out.print("Enter name for Player 1: ");
        String name1 = scanner.nextLine().trim();

        System.out.print("Enter name for Player 2: ");
        String name2 = scanner.nextLine().trim();

        Player player1 = new Player(name1, 5);
        Player player2 = new Player(name2, 5);

        GameState state = new GameState(List.of(player1, player2));

        while (!state.isGameOver()) {
            RoundLogic round = new RoundLogic(state);
            round.playRound();

            // Display dice counts after each round
            System.out.println("\n=== Dice Count After Round ===");
            for (Player p : state.getPlayers()) {
                System.out.println(p.getName() + " has " + p.getDiceCount() + " dice left.");
            }

            if (state.isGameOver()) {
                break;
            }

            System.out.println("\n=== Starting Next Round ===");
        }

        System.out.println("\n🎉 GAME OVER!");
        System.out.println("🏆 Winner: " + state.getWinner().getName());
    }
}