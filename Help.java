import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class Help {
    public Help() {
    }

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
            content.add("Help content not found for that command.");
        }  
        return content;
    }
}
