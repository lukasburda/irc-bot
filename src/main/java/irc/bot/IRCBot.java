package irc.bot;

public class IRCBot {

    public static void main(String[] args) throws Exception {
        System.setProperty("aphrodite.config", "src/main/resources/aphrodite.json");
        Bot bot = new Bot();
        bot.setVerbose(true);
        bot.connect(bot.getBotIp());
        bot.joinChannel(bot.getBotRoom());
    }

}
