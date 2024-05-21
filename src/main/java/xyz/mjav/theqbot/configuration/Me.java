package xyz.mjav.theqbot.configuration;

public class Me {

    private String name;
    private String description;
    private String sid;
    private String protocolVersion = "6100";
    private String versionFlags = "";
    private String fullVersionText = "qbot4u";
    private String adminInfo = "Sample administrative information.\nPlease contact foo@bar.";



    public Me() {

    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    /**
     * @return the sid
     */
    public String getSid() {
        return sid;
    }

    /**
     * @return the protocolVersion
     */
    public String getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * @return the versionFlags
     */
    public String getVersionFlags() {
        return versionFlags;
    }

    /**
     * @return the fullVersionText
     */
    public String getFullVersionText() {
        return fullVersionText;
    }

    /**
     * @return the adminInfo
     */
    public String getAdminInfo() {
        return adminInfo;
    }





}