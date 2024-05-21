package xyz.mjav.theqbot;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestMatch {

    private static String matcher(String s) {
        Pattern regex = Pattern.compile("[^*?]+|(\\*)|(\\?)");
        Matcher m = regex.matcher(s);
        StringBuffer b= new StringBuffer();
        while (m.find()) {
            if(m.group(1) != null) m.appendReplacement(b, ".*");
            else if(m.group(2) != null) m.appendReplacement(b, ".");
            else m.appendReplacement(b, "\\\\Q" + m.group(0) + "\\\\E");
        }
        m.appendTail(b);
        String replaced = b.toString();
        return replaced;
    }

    private static boolean match(String um, String input) {
        if (input.matches(matcher(um)) == true) return true;
        return false;
    }
    public static void main(String[] args) {
        String mask = "n?ck*!*ident@ho*st";
        Set<String> input = new LinkedHashSet<>();
        input.add("nick!ident@host");
        input.add("nick!1ident@host");
        input.add("nick!Adent@host");
        input.add("nick!identtt@host");
        input.add("nQck!ident@host");
        input.add("n.ck!ident@host");
        input.add("nickname!ident@host");
        input.add("nickname!oneident@hostname");
        input.add("n12k!dent@hopqrst");
        input.add("nick!ident@hojoioijoijst");
        input.add("nick!ident@abchost");
        for(String s: input) System.out.println(mask + " " + s + " = " + match(mask, s));
    }
}
