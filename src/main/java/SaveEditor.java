import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
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

    private static void yesNoQuestion(String question, Runnable func){
        System.out.print(question + " (y/n): ");        //print the question
        String in = input.nextLine();                   //take in the input
        in = in.toUpperCase();                          //make the input not case-sensitive
        if(in.equals("Y") || in.equals("YES")){         //if input is 'Y', run the function
            func.run();
        } else if (in.equals("N") || in.equals("NO")){  //if input is 'N', do nothing
            return;
        } else {                                        //if input invalid, ask the question again
            System.out.println("Invalid Input. Enter 'y' or 'n'");
            yesNoQuestion(question, func);
        }
    }

    private static void yesNoQuestion(String question, Runnable yesFunc, Runnable noFunc){
        System.out.print(question + " (y/n): ");        //print the question
        String in = input.nextLine();                   //take in the input
        in = in.toUpperCase();                          //make the input not case-sensitive
        if(in.equals("Y") || in.equals("YES")){         //if input is 'Y', run yesFunc
            yesFunc.run();
        } else if (in.equals("N") || in.equals("NO")){  //if input is 'N', run noFunc
            noFunc.run();
        } else {                                        //if input invalid, ask the question again
            System.out.println("Invalid Input. Enter 'y' or 'n'");
            yesNoQuestion(question, yesFunc, noFunc);
        }
    }

    public static void main(String[] args){
        System.out.print("Enter path for save file: (Default: DragaliaSaveEditor directory):");
        String path = input.nextLine();
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
                "Add all missing adventurers to roster?",
                () -> System.out.println("Added " + util.addMissingAdventurers() + " missing adventurers."));
        yesNoQuestion(
                "Add all missing dragons to roster?",
                () -> yesNoQuestion(
                        "\tExclude 3-star and 4-star dragons?",
                        () -> System.out.println(util.addMissingDragons(true)),
                        () -> System.out.println(util.addMissingDragons(false))));
        yesNoQuestion(
                "Add all missing weapons?",
                () -> System.out.println("Added " + util.addMissingWeapons() + " missing weapons."));
        yesNoQuestion(
                "Set all owned item count to 30,000?",
                "Done!",
                () -> util.addItems());
        yesNoQuestion(
                "Enter the Kaleidoscape? (Replaces portrait print inventory to a strong set of prints)",
                () -> yesNoQuestion(
                        "\tThis will completely replace portrait prints that you own. Is this ok?",
                        "Done!",
                        () -> util.backToTheMines()));
        yesNoQuestion(
                "Add missing weapon skins?",
                () -> System.out.println("Added " + util.addMissingWeaponSkins() + " missing weapon skins."));
        util.writeToFile();
        System.out.println("Press Enter to exit.");
        input.nextLine();
    }

}
