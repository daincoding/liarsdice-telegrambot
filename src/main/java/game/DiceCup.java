package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DiceCup {

    //region 🧱 Variables

    private static final int diceSides = 6;
    private List<Integer> diceValues;
    private Random random;

    //endregion

    //region 🛠️ Constructor
    // 📝 Constructor is creating a new dice cup with the given number of dice.

    public DiceCup(int numberOfDice) {
        if (numberOfDice <= 0) {
            throw new IllegalArgumentException("DiceCup must have at least one die.");
        }

        this.diceValues = new ArrayList<>();
        this.random = new Random();

        for (int i = 0; i < numberOfDice; i++) {
            diceValues.add(0);
        }
    }

    //endregion

    //region ⚙️ Methods

    private int rollSingleDie() {
        return random.nextInt(diceSides) + 1;
    }

    public void rollAll() {
        for (int i = 0; i < this.diceValues.size(); i++) {
            diceValues.set(i, rollSingleDie());
        }
    }

    public void rollSelected(List<Integer> dices) {
        for (int index : dices) {
            if (index >= 0 && index < this.diceValues.size()) {
                diceValues.set(index, rollSingleDie());
            }
        }
    }

    public void removeDice(int amount) {
        for (int i = 0; i < amount && !diceValues.isEmpty(); i++) {
            diceValues.removeLast();
        }
    }

    //endregion

    //region 🫴 Getters & Setters 🫳
    public List<Integer> getCurrentDice() {
        return new ArrayList<>(diceValues);
    }

    public int getNumberOfDice() {
        return diceValues.size();
    }

    //endregion
}
