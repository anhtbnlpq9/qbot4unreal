import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
* User account class
* @author me
*/
public class UserAccount {

    private static Logger log = LogManager.getLogger("common-log");

    private SqliteDb sqliteDb;

    private Config config;

    private Integer id;
    private Integer flags;

    private String email;    
    private String name;

    private HashSet<String> certFp;

    private Long registeredTS  = 0L;
    private Long lastAuthTS    = 0L;

    private UUID confirmationCode = null;

    /**
     * HS of the UserNodes loggued with the UserAccount
     * Table to map the SIDs loggued with that UserAccount
     */
    private HashSet<UserNode> attachedUserNodes = new HashSet<UserNode>();

    private HashMap<String, Integer> userChanlev = null;// = new HashMap<String, Integer>();


    interface AuthPassCheck {
        Boolean checkPass(HashMap<String, String> userParam, String userInput);
    }
    interface AuthCertfpCheck {
        Boolean checkCertFp(HashMap<String, String> userParam, String userInput);
    }


    /**
     * Constructor for UserAccount
     * @param sqliteDb database
     * @param userAccountName user account name
     * @param userAccountId user account id
     * @param userFlags user flags
     * @param userAccountEmail user account email
     * @param userAccountCertFP user account certfp
     * @param userAccountRegTS user account registration TS
     */
    public UserAccount(SqliteDb sqliteDb, String userAccountName, Integer userAccountId, Integer userFlags, String userAccountEmail, HashSet<String> userAccountCertFP, Long userAccountRegTS) {
        this.sqliteDb           = sqliteDb;
        this.name    = userAccountName;
        this.id      = userAccountId;
        this.flags   = userFlags;
        this.email   = userAccountEmail;
        this.certFp  = userAccountCertFP;
        this.registeredTS   = userAccountRegTS;

        try {
            this.userChanlev = sqliteDb.getUserChanlev(this); 
        }
        catch (Exception e) { log.error(String.format("UserAccount/constructor: error fetching account chanlev for %s: ", this.getName()) , e); }
    }

    /**
     * Constructor for UserAccount
     * @param sqliteDb database
     * @param userAccountName user account name
     * @param userFlags user flags
     * @param userAccountEmail user account email
     * @param userAccountRegTS user account registration TS
     */
    public UserAccount(SqliteDb sqliteDb, String userAccountName, Integer userFlags, String userAccountEmail, Long userAccountRegTS) {
        this.sqliteDb          = sqliteDb;
        this.name   = userAccountName;
        this.flags  = userFlags;
        this.email  = userAccountEmail;
        this.registeredTS  = userAccountRegTS;

        try {
            this.userChanlev = sqliteDb.getUserChanlev(this); 
        }
        catch (Exception e) { log.error(String.format("UserAccount/constructor: error fetching account chanlev for %s: ", this.getName()) , e); }

        try { this.id = sqliteDb.getAccountId(userAccountName); }
        catch (Exception e) { log.error(String.format("UserAccount/constructor: error fetching account ID for %s: ", this.getName()) , e);  }
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
            log.warn(String.format("UserAccount/addUserAuth: cannot add nick %s to authed list of account %s because it is already in there.", user.getNick(), this.getName()));
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
            log.warn(String.format("UserAccount/delUserAuth: cannot remove nick %s from authed list of account %s because it is not in there.", user.getNick(), this.getName()), e);
            throw new Exception("Cannot remove the usernode from the logged list because the usernode is not is there.");
        }
    }

    /**
     * Sets the user chanlev
     * @param chanlev User chanlev
     */
    public void setChanlev(HashMap<String, Integer> chanlev) {
        this.userChanlev = chanlev;
    }

    /**
     * Sets the user chanlev for the channel
     * @param channel Channel node
     * @param chanlev Chanlev
     */
    public void setChanlev(ChannelNode channel, Integer chanlev) {
        if (this.userChanlev.containsKey(channel.getName()) == true) {
            if (chanlev != 0) {
                this.userChanlev.replace(channel.getName(), chanlev);
            }
            else {
                this.userChanlev.remove(channel.getName());
            }
        }
        else {
            if (chanlev != 0) {
                this.userChanlev.put(channel.getName(), chanlev);
            }
        }
    }

    /**
     * Clear the user chanlev for the channel
     * @param channel channel object
     */
    public void clearUserChanlev(ChannelNode channel) {
        setChanlev(channel, 0);
    }

    /**
     * Fetches the user chanlev for all their known channels
     * @return Full user chanlev
     */
    public HashMap<String, Integer> getChanlev() {
        return this.userChanlev;
    }

    /**
     * Fetches the user chanlev for that channel
     * @param channel Channel node
     * @return Chanlev of the user on that channel
     * @throws Exception
     */
    public Integer getChanlev(ChannelNode channel) {
        if (this.userChanlev.containsKey(channel.getName()) == true) {
            return this.userChanlev.get(channel.getName());
        }
        else return 0;
    }

    /**
     * Returns user account id
     * @return user account id
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Returns the account registration timestamp
     * @return registration timestamp
     */
    public Long getRegTS() {
        return this.registeredTS;
    }

    public void setRegTS(Long ts) {
        this.registeredTS = ts;
    }

    public Long getLastAuthTS() {
        return this.lastAuthTS;
    }

    public void setLastAuthTS(Long ts) {
        this.lastAuthTS = ts;
    }

    /**
     * Returns the user account name
     * @return user account name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the user account certfp
     * @return user certfp
     */
    public HashSet<String> getCertFP() {
        return this.certFp;
    }

    public void setCertFP(HashSet<String> certfpList) {
        this.certFp = certfpList;
    }

    /**
     * Returns the user account email
     * @return user email
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Returns the user flags
     * @return user flags
     */
    public Integer getFlags() {
        return this.flags;
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
    public void setFlags(Integer userflags) {
        this.flags = userflags;
    }

    private Boolean auth(UserNode usernode, String inputValue, Integer authType) throws Exception {

        HashMap<String, String> inputParam;

        try {
            inputParam = sqliteDb.getUser(this);
        }
        catch (Exception e) { 
            log.error(String.format("UserAccount/auth: could not get account information for auth for %s from database: ", this.getName()), e);
            throw new Exception("(EE) auth: cannot get user account information for auth.");
        }
        
        AuthPassCheck checkPass = (userparam, inputpass) -> {

            String pwHash = null;

            try { 
                Base64.Decoder dec = Base64.getDecoder();
                KeySpec spec = new PBEKeySpec(inputpass.toCharArray(), dec.decode(userparam.get("salt")), 65536, 128);
                SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                byte[] hash = f.generateSecret(spec).getEncoded();
                Base64.Encoder enc = Base64.getEncoder();

                pwHash = enc.encodeToString(hash);
            }
            catch (Exception e) { log.error(String.format("UserAccount/auth: error with password check: "), e); }

            if (userparam.get("password").equals(pwHash)) return true;
            else return false;
        };

        AuthCertfpCheck checkCert = (userparam, inputcert) -> {
            if (userparam.get("certfp").matches("(.*)" + inputcert + "(.*)")) return true;
            else return false;
        };

        if (authType.equals(Const.AUTH_TYPE_PLAIN)) {  /* Plain auth (AUTH login pass) */
            return checkPass.checkPass(inputParam, inputValue);
        }
        else if (authType.equals(Const.AUTH_TYPE_CERTFP)) { /* Certfp auth (AUTH login) */
            return checkCert.checkCertFp(inputParam, inputValue);
        }
        else if (authType.equals(Const.AUTH_TYPE_SASL_PLAIN)) { /* SASL PLAIN auth */
            return checkPass.checkPass(inputParam, inputValue);
        }
        else if (authType.equals(Const.AUTH_TYPE_SASL_EXT)) { /* SASL EXTERNAL auth */
            return checkCert.checkCertFp(inputParam, inputValue);
        }
        else {
            return false;
        }
    }

    public void authUserToAccount(UserNode usernode, String inputChallenge, Integer authType) throws Exception {

        if (auth(usernode, inputChallenge, authType) == false) {
            log.warn("Command AUTH (" + Const.getAuthTypeString(authType) + ") failed (incorrect credentials) on user account " + this.getName());
            throw new Exception("(II) Auth failed (invalid credentials): " + this.getName() + " used by nick" + usernode.getMask1());
        }

        if (Flags.isUserSuspended(this.getFlags()) == true) {
            log.warn("Command AUTH (" + Const.getAuthTypeString(authType) + ") failed (suspended account) on user account " + this.getName());
            throw new Exception("(II) Auth failed (account suspended): " + this.getName() + " used by nick " + usernode.getMask1());
        }

        usernode.setUserAuthed(true);
        usernode.setAccount(this);
        try {
            sqliteDb.addUserAuth(usernode, authType);
        }
        catch (Exception e) {
            log.error(String.format("UserAccount/addAuthUserToAccount: cannot add nick to database authed users for account %s: ", this.getName()), e);
            throw new Exception("Command AUTH: Error finalizing the auth: nick = " + usernode.getMask1() + ", account = " + this.getName());
        }
    }

    public void deAuthUserFromAccount(UserNode usernode, Integer deAuthType) throws Exception {

        try { sqliteDb.delUserAuth(usernode, deAuthType, ""); }
        catch (Exception e) {
            log.error(String.format("UserAccount/delAuthUserFromAccount: cannot remove nick %s from database authed users for account %s: ", usernode.getNick(), this.getName()), e);
            throw new Exception("(EE) auth: Error finalizing the deauth, user may still be authed: nick = " + usernode.getNick() + ", account = " + this.getName());
        }
        usernode.setAccount(null);
        usernode.setUserAuthed(false);

    }

    public void setConfigRef(Config config) {
        this.config = config;
    }

    public void setConfirmationCode(UUID uuid) {
        this.confirmationCode = uuid;
    }

    public UUID getConfirmationCode() {
        return this.confirmationCode;
    }
}
