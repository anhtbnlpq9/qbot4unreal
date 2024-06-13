package xyz.mjav.theqbot;

import java.util.HashMap;
import java.util.Map;

public class UserMask implements Bei {

    /*
     * UserMask format
     * o nick!ident@host
     */
    private static final String    NICK_REGEX = "[A-Za-z0-9\\-\\_\\[\\]\\{\\}\\`\\|\\^\\~\\:\\?\\*]+";
    private static final String   IDENT_REGEX = "[A-Za-z0-9\\-\\~\\.\\?\\*]{1,10}";

    private static Map<String, UserMask> masks = new HashMap<>();

    private final String nick;
    private final String ident;
    private final String host;

    /**
     * Checks for usermask validity (can be either in the format nick!user@host or just nick).
     * Valid usermask:
     *  o nick!user@host
     *  o nick (will be translated to nick!*@*)
     *
     * @param s input usermask
     * @return whether the usermask is valid of not
     */
    public static boolean isValid(String s) {

        /* is it a regular ban */
        /* if id does not contains a ! and @, it may be a nick ban (nick!*@*) */
        if (s.contains("!") == false && s.contains("@") == false) return true;

        /* else it is not a ban */
        if (s.matches(NICK_REGEX + "\\!" + IDENT_REGEX + "\\@.*") == false) return false;
        return true;
    }

    /**
     * Strictly checks for usermask validity (must be in the format nick!user@host)
     * @param s input usermask
     * @return whether the usermask is strictly valid of not
     */
    public static boolean isValidStrict(String s) {
        if (s.matches(NICK_REGEX + "\\!" + IDENT_REGEX + "\\@.*") == false) return false;
        return true;
    }

    private static boolean exists(String s) {
        String mask = s.toLowerCase();
        if (masks.get(mask) == null) return false;
        return true;
    }

    public static UserMask create(String s) {
        String tmpMask = s;

        /* mask for nick ban */
        if (s.contains("!") == false && s.contains("@") == false) tmpMask = s + "!*@*";

        /* rewrite an invalid mask */
        if (isValid(tmpMask) == false)  tmpMask = "invalid!mask@provided";
        if (exists(tmpMask.toLowerCase()) == true) return masks.get(tmpMask.toLowerCase());
        return new UserMask(tmpMask);
    }

    public static int getUsermaskCounter() {
        return masks.size();
    }

    class Builder {
        private String nick  = "invalid";
        private String ident = "mask";
        private String host  = "provided";

        public Builder nick(String val) {
            this.nick = val;
            return this;
        }

        public Builder ident(String val) {
            this.ident = val;
            return this;
        }

        public Builder host(String val) {
            this.host = val;
            return this;
        }

        public UserMask build() {
            return new UserMask(this);
        }
    }

    private UserMask(Builder b) {
        this.nick = b.nick;
        this.ident = b.ident;
        this.host = b.host;
    }

    private UserMask(String s) /*throws InstantiationException*/ {
        /*
         * Input has the format nick!user@host
         */

        String userHost;
        userHost = s.split("!")[1];

        nick   = s.split("!")[0];
        ident  = userHost.split("@")[0];
        host   = userHost.split("@")[1];

        masks.put(this.getFullMask().toLowerCase(), this);

    }

    public boolean matches(Nick nick) {

        Wildcard wildcard = Wildcard.create(this.getString());

        for (UserMask nickum: nick.getAllUserMasks()) {
            if (wildcard.matches(nickum.getString()) == true) return true;
        }

        return false;
    }

    public String getFullMask() {
        return String.format("%s!%s@%s", this.nick, this.ident, this.host);
    }

    public String getNick() {
        return this.nick;
    }

    public String getIdent() {
        return this.ident;
    }

    public String getHost() {
        return this.host;
    }

    @Override public String getString() {
        return this.getFullMask();
    }

    @Override public String toString() {
        return this.getFullMask();
    }

}
