package game;

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
