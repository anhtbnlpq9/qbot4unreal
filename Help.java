import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
* Help class
* @author me
*/
public class Help {

    /**
     * Constructor
     */
    public Help() {
        
    }

    /**
     * Method to return the content of the file
     * @param type Help type (levels, commands, ...)
     * @param command Command name
     * @return File content
     */
    public static ArrayList<String> getHelp(String type, String command) {

        ArrayList<String> content = new ArrayList<String>();
        String currentLine;

        try {
            FileInputStream fis=new FileInputStream("help/" + type + "/" + command + ".txt");
            Scanner sc=new Scanner(fis);
            while(sc.hasNextLine()) {
                currentLine = sc.nextLine();
                if (currentLine.startsWith("//") == false) {
                    content.add(currentLine);
                }
            }
            sc.close();
        }
        catch(Exception e)  {
            e.printStackTrace();
            content.add("Help not available for that command.");
        }  
        return content;
    }
}
