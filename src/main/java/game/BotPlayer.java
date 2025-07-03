package game;

import java.util.*;

public class BotPlayer extends Player {

    //region 🧱 Variables
    private Random random;

    //endregion


    //region 🛠️ Constructor
    public BotPlayer(String name, int startingDice) {
        super(name, startingDice);
        this.random = new Random();
    }
    //endregion

    //region ⚙️ Methods

    // 📝BOT DECISION IF WE ACCUSE OF LYING
    public boolean shouldCallLie(int currentQuantity, int faceValue, int totalDiceInGame) {
        int botCount = countDiceOfFace(faceValue);

        int unknownDice = totalDiceInGame - getDiceCount();
        int neededFromOthers = currentQuantity - botCount;

        if (neededFromOthers <= 0) {
            // The Bot has enough Dice on its own to satisfy the Call he wants to do.
            return false;
        }

        double probabilityOtherHasEnough = calculateProbability(unknownDice, neededFromOthers, faceValue);

        // Decision thresholds:

        if (probabilityOtherHasEnough >= 0.7) {
            // Looks safe → never challenge
            return false;
        } else if (probabilityOtherHasEnough >= 0.4) {
            // Possibly true → rarely challenge
            return random.nextDouble() < 0.10;
        } else if (probabilityOtherHasEnough >= 0.2) {
            // Suspicious → challenge sometimes
            return random.nextDouble() < 0.30;
        } else {
            // Extremely unlikely → usually challenge
            return random.nextDouble() < 0.60;
        }
    }

    public boolean shouldReroll(){
        if (hasUsedReroll()){
            return false;
        }
        return random.nextDouble() < 0.10;
    }

    //📝 What dice the Bot wants to reroll
    public List<Integer> chooseDiceToReroll(){
        List<Integer> currentDice = revealDice();
        List<Integer> indices = new ArrayList<>();
        int minValue = Collections.min(currentDice);

        for (int i = 0; i < currentDice.size(); i++) {
            if (currentDice.get(i) == 1 || currentDice.get(i) == minValue) {
                indices.add(i);
            }
        }

        // Only reroll max 2 dice, but don't exceed available dice
        int maxAllowed = Math.min(2, currentDice.size());
        return indices.size() > maxAllowed
                ? indices.subList(0, maxAllowed)
                : indices;
    }

    //📝 We decide the call the bot should do.

    public String decideNextCall(int currentQuantity, int currentFace, int totalDice) {
        List<Integer> dice = revealDice();

        double bluffChance = 0.25 + Math.random() * 0.1;
        boolean willBluff = Math.random() < bluffChance;

        int quantity;
        int faceValue;

        if (currentQuantity >= totalDice && currentFace == 6) {
            System.out.println("🤖 Bot detects max call - must call lie!");
            return "lie";
        }

        if (willBluff && currentQuantity > 0) {
            // Bot attempts a true bluff
//            System.out.println("🤖 (Bot decides to BLUFF!)");

            // Choose a face value that the bot has 0 of
            List<Integer> possibleFaces = new ArrayList<>();
            for (int face = 1; face <= 6; face++) {
                if (!dice.contains(face)) {
                    possibleFaces.add(face);
                }
            }

            if (!possibleFaces.isEmpty()) {
                faceValue = possibleFaces.get(
                        (int) (Math.random() * possibleFaces.size())
                );
            } else {
                // No impossible faces found → pick random anyway
                faceValue = 1 + (int) (Math.random() * 6);
            }

            // Choose quantity just barely higher than current
            quantity = currentQuantity + 1;

            // Clamp to max dice
            if (quantity > totalDice) {
                quantity = totalDice;
            }
        } else {
            // Normal logic (non-bluff)
            Map<Integer, Integer> counts = new HashMap<>();
            for (int val : dice) {
                counts.put(val, counts.getOrDefault(val, 0) + 1);
            }

            int mostCommonFace = dice.get(0);
            int maxCount = counts.getOrDefault(mostCommonFace, 1);

            for (var entry : counts.entrySet()) {
                if (entry.getValue() > maxCount) {
                    mostCommonFace = entry.getKey();
                    maxCount = entry.getValue();
                }
            }

            quantity = Math.max(currentQuantity + 1, maxCount);
            faceValue = Math.max(currentFace, mostCommonFace);

            if (quantity > totalDice) {
                quantity = totalDice;
            }
        }

        return quantity + " " + faceValue;
    }

    //endregion

    //region 🧮 Calculations
    // 📝Calculate binomial probability for getting at least needed successes.
    private double calculateProbability(int unknownDice, int neededFromOthers, int faceValue) {
        double p = 1.0 / 6.0;
        double cumulative = 0.0;

        for (int i = neededFromOthers; i <= unknownDice; i++) {
            cumulative += binomialProbability(unknownDice, i, p);
        }
        return cumulative;
    }


    // 📝 Calculation Helper
    private double binomialProbability(int n, int k, double p) {
        double combinations = factorial(n) / (factorial(k) * factorial(n - k));
        return combinations * Math.pow(p, k) * Math.pow(1 - p, n - k);
    }

    private double factorial(int n) {
        double result = 1.0;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    // 📝 Returns face Value the bot has the most of

    private int pickStrongestFace() {
        Map<Integer, Integer> countMap = new HashMap<>();
        for (int die : revealDice()) {
            countMap.put(die, countMap.getOrDefault(die, 0) + 1);
        }
        return countMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(6); // default to 6
    }

    // 📝 BOT COUNTS ALL DICE HE HAS OF A GIVEN FACE VALUE
    private int countDiceOfFace(int face) {
        int count = 0;
        for (int die : revealDice()) {
            if (die == face) {
                count++;
            }
        }
        return count;
    }

    // 📝 WE DETERMINE A LIE CHANCE BETWEEN 25 and 35%
    private double getLieChance() {
        return 0.25 + random.nextDouble() * 0.10;
    }

    //endregion

}
