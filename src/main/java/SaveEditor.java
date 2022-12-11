import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.function.Consumer;

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

    private static void continuousInput(String val, Consumer<String> func){
        while(true){
            System.out.print(val + " (Enter 'exit' to return):");
            String in = input.nextLine().toUpperCase();
            if(in.equals("EXIT")){
                return;
            }
            func.accept(in);
        }
    }

    public static void main(String[] args){
        System.out.print("Enter path for save file: (Default: DragaliaSaveEditor directory):");
        String path = input.nextLine();
        if(path.equals("")){
            try{
                String programPath = getFilePath();
                path = Paths.get(programPath.substring(0, programPath.indexOf("DragaliaSaveEditor")), "DragaliaSaveEditor", "savedata.txt").toString();
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
                () -> System.out.println("Added " + util.addMissingAdventurers() + " missing adventurers."),
                () -> yesNoQuestion("\tWould you like to add specific adventurers to roster?",
                        () -> continuousInput("\t\tEnter adventurer name",
                        (advName) -> util.addAdventurer(advName))));
        yesNoQuestion(
                "Add all missing dragons to roster?",
                () -> yesNoQuestion(
                        "\tExclude 3-star and 4-star dragons?",
                        () -> System.out.println(util.addMissingDragons(true)),
                        () -> System.out.println(util.addMissingDragons(false))),
                () -> yesNoQuestion("\tWould you like to add specific dragons to roster?",
                        () -> continuousInput("\t\tEnter dragon name",
                        (dragonName) -> util.addDragon(dragonName))));
        yesNoQuestion(
                "Add all missing weapons?",
                () -> System.out.println("Added " + util.addMissingWeapons() + " missing weapons."));
        yesNoQuestion(
                "Add all missing wyrmprints?",
                () -> System.out.println("Added " + util.addMissingWyrmprints() + " missing wyrmprints."));
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
        yesNoQuestion(
                "Do additional hacked options?",
                () -> {
                    yesNoQuestion("\tGenerate random portrait prints? (This will replace your portrait print inventory)",
                    () -> util.kscapeRandomizer());
                    yesNoQuestion("\tAdd goofy kscapes?",
                    () -> util.addGoofyKscapes());
                });
        if(util.isSaveData2Present()){
            yesNoQuestion(
                    "savedata2.txt already exists in this directory. Would you like to overwrite it?",
                    () -> util.setOverwrite(true));
        }
        util.writeToFile();
    }

}
