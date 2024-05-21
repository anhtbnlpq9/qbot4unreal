package xyz.mjav.theqbot.configuration;

import java.util.HashSet;
import java.util.Set;

public class Features {

    private Set<String> enable = new HashSet<>();
    private int randomAccountNameLength     = 12;
    private int tempAccountPasswordLength   = 32;

    public Features() {
        this.enable.add("sasl");
        this.enable.add("svslogin");
    }


    public Set<String> getList() {
        return this.enable;
    }

    public int getRandomAccountNameLength() {
        return this.randomAccountNameLength;
    }

    public int getTempAccountPasswordLength() {
        return this.tempAccountPasswordLength;
    }

}
