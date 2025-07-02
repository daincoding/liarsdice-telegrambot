package game;

import java.util.List;

public class Player {

    //region 🧱 Variables

    private final String name;
    private DiceCup diceCup;

    //endregion

    //region 🛠️ Constructor
    public Player(String name, int startingDice) {

        if (startingDice <= 1) {
            throw new IllegalArgumentException("Invalid start dice! You need at least 2 dice.");
        }
        this.name = name;
        this.diceCup = new DiceCup(startingDice);
    }

    //endregion

    //region ⚙️ Methods

    public void rollAllDice() {
        diceCup.rollAll();
    }

    public void rerollSelectedDice(List<Integer> dices) {
        diceCup.rollSelected(dices);
    }

    public List<Integer> revealDice() {
        return diceCup.getCurrentDice();
    }

    public void loseDice(int count) {
        diceCup.removeDice(count);
    }

    //endregion

    //region ✅ Validation

    public boolean isEliminated() {
        return diceCup.getNumberOfDice() < 2;
    }

    //endregion

    //region 🫴 Getters & Setters 🫳

    public String getName() {
        return name;
    }

    public int getDiceCount() {
        return diceCup.getNumberOfDice();
    }

    //endregion

}
