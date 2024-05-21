package xyz.mjav.theqbot;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to store an IRCv3 tag
 */
public final class Ircv3Tag {

    /** Contains the standard tags */
    private final Map<String, String> v3tagsListStd;

    /** Contains the vendor specific tags */
    private final Map<String, String> v3tagsListVendorSpec;

    /** Contains the client-only tags */
    private final Map<String, String> v3tagsListClientOnly;

    /** Contains the client-only vendor specific tags */
    private final Map<String, String> v3tagsListVendorSpecClientOnly;

    /**
     * Class constructor
     * @param tag IRCv3 tag input (with format @...)
     * @throws Exception throws exception in case of parsing failure
     */
    public Ircv3Tag(String tag) /*throws Exception*/ {

        final Map< String, Map<String, String> > v3TagsList ;

        //try { v3TagsList = parseV3Tag(tag); }
        //catch (Exception e) { throw e; }
        v3TagsList = parseV3Tag(tag);

        try {
            this.v3tagsListStd                  = v3TagsList.get("standardized");
            this.v3tagsListVendorSpec           = v3TagsList.get("vendor-specific");
            this.v3tagsListClientOnly           = v3TagsList.get("client-only");
            this.v3tagsListVendorSpecClientOnly = v3TagsList.get("vendor-specific-client-only");
         }
        catch (Exception e) { throw e; }
    }

    /**
     * Returns standardized tags
     * @return standardized tags
     */
    public Map<String, String> getTagsStd() {
        final Map<String, String> tags = new HashMap<>(this.v3tagsListStd);
        return tags;
    }

    /**
     * Returns vendor specific tags
     * @return vendor specific tags
     */
    public Map<String, String> getTagsVendorSpec() {
        final Map<String, String> tags = new HashMap<>(this.v3tagsListVendorSpec);
        return tags;
    }

    /**
     * Returns client only tags
     * @return client only tags
     */
    public Map<String, String> getTagsClientOnly() {
        final Map<String, String> tags = new HashMap<>(this.v3tagsListClientOnly);
        return tags;
    }

    /**
     * Returns vendor specific and client only tags
     * @return vendor specific and client only tags
     */
    public Map<String, String> getTagsVendSpecClientOnly() {
        final Map<String, String> tags = new HashMap<>(this.v3tagsListVendorSpecClientOnly);
        return tags;
    }

    /**
     * Returns all the IRCv3 tags
     * @return tags
     */
    public Map<String, String> getTags() {

        final Map<String, String> tags = new HashMap<>();

        tags.putAll(this.v3tagsListStd);
        tags.putAll(this.v3tagsListVendorSpec);
        tags.putAll(this.v3tagsListClientOnly);
        tags.putAll(this.v3tagsListVendorSpecClientOnly);

        return tags;

    }

    /**
     * Parses an IRCv3 tag and sorts them by nature (standard, vendor specific, client only and vendor specific client only)
     * @param v3tagIn IRCv3 tag input (with format @...)
     * @return Returns a Map of Map containing sorted IRCv3 by their nature
     * @throws Exception Exception when the tag cannot be properly parsed
     */
    private Map< String, Map<String, String> > parseV3Tag(String v3tagIn) /*throws Exception*/ {

        /*
         * TAG FORMAT
         * ==========
         *
         * @...
         * Standardized: @prop1=val1;prop2=val2;prop3;prop4=val4...
         * Vendor-specific: @prop1=val1;vendorIdentifier/prop2=val2;prop3...
         * Client-only tag: @prop1=val1;+prop2=val2;prop3;+prop4...
         * Vendor-specific client-only tag: @prop1=val1;+vendorId/prop2=val2;prop3;+prop4...
         */

        /** Map to hold the typed tags maps */
        Map<String, Map<String, String> > v3TagsFamily      = new HashMap<>();

        /** Contains the standard tags */
        Map<String, String> v3tagsListStd                   = new HashMap<>();

        /** Contains the vendor specific tags */
        Map<String, String> v3tagsListVendorSpec            = new HashMap<>();

        /** Contains the client-only tags */
        Map<String, String> v3tagsListClientOnly            = new HashMap<>();

        /** Contains the client-only vendor specific tags */
        Map<String, String> v3tagsListVendorSpecClientOnly  = new HashMap<>();

        /** Contains the tags splitted */
        String[] v3tagsSplit;

        /** Contains the tag property and tag value splitted */
        String[] propValSplit;

        /** Contains the tag property name */
        String property = "";

        /** Contains the tag property value */
        String value    = "";

        /** Contains one IRCv3 tag */
        String v3tag    = "";

        /** Whether the tag is standard */
        Boolean isStandard   = false;

        /** Whether the tag is vendor-specific */
        Boolean isVendorSpec = false;

        /** Whether the tag is client-only */
        Boolean isClientOnly = false;


        /* First get rid of prepending @ then split the different tags (separated by a ;)*/
        v3tag = v3tagIn.replaceFirst("^@", "");
        v3tagsSplit = v3tag.split(";");

        v3TagsFamily.put("standardized",                v3tagsListStd);
        v3TagsFamily.put("vendor-specific",             v3tagsListVendorSpec);
        v3TagsFamily.put("client-only",                 v3tagsListClientOnly);
        v3TagsFamily.put("vendor-specific-client-only", v3tagsListVendorSpecClientOnly);

        for (String propVal: v3tagsSplit) {
            property = "";
            value    = "";
            propValSplit = propVal.split("=", 2);

            /* Getting property through try, but if we are thrown an exception, something really went bad */
            try { property = propValSplit[0]; }
        catch (IndexOutOfBoundsException e) { continue; /*throw new Exception(String.format("Tag item contains no property: %s", propVal));*/ }

            /* Try to get the value, if exception then there is no value */
            try { value = propValSplit[1]; }
            catch (IndexOutOfBoundsException e) { }

            /* First assume the tag item is standard */
            isStandard   = true;
            isVendorSpec = false;
            isClientOnly = false;

            /* Now checking for tag item property content / and + */
            if (property.contains("/") == true) { isStandard = false; isVendorSpec = true; }
            if (property.startsWith("+") == true) { isStandard = false; isClientOnly = true; }

            /* Now putting the tat item into the proper list */
            if (isStandard.equals(true) == true) { v3tagsListStd.put(property, value); }
            else if (isVendorSpec.equals(true) == true && isClientOnly.equals(true) == true) { v3tagsListVendorSpecClientOnly.put(property, value); }
            else if (isVendorSpec.equals(true) == true && isClientOnly.equals(true) == false) { v3tagsListVendorSpec.put(property, value); }
            else if (isVendorSpec.equals(true) == false && isClientOnly.equals(true) == true) { v3tagsListClientOnly.put(property, value); }
        }

        return v3TagsFamily;
    }

    @Override public String toString() {
        return getTags().toString();
    }
}
