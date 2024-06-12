package xyz.mjav.theqbot;


import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;

import xyz.mjav.theqbot.exceptions.ConfigFileErrorException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Configuration class
 * Loads and parses the configuration file
 */
public final class Config {

    private static Logger log = LogManager.getLogger("common-log");

    private InputStream inputStream = null;

    /* Server parameters */
    private String serverName                      = "";
    private String serverId                        = "";
    private String serverDescription               = "";
    private String serverProtocolVersion           = "6100";
    private String serverVersionFlags              = "";
    private String serverFullVersionText           = "qbot4u";
    private String serverVersionString             = "qbot4u - The Q Bot for UnrealIRCd.";

    /* Admin parameters */
    private String adminInformation = "Sample administrative information.\nPlease contact foo@bar.";

    /* Link parameters */
    private String  linkPeer               = "";
    private String  linkHost               = "";
    private String  linkPassword           = "*";
    private int     linkPort               = 0;

    /* Chanservice parameters */
    /*
     * UID: SSS + UUUUUU
     * SSS = Server ID (number + 2 alphanumerics)
     * UUUUUU = User ID (alphanumerics)
     *
     * Server ID ranges:
     *  o 000 -> 0ZZ => unused
     *  o 100 -> 1ZZ => unused
     *  o 200 -> 2ZZ => unused
     *  o 300 -> 3ZZ => unused
     *  o 400 -> 4ZZ => unused
     *  o 500 -> 5IZ => unused
     *  o 5J0 -> 5KZ => juped servers (72 servers)
     *  o 5L0 -> 5MZ => unused
     *  o 5N0 -> 5QZ => main servers (144 servers)
     *  o 5R0 -> 5SZ => unused
     *  o 5T0 -> 5TZ => test servers (36 servers)
     *  o 5U0 -> 5ZZ => unused
     *  o 600 -> 6ZZ => unused
     *  o 700 -> 7ZZ => unused
     *  o 800 -> 8ZZ => unused
     *  o 900 -> 9ZZ => unused
     *
     * User ID ranges for this service:
     *  o 000000 -> 0000ZZ => central bots (1296 nicks)
     *  o 000100 -> 0001ZZ => additional bots (1296 nicks)
     *  o 000200 -> IZZZZZ => unused
     *  o J00000 -> JZZZZZ => juped nicks (~60.5k nicks)
     *  o K00000 -> L001ZZ => unused
     *  o L00200 -> SZZZZZ => unused
     *  o T00000 -> TZZZZZ => tests (~60.5k nicks)
     *  o U00200 -> ZZZZZZ => unused
     */
    private String    cserviceBotNick                          = "Qbot";
    private String    cserviceBotUid                           = "00000Q";
    private String    cserviceBotIdent                         = "qb4u";
    private String    cserviceBotHost                          = "network.service";
    private String    cserviceBotReal                          = "qbot4u";
    private String    cserviceBotModes                         = "+o";
    private String    cserviceAccountHostPrefix                = "configureme";
    private String    cserviceAccountHostSuffix                = "please";
    private String    cserviceChanDefaultModes                 = "nt";
    private int       cserviceAccountMaxCertFP                 = 10;
    private int       cserviceAccountMinPassLength             = 10;
    private int       cserviceAccountMaxPassLength             = 20;
    private int       cserviceAccountWrongCredWait             = 3;
    private int       cserviceAccountMaxChannels               = 50;
    private int       cserviceAccountMaxAuthHistory            = 10;
    private int       cserviceChanBanTime                      = 600;
    private int       cserviceChanAutoLimit                    = 10;
    private int       cserviceChanAutoLimitFreq                = 30;
    private int       cserviceChanMaxChanlevs                  = 50;

    /* Oper Service parameters */
    private String    oserviceBotNick                       = "OperServ";
    private String    oserviceBotUid                        = "00000O";
    private String    oserviceBotIdent                      = "qb4u";
    private String    oserviceBotHost                       = "network.service";
    private String    oserviceBotReal                       = "qbot4u";
    private String    oserviceBotModes                      = "+o";

    /* Network parameters */
    private String networkName      = "";
    private String networkProtocol  = "unrealircd";

    /* Features parameters */
    private boolean   hasFeatureSasl                        = true;
    private boolean   hasFeatureSvslogin                    = true;
    private boolean   hasFeatureChgHost                     = false;
    private boolean   hasFeatureDenyAuthConnPainText        = false;
    private boolean   featureRandomAccountName              = false;
    private boolean   featureTempAccountPassword            = false;
    private int       featureRandomAccountNameLength        = 12;
    private int       featureTempAccountPasswordLength      = 32;
    private Map<String, Boolean> featuresList               = new HashMap<String, Boolean>();

    /* Logging parameters */
    private boolean      hasLogDebugIn           = false;
    private boolean      hasLogDebugOut          = false;
    private boolean      logElasticEnabled       = false;
    private String       logElasticUri           = "";
    private String       logElasticApiKey        = "    ";
    private String       logElasticIndexName     = "qbot4uneral-log";
    private Set<String>  logElasticComponent     = new HashSet<>();

    /* Database parameters */
    private int     databaseSchedFreq    = 60;
    private String  databaseType         = "";
    private String  dbSqlitePath         = "";
    private String  dbElasticUri         = "    ";
    private String  dbElasticApiKey      = "";
    private String  dbElasticIndexName  = "qbot4uneral";

    /* SSL parameters */
    private String sslTruststorePath      = "";
    private String sslTruststorePassword  = "";
    private String sslKeystorePath        = "";
    private String sslKeystorePassword    = "";

    /* Help parameters */
    private String helpCommandsPath       = "help/commands";
    private String helpCommandsListPath   = "help/commandsList";
    private String helpManualsPath        = "help/manuals";
    private String helpMotdFilePath       = "conf/motd.txt";
    private String helpRulesFilePath      = "conf/rules.txt";


    /* List of allowed database types */
    private Set<String> databaseTypes = Set.of(
        "sqlite3", "elastic"
    );

    private static Config instance;

   /**
    * Constructor for the class
    * @param configFile Configuration file name
    * @throws Exception
    */
    private Config(String configFile) throws ConfigFileErrorException, FileNotFoundException {

        Integer configErrors = 0;

        try { inputStream = new FileInputStream(new File(configFile)); }
        catch (FileNotFoundException e) { log.error(String.format("Config/constructor: configuration file \"%s\" not found: ", configFile), e); throw e; }

        Yaml yaml = new Yaml();
        Map<String, Object> data  = yaml.load(inputStream);

        /*
         * Server configuration variables
         */
        try {
            @SuppressWarnings("unchecked") Map<String, Object> confme = (Map<String, Object>) data.get("me");
            try { serverName            = (String) Objects.requireNonNull(confme.get("name")); } catch (Exception e) { log.fatal("Configuration: me::name is not defined!"); configErrors++; }
            try { serverId              = (String) Objects.requireNonNull(confme.get("sid")); } catch (Exception e) { log.fatal("Configuration: me::sid is not defined!"); configErrors++; }
            try { serverDescription     = (String) Objects.requireNonNull(confme.get("description")); } catch (Exception e) { log.fatal("Configuration: me::description is not defined!"); configErrors++; }
            try { serverProtocolVersion = (String) Objects.requireNonNull(confme.get("protocolVersion")); } catch (Exception e) { }
            try { serverVersionFlags    = (String) Objects.requireNonNull(confme.get("versionFlags")); } catch (Exception e) { }
            try { serverFullVersionText = (String) Objects.requireNonNull(confme.get("fullVersionText")); } catch (Exception e) { }
            try { serverVersionString   = (String) Objects.requireNonNull(confme.get("versionString")); } catch (Exception e) { }
            try { adminInformation      = (String) Objects.requireNonNull(confme.get("adminInfo")); } catch (Exception e) { }
        }
        catch (Exception e) { throw new ConfigFileErrorException(); }

        /*
         * Link peer configuration variables
         */
        try {
            @SuppressWarnings("unchecked") Map<String, Object> conflink = (Map<String, Object>) data.get("link");
            try { linkPeer      = (String)  Objects.requireNonNull(conflink.get("peer")); } catch (Exception e) { log.fatal("Configuration: link::peer is not defined!"); configErrors++; }
            try { linkHost      = (String)  Objects.requireNonNull(conflink.get("host")); } catch (Exception e) { log.fatal("Configuration: link::host is not defined!"); configErrors++; }
            try { linkPassword  = (String)  Objects.requireNonNull(conflink.get("password")); } catch (Exception e) {
                log.warn("Configuration: link::password is not defined. Unless you are using certificate authentication (in which case you may put * as password), the link might not work.");
            }
            try { linkPort      = (int)     Objects.requireNonNull(conflink.get("port")); } catch (Exception e) { log.fatal("Configuration: link::port is not defined!"); configErrors++; }
        }
        catch (Exception e) { throw new ConfigFileErrorException(); }

        /*
         * CService configuration variables
         */
        try {
            @SuppressWarnings("unchecked") Map<String, Object> confcservice = (Map<String, Object>) data.get("cservice");
            @SuppressWarnings("unchecked") Map<String, Object> confcserviceBot = (Map<String, Object>) confcservice.get("bot");
            @SuppressWarnings("unchecked") Map<String, Object> cserviceaccount = (Map<String, Object>) confcservice.get("accountsettings");
            @SuppressWarnings("unchecked") Map<String, Object> cservicechan = (Map<String, Object>) confcservice.get("chansettings");

            try { cserviceBotNick       = (String) Objects.requireNonNull(confcserviceBot.get("nick")); } catch (Exception e) { log.fatal("Configuration: cservice::bot::nick is not defined!"); configErrors++; }
            try { cserviceBotUid        = (String) Objects.requireNonNull(confcserviceBot.get("uid")); } catch (Exception e) {  }
            try { cserviceBotIdent      = (String) Objects.requireNonNull(confcserviceBot.get("ident")); } catch (Exception e) { }
            try { cserviceBotHost       = (String) Objects.requireNonNull(confcserviceBot.get("host")); } catch (Exception e) { }
            try { cserviceBotReal       = (String) Objects.requireNonNull(confcserviceBot.get("realname")); } catch (Exception e) { }
            try { cserviceBotModes      = ((String) Objects.requireNonNull(confcserviceBot.get("modes"))).replaceAll("[^a-zA-Z]", ""); } catch (Exception e) { }

            try { cserviceAccountHostPrefix      = (String)   Objects.requireNonNull(cserviceaccount.get("authvhostprefix")); } catch (Exception e) { }
            try { cserviceAccountHostSuffix      = (String)   Objects.requireNonNull(cserviceaccount.get("authvhostsuffix")); } catch (Exception e) { }
            try { cserviceAccountMaxCertFP       = (int)      Objects.requireNonNull(cserviceaccount.get("maxcertfp")); } catch (Exception e) { }
            try { cserviceAccountMinPassLength   = (int)      Objects.requireNonNull(cserviceaccount.get("minpasslen")); } catch (Exception e) { }
            try { cserviceAccountMaxPassLength   = (int)      Objects.requireNonNull(cserviceaccount.get("maxpasslen")); } catch (Exception e) { }
            try { cserviceAccountMaxChannels     = (int)      Objects.requireNonNull(cserviceaccount.get("maxchannels")); } catch (Exception e) { }
            try { cserviceAccountWrongCredWait   = (int)      Objects.requireNonNull(cserviceaccount.get("wrongcredwait")); } catch (Exception e) { }

            try { cserviceChanAutoLimitFreq   = (int)     Objects.requireNonNull(cservicechan.get("autolimitfreq")); } catch (Exception e) { }
            try { cserviceChanAutoLimit       = (int)     Objects.requireNonNull(cservicechan.get("autolimitvalue")); } catch (Exception e) { }
            try { cserviceChanMaxChanlevs     = (int)     Objects.requireNonNull(cservicechan.get("maxchanlevs")); } catch (Exception e) { }
            try { cserviceChanDefaultModes    = (String)  Objects.requireNonNull(cservicechan.get("defaultmodes")); } catch (Exception e) { }
            try { cserviceChanBanTime         = (int)     Objects.requireNonNull(cservicechan.get("bantime")); } catch (Exception e) { }
        }
        catch (Exception e) { throw new ConfigFileErrorException(); }


        /*
         * OService configuration variables
         */
        try {
            @SuppressWarnings("unchecked") Map<String, Object> confoservice = (Map<String, Object>) data.get("operservice");
            @SuppressWarnings("unchecked") Map<String, Object> confoserviceBot = (Map<String, Object>) confoservice.get("bot");

            try { oserviceBotNick       = (String) Objects.requireNonNull(confoserviceBot.get("nick")); } catch (Exception e) { log.fatal("Configuration: oservice::bot::nick is not defined!"); configErrors++; }
            try { oserviceBotUid        = (String) Objects.requireNonNull(confoserviceBot.get("uid")); } catch (Exception e) {  }
            try { oserviceBotIdent      = (String) Objects.requireNonNull(confoserviceBot.get("ident")); } catch (Exception e) { }
            try { oserviceBotHost       = (String) Objects.requireNonNull(confoserviceBot.get("host")); } catch (Exception e) { }
            try { oserviceBotReal       = (String) Objects.requireNonNull(confoserviceBot.get("realname")); } catch (Exception e) { }
            try { oserviceBotModes      = (String) Objects.requireNonNull(confoserviceBot.get("modes")); } catch (Exception e) { }
        }
        catch (Exception e) { throw new ConfigFileErrorException(); }

        /*
         * Network configuration (name, protocol)
         */
        try {
            @SuppressWarnings("unchecked") Map<String, Object> confNetwork  = (Map<String, Object>) data.get("network");
            try { networkName      = (String) Objects.requireNonNull(confNetwork.get("name")); } catch (Exception e) { log.fatal("Configuration: network::name is not defined!"); configErrors++; }
            try { networkProtocol  = (String) Objects.requireNonNull(confNetwork.get("protocol")); } catch (Exception e) { }
        }
        catch (Exception e) { throw new ConfigFileErrorException(); }

        /*
         * Logging variables
         */
        try {
            @SuppressWarnings("unchecked") Map<String, Object> confLogging        = (Map<String, Object>)        data.get("logging");
            @SuppressWarnings("unchecked") Map<String, Object> confLoggingElastic = (Map<String, Object>) confLogging.get("elastic");
            try { hasLogDebugIn  = (boolean) confLogging.get("debugIn"); } catch (Exception e) { }
            try { hasLogDebugOut = (boolean) confLogging.get("debugOut"); } catch (Exception e) { }


            try { logElasticEnabled        = (boolean)   confLoggingElastic.get("enable"); }   catch (Exception e) { }

            try { logElasticUri            = (String)    confLoggingElastic.get("uri"); }
            catch (Exception e) { if (logElasticEnabled == true) { log.fatal("Configuration: logging::elastic::uri is not defined but elastic is enabled!"); configErrors++; } }

            try { logElasticApiKey         = (String)    confLoggingElastic.get("apikey"); }
            catch (Exception e) { if (logElasticEnabled == true) { log.fatal("Configuration: logging::elastic::apikey is not defined but elastic is enabled!"); configErrors++; } }

            try { logElasticIndexName      = (String)    confLoggingElastic.get("index"); }
            catch (Exception e) { if (logElasticEnabled == true) { log.fatal("Configuration: logging::elastic::index is not defined but elastic is enabled!"); configErrors++; } }
            try {
                @SuppressWarnings("unchecked")
                ArrayList<String> logElasticComponentTmp = (ArrayList<String>) confLoggingElastic.get("components");
                if (logElasticComponentTmp != null) for (String s: logElasticComponentTmp) logElasticComponent.add(s);
            } catch (Exception e) { }


        }
        catch (Exception e) { throw new ConfigFileErrorException(); }

        /*
         * Features variables
         */
        try {
            @SuppressWarnings("unchecked") Map<String, Object> confFeatures = (Map<String, Object>) data.get("features");
            try { hasFeatureSasl                       = (boolean) Objects.requireNonNull(confFeatures.get("sasl")); } catch (Exception e) { }
            try { hasFeatureSvslogin                   = (boolean) Objects.requireNonNull(confFeatures.get("svslogin")); } catch (Exception e) { }
            try { hasFeatureChgHost                    = (boolean) Objects.requireNonNull(confFeatures.get("chghost")); } catch (Exception e) { }
            try { hasFeatureDenyAuthConnPainText       = (boolean) Objects.requireNonNull(confFeatures.get("denyauthplainconn")); } catch (Exception e) { }
            try { featureRandomAccountName             = (boolean) Objects.requireNonNull(confFeatures.get("randomAccountName")); } catch (Exception e) { }
            try { featureTempAccountPassword           = (boolean) Objects.requireNonNull(confFeatures.get("tempAccountPassword")); } catch (Exception e) { }

            try { featureRandomAccountNameLength       = (int)     Objects.requireNonNull(confFeatures.get("randomAccountNameLenght")); } catch (Exception e) { }
            try { featureTempAccountPasswordLength     = (int)     Objects.requireNonNull(confFeatures.get("tempAccountPasswordLength")); } catch (Exception e) { }

            try { featuresList.put("sasl",                hasFeatureSasl); } catch (Exception e) { }
            try { featuresList.put("svslogin",            hasFeatureSvslogin); } catch (Exception e) { }
            try { featuresList.put("chghost",             hasFeatureChgHost); } catch (Exception e) { }
            try { featuresList.put("denyauthplainconn",   hasFeatureDenyAuthConnPainText); } catch (Exception e) { }
            try { featuresList.put("randomAccountName",   featureRandomAccountName); } catch (Exception e) { }
            try { featuresList.put("tempAccountPassword", featureTempAccountPassword); } catch (Exception e) { }
        }
        catch (Exception e) { throw new ConfigFileErrorException(); }

        /*
         * Database configuration variables
         */
        try {
            @SuppressWarnings("unchecked") Map<String, Object> confDatabase  = (Map<String, Object>) data.get("database");
            @SuppressWarnings("unchecked") Map<String, Object> confDbSqlite  = (Map<String, Object>) confDatabase.get("sqlite3");
            @SuppressWarnings("unchecked") Map<String, Object> confDbElastic = (Map<String, Object>) confDatabase.get("elastic");

            try { databaseType = (String)  Objects.requireNonNull(confDatabase.get("type")); } catch (Exception e) { log.fatal("Configuration: database::type is not defined!"); configErrors++; }
            if (databaseTypes.contains(databaseType) == false) { log.fatal("Configuration: database::type is not defined or type is unknown!"); configErrors++; }

            try { databaseSchedFreq  = (Integer) Objects.requireNonNull(confDatabase.get("schedulefreq")); } catch (Exception e) { }

            try { dbSqlitePath  = (String) Objects.requireNonNull(confDbSqlite.get("path")); }
            catch (Exception e) {
                if (databaseType.equals("sqlite3")) { log.fatal("Configuration: database::sqlite3::path is not defined!"); configErrors++; }
            }

            try { dbElasticUri = (String) Objects.requireNonNull(confDbElastic.get("uri")); }
            catch (Exception e) {
                if (databaseType.equals("elastic")) { log.fatal("Configuration: database::elastic::uri is not defined!"); configErrors++; }
            }
            try { dbElasticApiKey  = (String) Objects.requireNonNull(confDbElastic.get("apiKey")); }
            catch (Exception e) {
                if (databaseType.equals("elastic")) { log.fatal("Configuration: database::elastic::apiKey is not defined!"); configErrors++; }
            }
            try { dbElasticIndexName  = (String) Objects.requireNonNull(confDbElastic.get("index")); }
            catch (Exception e) {
                if (databaseType.equals("elastic")) { log.fatal("Configuration: database::elastic::index is not defined!"); configErrors++; }
            }
        }
        catch (Exception e) { throw new ConfigFileErrorException(); }

        /*
         * TLS configuration variables (Truststore, Keystore)
         */
        try {
            @SuppressWarnings("unchecked") Map<String, Object> confTls       = (Map<String, Object>) data.get("tls");
            @SuppressWarnings("unchecked") Map<String, Object> sslKeystore   = (Map<String, Object>) confTls.get("keystore");
            @SuppressWarnings("unchecked") Map<String, Object> sslTuststore  = (Map<String, Object>) confTls.get("truststore");

            try { sslKeystorePath        = (String) Objects.requireNonNull(sslKeystore.get("path")); } catch (Exception e) { log.fatal("Configuration: tls::keystore::path is not defined!"); configErrors++; }
            try { sslKeystorePassword    = (String) Objects.requireNonNull(sslKeystore.get("password")); } catch (Exception e) { log.fatal("Configuration: tls::keystore::password is not defined!"); configErrors++;  }

            try { sslTruststorePath      = (String) Objects.requireNonNull(sslTuststore.get("path")); } catch (Exception e) { log.fatal("Configuration:ssl::truststore::path is not defined!"); configErrors++; }
            try { sslTruststorePassword  = (String) Objects.requireNonNull(sslTuststore.get("password")); } catch (Exception e) { log.fatal("Configuration: tls::truststore::password is not defined!"); configErrors++; }
        }
        catch (Exception e) { }


        /*
         * Help variables
         */
        try {
            @SuppressWarnings("unchecked") Map<String, Object> confHelp = (Map<String, Object>) data.get("help");
            try { helpCommandsPath                 = (String) Objects.requireNonNull(confHelp.get("commandsPath")); } catch (Exception e) { }
            try { helpCommandsListPath             = (String) Objects.requireNonNull(confHelp.get("commandsListPath")); } catch (Exception e) { }
            try { helpManualsPath                  = (String) Objects.requireNonNull(confHelp.get("manualsPath")); } catch (Exception e) { }
            try { helpMotdFilePath                 = (String) Objects.requireNonNull(confHelp.get("motdFilePath")); } catch (Exception e) { }
            try { helpRulesFilePath                = (String) Objects.requireNonNull(confHelp.get("rulesFilePath")); } catch (Exception e) { }

            File f;

            f = new File(helpCommandsPath);
            if (f.exists() == false || f.isDirectory() == false) { log.fatal("Configuration: help::commandsPath does not exist!"); configErrors++; }

            f = new File(helpCommandsListPath);
            if (f.exists() == false || f.isDirectory() == false) { log.fatal("Configuration: help::commandsListPath does not exist!"); configErrors++; }

            f = new File(helpManualsPath);
            if (f.exists() == false || f.isDirectory() == false) { log.fatal("Configuration: help::manualsPath does not exist!"); configErrors++; }

        }
        catch (Exception e) { throw new ConfigFileErrorException(); }

        if (configErrors > 0) {
            log.fatal("Configuration check failed: " + configErrors +" configuration issues.");
            throw new ConfigFileErrorException("Errors in configuration file.");
        }

        log.info("=== Configuration summary ===");
        log.info("# Me:");
        log.info(" o me::name                            :: " + serverName);
        log.info(" o me::sid                             :: " + serverId);
        log.info(" o me::description                     :: " + serverDescription);
        log.info(" o me::protocolVersion                 :: " + serverProtocolVersion);
        log.info(" o me::versionFlags                    :: " + serverVersionFlags);
        log.info(" o me::fullVersionText                 :: " + serverFullVersionText);
        log.info(" o me::versionString                   :: " + serverVersionString);
        log.info(" o me::adminInfo                       :: " + adminInformation);
        log.info("");
        log.info("# Network:");
        log.info(" o network::name                       :: " + networkName);
        log.info(" o network::protocol                   :: " + networkProtocol);
        log.info("");
        log.info("# Features:");
        log.info(" o features::sasl                      :: " + featuresList.get("sasl"));
        log.info(" o features::svslogin                  :: " + featuresList.get("svslogin"));
        log.info(" o features::chghost                   :: " + featuresList.get("chghost"));
        log.info(" o features::denyauthplainconn         :: " + featuresList.get("denyauthplainconn"));
        log.info(" o features::randomAccountName         :: " + featureRandomAccountName);
        log.info(" o features::randomAccountNameLength   :: " + featureRandomAccountNameLength);
        log.info(" o features::tempAccountPassword       :: " + featureTempAccountPassword);
        log.info(" o features::tempAccountPasswordLength :: " + featureTempAccountPasswordLength);
        log.info("");
        log.info("# Link:");
        log.info(" o Peer hostname                       :: " + linkHost);
        log.info(" o Peer port                           :: " + linkPort);
        log.info("");
        log.info("# Database:");
        log.info(" o database::type                      :: " + databaseType);
        log.info(" o database::sqlite3::path             :: " + dbSqlitePath);
        log.info(" o database::elastic::uri              :: " + dbElasticUri);
        log.info(" o database::elastic::apiKey           :: " + dbElasticApiKey.substring(0, 4) + "...");
        log.info(" o database::elastic::indice           :: " + dbElasticIndexName);
        log.info("");
        log.info("# Logging:");
        log.info(" o logging::loglevel                   :: ");
        log.info(" o Traffic debug input                 :: " + hasLogDebugIn);
        log.info(" o Traffic debug output                :: " + hasLogDebugOut);
        log.info(" o logging::elastic::enable            :: " + logElasticEnabled);
        log.info(" o logging::elastic::uri               :: " + logElasticUri);
        log.info(" o logging::elastic::apikey            :: " + logElasticApiKey.substring(0, 4) + "...");
        log.info(" o logging::elastic::index             :: " + logElasticIndexName);
        log.info(" o logging::elastic::components        :: " + logElasticComponent);


        log.info("");
        log.info(String.format("# CService: configured as as %s!%s@%s, UID %s, Modes %s, Realname \"%s\"", cserviceBotNick, cserviceBotIdent, cserviceBotHost, cserviceBotUid, cserviceBotModes, cserviceBotReal));
        log.info(String.format("# OService: configured as as %s!%s@%s, UID %s, Modes %s, Realname \"%s\"", oserviceBotNick, oserviceBotIdent, oserviceBotHost, oserviceBotUid, oserviceBotModes, oserviceBotReal));
        log.info("");

        instance = this;

    }

    public static Config getConfig() {
        return instance;
    }

    /**
     * Static method to create the singleton
     * @throws ConfigFileErrorException
     * @throws FileNotFoundException
     */
    public static synchronized Config getInstance(String configFile) throws ConfigFileErrorException, FileNotFoundException {
        if (instance == null) {
            try { instance = new Config(configFile); }
            catch (ConfigFileErrorException e) { throw e; }
            catch (FileNotFoundException e)    { throw e; }
            catch (Exception e)                { throw e; }

            return instance;
        }
        else return instance;
    }

    /**
     * Returns the server name
     *
     * @return this.serverName Server name
     */
    public String getServerName() {
        return this.serverName;
    }

    /**
     * Returns the server SID
     *
     * @return this.serverId server SID
     */
    public String getServerId() {
        return this.serverId;
    }

    /**
     * Returns the server description
     * @return description
     */
    public String getServerDescription() {
        return this.serverDescription;
    }

    /**
     * Returns the remote server name
     * @return remote server name
     */
    public String getLinkPeerName() {
        return this.linkPeer;
    }

    /**
     * Returns the link remote host
     * @return remote host
     */
    public String getLinkPeerHost() {
        return this.linkHost;
    }

    /**
     * Returns the link remote port
     * @return remote port
     */
    public Integer getLinkPeerPort() {
        return this.linkPort;
    }

    /**
     * Returns the link password sent to the peer
     * @return link password
     */
    public String getLinkPassword() {
        return this.linkPassword;
    }

    /**
     * Returns the EAUTH string
     * @return EAUTH string
     */
    public String getEAUTH() {
        return String.format("%s,%s,%s,%s", this.serverName, this.serverProtocolVersion, this.serverVersionFlags, this.serverFullVersionText);
    }

    /**
     * Returns the CService bot SID
     * @return
     */
    public String getCServeUniq() {
        return this.cserviceBotUid;
    }

    /**
     * Returns the CService nick
     * @return nickname
     */
    public String getCServeNick() {
        return this.cserviceBotNick;
    }

    /**
     * Returns the CService bot ident
     * @return ident
     */
    public String getCServeIdent() {
        return this.cserviceBotIdent;
    }

    /**
     * Returns the CService bot host
     * @return host
     */
    public String getCServeHost() {
        return this.cserviceBotHost;
    }

    /**
     * Returns the CService bot realnam
     * @return gecos
     */
    public String getCServeRealName() {
        return this.cserviceBotReal;
    }

    /**
     * Returns the modes that the CService bot will have
     * @return modes for cserve bot
     */
    public String getCServeModes() {
        return this.cserviceBotModes;
    }

    /**
     * Returns the administrative information
     * @return administrative information
     */
    public String getAdminInfo() {
        return new String(this.adminInformation);
    }

    /**
     * Returns the network name
     * @return network name
     */
    public String getNetworkName() {
        return this.networkName;
    }

    /**
     * Fetches the configured database type
     * @return Type of the database
     */
    public String getDbType() {
        return this.databaseType;
    }

    /**
     * Fetches the database task scheduler frequency
     * @return Task scheduler frequency
     */
    public Integer getDbSchedFreq() {
        return this.databaseSchedFreq;
    }

    /**
     * Fetches the configured database path
     * @return Path of the database
     */
    public String getDbSqlitePath() {
        return this.dbSqlitePath;
    }

    /**
     * Fetches the Elastic db URI
     * @return Fetches the Elastic db URI
     */
    public String getDbElasticUri() {
        return this.dbElasticUri;
    }

    /**
     * Fetches the Elastic db API key
     * @return Elastic database API key
     */
    public String getDbElasticApiKey() {
        return this.dbElasticApiKey;
    }

    /**
     * Fetches the Elastic db indice name
     * @return Elastic database indice name
     */
    public String getDbElasticIndexName() {
        return this.dbElasticIndexName;
    }

    /**
     * Returns the features of services
     * @param feature
     * @return feature enable/disable
     */
    public Boolean hasFeature(String feature) {
        if (this.featuresList.containsKey(feature) == true) {
            return this.featuresList.get(feature);
        }
        else {
            return false;
        }
    }

    /**
     * Returns logging parameters
     * @param source steam direction
     * @return logging status
     */
    public Boolean hasLogging(String source) {
        switch (source) {
            case "debugIn":
                return this.hasLogDebugIn;

            case "debugOut":
                return this.hasLogDebugOut;

            default:
                return false;
        }
    }

    /**
     * Returns if debug loggin is enabled in any direction
     * @param source steam direction
     * @return logging status
     */
    public Boolean hasDbgLogging() {
        if (this.hasLogDebugIn == false && this.hasLogDebugOut == false) return false;
        return true;
    }

    /**
     * Returns the key store path
     * @return path
     */
    public String getKeyStorePath() {
        return sslKeystorePath;
    }

    /**
     * Returns the key store password
     * @return password
     */
    public String getKeyStorePassword() {
        return sslKeystorePassword;
    }

    /**
     * Returns the trust store path
     * @return path
     */
    public String getTrustStorePath() {
        return sslTruststorePath;
    }

    /**
     * Returns the trust store password
     * @return password
     */
    public String getTrustStorePassword() {
        return sslTruststorePassword;
    }

    /**
     * Returns the configures vhost prefix for user authentication
     * @return host prefix
     */
    public String getCServeHostPrefix() {
        return this.cserviceAccountHostPrefix;
    }

    /**
     * Returns the configures vhost suffix for user authentication
     * @return host suffix
     */
    public String getCServeHostSuffix() {
        return this.cserviceAccountHostSuffix;
    }

    /**
     * Returns the frequency in seconds of the channel autolimit auto settings
     * @return frequency in seconds
     */
    public Integer getCServeAutoLimitFreq(){
        return this.cserviceChanAutoLimitFreq;
    }

    /**
     * Returns the CService account maximum CertFP items
     * @return cservice account max certfp
     */
    public Integer getCServeAccountMaxCertFP() {
        return this.cserviceAccountMaxCertFP;
    }

    /**
     * Returns the CService account maximum channels (chanlevs)
     * @return cservice account max channels
     */
    public Integer getCServeAccountMaxChannels() {
        return this.cserviceAccountMaxChannels;
    }

    /**
     * Returns the CService account authentication history maximum lines to output
     * @return cservice auth history max output lines
     */
    public Integer getCServeAccountMaxAuthhistory() {
        return this.cserviceAccountMaxAuthHistory;
    }

    /**
     * Returns the CService maximum chanlevs for a channel
     * @return cservice channel max chanlevs
     */
    public Integer getCServeChanMaxChanlevs() {
        return this.cserviceChanMaxChanlevs;
    }

    /**
     * Returns the number of seconds to wait after an invalid credential auth failure
     * @return cservice invalid credential auth failure timeout
     */
    public Integer getCServeAccountWrongCredWait() {
        return this.cserviceAccountWrongCredWait;
    }

    /**
     * Returns the CService account minimum password length
     * @return cservice account min pass length
     */
    public Integer getCServiceAccountMinPassLength() {
        return this.cserviceAccountMinPassLength;
    }

    /**
     * Returns the CService account maximum password length
     * @return cservice account max pass length
     */
    public Integer getCServiceAccountMaxPassLength() {
        return this.cserviceAccountMaxPassLength;
    }

    /**
     * Returns the default modes to set when CService joins a channel
     * @return default channel modes
     */
    public String getCserveChanDefaultModes() {
        return this.cserviceChanDefaultModes;
    }

    /**
     * Returns the ChanServ Channel ban time
     * @return channel ban time
     */
    public Integer getCserveChanBanTime() {
        return this.cserviceChanBanTime;

    }

    /**
     * Returns the ChanServ Channel auto limit
     * @return channel auto limit
     */
    public Integer getCserveChanAutoLimit() {
        return this.cserviceChanAutoLimit;
    }

    /**
     * Returns the network protocol string
     * @return network protocol string
     */
    public String getNetworkProtocol() {
        return this.networkProtocol;
    }

    /**
     * Returns the OperService bot SID
     * @return
     */
    public String getOServeUniq() {
        return this.oserviceBotUid;
    }

    /**
     * Returns the OperService nick
     * @return nickname
     */
    public String getOServeNick() {
        return this.oserviceBotNick;
    }

    /**
     * Returns the OperService bot ident
     * @return ident
     */
    public String getOServeIdent() {
        return this.oserviceBotIdent;
    }

    /**
     * Returns the OperService bot host
     * @return host
     */
    public String getOServeHost() {
        return this.oserviceBotHost;
    }

    /**
     * Returns the OperService bot realnam
     * @return gecos
     */
    public String getOServeRealName() {
        return this.oserviceBotReal;
    }

    /**
     * Returns the modes that the OperService bot will have
     * @return modes for cserve bot
     */
    public String getOServeModes() {
        return this.oserviceBotModes;
    }

    /**
     * Returns commands help path
     * @return commands help path
     */
    public String getHelpCommandsPath() {
        return this.helpCommandsPath;
    }

    /**
     * Returns commands list path
     * @return commands list path
     */
    public String getHelpCommandsListPath() {
        return this.helpCommandsListPath;
    }

    /**
     * Returns manuals path
     * @return manuals path
     */
    public String getHelpManualssPath() {
        return this.helpManualsPath;
    }

    /**
     * Returns MOTD file path
     * @return MOTD file path
     */
    public String getHelpMotdFilePath() {
        return this.helpMotdFilePath;
    }

    /**
     * Returns Rules file path
     * @return Rules file path
     */
    public String getHelpRulesFilePath() {
        return this.helpRulesFilePath;
    }

    public boolean getCServeAccountAllowManualCertFP() {
        return false;
    }

    public int getRandomAccountNameLength() {
        return this.featureRandomAccountNameLength;
    }

    public int getTempAccountPasswordLength() {
        return this.featureTempAccountPasswordLength;
    }

    public boolean getLoggingElasticEnabled() {
        return this.logElasticEnabled;
    }

    public String getLoggingElasticUri() {
        return this.logElasticUri;
    }

    public String getLoggingElasticApiKey() {
        return this.logElasticApiKey;
    }

    public String getLoggingElasticIndex() {
        return this.logElasticIndexName;
    }

    public boolean isLoggingElasticComponent(String s) {
        if (this.logElasticComponent.contains(s)) return true;
        return false;
    }
}
