package xyz.mjav.theqbot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.Exception;
import java.lang.String;
import java.lang.Thread;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocketFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Client class
 */
public class Client implements Runnable {

    private static Logger log     = LogManager.getLogger("common-log");
    private static Logger trafLog = LogManager.getLogger("trafficLog");

    private Config config;

    private Socket clientSocket;

    private BufferedReader in;

    private BufferedWriter out;

    private Boolean isClientReady = false;

    private Protocol protocol;

    private Database database;

    private CService cservice;

    private OService oservice;

    private String clientId;

    /**
     * Client constructor
     * @param config
     * @param database
     */
    public Client(Config config, Database database) {
        this.config   = config;
        this.database = database;

        trafLog.debug("================================");
        trafLog.debug("=      New client session      =");
        trafLog.debug("================================");
    }

    /**
     * Returns the reference to the client object
     * @return client object
     */
    public Client getClientRef() {
        return this;
    }

    /**
     * Starts the client thread
     */
    public void run() {
        log.info("Starting client thread");

        RandomString rds = new RandomString(32);
        this.clientId = rds.nextString();

        try {
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            clientSocket = ssf.createSocket(config.getLinkPeerHost(), config.getLinkPeerPort());

            /*SSLSession session = ((SSLSocket) clientSocket).getSession();*/
            /*Certificate[] cchain = session.getPeerCertificates();*/

            log.info("Connected");

            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            /* The socket is connected to the server, we can send the first data into it */
            log.info("Client is ready");
            this.isClientReady = true;

            switch(config.getNetworkProtocol()) {
                case "unrealircd": protocol = new UnrealIRCd(config, database, this); break;
                /* case "inspircd":   protocol = new InspIRCd(config, database); break; */

                default: {
                    log.fatal("Protocol is not defined! Quitting.");
                    System.exit(0);
                }
            }

            String str;
            while ((str = in.readLine()) != null) {
                if (config.hasLogging("debugIn") == true) trafLog.debug("<<< " + censorInTrafficCred(str));
                protocol.getResponse(str);
            }
            throw new Exception("Connection has been closed.");
        }
        catch (Exception e) {
            log.fatal(String.format("Client/run: Error during client runtime. Trying to restart client."), e);
            Thread.currentThread().interrupt();
            try { Thread.sleep(5000); }
            catch (Exception f) { }

            Qbot.runClient(config, database);
        }
    }

    /**
     * Starts channel service
     */
    public void launchCService() {

        cservice = new CService(protocol, database);
        log.info("CService is going online.");
        cservice.runCService(config, protocol);

    }


    /**
     * Starts oper service
     */
    public void launchOService() {

        oservice = new OService(protocol, database);
        log.info("OService is going online.");
        oservice.runOService(config, protocol);

    }

    /**
     * Writes data into the socket
     * @param str string to send
     */
    public void write(String str) {
		try {
            if (config.hasLogging("debugOut") == true) { trafLog.debug(">>> " + str); }

            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			out.write(str + "\n");
			out.flush();
		}
        catch (IOException e) { log.error(String.format("Client/write: Error writing into socket: "), e); }
    }

    /**
     * Returns whether the client is readu or not
     * @return true if the client is ready, else false
     */
    public Boolean isReady() {
        return this.isClientReady;
    }

    /**
     * Filters strings that are input for "sensitive" commands such as PRIVMSG (that can contain credentials)
     * @param str
     * @return
     */
    private String censorInTrafficCred(String str) {
        /*
         * Examples:
         * @ircv3tag :fromWho PRIVMSG toWho :AUTH myUserName myPassword
         *   would be logged as @ircv3tag :fromWho PRIVMSG toWho :AUTH *****
         * Sensitive information to filter:
         *  - user names (not sensitive but makes the filter of the rest simplier)
         *  - passwords
         *  - email addresses
         */

        String[] splitStr;

        String regex;
        String command;
        String outputString;

        Boolean hasAMatch = false;

        Pattern pattern;
        Matcher matcher;

        /*
         * We want to have:
         * - :XXXXXXXXX PRIVMSG (theBot) :AUTH (theUser) (thePass) -> :XXXXXXXXX PRIVMSG (theBot) :AUTH *****
         * - :XXXXXXXXX PRIVMSG (theBot) :HELLO (emailAddress) -> :XXXXXXXXX PRIVMSG (theBot) :HELLO *****
         * - :YYY(XXXXXX) SASL (theBot) (theBotServer) (theUserId) (saslArgument) -> :YYY(XXXXXX) SASL (theBotServer) (theUserId) *****
         */
        regex = "^(HELLO|AUTH|SASL)";
        pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        splitStr = str.split(" ");

        try { command = splitStr[4].replaceAll("^[:]", ""); }
        catch (IndexOutOfBoundsException e) { return str; } /* If we catch this exception, it is likely that this is not a sensitive message */

        matcher = pattern.matcher(command);

        hasAMatch = matcher.find();

        if (hasAMatch == true) {
            /* Has matched a message beginning with a sensitive command => censoring some of the content */
            outputString = String.format("%s %s %s %s %s *****", splitStr[0], splitStr[1], splitStr[2], splitStr[3], splitStr[4]);
        }
        else { outputString = str; } /* No match => not sensitive information */

        return outputString;
    }

    public String getClientId() {
        return this.clientId;
    }
}
