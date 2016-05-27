package irc.bot;

import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import junit.framework.Assert;
import org.jboss.set.aphrodite.spi.AphroditeException;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.json.simple.parser.ParseException;
import org.junit.After;

public class BotTest {

    private String resultMsg;
    private Bot bot;
    private TestBot testBot;
    private String dfnmessage = "bz1234567";
    private String EXPECTED = "[bz1234567] [erlang] : Optional[Package should not ship a separate emacs sub-package], https://bugzilla.redhat.com/show_bug.cgi?id=1234567";

    @Before
    public void before() throws AphroditeException, IOException, ParseException, IrcException {
        System.setProperty("aphrodite.config", "src/test/resources/aphrodite.json");
        bot = new Bot();
        testBot = new TestBot();
        bot.connect(bot.getBotIp());
        bot.joinChannel(bot.getBotRoom());
        testBot.connect(bot.getBotIp());
        testBot.joinChannel(bot.getBotRoom());
        bot.setVerbose(true);
        testBot.setVerbose(true);
    }

    @Test
    public void messageTest() throws InterruptedException {
        testBot.sendMessage(bot.getBotRoom(), dfnmessage);
        Thread.sleep(5000);
        Assert.assertEquals("Message is different than sended message", EXPECTED, resultMsg);
    }

    @After
    public void after() {
        bot.disconnect();
        testBot.disconnect();
    }

    private class TestBot extends PircBot {

        public TestBot() {
            this.setName("TestBot");
        }

        @Override
        public void onMessage(String channel, String sender,
                String login, String hostname, String message) {
            if (sender.equals(bot.getNick())) {
                resultMsg = message;
            }
        }
    }
}
