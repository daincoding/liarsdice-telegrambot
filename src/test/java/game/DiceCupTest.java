package game;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class DiceCupTest {

    @Test
    public void testRollAll() {
        DiceCup cup = new DiceCup(5);
        cup.rollAll();

        List<Integer> values = cup.getCurrentDice();
        assertEquals(5, values.size(), "Should have 5 dice");
        System.out.println("Rolled dice values: " + values);

        for (Integer die : values) {
            assertTrue(die >= 1 && die <= 6, "Die value should be between 1 and 6");
        }
    }

    @Test
    public void testRollSelected() {
        DiceCup cup = new DiceCup(5);
        cup.rollAll();
        List<Integer> before = cup.getCurrentDice();
        System.out.println("Before reroll: " + before);

        // Reroll first and third dice
        cup.rollSelected(List.of(0, 2));
        List<Integer> after = cup.getCurrentDice();
        System.out.println("After reroll: " + after);

        assertEquals(5, after.size(), "Should still have 5 dice");
    }
}