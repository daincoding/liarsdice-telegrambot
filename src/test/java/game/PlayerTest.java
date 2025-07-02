package game;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    @Test
    public void testPlayerCreationAndRolling() {
        Player player = new Player("Alice", 5);

        player.rollAllDice();
        List<Integer> dice = player.revealDice();

        System.out.println(player.getName() + " rolled: " + dice);

        assertEquals(5, dice.size(), "Player should have 5 dice.");

        for (Integer die : dice) {
            assertTrue(die >= 1 && die <= 6, "Die value should be between 1 and 6.");
        }
    }

    @Test
    public void testRerollSelectedDice() {
        Player player = new Player("Bob", 5);
        player.rollAllDice();

        List<Integer> before = player.revealDice();
        System.out.println("Before reroll: " + before);

        player.rerollSelectedDice(List.of(0, 2));
        List<Integer> after = player.revealDice();
        System.out.println("After reroll: " + after);

        assertEquals(5, after.size(), "Player should still have 5 dice after reroll.");
    }

    @Test
    public void testLoseDice() {
        Player player = new Player("Charlie", 5);

        assertEquals(5, player.getDiceCount(), "Should start with 5 dice.");
        player.loseDice(2);
        assertEquals(3, player.getDiceCount(), "Should have 3 dice after losing 2.");

        player.loseDice(2);
        assertEquals(1, player.getDiceCount(), "Should have 1 die left.");

        assertTrue(player.isEliminated(), "Player should be eliminated with 1 die left.");
    }

    @Test
    public void testIsEliminated() {
        Player player = new Player("Dana", 5);

        System.out.println(player.getName() + " has " + player.getDiceCount() + " dice.");
        System.out.println("Is eliminated? " + player.isEliminated());

        player.loseDice(3);
        System.out.println(player.getName() + " has " + player.getDiceCount() + " dice.");
        System.out.println("Is eliminated? " + player.isEliminated());

        assertFalse(player.isEliminated(), "Player should not be eliminated with 2 dice.");

        player.loseDice(1);
        System.out.println(player.getName() + " has " + player.getDiceCount() + " dice.");
        System.out.println("Is eliminated? " + player.isEliminated());


        player.loseDice(1);
        System.out.println(player.getName() + " has " + player.getDiceCount() + " dice.");
        System.out.println("Is eliminated? " + player.isEliminated());

        assertTrue(player.isEliminated(), "Player should be eliminated with fewer than 2 dice.");
    }
}