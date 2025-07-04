package telegram;


//region ⬆️ Imports
import game.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
//endregion


public class MyGameBot extends TelegramLongPollingBot {

    //region 🧱 Variables

    private final String botToken;
    private final String botUsername;
    private final Map<Long, GameSession> gameSessions = new HashMap<>(); // <-- Maps damit einzelne Verbindungen durch chatID die Keys bekommen und jeder spielen kann

    //endregion

    //region 🛠️ Constructor

    public MyGameBot() {
        Dotenv dotenv = Dotenv.load();
        this.botToken = dotenv.get("BOT_TOKEN");
        this.botUsername = dotenv.get("BOT_USERNAME");
    }

    //endregion

    //region 🏁 Start Options

    // 📝 Dies ist die zentrale Methode des Telegram-Bots.
//     Sie wird bei jeder eingehenden Nachricht von Telegram aufgerufen.
//     Hier entscheidet der Bot, wie er auf verschiedene Befehle oder Spielzüge reagiert.

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId(); // <-- speichert die Chat-ID, damit die Antwort später an den richtigen Nutzer zurückgeschickt wird
            String incomingText = message.getText().trim(); // <-- entfernt überflüssige Leerzeichen

            System.out.println("✅ Incoming from Telegram [" + chatId + "]: " + incomingText);

            String response;

            // 📝 Hier wird geprüft, ob der User einen bestimmten Befehl geschickt hat.
            if (incomingText.equalsIgnoreCase("/start")) {
                response = getWelcomeText(); // <-- ruft Begrüßungstext auf
            } else if (incomingText.equalsIgnoreCase("/help")) {
                response = getHelpText(); // <-- ruft Hilfetext auf
            } else if (incomingText.equalsIgnoreCase("/rules")) {
                response = getRulesText(); // <-- ruft Spielregeln auf
            } else if (incomingText.equalsIgnoreCase("/newgame")) {
                response = startNewGame(chatId, message.getFrom().getFirstName()); // <-- startet ein neues Spiel
            } else if (incomingText.equalsIgnoreCase("/endgame")) {
                response = endGame(chatId); // <-- beendet ein laufendes Spiel
            } else {
                response = handleGameInput(chatId, incomingText); // <-- behandelt alle anderen Texteingaben als Spielzüge
            }

            sendTextMessage(chatId.toString(), response); // <-- sendet die generierte Antwort zurück an den User
        }
    }


    private String getWelcomeText() {
        return """
                👋 *Willkommen bei Liars Dice!*
                
                Starte dein Spiel mit:
                • `/newgame` – Neues Spiel beginnen
                • `/help` – Hilfe anzeigen
                • `/rules` – Anleitung anzeigen
                • `/endgame` – Beendet das Spiel
                """;
    }

    private String getHelpText() {
        return """
                ℹ️ *Hilfe*:
                
                • `/newgame` – Neues Spiel starten
                • `2 5` – einen Call machen (z. B. „2 Würfel mit Wert 5“)
                • `lie` – den letzten Call anzweifeln
                • `reroll 0 2` – einzelne Würfel neu würfeln (max. 1x pro Spiel) - wobei 0 dein Erster und 5 dein Letzter Würfel ist!
                """;
    }

    private String getRulesText() {
        return """
            ℹ️ *Regeln*:
            
            • Bei Liars Dice geht es um Lug & Betrug.
            • Ziel ist am Ende noch mindestens 2 Würfel zu besitzen.
            • Man verliert Würfel, wenn man beim Lügen erwischt wird (2 Stück)
            • oder jemanden fälschlicherweise des Betrugs beschuldigt (1 Würfel).
            • Jedes Mal wenn man dran ist, sagt man eine Anzahl der Würfel mit einem bestimmten Wert.
            • Z. B. 2 3 (2 Würfel, die eine 3 zeigen).
            • Der nächste Spieler kann „Lüge“ rufen oder höher bieten.
            • Hier kommt der Twist: Alle Würfel werden aufgedeckt und alle Würfel auf dem Tisch zählen.
            • Glaubt man während des Spiels dem Bot – oder nicht?
            • Außerdem kann man 1× pro Match beliebige Würfel neu würfeln.
            
            Möge der Bessere gewinnen!
            """;
    }

    private String endGame(Long chatId) {
        if (gameSessions.containsKey(chatId)) {
            gameSessions.remove(chatId);
            return """
                🛑 *Spiel beendet!*
                
                Danke fürs Spielen. Starte ein neues Spiel mit `/newgame`.
                """;
        } else {
            return "⚠️ Es läuft gerade kein Spiel, das du beenden könntest.";
        }
    }

    //endregion

    //region ⚙️ Methods

    private String startNewGame(Long chatId, String playerName) {
        Player human = new Player(playerName, 5);
        BotPlayer bot = new BotPlayer("Bot", 5);

        GameState state = new GameState(List.of(human, bot));
        RoundLogic round = new RoundLogic(state);

        for (Player p : state.getPlayers()) {
            p.rollAllDice();
        }

        gameSessions.put(chatId, new GameSession(state, round));

        return """
                🎲 *Neues Spiel gestartet!*
                
                Du hast 5 Würfel.
                """ + showPlayerDice(human)
                + "\n\n*Mach deinen ersten Call* – z. B. `2 3`.";
    }

    /**
     * 📝 Diese Methode verarbeitet alle Texteingaben des Spielers,
     * die keine Bot-Kommandos wie /start oder /newgame sind.
     *
     * Je nach Inhalt der Eingabe wird entschieden:
     * - Lüge auflösen
     * - Reroll ausführen
     * - Neuen Call machen
     *
     * Außerdem wird hier geprüft, ob der Bot nach dem Spielzug an der Reihe ist.
     */
    private String handleGameInput(Long chatId, String input) {
        GameSession session = gameSessions.get(chatId); // <-- Trennung der Sessions für verschiedene Nutzer

        if (session == null) {
            return "⚠️ Du hast noch kein Spiel gestartet. Nutze /newgame.";
        }

        // 📝 Lädt den aktuellen Spielzustand und die Logik-Instanz.
        GameState state = session.getGameState();
        RoundLogic round = session.getRoundLogic();

        // 📝 Ermittelt, welcher Spieler gerade an der Reihe ist.
        Player currentPlayer = state.getCurrentPlayer();

        if (currentPlayer instanceof BotPlayer) {
            return "⚠️ *Warte, der Bot ist gerade am Zug.*";
        }

        String result;

        // 📝 Prüft, ob der Spieler „lie“ eingegeben hat.
        if (input.equalsIgnoreCase("lie")) {
            // 📝 Löst die Prüfung auf, ob der letzte Call eine Lüge war.
            String lieResolution = round.resolveLie();

            // 📝 Alle Würfel werden nach einer Lüge neu gewürfelt.
            rerollAllDice(state);

            result = "🙅 *Du hast Lüge gerufen!*\n\n"
                    + lieResolution
                    + "\n\n" + buildRoundSummary(state)
                    + "\n🔄 *Neue Runde gestartet!* Mach deinen ersten Call.";
        } else if (input.startsWith("reroll")) {
            // 📝 Spieler will einzelne Würfel neu würfeln.
            result = handleReroll(input, currentPlayer, round, state);
        } else {
            // 📝 Andernfalls interpretiert es die Eingabe als neuen Call (z. B. „2 5“).
            result = handleNewCall(input, currentPlayer, round, state);
        }

        // 📝 Nachdem der Spieler gezogen hat, prüft die Schleife,
        // ob der Bot nun an der Reihe ist, und lässt ihn ggf. mehrfach agieren.
        while (!state.isGameOver() && state.getCurrentPlayer() instanceof BotPlayer) {
            result += "\n\n" + handleBotTurn(round, state);
        }

        if (state.isGameOver()) {
            // 📝 Wenn das Spiel vorbei ist, wird der Gewinner ausgegeben.
            result += "\n\n🎉 *GAME OVER!* \n🏆 Gewinner: " + state.getWinner().getName();
            gameSessions.remove(chatId); // <-- Session wird entfernt, damit der User neu starten kann
        }

        return result;
    }

    private String handleNewCall(String input, Player currentPlayer, RoundLogic round, GameState state) {
        try {
            String[] parts = input.split(" ");
            int quantity = Integer.parseInt(parts[0]);
            int faceValue = Integer.parseInt(parts[1]);

            if (quantity > state.getTotalDiceCount()) {
                return "⚠️ Es gibt nicht so viele Würfel im Spiel.";
            }
            if (!state.isCallHigher(quantity, faceValue)) {
                return "⚠️ Dein Call muss höher sein als der letzte.";
            }

            state.setCurrentCall(quantity, faceValue);
            state.advanceTurn();

            return "*"+currentPlayer.getName()+"* calls " + quantity + " × " + faceValue
                    + "\n\n" + showPlayerDice(currentPlayer)
                    + "\n\n_Bitte warte, Bot denkt nach…_";

        } catch (Exception e) {
            return "⚠️ Ungültige Eingabe. Nutze z. B. `2 3`.";
        }
    }

    private String handleReroll(String input, Player player, RoundLogic round, GameState state) {
        if (player.hasUsedReroll()) {
            return "⚠️ Du hast deinen Reroll bereits benutzt.";
        }

        String[] parts = input.split(" ");
        List<Integer> indices = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            indices.add(Integer.parseInt(parts[i]));
        }

        player.rerollSelectedDice(indices);
        player.useReroll();

        return "🎲 *Neue Würfel:* " + player.revealDice()
                + "\n\n_Mach nun einen höheren Call._";
    }

    private String handleBotTurn(RoundLogic round, GameState state) {
        BotPlayer bot = (BotPlayer) state.getCurrentPlayer();

        int currentQuantity = state.getCurrentQuantityCalled();
        int currentFace = state.getCurrentFaceValueCalled();

        boolean callLie = false;
        if (currentQuantity > 0) {
            callLie = bot.shouldCallLie(
                    currentQuantity,
                    currentFace,
                    state.getTotalDiceCount()
            );
        }

        if (callLie) {
            String lieResolution = round.resolveLie();
            rerollAllDice(state);

            return "🤖 *Bot ruft LIE!*\n\n"
                    + lieResolution
                    + "\n\n" + buildRoundSummary(state)
                    + "\n🔄 *Neue Runde gestartet!* Mach deinen ersten Call.";
        } else {
            if (!bot.hasUsedReroll() && bot.shouldReroll()) {
                List<Integer> rerollIndices = bot.chooseDiceToReroll();
                bot.rerollSelectedDice(rerollIndices);
                bot.useReroll();

                return "🤖 *Bot entscheidet sich für einen Reroll.*\n"
                        + botMakesCall(bot, state);
            } else {
                return botMakesCall(bot, state);
            }
        }
    }

    private String botMakesCall(BotPlayer bot, GameState state) {
        String call = bot.decideNextCall(
                state.getCurrentQuantityCalled(),
                state.getCurrentFaceValueCalled(),
                state.getTotalDiceCount()
        );

        state.setCurrentCall(
                Integer.parseInt(call.split(" ")[0]),
                Integer.parseInt(call.split(" ")[1])
        );
        state.advanceTurn();

        return "🤖 *Bot calls:* `" + call + "`" + " Überbiete es oder schreib 'lie'!";
    }

    private String buildRoundSummary(GameState state) {
        StringBuilder sb = new StringBuilder();

        for (Player p : state.getPlayers()) {
            sb.append("🎲 *").append(p.getName()).append("* hat noch ")
                    .append(p.getDiceCount()).append(" Würfel.\n");

            if (!(p instanceof BotPlayer)) {
                sb.append("Würfel: ").append(p.revealDice()).append("\n");
            }
        }
        return sb.toString();
    }

    private void rerollAllDice(GameState state) {
        for (Player p : state.getPlayers()) {
            p.rollAllDice();
        }
    }

    private void sendTextMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.enableMarkdown(true);

        try {
            execute(message);
            System.out.println("📤 Sent to Telegram [" + chatId + "]: " + text);
        } catch (TelegramApiException e) {
            System.err.println("❌ Error sending message: " + e.getMessage());
        }
    }

    //endregion

    // region 🫴Getters
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private String showPlayerDice(Player player) {
        return "🎲 *Deine Würfel:* " + player.revealDice();
    }

//endregion
}