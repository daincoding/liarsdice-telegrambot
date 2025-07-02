package game;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RoundLogicTest {

    @Test
    public void testPlayerRerollFlag() {
        Player player = new Player("Alice", 5);
        assertFalse(player.hasUsedReroll(), "Player should not have used reroll yet.");

        player.useReroll();

        assertTrue(player.hasUsedReroll(), "Player should have used reroll.");
    }

    @Test
    public void testResolveLieCallTrue() {
        Player alice = new Player("Alice", 5);
        Player bob = new Player("Bob", 5);

        alice.rollAllDice();
        bob.rollAllDice();

        GameState state = new GameState(List.of(alice, bob));
        state.setCurrentCall(1, alice.revealDice().get(0)); // make sure it's true

        state.advanceTurn();

        RoundLogic logic = new RoundLogic(state);
        logic.resolveLie();

        // After a true call, challenger (Bob) should lose 1 die
        assertEquals(4, bob.getDiceCount(), "Bob should lose 1 die.");
    }

    @Test
    public void testResolveLieCallFalse() {
        Player alice = new Player("Alice", 5);
        Player bob = new Player("Bob", 5);

        alice.rollAllDice();
        bob.rollAllDice();

        GameState state = new GameState(List.of(alice, bob));
        state.setCurrentCall(10, 6); // intentionally impossible call

        state.advanceTurn();

        RoundLogic logic = new RoundLogic(state);
        logic.resolveLie();

        // After a false call, caller (Alice) should lose 2 dice
        assertEquals(3, alice.getDiceCount(), "Alice should lose 2 dice.");
    }
}