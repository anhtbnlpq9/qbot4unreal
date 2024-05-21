package xyz.mjav.theqbot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import xyz.mjav.theqbot.exceptions.AccountNotFoundException;
import xyz.mjav.theqbot.exceptions.ItemExistsException;
import xyz.mjav.theqbot.exceptions.ItemNotFoundException;
import xyz.mjav.theqbot.exceptions.ItemSuspendedException;
import xyz.mjav.theqbot.exceptions.NickNotFoundException;
import xyz.mjav.theqbot.exceptions.UserAuthCredException;
import xyz.mjav.theqbot.exceptions.UserAuthException;
import xyz.mjav.theqbot.exceptions.UserNoAuthException;

/**
* User account class
*/
public class UserAccount extends Account implements Comparable<UserAccount> {

    private static Database database;

    private static Set<UserAccount> accountList = new HashSet<>();

    private static Map<String, UserAccount>   accountListByName = new HashMap<>();
    private static Map<Integer, UserAccount>  accountListById   = new HashMap<>();

    /** User id */
    private Integer id;

    /** User flags */
    private Integer flags;

    /** Suspend counter */
    //private Integer suspendCount;

    /** User email */
    private String email;

    /** User name (login) */
    //private String name;

    /** Suspend reason */
    //private String suspendReason;

    /** Set of all the certfps associated with the account */
    private Set<String> certFPs;

    /** Last user auth TS */
    private Timestamp authLastTS;

    /** Last user email change TS */
    private Timestamp emailLastTS;

    /** Last user password change TS */
    private Timestamp passLastTS;

    /** Set of the Nick loggued with the UserAccount */
    private Set<Nick> attachedNicks;

    /** Map of the user chanlev. Map Channel name -> Chanlev value */
    private Map<String, Integer> userChanlev;

    /** Map of attached nickname aliases */
    private Map<String, NickAlias> nickAliases;

    /** Interface to check the password challenge regarding user authentication */
    private interface AuthPassCheck {
        Boolean checkPass(Map<String, String> userParam, String userInput);
    }

    /**
     * Interface to check the certfp challenge regarding user authentication
     */
    private interface AuthCertfpCheck {
        Boolean checkCertFp(Map<String, String> userParam, String userInput);
    }

    /**
     * Adds an user account to the memory account list
     * @param u user account
     * @throws ItemExistsException when the user account is already in the list
     */
    public static void addUser(UserAccount u) throws ItemExistsException {

        if(accountList.contains(u) == true)  throw new ItemExistsException("The nick is already registered on the network.");

        accountList.add(u);
        accountListByName.put(u.name, u);
        accountListById.put(u.id, u);
    }

    /**
     * Removes an user account from the memory account list
     * @param u user account
     * @throws ItemNotFoundException when the user account to remove is not in the list
     */
    public static void removeUser(UserAccount u) throws ItemNotFoundException {

        if(accountList.contains(u) == false) throw new ItemNotFoundException("The nick is not registered on the network.");

        accountList.remove(u);
        accountListByName.remove(u.name);
        accountListById.remove(u.id);
    }

    /**
     * Returns the user account by its id
     * @param i user account id
     * @return user account
     * @throws ItemNotFoundException when the user account is not found
     */
    public static UserAccount getUser(Integer i) throws ItemNotFoundException {
        if (accountListById.containsKey(i) == false) throw new ItemNotFoundException();
        return accountListById.get(i);
    }

    /**
     * Returns the user account by its name (case insensitive)
     * @param name user account name
     * @return user account
     * @throws ItemNotFoundException when the user account is not found
     */
    public static UserAccount getUserByNameCi(String name) throws ItemNotFoundException {
        if (accountListByName.containsKey(name.toLowerCase()) == false) throw new ItemNotFoundException();
        return accountListByName.get(name.toLowerCase());
    }

    /**
     * Returns a Set of account list
     * @return a copy of account list Set
     */
    public static Set<UserAccount> getUserList() {
        return new HashSet<UserAccount>(accountList);
    }

    /**
     * Sets the user list
     * @param l user list Set
     */
    public static void setUserList(Set<UserAccount> l) {
        accountList = new HashSet<UserAccount>(l);

        /* Then populates the other lists */
        accountList.forEach(
            (u) -> {
                accountListById.put(u.id, u);
                accountListByName.put(u.name.toLowerCase(), u);
            }
        );
    }

    /**
     * Static method to create an anonymous user account
     * @param id id of account to create
     * @return anonymous user accound
     */
    public static UserAccount anonymous(Integer id) {
        return new UserAccount(id, String.format("Unknown-%s", id));
    }


    /**
     * Builder class for UserAccount
     */
    public static class Builder extends Account.Builder {

        private Integer id            = 0;
        private Integer flags         = 0;

        private String  email          = "";

        private Set<String> certFPs = new HashSet<String>();

        private Map<String, Integer> userChanlev = new HashMap<String, Integer>();

        private Map<String, NickAlias> nickAliases = new HashMap<>();

        private Set<Nick> attachedUserNodes = new HashSet<Nick>();

        private Timestamp authLastTS      = new Timestamp(0L);
        private Timestamp emailLastTS     = new Timestamp(0L);
        private Timestamp passLastTS      = new Timestamp(0L);

        /**
         * Sets the user id
         * @param val user id
         * @return Builder
         */
        public Builder id(Integer val) {
            id = val;
            return this;
        }

        /**
         * Sets the user flags
         * @param val user flags
         * @return Builder
         */
        public Builder flags(Integer val) {
            flags = val;
            return this;
        }

        /**
         * Sets the email address
         * @param val email address
         * @return Builder
         */
        public Builder email(String val) {
            email = val;
            return this;
        }

        /**
         * Sets the user name
         * @param val user name
         * @return Builder
         */
        public Builder name(String val) {
            super.name(val);
            return this;
        }

        public Builder registered() {
            super.registered();
            return this;
        }

        /**
         * Sets the registration timestamp
         * @param val timestamp
         * @return Builder
         */
        public Builder registrationTS(Timestamp val) {
            //registrationTS = val;
            super.registrationTS(val);
            return this;
        }

        /**
         * Sets the last authentication timestamp
         * @param val timestamp
         * @return Builder
         */
        public Builder authLastTS(Timestamp val) {
            authLastTS = val;
            return this;
        }

        /**
         * Sets the email last change timestamp
         * @param val timestamp
         * @return Builder
         */
        public Builder emailLastTS(Timestamp val) {
            emailLastTS = val;
            return this;
        }

        /**
         * Sets the password last change timestamp
         * @param val timestamp
         * @return Builder
         */
        public Builder passLastTS(Timestamp val) {
            passLastTS = val;
            return this;
        }

        /**
         * Sets the user certfp
         * @param val certfp
         * @return Builder
         */
        public Builder certFPs(Set<String> val) {
            this.certFPs = val;
            return this;
        }

        /**
         * Sets the user chanlev
         * @param val user chanlev
         * @return Builder
         */
        public Builder userChanlev(Map<String, Integer> val) {
            this.userChanlev = val;
            return this;
        }

        public Builder nickAliases(Map<String, NickAlias> val) {
            this.nickAliases = val;
            return this;
        }

        /**
         * Creates the user account
         * @return user account
         */
        public UserAccount build(){
            return new UserAccount(this);
        }

    }

    /**
     * Private constructor to build the object from the Builder
     * @param builder
     */
    private UserAccount(Builder builder) {
        super(builder);
        this.id                   = builder.id;
        this.flags                = builder.flags;
        this.email                = builder.email;
        this.name                 = builder.name;
        this.authLastTS           = builder.authLastTS;
        this.emailLastTS          = builder.emailLastTS;
        this.passLastTS           = builder.passLastTS;
        this.certFPs              = builder.certFPs;
        this.userChanlev          = builder.userChanlev;
        this.attachedNicks        = builder.attachedUserNodes;
        this.nickAliases          = builder.nickAliases;

        try { addUser(this); }
        catch (ItemExistsException e) {
            log.error(String.format("UserAccount::UserAccount: cannot add the account %s because it already exist in the list.", this.name), e);
        }
    }

    private UserAccount(Integer id, String name) {
        super(name);
        this.id = id;
    }

    /**
     * Adds the UserNode to the UserAccount login tracker
     * @param user User node
     */
    public void addUserAuth(Nick user) throws ItemExistsException {
        if (this.attachedNicks.contains(user) == false) {
            this.attachedNicks.add(user);
        }
        else {
            log.warn(String.format("UserAccount/addUserAuth: cannot add nick %s to authed list of account %s because it is already in there.", user.getNick(), this.getName()));
            throw new ItemExistsException("Cannot add the usernode to the list because it is already in there");
        }
    }

    /**
     * Removes the UserNode from the UserAccount login tracker
     * @param user User node
     * @throws Exception
     */
    public void delUserAuth(Nick user) throws Exception {
        try {
            this.attachedNicks.remove(user);
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
    public void setChanlev(Map<String, Integer> chanlev) {
        this.userChanlev = chanlev;
    }

    /**
     * Sets the user chanlev for the channel
     * @param channel Channel node
     * @param chanlev Chanlev
     */
    public void setChanlev(Channel channel, Integer chanlev) {
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
    public void clearUserChanlev(Channel channel) {
        setChanlev(channel, 0);
    }

    /**
     * Fetches the user chanlev for all their known channels
     * @return Full user chanlev
     */
    public Map<String, Integer> getChanlev() {
        return this.userChanlev;
    }

    /**
     * Fetches the user chanlev for that channel
     * @param channel Channel node
     * @return Chanlev of the user on that channel
     * @throws Exception
     */
    public Integer getChanlev(Channel channel) {
        if (this.userChanlev.containsKey(channel.getName()) == true) { return this.userChanlev.get(channel.getName()); }
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
    //public Timestamp getRegistrationTS() {
    //    return this.registrationTS;
    //}

    /**
     * Sets the account registration timestamp
     * @param ts timestamp
     */
    //public void setRegistrationTS(Timestamp ts) {
    //    this.registrationTS = ts;
    //}

    /**
     * Gets the last auth timestamp
     * @return timestamp
     */
    public Timestamp getAuthLastTS() {
        return this.authLastTS;
    }

    /**
     * Sets the last auth timestamp
     * @param ts timestamp
     */
    public void setAuthLastTS(Timestamp ts) {
        this.authLastTS = ts;
    }



    /**
     * Returns the user account certfp
     * @return user certfp
     */
    public Set<String> getCertFP() {
        return this.certFPs;
    }

    /**
     * Sets the user certfps as a list
     * @param certfpList list of the user certfps
     */
    public void setCertFP(Set<String> certfpList) {
        this.certFPs = certfpList;
    }

    /**
     * Adds the certfp to the user account
     * @param certfp certfp to add
     */
    public void addCertFP(String certfp) {
        this.certFPs.add(certfp);
    }

    /**
     * Deletes the certfp from the user account
     * @param certfp certfp to delete
     */
    public void removeCertFP(String certfp) {
        this.certFPs.remove(certfp);
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
    public Set<Nick> getUserLogins() {
        return this.attachedNicks;
    }

    /**
     * Sets the user flags
     * @param flags user flags
     */
    public void setFlags(Integer flags) {
        this.flags = flags;
    }

    /**
     * Sets the user id
     * @param id user id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Auths the user regarding the provided credentials
     * @param usernode user node
     * @param inputValue input challenge
     * @param authType authentication type
     * @return true if the authentication has succeded, false otherwise
     * @throws UserAuthException if there have been an issue during the authentication process
     */
    private Boolean auth(Nick usernode, String inputValue, Integer authType) throws UserAuthException {

        Map<String, String> inputParam;

        try { inputParam = database.getUserInfoByNameCi(this); }
        catch (Exception e) {
            log.error(String.format("UserAccount/auth: could not get account information for auth for %s from database: ", this.getName()), e);
            throw new UserAuthException("(EE) auth: cannot get user account information for auth.");
        }

        AuthPassCheck checkPass = (userparam, inputpass) -> {
            try {
                Argon2Hash pwCheck = new Argon2Hash(userparam.get("salt"));

                if (pwCheck.checkHash(userparam.get("password"), inputpass) == true) return true;
                else return false;
            }
            catch (Exception e) { log.error(String.format("UserAccount/auth: error with password check: "), e); return false; }

        };

        AuthCertfpCheck checkCert = (userparam, inputcert) -> {
            try {
                if (userparam.get("certfp").matches("(.*)" + inputcert + "(.*)")) return true;
                else return false;
            }
            catch (Exception e) { return false; }
        };

        /* Plain auth (AUTH login pass) */
        if (authType.equals(Const.AUTH_TYPE_PLAIN))  return checkPass.checkPass(inputParam, inputValue);

        /* Certfp auth (AUTH login) */
        else if (authType.equals(Const.AUTH_TYPE_CERTFP)) return checkCert.checkCertFp(inputParam, inputValue);

        /* SASL PLAIN auth */
        else if (authType.equals(Const.AUTH_TYPE_SASL_PLAIN)) return checkPass.checkPass(inputParam, inputValue);

        /* SASL EXTERNAL auth */
        else if (authType.equals(Const.AUTH_TYPE_SASL_EXT)) return checkCert.checkCertFp(inputParam, inputValue);

        else return false;
    }

    /**
     * Authenticates an user from its account
     * @param usernode user node
     * @param inputChallenge password or certfp challenge
     * @param authType type of authentication
     * @throws UserAuthCredException if the credentials check fails
     * @throws ItemSuspendedException if the user account is suspended
     * @throws UserAuthException if the user cannot be authenticated
     */
    public void authUserToAccount(Nick usernode, String inputChallenge, Integer authType) throws UserAuthCredException, ItemSuspendedException, UserAuthException {

        Boolean authUserTry = false;

        log.debug(String.format("UserAccount::authUserToAccount: auth request for nick %s and auth type %s wants to auth as %s", usernode.getNick(), authType, this.getName()));

        try { authUserTry = auth(usernode, inputChallenge, authType); }
        catch (Exception e) { authUserTry = false; }

        if (authUserTry == false) {
            log.warn("UserAccount::authUserToAccount: (" + Const.getAuthTypeString(authType) + ") failed (incorrect credentials) on user account " + this.getName());
            throw new UserAuthCredException("(II) Auth failed (invalid credentials): " + this.getName() + " used by nick" + usernode.getMask("nick!ident@realhost+ip"));
        }

        if (Flags.isUserSuspended(this.getFlags()) == true) {
            log.warn("UserAccount::authUserToAccount: (" + Const.getAuthTypeString(authType) + ") failed (suspended account) on user account " + this.getName());
            throw new ItemSuspendedException("(II) Auth failed (account suspended): " + this.getName() + " used by nick " + usernode.getMask("nick!ident@realhost+ip"));
        }

        usernode.setAuthed(true);
        usernode.setAccount(this);
        try { database.addUserAuth(usernode, authType); }
        catch (Exception e) {
            log.error(String.format("UserAccount/addAuthUserToAccount: cannot add nick to database authed users for account %s: ", this.getName()), e);
            throw new UserAuthException("Command AUTH: Error finalizing the auth: nick = " + usernode.getMask("nick!ident@realhost+ip") + ", account = " + this.getName());
        }
        log.debug(String.format("UserAccount::authUserToAccount: nick %s with auth type %s successfully authed as %s", usernode.getNick(), authType, this.getName()));
    }

    /**
     * De-authenticates an user from its account
     * @param usernode user node
     * @param deAuthType type of deauthentication
     * @throws UserAuthException if the user cannot be unauthenticated
     */
    public void deAuthUserFromAccount(Nick usernode, Integer deAuthType) throws UserAuthException {

        try { database.delUserAuth(usernode, deAuthType, ""); }
        catch (Exception e) {
            log.error(String.format("UserAccount/delAuthUserFromAccount: cannot remove nick %s from database authed users for account %s: ", usernode.getNick(), this.getName()), e);
            throw new UserAuthException("(EE) auth: Error finalizing the deauth, user may still be authed: nick = " + usernode.getNick() + ", account = " + this.getName());
        }
        usernode.unsetAccount();
        usernode.setAuthed(false);

    }

    /**
     * Returns the email last change timestamp
     * @return timestamp
     */
    public Timestamp getEmailLastTS() {
        return this.emailLastTS;
    }

    /**
     * Gets the timestamp for the last password change
     * @return last password change timestamp
     */
    public Timestamp getPassLastTS() {
        return this.passLastTS;
    }

    /**
     * Sets the reference to the Database
     * @param d Database object
     */
    public static void setDatabase(Database d) {
        database = d;
    }

    public void setNickAliases(Map<String, NickAlias> set) {
        this.nickAliases = set;
    }

    public void addNickAlias(NickAlias n) {
        this.nickAliases.put(n.getAlias(), n);
    }

    public void removeNickAlias(NickAlias n) {
        this.nickAliases.remove(n.getAlias());
    }

    public Map<String, NickAlias> getNickAlias() {
        return new TreeMap<>(this.nickAliases);
    }

    public NickAlias getNickAlias(String nick) {
        return nickAliases.get(nick);
    }

    public static UserAccount getUserByNameOrNickCi(String s) throws NickNotFoundException, AccountNotFoundException, UserNoAuthException {
        if (s.startsWith(Const.USER_ACCOUNT_PREFIX) == true) {
            try { return getUserByNameCi(s.replaceFirst(String.valueOf(s.charAt(0)), "")); }
            catch (ItemNotFoundException e) { throw new AccountNotFoundException(); }
        }
        else {
            try { return getUserByNickCi(s); }
            catch (NickNotFoundException e) { throw e; }
            catch (UserNoAuthException e) { throw e; }
        }
    }

    public static UserAccount getUserByNickCi(String s) throws UserNoAuthException, NickNotFoundException {
        Nick nick;

        try { nick = Nick.getUserByNickCi(s); }
        catch (NickNotFoundException e) { throw e; }

        if (nick.isAuthed() == false) throw new UserNoAuthException("user not authed");

        return nick.getAccount();
    }



    @Override public int compareTo(UserAccount u) {
        return this.name.compareTo(u.getName());
    }

}
