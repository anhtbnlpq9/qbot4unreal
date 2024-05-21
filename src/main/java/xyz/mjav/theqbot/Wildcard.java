package xyz.mjav.theqbot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to manage wildcards and match them against regular strings.
 * Supported wildcards:
 * o * (matches any character or no character)
 * o ? (matches exactly one character)
 * o _ (matches a space or _)
 *
 * Note: Matches are performed case insensitive
 */
public class Wildcard {

    /**
     * Inspired from <https://stackoverflow.com/questions/24337657/wildcard-matching-in-java>
     * @param s input wildcard
     * @return regexed wildcard
     */
    public static String wildcardToRegex(String s) {
        String sLower;
        String replaced;
        Pattern regex;
        Matcher m;
        StringBuffer b;

        sLower = s.toLowerCase();
        regex = Pattern.compile("[^*?_]+|(\\*)|(\\?)|(_)");
        m = regex.matcher(sLower);
        b = new StringBuffer();

        while (m.find()) {
            if(m.group(1) != null) m.appendReplacement(b, ".*");
            else if(m.group(2) != null) m.appendReplacement(b, ".");
            else if(m.group(3) != null) m.appendReplacement(b, "(\s|_)");
            else m.appendReplacement(b, "\\\\Q" + m.group(0) + "\\\\E");
        }

        m.appendTail(b);
        replaced = b.toString();
        return replaced;
    }

    /**
     * Checks for a match between an input string and a wildcard string
     * @param in
     * @param wildcard
     * @return
     */
    public static boolean match(String in, String wildcard) {
        Wildcard w = Wildcard.create(wildcard);
        return w.matches(in);
    }

    /**
     * Creates a wildcard
     * @param s input wildcard string
     * @return a wildcard
     */
    public static Wildcard create(String s) {
        return new Wildcard(s.toLowerCase());
    }

    /**
     * Checks if the input strnig matches the wildcard
     * @param s input string
     * @return whether the wildcard matches or not
     */
    public boolean matches(String s) {
        if (s.toLowerCase().matches(this.getRegex()) == true) return true;
        return false;
    }

    /** Stores the regex derived from the wildcard */
    private String regex;

    /** Stores the original wildcard */
    private String wildcard;

    /**
     * Wildcard constructor
     * @param s input wildcard
     */
    private Wildcard(String s) {
        this.regex = wildcardToRegex(s);
        this.wildcard = s;
    }

    /**
     * Gets the wildcard regex value
     * @return regex value
     */
    public String getRegex() {
        return this.regex;
    }

    @Override public String toString() {
        return this.wildcard;
    }

}
