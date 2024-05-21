package xyz.mjav.theqbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public abstract class StringTools {

    /**
     * Separates items in a list containing '+item1, +item2, -item3, +item4, ...' into separate maps
     * @param argv
     * @param argc
     * @return
     */
    public static final Map<String, Set<String>> addRemoveString(List<String> argv, int argc) {
        Map<String, Set<String>> outMap = new HashMap<>();

        Set<String> stringSetP = new HashSet<>();
        Set<String> stringSetM = new HashSet<>();

        char mod;

        outMap.put("+", stringSetP);
        outMap.put("-", stringSetM);

        int i = 0;
        for (String s: argv) {
            if (i >= argc) break;

            mod = s.charAt(0);

            switch(mod) {
                case '+': stringSetP.add(s.substring(1)); break;
                case '-': stringSetM.add(s.substring(1)); break;
                default: continue;
            }

            i++;
        }

        return outMap;
    }

    public static final Map<String, Set<String>> addRemoveString(String argv, int argc) {
        String[] strSplit = argv.split(" ");
        List<String> argl = new ArrayList<>();
        Map<String, Set<String>> outMap;
        for (String s: strSplit) argl.add(s);
        outMap = addRemoveString(argl, argc);
        return outMap;
    }

    /**
     * Removes duplicate chars in a string
     * @param s Input string
     * @return String with unique chars
     */
    public static String removeDuplicate(String s) {

        char[] chars = s.toCharArray();
        Set<Character> charSet = new LinkedHashSet<Character>();
        for (char c : chars) { charSet.add(c); }

        StringBuilder sb = new StringBuilder();
        for (Character character : charSet) { sb.append(character); }
        return sb.toString();

    }

    public static String listToString(List<?> list) {
        StringJoiner sj = new StringJoiner(" ");
        for (Object o: list) sj.add(o.toString());
        return sj.toString();
    }
}
