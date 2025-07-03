import game.*;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.println("🎲 Welcome to Liars Dice vs BOT!");

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your name: ");
        String humanName = scanner.nextLine().trim();

        Player human = new Player(humanName, 5);
        BotPlayer bot = new BotPlayer("Bot", 5);

        GameState state = new GameState(List.of(human, bot));

        while (!state.isGameOver()) {

            // 🐍 ROLL DICE ONCE PER ROUND!
            System.out.println("\n🎲 Rolling dice for all players...");
//            for (Player p : state.getPlayers()) {
//                p.rollAllDice();
//                System.out.println(p.getName() + " rolled: " + p.revealDice());
//            }
            for (Player p : state.getPlayers()) {
                p.rollAllDice();
                if (p instanceof BotPlayer) {
                    System.out.println(p.getName() + " rolled their dice secretly...");
                } else {
                    System.out.println(p.getName() + " rolled: " + p.revealDice());
                }
            }

            RoundLogic round = new RoundLogic(state);
            round.playRound();

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