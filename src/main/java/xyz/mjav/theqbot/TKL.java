package xyz.mjav.theqbot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.mjav.theqbot.exceptions.ItemNotFoundException;

public class TKL {

    /*
     * TKL + Q * nick set_by expire_timestamp set_at_timestamp :reason
     * TKL + G user host set_by expire_timestamp set_at_timestamp :reason
     * TKL + Z * ip set_by expire_timestamp set_at_timestamp :reason
     * TKL + s user host set_by expire_timestamp set_at_timestamp :reason
     */

    private static Map<String, TKL> tklList = new HashMap<>();

    /**
     * Type shall be among sqline, gline, gzline, shun
     */
    private String type;

    private String ident;
    private String host;
    private String setBy;
    private String reason;

    private byte[] ipAddress;

    private Timestamp setTime;
    private Timestamp expireTime;

    public static class Builder {

        private String type;

        private String ident;
        private String host;
        private String setBy;
        private String reason;

        private byte[] ipAddress;

        private Timestamp setTime;
        private Timestamp expireTime;

        public Builder type(String x) {
            this.type = x;
            return this;
        }

        public Builder ident(String x) {
            this.ident = x;
            return this;
        }

        public Builder host(String x) {
            this.host = x;
            return this;
        }

        public Builder setBy(String x) {
            this.setBy = x;
            return this;
        }

        public Builder reason(String x) {
            this.reason = x;
            return this;
        }

        public Builder ipAddress(byte[] x) {
            this.ipAddress = x;
            return this;
        }

        public Builder setTime(Timestamp x) {
            this.setTime = x;
            return this;
        }

        public Builder expireTime(Timestamp x) {
            this.expireTime = x;
            return this;
        }

        public TKL build() {
            return new TKL(this);
        }
    }

    public TKL(Builder b) {
        this.type = b.type;
        this.ident = b.ident;
        this.host = b.host;
        this.setBy = b.setBy;
        this.reason = b.reason;
        this.ipAddress = b.ipAddress;
        this.setTime = b.setTime;
        this.expireTime = b.expireTime;

        tklList.put(String.format("%s@%s", this.ident, this.host), this);
    }


    public static Map<String, TKL> getTklList() {
        return new HashMap<>(tklList);
    }

    public static TKL getTkl(String s) throws ItemNotFoundException {
        if (tklList.containsKey(s) == true) return tklList.get(s);
        throw new ItemNotFoundException("TKL::getTkl: TKL not found");
    }

    public static void removeTkl(String s) throws ItemNotFoundException {
        if (tklList.containsKey(s) == true) tklList.remove(s);
        throw new ItemNotFoundException("TKL::removeTkl: TKL not found");
    }

}
