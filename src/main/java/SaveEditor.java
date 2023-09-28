import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

public class SaveEditor {

    private static boolean isOutOfIDE = false;

    public static void main(String[] args){
        System.out.println("\nDragalia Save Editor (v11.5)\n");
        String programPath = PathHandler.getProgramPath();
        isOutOfIDE = PathHandler.getIsOutOfIDE();
        String optionsPath = PathHandler.getPath(programPath, "DLSaveEditor_options.txt", isOutOfIDE);
        String teamDataPath = PathHandler.getPath(programPath, "teams.json", isOutOfIDE);

        // resources
        DragaliaData.init();

        // options file stuff
        Options.init(optionsPath);
        System.out.println();
        if (Options.getFieldAsBoolean("promptEditOptions")) {
            InputUtils.yesNoQuestion("Edit save editing options?",
                    () -> {
                        InputUtils.writeOptionsValue("\tMax out added adventurers?", "maxAddedAdventurers");
                        InputUtils.writeOptionsValue("\tMax out added dragons?", "maxAddedDragons");
                        InputUtils.writeOptionsValue("\tMax out added wyrmprints?", "maxAddedWyrmprints");
                        InputUtils.writeOptionsValue("\tMax out added weapons?", "maxAddedWeapons");
                        InputUtils.writeOptionsValue("\tMax out dragon bond levels?", "maxDragonBonds");
                        InputUtils.writeOptionsValue("\tSkip automatically finding a savefile in this directory?", "ignoreAutoFindingSaveFile");
                        InputUtils.writeOptionsValue("\tAsk to edit these options next time the program is run?", "promptEditOptions");
                        System.out.println("\tFinished editing options.");
                        Options.export();
                    });
            System.out.println();
        }

        //// Save file checking
        String savePath = "";

        // Check for default save path specified in options
        if (!Options.getFieldAsString("defaultSaveName").equals("?")) {
            // default save path input
            System.out.println("Using default savefile name specified in options.");
            String defaultSaveName = Options.getFieldAsString("defaultSaveName");
            String maybeSavePath = PathHandler.getPath(programPath, defaultSaveName, isOutOfIDE);
            int fileCheckCode = JsonUtils.checkIfJsonObject(maybeSavePath);
            if (fileCheckCode == 1) {
                Logging.print("savedata not found at path: '{0}'! Did you forget to include the file extension? (.txt or .json)\n", maybeSavePath);
            }
            if (fileCheckCode == 2) {
                Logging.print("savedata at path: '{0}' does not appear to be in JSON format!\n", maybeSavePath);
            }
            savePath = (fileCheckCode == 0) ? (maybeSavePath) : ("");
        }

        // Check for generic default save names "save.json", "savedata.txt", etc
        if (savePath.equals("")) { // if save path still empty
            if (!Options.getFieldAsBoolean("ignoreAutoFindingSaveFile")) {
                System.out.println("Attempting to find a save file in this directory. (This can be disabled in options)");
                savePath = PathHandler.validateDefaultSavePaths(programPath, isOutOfIDE);
            } else {
                System.out.println("Skipping automatically finding a save file.");
            }
        }

        // Check for user input save names
        if (savePath.equals("")) { // if save path still empty
            System.out.print("Enter path for save file: ");
            String path = InputUtils.input.nextLine();
            boolean isFilePathInvalid = true;
            while(isFilePathInvalid){
                if (!path.contains("/")) {
                    System.out.println("Using this directory for filepath.");
                    savePath = PathHandler.getPath(programPath, path, isOutOfIDE);
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
                    System.out.print("Enter path for save file: ");
                    path = InputUtils.input.nextLine();
                }
            }
        }


        // JsonUtils
        JsonUtils.init(savePath, programPath, isOutOfIDE);
        System.out.println("Save data found at: " + savePath + "\n");
        System.out.println("Hello " + JsonUtils.getFieldAsString("data", "user_data", "name") + "!");

        JsonUtils.deleteDupeIds(); // sanity check for dupe IDs. shouldn't happen
        JsonUtils.applyFixes();
        JsonUtils.validate();

        if(Options.getFieldAsBoolean("openTeamEditor")) {
            InputUtils.yesNoQuestion("Enter teams manager?",
                    () -> {
                        TeamsUtil.init(teamDataPath);
                        TeamsUtil.run();
                    });
        }

        InputUtils.yesNoQuestion("Uncap mana? (Sets mana to 10m)", JsonUtils::uncapMana);
        InputUtils.yesNoQuestion("Set rupies count to 2b?", JsonUtils::setRupies);
        InputUtils.yesNoQuestion(
                "Rob Donkay? (Sets wyrmites to 710k, singles to 2.6k, tenfolds to 170)",
                "Thanks Donkay!",
                JsonUtils::plunderDonkay);
        InputUtils.yesNoQuestion("Play Ch13 Ex1-2 Battle On The Byroad? (Sets eldwater to 10m)", JsonUtils::battleOnTheByroad);
        //check for invisible adventurers (skipped raid welfares)
        List<String> skippedTempAdventurers = JsonUtils.checkSkippedTempAdventurers();
        if(skippedTempAdventurers.size() > 0){
            System.out.println("Skipped raid welfare adventurers: " + Logging.listPrettify(skippedTempAdventurers) + " found.");
            InputUtils.yesNoQuestion("\tWould you like to max out their friendship level and add them to your roster?",
                    "Done!",
                    JsonUtils::setAdventurerVisibleFlags);
        }
        InputUtils.yesNoQuestion(
                "Max out existing adventurers/dragon/weapons/wyrmprints?",
                () -> {
                    InputUtils.yesNoQuestion("\tMax out existing adventurers?",
                            JsonUtils::maxAdventurers);
                    InputUtils.yesNoQuestion("\tMax out existing dragons?",
                            JsonUtils::maxDragons);
                    InputUtils.yesNoQuestion("\tMax out existing weapons?",
                            JsonUtils::maxWeapons);
                    InputUtils.yesNoQuestion("\tMax out existing wyrmprints?",
                            JsonUtils::maxWyrmprints);
                });
        InputUtils.yesNoQuestion(
                "Add all missing adventurers to roster?",
                () -> System.out.println("Added " + JsonUtils.addMissingAdventurers() + " missing adventurers."),
                () -> InputUtils.yesNoQuestion("\tWould you like to add specific adventurers to roster?",
                        () -> InputUtils.continuousInput("\t\tEnter adventurer name",
                                JsonUtils::addAdventurer)));
        InputUtils.yesNoQuestion(
                "Add all missing dragons to roster?",
                () -> {
                    InputUtils.yesNoQuestion(
                            "\tInclude 3-star and 4-star dragons?",
                            () -> System.out.println(JsonUtils.addMissingDragons(false)),
                            () -> System.out.println(JsonUtils.addMissingDragons(true)));
                    InputUtils.yesNoQuestion("\tAdd additional dragons to roster?",
                            () -> InputUtils.continuousInput("\t\tEnter dragon name",
                                    JsonUtils::addDragon));
                    },
                () -> InputUtils.yesNoQuestion("\tWould you like to add additional specific dragons to roster?",
                        () -> InputUtils.continuousInput("\t\tEnter dragon name",
                                JsonUtils::addDragon)));
        InputUtils.yesNoQuestion(
                "Add all missing weapons?",
                () -> System.out.println("Added " + JsonUtils.addMissingWeapons() + " missing weapons."));
        InputUtils.yesNoQuestion(
                "Add all missing wyrmprints?",
                () -> System.out.println("Added " + JsonUtils.addMissingWyrmprints() + " missing wyrmprints."));
        InputUtils.yesNoQuestion(
                "Set all material counts to 30,000?",
                "Done!",
                JsonUtils::addMaterials);
        InputUtils.yesNoQuestion(
                "Enter the Kaleidoscape? (Replaces portrait print inventory to a strong set of prints)",
                () -> InputUtils.yesNoQuestion(
                        "\tThis will completely replace portrait prints that you own. Is this ok?",
                        "Done!",
                        JsonUtils::backToTheMines));
        InputUtils.yesNoQuestion("Add missing weapon skins?",
                () -> System.out.println("Added " + JsonUtils.addMissingWeaponSkins() + " missing weapon skins."));
        InputUtils.yesNoQuestion("Max Halidom facilities?", JsonUtils::maxFacilities);
        InputUtils.yesNoQuestion("Set epithet?",
                () -> InputUtils.validatedInputAndCall(
                        "\tEnter epithet name",
                        (name) -> DragaliaData.nameToEpithetId.containsKey(name),
                        (name) -> {
                            JsonUtils.setEpithet(name);
                            System.out.println("Set epithet!");
                        },
                        "\tUnknown epithet name."
                ));
        InputUtils.yesNoQuestion(
                "Do additional hacked options? (Enter 'n' if you wish to keep your save data \"vanilla\")",
                () -> {
                        InputUtils.yesNoQuestion("\tGenerate random portrait prints? (This will replace your portrait print inventory)",
                                JsonUtils::kscapeRandomizer);
                        InputUtils.yesNoQuestion("\tAdd some hacked portrait prints?",
                                JsonUtils::addGoofyKscapes);
                        InputUtils.maybeQuestion(
                                "\tAdd unplayable units? (Note: These units are not fully implemented and may cause softlocks or crashes.)",
                                "\tHidden Option: Extra options for adding unplayable units",
                            () -> {
                                InputUtils.yesNoQuestion("\tAdd unit: Tutorial Zethia?",
                                        JsonUtils::addTutorialZethia);
                                InputUtils.yesNoQuestion("\tAdd units: Story Leif(s)?",
                                        JsonUtils::addStoryLeifs);
                                InputUtils.yesNoQuestion("\tAdd unit: Sharpshooter Cleo",
                                        JsonUtils::addGunnerCleo);
                                InputUtils.yesNoQuestion("\tAdd unique shapeshift dragons?",
                                        JsonUtils::addUniqueShapeshiftDragons);
                            },
                            () -> {
                                InputUtils.yesNoQuestion("\tAdd unit: Dog?",
                                        JsonUtils::addDog);
                                InputUtils.yesNoQuestion("\tAdd units: Anniversary Notte(s)?",
                                        JsonUtils::addNottes);
                                InputUtils.yesNoQuestion("\tAdd units: Story NPCs (a lot)?",
                                        JsonUtils::addStoryNPCs);
                                InputUtils.yesNoQuestion("\tAdd units: ABR 3-star units?",
                                        JsonUtils::addABR3Stars);
                                InputUtils.yesNoQuestion("\tAdd other dragons (a lot)?",
                                        JsonUtils::addUnplayableDragons);
                            });
                        }
                );
        System.out.println("\nFinished editing save...getting ready for exporting.");
        if(Tests.didTestsPass()){
            if(JsonUtils.isSaveData2Present()){
                InputUtils.yesNoQuestion(
                        "savedata2.txt already exists in this directory. Would you like to overwrite it?",
                        () -> JsonUtils.setOverwrite(true));
            }
            JsonUtils.writeToFile();
        } else {
            System.out.println("One or more tests failed... cannot export savedata. " +
                    "Contact @sockperson if this message appears.");
        }
        System.out.println();
        InputUtils.yesNoQuestion("View logs?", () -> Logging.printLogs());
        System.out.println();
        System.out.println("Program finished. Enter anything to exit...");
        InputUtils.input.nextLine();
    }

    public static void exit() {
        System.out.println("The program encountered an error ^, enter anything to exit.");
        InputUtils.input.nextLine();
        System.exit(42);
    }
}
