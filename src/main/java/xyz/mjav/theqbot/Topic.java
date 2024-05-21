package xyz.mjav.theqbot;

/**
 * Topic class
 * @author thib
 */
public class Topic {

    /** Topic text */
    private String text     = "";

    /** Topic author as nick!ident@host */
    private String author   = "";

    /** Topic timestamp */
    private Timestamp timestamp  = new Timestamp();


    /**
     * Constructor that initiate an empty topic
     */
    public Topic() {
        this.text = "";
        this.author = "";
        this.timestamp = new Timestamp(0L);
    }

    /**
     * Constructor to init a topic
     * @param t text
     * @param a author as nick!ident@host
     * @param ts timestamp
     */
    public Topic(String t, String a, Timestamp ts) {
        this.text = t;
        this.author = a;
        this.timestamp = ts;
    }

    /**
     * Constructor to init a topic
     * @param t text
     * @param a author as nick!ident@host
     * @param ts timestamp
     */
    public Topic(Topic topic) {
        this.text = topic.getText();
        this.author = topic.getAuthor();
        this.timestamp = topic.getTS();
    }

    @Override
    public String toString() {
        return this.text;
    }

    /**
     * Gets topic text
     * @return text
     */
    public String getText() {
        return this.text;
    }

    /**
     * Gets topic author
     * @return author
     */
    public String getAuthor() {
        return this.author;
    }

    /**
     * Gets topic timestamp
     * @return timestamp
     */
    public Timestamp getTS() {
        return this.timestamp;
    }

    /**
     * Sets the topic text
     * @param s text
     */
    public void setText(String s) {
        this.text = s;
    }

    /**
     * Sets the topic author
     * @param s author
     */
    public void setAuthor(String s) {
        this.author = s;
    }

    /**
     * Sets the topic timestamp
     * @param ts timestamp
     */
    public void setTS(Timestamp ts) {
        this.timestamp = ts;
    }

}
