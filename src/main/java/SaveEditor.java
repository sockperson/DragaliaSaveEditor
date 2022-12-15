import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public class SaveEditor {

    private static final Scanner input = new Scanner(System.in);
    private static boolean inJar = false;

    private static String getFilePath() {
        String programPath = null;
        try {
            programPath = new File(SaveEditor.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        //probably jank
        int length = programPath.length();
        String extension = programPath.substring(length - 4, length);
        inJar = extension.equals(".jar") || extension.equals(".exe");
        return programPath;
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
            System.out.print(val + " (Enter 'exit' to return): ");
            String in = input.nextLine().toUpperCase();
            if(in.equals("EXIT")){
                return;
            }
            func.accept(in);
        }
    }

    public static void main(String[] args){
        System.out.println();
        String programPath = getFilePath();

        System.out.print("Enter path for save file: (Default: same folder that this program is in): ");
        String path = input.nextLine();
        String savePath = "";
        boolean isFilePathInvalid = true;
        while(isFilePathInvalid){
            if(path.equals("")){
                if(inJar){
                    savePath = Paths.get(new File(programPath).getParent(), "savedata.txt").toString();
                } else {
                    int indexOfDir = programPath.indexOf("DragaliaSaveEditor");
                    if(indexOfDir == -1){
                        System.out.println("Directory 'DragaliaSaveEditor' not found!");
                        System.exit(98);
                    }
                    String editorPath = Paths.get(programPath.substring(0, indexOfDir), "DragaliaSaveEditor").toString();
                    savePath = Paths.get(editorPath, "savedata.txt").toString();
                }
            } else {
                savePath = path;
            }
            isFilePathInvalid = !new File(savePath).exists(); //basic file path exists check
            if(isFilePathInvalid){
                System.out.println("savedata not found at: " + savePath + "!");
                System.out.println();
                System.out.print("Enter path for save file: (Default: same folder that this program is in): ");
                path = input.nextLine();
            }
        }

        System.out.println();
        JsonUtils util = new JsonUtils(savePath, programPath, inJar);
        System.out.println("Hello " + util.getFieldAsString("data", "user_data", "name") + "!");
        yesNoQuestion("Uncap mana? (Sets mana to 10m)", () -> util.uncapMana());
        yesNoQuestion("Set rupies count to 2b?", () -> util.setRupies());
        yesNoQuestion(
                "Rob Donkay? (Sets wyrmites to 710k, singles to 2.6k, tenfolds to 170)",
                "Thanks Donkay!",
                () -> util.plunderDonkay());
        yesNoQuestion("Play Ch13 Ex1-2 Battle On The Byroad? (Sets eldwater to 10m)", () -> util.battleOnTheByroad());
        //check for invisible adventurers (skipped raid welfares)
        List<String> skippedTempAdventurers = util.checkSkippedTempAdventurers();
        if(skippedTempAdventurers.size() > 0){
            System.out.println("Skipped raid welfare adventurers: " + JsonUtils.listPrettify(skippedTempAdventurers) + " found.");
            yesNoQuestion("\tWould you like to max out their friendship level and add them to your roster?",
                    "Done!",
                    () -> util.setAdventurerVisibleFlags());
        }
        yesNoQuestion(
                "Max out existing adventurers/dragon/weapons/wyrmprints?",
                () -> {
                    yesNoQuestion("\tMax out existing adventurers?",
                            () -> util.maxAdventurers());
                    yesNoQuestion("\tMax out existing dragons?",
                            () -> util.maxDragons());
                    yesNoQuestion("\tMax out existing weapons?",
                            () -> util.maxWeapons());
                    yesNoQuestion("\tMax out existing wyrmprints?",
                            () -> util.maxWyrmprints());
                });
        yesNoQuestion(
                "Add all missing adventurers to roster?",
                () -> System.out.println("Added " + util.addMissingAdventurers() + " missing adventurers."),
                () -> yesNoQuestion("\tWould you like to add specific adventurers to roster?",
                        () -> continuousInput("\t\tEnter adventurer name",
                        (advName) -> util.addAdventurer(advName))));
        yesNoQuestion(
                "Add all missing dragons to roster?",
                () -> {
                        yesNoQuestion(
                            "\tExclude 3-star and 4-star dragons?",
                            () -> System.out.println(util.addMissingDragons(true)),
                            () -> System.out.println(util.addMissingDragons(false)));
                        yesNoQuestion("\tAdd additional dragons to roster?",
                            () -> continuousInput("\t\tEnter dragon name",
                            (dragonName) -> util.addDragon(dragonName)));
                    },
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
                "Set all material counts to 30,000?",
                "Done!",
                () -> util.addItems());
        yesNoQuestion(
                "Enter the Kaleidoscape? (Replaces portrait print inventory to a strong set of prints)",
                () -> yesNoQuestion(
                        "\tThis will completely replace portrait prints that you own. Is this ok?",
                        "Done!",
                        () -> util.backToTheMines()));
        yesNoQuestion("Add missing weapon skins?",
                () -> System.out.println("Added " + util.addMissingWeaponSkins() + " missing weapon skins."));
        yesNoQuestion("Max Halidom facilities?", () -> util.maxFacilities());
        yesNoQuestion(
                "Do additional hacked options?",
                () -> {
                    yesNoQuestion("\tGenerate random portrait prints? (This will replace your portrait print inventory)",
                    () -> util.kscapeRandomizer());
                    yesNoQuestion("\tAdd some hacked portrait prints?",
                    () -> util.addGoofyKscapes());
                });
        if(util.isSaveData2Present()){
            yesNoQuestion(
                    "savedata2.txt already exists in this directory. Would you like to overwrite it?",
                    () -> util.setOverwrite(true));
        }
        System.out.println();
        util.writeToFile();
        System.out.println();
        yesNoQuestion("View logs?", () -> util.printLogs());
        input.nextLine();
        System.out.println();
        System.out.println("Program finished. Enter anything to exit...");
    }

}
