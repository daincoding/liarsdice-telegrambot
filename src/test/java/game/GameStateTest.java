package game;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameStateTest {

    @Test
    public void testGameStateInitializationAndTurns() {
        Player alice = new Player("Alice", 5);
        Player bob = new Player("Bob", 5);

        GameState state = new GameState(List.of(alice, bob));

        assertEquals("Alice", state.getCurrentPlayer().getName(), "First player should be Alice");

        state.advanceTurn();
        assertEquals("Bob", state.getCurrentPlayer().getName(), "Second player should be Bob");

        state.advanceTurn();
        assertEquals("Alice", state.getCurrentPlayer().getName(), "Should cycle back to Alice");
    }

    @Test
    public void testSettingAndValidatingCalls() {
        Player alice = new Player("Alice", 5);
        Player bob = new Player("Bob", 5);

        GameState state = new GameState(List.of(alice, bob));

        state.setCurrentCall(2, 3);
        assertTrue(state.isCallHigher(3, 2), "3x any face should be higher than 2x3");
        assertTrue(state.isCallHigher(2, 5), "2x5 should be higher than 2x3");
        assertFalse(state.isCallHigher(2, 2), "2x2 should not be higher than 2x3");
    }

    @Test
    public void testCountingDiceAcrossPlayers() {
        Player alice = new Player("Alice", 5);
        Player bob = new Player("Bob", 5);

        alice.rollAllDice();
        bob.rollAllDice();

        System.out.println("Alice's dice: " + alice.revealDice());
        System.out.println("Bob's dice: " + bob.revealDice());

        GameState state = new GameState(List.of(alice, bob));

        for (int face = 1; face <= 6; face++) {
            int count = state.getTotalDiceForFace(face);
            System.out.println("Total of face " + face + ": " + count);
            assertTrue(count >= 0 && count <= 10, "Dice count should be in valid range");
        }
    }

    @Test
    public void testEliminationAndGameOver() {
        Player alice = new Player("Alice", 5);
        Player bob = new Player("Bob", 5);

        GameState state = new GameState(List.of(alice, bob));

        assertFalse(state.isGameOver(), "Game should not be over initially");

        bob.loseDice(4);
        bob.loseDice(1);
        assertTrue(bob.isEliminated(), "Bob should be eliminated");

        state.removeEliminatedPlayers();
        assertEquals(1, state.getPlayers().size(), "Only one player should remain");

        assertTrue(state.isGameOver(), "Game should be over with one player left");
        assertEquals("Alice", state.getWinner().getName(), "Alice should be the winner");
    }

    @Test
    public void testTotalDiceCount() {
        Player alice = new Player("Alice", 5);
        Player bob = new Player("Bob", 5);

        GameState state = new GameState(List.of(alice, bob));
        assertEquals(10, state.getTotalDiceCount(), "Total dice should be 10");

        bob.loseDice(2);
        assertEquals(8, state.getTotalDiceCount(), "Total dice should be 8 after Bob loses 2");
    }
}