package telegram;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MyGameBotTest {


    // 🐛 Is Bot Running?
    @Test
    public void testBotInitialization() {
        MyGameBot bot = new MyGameBot();

        assertNotNull(bot.getBotUsername(), "Bot username should not be null");
        assertNotNull(bot.getBotToken(), "Bot token should not be null");
        assertFalse(bot.getBotToken().isEmpty(), "Bot token should not be empty");
        System.out.println("Bot is running...");
    }
}