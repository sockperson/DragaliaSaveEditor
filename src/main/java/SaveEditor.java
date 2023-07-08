import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public class SaveEditor {

    private static final Scanner input = new Scanner(System.in);
    private static boolean isOutOfIDE = false;
    private static Options options;

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
        isOutOfIDE = extension.equals(".jar") || extension.equals(".exe");
        return programPath;
    }


    //TODO convert these to while loop (this impl can cause a stack overflow lol)
    //this code sucks who wrote it

    public static String input(String question) {
        System.out.print(question + ": ");
        return input.nextLine();
    }

    //for writing Options values
    public static void passYesNoArg(String question, String fieldName, Consumer<Boolean> func) {
        String fieldVal = options.getFieldAsString(fieldName);
        if (fieldVal.equals("true")) {
            fieldVal = "y";
        } else if (fieldVal.equals("false")) {
            fieldVal = "n";
        }

        System.out.print(question + " (y/n) (Current: " + fieldVal + "): ");
        String in = input.nextLine();
        in = in.toUpperCase();

        if(in.equals("Y") || in.equals("YES")){
            func.accept(true);
        } else if (in.equals("N") || in.equals("NO")){
            func.accept(false);
        } else {
            System.out.println("Invalid Input. Enter 'y' or 'n'");
            passYesNoArg(question, fieldName, func);
        }
    }

    public static boolean passYesNo(String question) {
        System.out.print(question + " (y/n): ");
        String in = input.nextLine();
        in = in.toUpperCase();

        if(in.equals("Y") || in.equals("YES")){
            return true;
        } else if (in.equals("N") || in.equals("NO")){
            return false;
        } else {
            System.out.println("Invalid Input. Enter 'y' or 'n'");
            return passYesNo(question);
        }
    }

    public static void yesNoQuestion(String question, String response, Runnable func){
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

    public static void yesNoQuestion(String question, Runnable func){
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

    private static void maybeQuestion(String question, String response2, Runnable func, Runnable func2){
        System.out.print(question + " (y/n): ");        //print the question
        String in = input.nextLine();                   //take in the input
        in = in.toUpperCase();                          //make the input not case-sensitive
        if(in.equals("Y") || in.equals("YES")){         //if input is 'Y', print response and run the function
            func.run();
        } else if (in.equals("N") || in.equals("NO")){  //if input is 'N', do nothing
            return;
        } else if (in.equals("MITSUBAP")){              //do both of the funcs if secret input
            System.out.println(response2);
            func.run();
            func2.run();
        } else {                                        //if input invalid, ask the question again
            System.out.println("Invalid Input. Enter 'y' or 'n'");
            maybeQuestion(question, response2, func, func2);
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

    private static String getPath(String path, String more) {
        if(isOutOfIDE){
            return Paths.get(new File(path).getParent(), more).toString();
        } else {
            return getPathInIDE(path, more);
        }
    }

    private static String getPathInIDE(String programPath, String more) {
        int indexOfDir = programPath.indexOf("DragaliaSaveEditor");
        if(indexOfDir == -1){
            System.out.println("Directory 'DragaliaSaveEditor' not found!");
            System.exit(98);
        }
        String editorPath = Paths.get(programPath.substring(0, indexOfDir), "DragaliaSaveEditor").toString();
        return Paths.get(editorPath, more).toString();
    }

    public static void main(String[] args){
        System.out.println("\nDragalia Save Editor (v11.2.??)\n");
        String programPath = getFilePath();
        String optionsPath = getPath(programPath, "DLSaveEditor_options.txt");
        String teamDataPath = getPath(programPath, "teams.json");
        // options file stuff
        options = new Options(optionsPath);
        if (options.getFieldAsBoolean("promptEditOptions")) {
            yesNoQuestion("Edit save editing options?",
                    () -> {
                        passYesNoArg("\tMax out added adventurers?", "maxAddedAdventurers", (arg) ->
                                options.editBooleanOption("maxAddedAdventurers", arg));
                        passYesNoArg("\tMax out added dragons?", "maxAddedDragons", (arg) ->
                                options.editBooleanOption("maxAddedDragons", arg));
                        passYesNoArg("\tMax out added wyrmprints?", "maxAddedWyrmprints", (arg) ->
                                options.editBooleanOption("maxAddedWyrmprints", arg));
                        passYesNoArg("\tMax out added weapons?", "maxAddedWeapons", (arg) ->
                                options.editBooleanOption("maxAddedWeapons", arg));
                        passYesNoArg("\tAsk to edit these options next time the program is run?", "promptEditOptions", (arg) ->
                                options.editBooleanOption("promptEditOptions", arg));
                        System.out.println("\tFinished editing options.");
                        options.export();
                    });
        }
        //
        String savePath = "";
        boolean validDefaultSavePath = false;
        if (!options.getFieldAsString("defaultSaveName").equals("?")) {
            // default save path input
            System.out.println("Using this directory for default filepath.");
            String defaultSaveName = options.getFieldAsString("defaultSaveName");
            savePath = getPath(programPath, defaultSaveName);
            int fileCheckCode = JsonUtils.checkIfJsonObject(savePath);
            if (fileCheckCode == 1) {
                Logging.print("savedata not found at path: '{0}'! Did you forget to include the file extension? (.txt or .json)\n", savePath);
            }
            if (fileCheckCode == 2) {
                Logging.print("savedata at path: '{0}' does not appear to be in JSON format!\n", savePath);
            }
            validDefaultSavePath = fileCheckCode == 0;
        }
        if (!validDefaultSavePath) {
            // normal save path input
            System.out.println("(Leave this input empty and press 'Enter' key if the save file (savedata.txt) is in the same folder as this program.)");
            System.out.print("Enter path for save file: ");
            String path = input.nextLine();
            boolean isFilePathInvalid = true;
            while(isFilePathInvalid){
                if(path.equals("")){
                    savePath = getPath(programPath, "savedata.txt");
                } else {
                    if (!path.contains("/")) {
                        System.out.println("Using this directory for filepath.");
                        savePath = getPath(programPath, path);
                    }
                }
                int fileCheckCode = JsonUtils.checkIfJsonObject(savePath);
                isFilePathInvalid = fileCheckCode != 0;
                if(isFilePathInvalid){
                    if (fileCheckCode == 1) {
                        Logging.print("savedata not found at path: '{0}'! Did you forget to include the file extension? (.txt or .json)\n", savePath);
                    }
                    if (fileCheckCode == 2) {
                        Logging.print("savedata at path: '{0}' does not appear to be in JSON format!\n", savePath);
                    }
                    System.out.println("(Leave this input empty and press 'Enter' key if the save file is in the same folder as this program.)");
                    System.out.print("Enter path for save file: ");
                    path = input.nextLine();
                }
            }
        }

        JsonUtils util = new JsonUtils(savePath, programPath, isOutOfIDE);
        System.out.println("Save data found at: " + savePath + "\n");
        System.out.println("Hello " + util.getFieldAsString("data", "user_data", "name") + "!");

        util.deleteDupeIds(); // sanity check for dupe IDs. shouldn't happen

        util.setOptions(options);

        if(options.getFieldAsBoolean("openTeamEditor")) {
            yesNoQuestion("Enter teams manager?",
                    () -> {
                        util.enterTeamEditor(teamDataPath);
                    });
        }

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
            System.out.println("Skipped raid welfare adventurers: " + Logging.listPrettify(skippedTempAdventurers) + " found.");
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
                            "\tInclude 3-star and 4-star dragons?",
                            () -> System.out.println(util.addMissingDragons(false)),
                            () -> System.out.println(util.addMissingDragons(true)));
                        yesNoQuestion("\tAdd additional dragons to roster?",
                            () -> continuousInput("\t\tEnter dragon name",
                            (dragonName) -> util.addDragon(dragonName)));
                    },
                () -> yesNoQuestion("\tWould you like to add additional specific dragons to roster?",
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
                () -> util.addMaterials());
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
                "Do additional hacked options? (Enter 'n' if you wish to keep your save data \"vanilla\")",
                () -> {
                        yesNoQuestion("\tGenerate random portrait prints? (This will replace your portrait print inventory)",
                            () -> util.kscapeRandomizer());
                        yesNoQuestion("\tAdd some hacked portrait prints?",
                            () -> util.addGoofyKscapes());
                        maybeQuestion(
                                "\tAdd unplayable units? (Note: These units are not fully implemented and may cause softlocks or crashes.)",
                                "\tHidden Option: Extra options for adding unplayable units",
                            () -> {
                                yesNoQuestion("\tAdd unit: Tutorial Zethia?",
                                        () -> util.addTutorialZethia());
                                yesNoQuestion("\tAdd units: Story Leif(s)?",
                                        () -> util.addStoryLeifs());
                                yesNoQuestion("\tAdd unit: Sharpshooter Cleo",
                                        () -> util.addGunnerCleo());
                                yesNoQuestion("\tAdd unique shapeshift dragons?",
                                        () -> util.addUniqueShapeshiftDragons());
                            },
                            () -> {
                                yesNoQuestion("\tAdd unit: Dog?",
                                        () -> util.addDog());
                                yesNoQuestion("\tAdd units: Anniversary Notte(s)?",
                                        () -> util.addNottes());
                                yesNoQuestion("\tAdd units: Story NPCs (a lot)?",
                                        () -> util.addStoryNPCs());
                                yesNoQuestion("\tAdd units: ABR 3-star units?",
                                        () -> util.addABR3Stars());
                                yesNoQuestion("\tAdd other dragons (a lot)?",
                                        () -> util.addUnplayableDragons());
                            });
                        }
                );
        System.out.println("\nFinished editing save...getting ready for exporting.");
        boolean passedTests = util.checkIfTestsPassed();
        if(passedTests){
            if(util.isSaveData2Present()){
                SaveEditor.yesNoQuestion(
                        "savedata2.txt already exists in this directory. Would you like to overwrite it?",
                        () -> util.setOverwrite(true));
            }
            util.writeToFile();
        }
        System.out.println();
        yesNoQuestion("View logs?", () -> Logging.printLogs());
        System.out.println();
        System.out.println("Program finished. Enter anything to exit...");
        input.nextLine();
    }

}
