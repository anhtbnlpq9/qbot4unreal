import java.util.HashMap;
import java.util.HashSet;

/**
* User account class
* @author me
*/
public class UserAccount {

    private SqliteDb sqliteDb;

    private String  userAccountFlags;
    private Integer  userAccountId;
    private String   userAccountName;
    private String   userAccountCertFP;
    private String   userAccountEmail;

    /**
     * HS of the UserNodes loggued with the UserAccount
     * Table to map the SIDs loggued with that UserAccount
     */
    private HashSet<UserNode> attachedUserNodes;

    /**
     * HS of the nicks attached to the UserAccount
     * Table used for nick registration/protection feature
     */
    private HashSet<String>   attachedUserNicks;

    /**
     * HM of the previously authed SIDs to the UserAccount, from the db
     * Table used to restore auth after Chanserv disconnect
     */
    private HashMap<String, Integer> attachedLogins;

    private HashMap<String, Integer> userChanlev = new HashMap<String, Integer>();


    /**
     * Class constructor
     */
    public UserAccount() {
    }

    /**
     * Class constructor
     * @param userAccountName User account name
     * @param userAccountId User account number
     */
    public UserAccount(SqliteDb sqliteDb, String userAccountName, Integer userAccountId) {
        this.sqliteDb = sqliteDb;
        this.userAccountName = userAccountName;
        this.userAccountId = userAccountId;

        try { this.userChanlev = sqliteDb.getUserChanlev(userAccountName); }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: could not retrieve chanlev");
        }
        
        try { this.userAccountFlags = sqliteDb.getUserFlags(userAccountName); }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: could not retrieve flags");
        }

        try { this.userAccountEmail = sqliteDb.getUserEmail(userAccountName); }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: could not retrieve email");
        }

        try { this.userAccountCertFP = sqliteDb.getUserCertFP(userAccountName); }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: could not retrieve email");
        }
    
    }

    public UserAccount(SqliteDb sqliteDb, String userAccountName, Integer userAccountId, Integer userFlags, String userAccountEmail, String userAccountCertFP) {
        this.sqliteDb = sqliteDb;
        this.userAccountName = userAccountName;
        this.userAccountId = userAccountId;
        this.userAccountFlags = userFlags;
        this.userAccountEmail = userAccountEmail;
        this.userAccountCertFP = userAccountCertFP;

        try { this.userChanlev = sqliteDb.getUserChanlev(userAccountName); }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: could not retrieve chanlev");
        }

        try { this.userChanlev = sqliteDb.getUserLoginTokens(userAccountName); }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: could not retrieve tokens");
        }
    }

    /**
     * Adds the UserNode to the UserAccount login tracker
     * @param user User node
     */
    public void addUserAuth(UserNode user) throws Exception {
        if (this.attachedUserNodes.contains(user) == false) {
            this.attachedUserNodes.add(user);
        }
        else {
            throw new Exception("Cannot add the usernode to the list because it is already in there");
        }
    }

    /**
     * Removes the UserNode from the UserAccount login tracker
     * @param user User node
     * @throws Exception
     */
    public void delUserAuth(UserNode user) throws Exception {
        try {
            this.attachedUserNodes.remove(user);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Cannot remove the usernode from the logged list because the usernode is not is there.");
        }
    }

    /**
     * Adds the Nick to the UserAccount
     * @param user User nick
     */
    public void addUserNick(String nick) throws Exception {
        if (this.attachedUserNicks.contains(nick) == false) {
            this.attachedUserNicks.add(nick);
        }
        else {
            throw new Exception("Cannot add the nick to the list because it is already in there");
        }
    }

    /**
     * Removes the Nick from the UserAccount
     * @param user User nick
     * @throws Exception
     */
    public void delUserNick(String nick) throws Exception {
        try {
            this.attachedUserNicks.remove(nick);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Cannot remove the nick from the attached nicklist because the nick is not is there.");
        }
    }

    /**
     * Sets the user chanlev
     * @param chanlev User chanlev
     */
    public void setUserChanlev(HashMap<String, Integer> chanlev) {
        this.userChanlev = chanlev;
    }

    /**
     * Sets the user chanlev for the channel
     * @param channel Channel node
     * @param chanlev Chanlev
     */
    public void setUserChanlev(String channel, Integer chanlev) {
        if (this.userChanlev.containsKey(channel) == true) {
            if (chanlev != 0) {
                this.userChanlev.replace(channel, chanlev);
            }
            else {
                this.userChanlev.remove(channel);
            }
        }
        else {
            if (chanlev != 0) {
                this.userChanlev.put(channel, chanlev);
            }
        }
    }

    /**
     * Fetches the user chanlev for all their known channels
     * @return Full user chanlev
     */
    public HashMap<String, Integer> getUserChanlev() {
        return this.userChanlev;
    }

    /**
     * Fetches the user chanlev for that channel
     * @param channel Channel node
     * @return Chanlev of the user on that channel
     * @throws Exception
     */
    public Integer getUserChanlev(ChannelNode channel) throws Exception {
        try {
            return this.userChanlev.get(channel);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Cannot fetch chanlev because it does not exist for that (user, channel).");
        }
    }


}
