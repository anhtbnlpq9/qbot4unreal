package xyz.mjav.theqbot;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Cache {

    /* Static fields */

    /** Config access */
    protected static Config config;

    /** Logger */
    protected static final Logger log = LogManager.getLogger("common-log");

    /** Pattern to validate content */
    protected static final Pattern PATTERN_NON_ALPHNUM_ANYLANG = Pattern.compile("[^\\p{ASCII}]");


    /** Cache default age in seconds before flush */
    private static final long               MAX_AGE = 300L;

    /** List of active caches */
    private static final Map<String, Cache>       cacheListByName       = new HashMap<>();
    private static final Map<String, Timestamp>   timestampListByName   = new HashMap<>();

    /**
     * Creates a cache
     * @param name cache name
     * @param type cache type
     * @return cache object
     * @throws FileNotFoundException
     */
    public static Cache create(String name, String type) throws FileNotFoundException {
        if (config == null) config = Config.getConfig();

        if (getCacheByName().keySet().contains(name) == true) return getCacheByName().get(name);


        switch (type) {
            case "helpfile":
                try { return FileCache.create(name, type, String.format("%s/%s.txt", config.getHelpCommandsPath(), name)); }
                catch (FileNotFoundException e) { throw e; }

            case "motdfile":
                try { return FileCache.create(name, type, config.getHelpMotdFilePath()); }
                catch (FileNotFoundException e) { throw e; }

            case "rulesfile":
                try { return FileCache.create(name, type, config.getHelpRulesFilePath()); }
                catch (FileNotFoundException e) { throw e; }

            default: return null;
        }

    }

    /**
     * Checks of the string contains non-ASCII chars
     * @param input input string
     * @return if the input string contains non-ASCII chars
     */
    protected static final boolean containsNonAlphanumeric(String input) {
        Matcher matcher = PATTERN_NON_ALPHNUM_ANYLANG.matcher(input);
        return matcher.find();
    }

    /**
     * Add an element to the cache list
     * @param c cache element
     */
    public static final void addToCache(Cache c) {
        cacheListByName.put(c.name, c);
        timestampListByName.put(c.name, new Timestamp());
    }

    /**
     * Deletes an element to the cache list
     * @param c cache object
     */
    public static final void removeFromCache(Cache c) {
        Map<String, Cache>    cacheList = new HashMap<>(Cache.cacheListByName);

        if (cacheList.values().contains(c) == true) {
            for (String s: cacheList.keySet()) {
                if (cacheList.get(s).equals(c)) {
                    removeFromCache(s);
                    break;
                }
            }
        }
    }

    /**
     * Deletes an element to the cache list
     * @param s cache object name
     */
    protected static final void removeFromCache(String s) {
        cacheListByName.remove(s);
        timestampListByName.remove(s);
    }

    /**
     * Returns cache list by name
     * @return cache list by name
     */
    public static final Map<String, Cache> getCacheByName() {
        return new HashMap<>(cacheListByName);
    }

    /**
     * Flushes the cache
     */
    public static final void flush() {
        log.info("Cache::flush: Flushing all the cache");
        for (String name: new HashSet<>(cacheListByName.keySet())) {
            removeFromCache(name);
            log.debug("Cache::flush: flushed cache item " + name);
        }
    }

    public static final void flushExpired() {
        log.debug("Cache::flush: Flushing expired cache items");
        Timestamp currentTime = new Timestamp();
        Map<String, Timestamp> timestampList = new HashMap<>(timestampListByName);

        timestampList.entrySet().stream()
            .filter( (entry) -> (entry.getValue().getValue() + MAX_AGE) <= currentTime.getValue())
            .forEach(
                (entry) -> {
                    log.debug("Cache::flushExpired: flushing expired item " + entry.getKey());
                    removeFromCache(entry.getKey());
                }
            );

    }

    /* Nonstatic fields */

    /** Cache name */
    protected String                     name;

    /** Cache type */
    protected String                     type;

    /** Data contained in the cache */
    protected List<String>               data;

    /** Metadata format map property => value */
    protected Map<String, String>        metadata;

    /** Time of cache data has been built */
    protected Timestamp                  creationTime;

    /** Max age of cache item */
    protected long                       maxAge;

    /**
     * Builds cache data
     */
    protected void build() {
        if (this instanceof FileCache) this.build();
    }

    /**
     * Returns the cache data
     * @return cache data
     */
    public List<String> getData() {
        return this.data;
    }

    /**
     * Returns the cache metadata
     * @return cache metadata
     */
    public Map<String, String> getMetadata() {
        return this.metadata;
    }

}
