import java.util.HashMap;
import java.util.HashSet;

/**
* User account class
* @author me
*/
public class UserAccount {

    private SqliteDb sqliteDb;

    private Integer  userAccountId;
    private String   userAccountName;
    private String   userAccountCertFP;
    private Integer  userAccountFlags;
    private String   userAccountEmail;

    /**
     * HS of the UserNodes loggued with the UserAccount
     * Table to map the SIDs loggued with that UserAccount
     */
    private HashSet<UserNode> attachedUserNodes = new HashSet<UserNode>();

    /**
     * HM of the previously authed SIDs to the UserAccount, from the db
     * Table used to restore auth after Chanserv disconnect
     */
    private HashMap<String, Integer> attachedLoginTokens;

    private HashMap<String, Integer> userChanlev = null;// = new HashMap<String, Integer>();

    /**
     * Constructor for UserAccount
     * @param sqliteDb database
     * @param userAccountName user account name
     * @param userAccountId user account id
     * @param userFlags user flags
     * @param userAccountEmail user account email
     * @param userAccountCertFP user account certfp
     */
    public UserAccount(SqliteDb sqliteDb, String userAccountName, Integer userAccountId, Integer userFlags, String userAccountEmail, String userAccountCertFP, Long userAccountRegTS) {
        this.sqliteDb = sqliteDb;
        this.userAccountName = userAccountName;
        this.userAccountId = userAccountId;
        this.userAccountFlags = userFlags;
        this.userAccountEmail = userAccountEmail;
        this.userAccountCertFP = userAccountCertFP;
        this.userAccountRegTS = userAccountRegTS;

        try {
            this.userChanlev = sqliteDb.getUserChanlev(userAccountName); 
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: could not retrieve chanlev");
        }

        try { this.attachedLoginTokens = sqliteDb.getUserLoginTokens(userAccountId); }
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
     * Sets the user chanlev
     * @param chanlev User chanlev
     */
    public void setUserChanlev(HashMap<String, Integer> chanlev) {
        //System.out.println("BFN");
        this.userChanlev = chanlev;
    }

    /**
     * Sets the user chanlev for the channel
     * @param channel Channel node
     * @param chanlev Chanlev
     */
    public void setUserChanlev(ChannelNode channel, Integer chanlev) {
        //System.out.println("BFL");
        if (this.userChanlev.containsKey(channel.getChanName()) == true) {
            if (chanlev != 0) {
                this.userChanlev.replace(channel.getChanName(), chanlev);
            }
            else {
                this.userChanlev.remove(channel.getChanName());
            }
        }
        else {
            if (chanlev != 0) {
                this.userChanlev.put(channel.getChanName(), chanlev);
            }
        }
    }

    /**
     * Clear the user chanlev for the channel
     * @param channel channel object
     */
    public void clearUserChanlev(ChannelNode channel) {
        //System.out.println("BFM");
        setUserChanlev(channel, 0);
    }

    /**
     * Fetches the user chanlev for all their known channels
     * @return Full user chanlev
     */
    public HashMap<String, Integer> getUserChanlev() {
        //this.userChanlev.forEach( (chan, chanlev) -> { System.out.println("BFJ chan=" + chan + " chanlev=" + chanlev); });
        return this.userChanlev;
    }

    /**
     * Fetches the user chanlev for that channel
     * @param channel Channel node
     * @return Chanlev of the user on that channel
     * @throws Exception
     */
    public Integer getUserChanlev(ChannelNode channel) {
            return this.userChanlev.get(channel.getChanName());
    }

    /**
     * Returns user account id
     * @return user account id
     */
    public Integer getUserAccountId() {
        return this.userAccountId;
    }

    /**
     * Returns the user account name
     * @return user account name
     */
    public String getUserAccountName() {
        return this.userAccountName;
    }

    /**
     * Returns the user account certfp
     * @return user certfp
     */
    public String getUserAccountCertFP() {
        return this.userAccountCertFP;
    }

    /**
     * Returns the user account email
     * @return user email
     */
    public String getUserAccountEmail() {
        return this.userAccountEmail;
    }

    /**
     * Returns the user flags
     * @return user flags
     */
    public Integer getUserAccountFlags() {
        return this.userAccountFlags;
    }

    /**
     * Returns the user logins (attached nicks to the account)
     * @return user nodes
     */
    public HashSet<UserNode> getUserLogins() {
        return this.attachedUserNodes;
    }

    /**
     * Sets the user flags
     * @param userflags user flags
     */
    public void setUserAccountFlags(Integer userflags) {
        this.userAccountFlags = userflags;
    }

}
