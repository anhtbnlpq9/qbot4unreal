package xyz.mjav.theqbot;

import xyz.mjav.theqbot.exceptions.InvalidFormatException;

public interface Bei {

    /*
     * Bei element:
     * o UserMask
     * o ExtBan
     */



    public static Bei create(String s) throws InvalidFormatException {

        if (s.startsWith("~") == true) {
            try { return Extban.create(s); }
            catch (InvalidFormatException e) { throw e; }
        }
        else if (UserMask.isValid(s) == true) return UserMask.create(s);
        throw new InvalidFormatException();

    }

    public boolean matches(Nick nick);

    public String getString();

}
