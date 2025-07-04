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

    // 📝 BOT-ENTSCHEIDUNG, OB EINE LÜGE GERUFEN WIRD
    public boolean shouldCallLie(int currentQuantity, int faceValue, int totalDiceInGame) {
        // 📝 Zählt, wie viele Würfel der Bot selbst vom gewünschten Wert hat.
        int botCount = countDiceOfFace(faceValue);

        // 📝 Anzahl der Würfel, die dem Bot unbekannt sind (also die der anderen Spieler).
        int unknownDice = totalDiceInGame - getDiceCount();

        // 📝 Wie viele Würfel vom Gegner benötigt würden, um die Behauptung wahr zu machen.
        int neededFromOthers = currentQuantity - botCount;

        if (neededFromOthers <= 0) {
            // 📝 Der Bot hat genug Würfel selbst, um den Call zu decken → niemals Lüge rufen.
            return false;
        }

        // 📝 Berechnet die Wahrscheinlichkeit, dass die unbekannten Würfel genügend passende Augen zeigen.
        double probabilityOtherHasEnough = calculateProbability(
                unknownDice,
                neededFromOthers,
                faceValue
        );

        // 📝 Entscheidung auf Basis der Wahrscheinlichkeit:
        if (probabilityOtherHasEnough >= 0.7) {
            // 📝 Sehr wahrscheinlich wahr → niemals Lüge rufen.
            return false;
        } else if (probabilityOtherHasEnough >= 0.4) {
            // 📝 Könnte wahr sein → selten Lüge rufen (10 %).
            return random.nextDouble() < 0.10;
        } else if (probabilityOtherHasEnough >= 0.2) {
            // 📝 Verdächtig → manchmal Lüge rufen (30 %).
            return random.nextDouble() < 0.30;
        } else {
            // 📝 Extrem unwahrscheinlich → oft Lüge rufen (60 %).
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
    // 📝 Berechnet die kumulierte Wahrscheinlichkeit, dass unter den unbekannten Würfeln
    // mindestens die benötigte Anzahl eines bestimmten Wertes vorkommt.
    // Beispiel: Es werden mindestens 3 Fünfen unter 7 unbekannten Würfeln benötigt.
    private double calculateProbability(int unknownDice, int neededFromOthers, int faceValue) {
        double p = 1.0 / 6.0; // 📝 Wahrscheinlichkeit für eine bestimmte Zahl (z. B. eine Fünf) bei einem Würfelwurf.
        double cumulative = 0.0; // 📝 Zwischenspeicher für die aufsummierte Wahrscheinlichkeit.

        // 📝 Schleife über alle möglichen Anzahlen des gewünschten Wertes (von neededFromOthers bis alle unknownDice).
        for (int i = neededFromOthers; i <= unknownDice; i++) {
            cumulative += binomialProbability(unknownDice, i, p);
            // 📝 Addiert die Wahrscheinlichkeit, dass genau i der unknownDice den gesuchten Wert zeigen.
        }

        // 📝 Gibt die Wahrscheinlichkeit zurück, mindestens neededFromOthers Erfolge zu haben.
        return cumulative;
    }


    // 📝 Hilfsmethode zur Berechnung der Binomial-Wahrscheinlichkeit:
    // Berechnet die Wahrscheinlichkeit dafür, dass bei n Würfen
    // genau k Würfel einen bestimmten Wert zeigen.
    // Formel: P(X = k) = (n über k) * p^k * (1-p)^(n-k)
    private double binomialProbability(int n, int k, double p) {
        // 📝 Berechnung der Anzahl möglicher Kombinationen (n über k).
        double combinations = factorial(n) / (factorial(k) * factorial(n - k));

        // 📝 Wahrscheinlichkeit, dass genau k Würfel den gesuchten Wert zeigen,
        // multipliziert mit der Wahrscheinlichkeit, dass die übrigen (n-k) Würfel es nicht tun.
        return combinations * Math.pow(p, k) * Math.pow(1 - p, n - k);
    }

    // 📝 Hilfsmethode zur Berechnung der Fakultät (n!).
    // Beispiel: 5! = 5 × 4 × 3 × 2 × 1 = 120
    private double factorial(int n) {
        double result = 1.0;

        // 📝 Multipliziert alle Zahlen von 2 bis n.
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
