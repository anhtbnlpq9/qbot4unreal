package xyz.mjav.theqbot;

import java.util.HashSet;
import java.util.Set;

public class NickAlias implements Comparable<NickAlias> {

    /* Static fields */
    private static Set<String> nickAliases = new HashSet<>();

    public static final NickAlias create(String alias, Timestamp created, Timestamp lastUsed, UserAccount userAccount) {
        return new NickAlias.Builder().alias(alias).created(created).lastUsed(lastUsed).userAccount(userAccount).build();
    }

    public static final void addNickAlias(NickAlias nickAlias) {
        nickAliases.add(nickAlias.getAlias());
    }

    public static final void removeNickAlias(NickAlias nickAlias) {
        nickAliases.remove(nickAlias.getAlias());
    }

    public static final boolean exists(NickAlias nickAlias) {
        return (nickAliases.contains(nickAlias.getAlias()) == true) ? true : false;
    }

    /* Nonstatic fields */
    private final String            alias;
    private final UserAccount       userAccount;
    private final Timestamp         created;
    private Timestamp               lastUsed;

    private NickAlias(String alias, Timestamp created, Timestamp lastUsed, UserAccount userAccount) {
        this.alias = alias;
        this.created = created;
        this.lastUsed = lastUsed;
        this.userAccount = userAccount;
    }

    private NickAlias(Builder b) {
        this.alias = b.alias;
        this.created = b.created;
        this.lastUsed = b.lastUsed;
        this.userAccount = b.userAccount;
    }

    public String getAlias() {
        return this.alias;
    }

    public Timestamp getCreatedTS() {
        return this.created;
    }

    public Timestamp getLastUsedTS() {
        return this.lastUsed;
    }

    public UserAccount getUserAccount() {
        return this.userAccount;
    }

    @Override public String toString() {
        return this.alias;
    }

    public static class Builder {
        private String alias;
        private Timestamp created = new Timestamp();
        private Timestamp lastUsed = new Timestamp(0L);
        private UserAccount userAccount;

        public Builder alias(String val) {
            this.alias = val;
            return this;
        }

        public Builder created(Timestamp val) {
            this.created = val;
            return this;
        }

        public Builder lastUsed(Timestamp val) {
            this.lastUsed = val;
            return this;
        }

        public Builder userAccount(UserAccount val) {
            this.userAccount = val;
            return this;
        }

        public NickAlias build() {
            return new NickAlias(this);
        }
    }

    @Override public int compareTo(NickAlias n) {
        return this.alias.compareTo(n.getAlias());
    }
}
