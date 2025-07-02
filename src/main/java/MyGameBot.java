import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class MyGameBot extends TelegramLongPollingBot {

    //region 🧱 Variables

    private final String botToken;
    private final String botUsername;

    //endregion

    //region 🛠️ Constructor

    public MyGameBot() {
        Dotenv dotenv = Dotenv.load();
        this.botToken = dotenv.get("BOT_TOKEN");
        this.botUsername = "LiarsDiceGame_Bot";
    }

    //endregion


    //region ⚠️Overrides

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String incomingText = update.getMessage().getText();

            System.out.println("Received message: " + incomingText);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Hello, world!");

            System.out.println("Send Message: " + message.getText());

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    //endregion
}