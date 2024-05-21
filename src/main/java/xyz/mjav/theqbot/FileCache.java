package xyz.mjav.theqbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Caches data based on file content.
 * If the file is not found the cached data is empty.
 */
public final class FileCache extends Cache {

    /* Static fields */

    /**
     * Creates a FileCache object with its cache filled.
     * @param name cache name
     * @param filePath cache file path
     * @return FileCache object
     * @throws FileNotFoundException
     */
    public static FileCache create(String name, String type, String filePath) throws FileNotFoundException {
        if (fileExists(filePath) == false) throw new FileNotFoundException();

        FileCache fileCache;
        fileCache = new FileCache(name, filePath);

        return fileCache;
    }

    /**
     * Tests if a file exists
     * @param path file path
     * @return if the file exists or not
     */
    private static final boolean fileExists(String path) {
        File file = new File(path);
        if (file.exists() == true && file.isDirectory() == false) return true;
        return false;
    }


    /* Nonstatic fields */

    /** Cache file path */
    private String path;

    /**
     * Constructor
     * @param name cache name
     * @param path cache file path
     */
    private FileCache(String name, String path) {
        this.name = name;
        this.path = path;

        build();
        addToCache(this);
    }


    /**
     * Injects the cache with the file content.
     */
    @Override protected void build() {
        Timestamp ts = new Timestamp();

        List<String> dataLine = new ArrayList<>();

        Map<String, String> properties = new HashMap<>();

        String line;
        String propertiesListStr;
        String propertyName;
        String propertyValue;
        String filePath;

        String[] propertiesListArray;
        String[] property;

        FileInputStream fis;
        Scanner sc;

        //filePath = String.format("%s/%s.txt", this.path, this.name);
        filePath = this.path;
        try { fis = new FileInputStream(filePath); }
        catch (FileNotFoundException e) {
            log.warn(String.format("FileCache::build: File '%s' does not exist.", filePath));
            //throw new FileNotFoundException(String.format("FileCache::build: File or path %s does not exist.", path));
            this.data          = dataLine;
            this.metadata      = properties;
            this.creationTime  = ts;
            return;
        }

        sc = new Scanner(fis);

        while(sc.hasNextLine()) {
            line = sc.nextLine();

            /* Ignores lines with non-ASCII chars */
            //if (containsNonAlphanumeric(line) == true) continue;

            /* Ignores commented lines (//...) */
            if (line.startsWith("//") == true) continue;

            /* Treats lines with metadata tag */
            if (line.startsWith("@@") == true) {
                propertiesListStr = line.replaceFirst("^@@", "");
                propertiesListArray = propertiesListStr.split(";");

                propertyName = "";
                propertyValue = "";

                for (String s: propertiesListArray) {
                    property = s.split("=");

                    try { propertyName = property[0]; }
                    catch (IndexOutOfBoundsException e) { continue; }

                    try { propertyValue = property[1]; }
                    catch (IndexOutOfBoundsException e) { propertyValue = ""; }

                    properties.put(propertyName, propertyValue);
                }
            }

            else dataLine.add(line);
        }

        sc.close();

        this.metadata      = properties;
        this.data          = dataLine;
        this.creationTime  = ts;
    }

}
