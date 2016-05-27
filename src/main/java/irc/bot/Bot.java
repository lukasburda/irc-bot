package irc.bot;

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.domain.Issue;
import org.jboss.set.aphrodite.spi.AphroditeException;
import org.jboss.set.aphrodite.spi.NotFoundException;
import org.jibble.pircbot.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Bot extends PircBot {

    private static final String BZ_URL = "https://bugzilla.redhat.com/show_bug.cgi?id=";
    private static final String JBEAP_URL = "https://issues.jboss.org/browse/";
    private static final String JBEAP_PREFIX = "JBEAP-";
    private static final String BZ_PREFIX = "bz";
    private final Pattern patternBz;
    private final Pattern patternJbeap;
    private final Aphrodite aphrodite = Aphrodite.instance();
    private String room;
    private String ip;
    private String botName;

    public Bot() throws AphroditeException, IOException, ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader("src/main/resources/ircbotcon.json"));
        JSONObject unitsJson = (JSONObject) obj;
        room = (String) unitsJson.get("room");
        ip = (String) unitsJson.get("ip");
        botName = (String) unitsJson.get("botname");

        this.setName(botName);
        String bz = "\\b" + BZ_PREFIX + "\\d+\\b";
        patternBz = Pattern.compile(bz);
        String jbeap = "\\b" + JBEAP_PREFIX + "\\d+\\b";
        patternJbeap = Pattern.compile(jbeap);
    }

    @Override
    public void onMessage(String channel, String sender,
            String login, String hostname, String message) {
        if (message.contains(BZ_PREFIX)) {
            try {
                printBzUrl(message, channel);

            } catch (Exception ex) {
                Logger.getLogger(Bot.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        } else if (message.contains(JBEAP_PREFIX)) {
            try {
                printJbeapUrl(message, channel);

            } catch (Exception ex) {
                Logger.getLogger(Bot.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String getBotIp() {
        return ip;
    }

    public String getBotRoom() {
        return room;
    }

    private void printBzUrl(String checkstr, String channel) throws Exception {
        Matcher matcher;
        matcher = patternBz.matcher(checkstr);
        if (matcher.find()) {
            String bzName = matcher.group().trim();
            String bzId = getBzId(bzName);
            Issue issue = aphrodite.getIssue(new URL(BZ_URL + bzId));
            sendMessage(channel, printIssue(bzName, issue));
        }
    }

    private void printJbeapUrl(String checkstr, String channel) {
        Matcher matcher;
        matcher = patternJbeap.matcher(checkstr);
        if (matcher.find()) {
            try {
                String jiraName = matcher.group().trim();
                Issue issue = aphrodite.getIssue(new URL(JBEAP_URL + jiraName));
                sendMessage(channel, printIssue(jiraName, issue));

            } catch (NotFoundException | MalformedURLException ex) {
                Logger.getLogger(Bot.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private String getBzId(String bzString) {
        return bzString.substring(BZ_PREFIX.length());
    }

    private String printIssue(String name, Issue issue) {

        StringBuilder sb = new StringBuilder("");
        sb.append("[" + name + "] ").append(issue.getComponents() + " : ").append(issue.getSummary() + ", ").append(issue.getURL());
        return sb.toString();
    }
}
