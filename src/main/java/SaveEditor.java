import java.io.File;
import java.net.URISyntaxException;
import java.util.Scanner;

public class SaveEditor {

    private static final Scanner input = new Scanner(System.in);

    private static String getFilePath() throws URISyntaxException {
        return new File(SaveEditor.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
    }

    private static void yesNoQuestion(String question, String response, Runnable func){
        System.out.print(question + " (y/n): ");        //print the question
        String in = input.nextLine();                   //take in the input
        in = in.toUpperCase();                          //make the input not case-sensitive
        if(in.equals("Y") || in.equals("YES")){         //if input is 'Y', print response and run the function
            System.out.println(response);
            func.run();
        } else if (in.equals("N") || in.equals("NO")){  //if input is 'N', do nothing
            return;
        } else {                                        //if input invalid, ask the question again
            System.out.println("Invalid Input. Enter 'y' or 'n'");
            yesNoQuestion(question, response, func);
        }
    }

    public static void main(String[] args){
        //System.out.print("Enter path for save file: (Default: DLSaveEditor directory):");
        //String path = input.nextLine();
        String path = "";
        if(path.equals("")){
            try{
                String programPath = getFilePath();
                path = programPath.substring(0,programPath.indexOf("DragaliaSaveEditor")) + "/DragaliaSaveEditor/savedata.txt";
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Save data path: " + path);
        JsonUtils util = new JsonUtils(path);
        yesNoQuestion(
                "Uncap mana? (Sets mana to 10m)",
                "Mana uncapped!",
                () -> util.uncapMana());
        yesNoQuestion(
                "Rob Donkay? (Sets wyrmites to 1m)",
                "Thanks Donkay!",
                () -> util.plunderDonkay());
        yesNoQuestion(
                "Play Ch13 Ex1-2 Battle On The Byroad? (Sets eldwater to 10m)",
                "Set eldwater to 10m.",
                () -> util.battleOnTheByroad());
        yesNoQuestion(
                "Add all adventurers to roster?",
                "Added all missing adventurers.",
                () -> util.addMissingAdventurers());
        yesNoQuestion(
                "Set all owned item count to 30,000?",
                "Done!",
                () -> util.addItems());
        yesNoQuestion(
                "Enter the Kaleidoscape? (Replaces portrait print inventory to a strong set of prints)",
                "",
                () -> yesNoQuestion(
                        "This will delete portrait prints that you already have equipped. Is this ok?",
                        "Done!",
                        () -> util.backToTheMines()));
        util.writeToFile();
    }
}
