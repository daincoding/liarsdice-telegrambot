package telegram;

import game.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class MyGameBot extends TelegramLongPollingBot {

    //region 🧱 Variables

    private final String botToken;
    private final String botUsername;

    private final Map<Long, GameSession> gameSessions = new HashMap<>();

    //endregion

    //region 🛠️ Constructor

    public MyGameBot() {
        Dotenv dotenv = Dotenv.load();
        this.botToken = dotenv.get("BOT_TOKEN");
        this.botUsername = dotenv.get("BOT_USERNAME");
    }

    //endregion

    //region ⚙️ Methods

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            String incomingText = message.getText().trim();

            System.out.println("✅ Incoming from Telegram [" + chatId + "]: " + incomingText);

            String response;

            if (incomingText.equalsIgnoreCase("/start")) {
                response = "👋 Willkommen bei Liars Dice!\n" +
                        "Tippe zum Beispiel:\n" +
                        "`/newgame` - Neues Spiel starten\n" +
                        "`/help` - Hilfe anzeigen";
            } else if (incomingText.equalsIgnoreCase("/help")) {
                response = "ℹ️ Hilfe:\n" +
                        "• `/newgame` → neues Spiel starten\n" +
                        "• z.B. `2 5` → eine Ansage machen (2 mal die Zahl 5)\n" +
                        "• `lie` → den vorherigen Spieler der Lüge bezichtigen\n" +
                        "• `reroll 0 2` → Würfel neu würfeln (max 1x pro Spiel)";
            } else if (incomingText.equalsIgnoreCase("/newgame")) {
                response = startNewGame(chatId, message.getFrom().getFirstName());
            } else {
                response = handleGameInput(chatId, incomingText);
            }

            sendTextMessage(chatId.toString(), response);
        }
    }

    private String startNewGame(Long chatId, String playerName) {
        Player human = new Player(playerName, 5);
        BotPlayer bot = new BotPlayer("Bot", 5);

        GameState state = new GameState(List.of(human, bot));
        RoundLogic round = new RoundLogic(state);

        for (Player p : state.getPlayers()) {
            p.rollAllDice();
        }

        GameSession session = new GameSession(state, round);
        gameSessions.put(chatId, session);

        return "🎲 Neues Spiel gestartet!\n" +
                showPlayerDice(human) +
                "\nMach deinen ersten Call, z. B. `2 3`.";
    }

    private String handleGameInput(Long chatId, String input) {
        GameSession session = gameSessions.get(chatId);

        if (session == null) {
            return "⚠️ Du hast noch kein Spiel gestartet. Nutze /newgame.";
        }

        GameState state = session.getGameState();
        RoundLogic round = session.getRoundLogic();

        Player currentPlayer = state.getCurrentPlayer();

        if (currentPlayer instanceof BotPlayer) {
            return "⚠️ Moment! Der Bot ist gerade am Zug.";
        }

        String result;
        if (input.equalsIgnoreCase("lie")) {
            round.resolveLie();
            for (Player p : state.getPlayers()) {
                p.rollAllDice();
            }
            result = buildRoundSummary(state);
        } else if (input.startsWith("reroll")) {
            result = handleReroll(input, currentPlayer, round, state);
        } else {
            result = handleNewCall(input, currentPlayer, round, state);
        }

        // Prüfen, ob Bot jetzt dran ist:
        while (!state.isGameOver() && state.getCurrentPlayer() instanceof BotPlayer) {
            result += "\n\n" + handleBotTurn(round, state);
        }

        if (state.isGameOver()) {
            result += "\n\n🎉 GAME OVER!\n🏆 Gewinner: " + state.getWinner().getName();
            gameSessions.remove(chatId);
        }

        return result;
    }

    private String handleNewCall(String input, Player currentPlayer, RoundLogic round, GameState state) {
        try {
            String[] parts = input.split(" ");
            int quantity = Integer.parseInt(parts[0]);
            int faceValue = Integer.parseInt(parts[1]);

            if (quantity > state.getTotalDiceCount()) {
                return "⚠️ Illegaler Call! Es gibt nicht so viele Würfel im Spiel.";
            }

            if (!state.isCallHigher(quantity, faceValue)) {
                return "⚠️ Dein Call muss höher als der letzte sein.";
            }

            state.setCurrentCall(quantity, faceValue);
            state.advanceTurn();

            return currentPlayer.getName() + " calls " + quantity + " x " + faceValue
                    + "\n\n" + showPlayerDice(currentPlayer);

        } catch (Exception e) {
            return "⚠️ Ungültige Eingabe. Bitte gib zwei Zahlen ein, z. B. `2 3`.";
        }
    }

    private String handleReroll(String input, Player player, RoundLogic round, GameState state) {
        if (player.hasUsedReroll()) {
            return "⚠️ Du hast deinen Reroll bereits verwendet.";
        }

        String[] parts = input.split(" ");
        List<Integer> indices = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            indices.add(Integer.parseInt(parts[i]));
        }

        player.rerollSelectedDice(indices);
        player.useReroll();

        return "🎲 Neue Würfel: " + player.revealDice() +
                "\nBitte mache jetzt einen höheren Call.";
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
            round.resolveLie();
            return "🤖 Bot ruft LIE!\n" + buildRoundSummary(state);
        } else {
            if (!bot.hasUsedReroll() && bot.shouldReroll()) {
                List<Integer> rerollIndices = bot.chooseDiceToReroll();
                bot.rerollSelectedDice(rerollIndices);
                bot.useReroll();
            }

            String call = bot.decideNextCall(
                    currentQuantity,
                    currentFace,
                    state.getTotalDiceCount()
            );

            state.setCurrentCall(
                    Integer.parseInt(call.split(" ")[0]),
                    Integer.parseInt(call.split(" ")[1])
            );
            state.advanceTurn();

            return "🤖 Bot calls: " + call;
        }
    }

    private String buildRoundSummary(GameState state) {
        StringBuilder sb = new StringBuilder();
        for (Player p : state.getPlayers()) {
            sb.append("🎲 ").append(p.getName())
                    .append(" hat noch ").append(p.getDiceCount()).append(" Würfel.\n");
            sb.append("Würfel: ").append(p.revealDice()).append("\n\n");
        }
        return sb.toString();
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

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private String showPlayerDice(Player player) {
        return "🎲 Deine Würfel: " + player.revealDice();
    }

    //endregion
}