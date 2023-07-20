import meta.AdventurerMeta;

import java.io.*;
import java.util.*;

public class Options {

    private static HashMap<String, String> optionMap = new HashMap<>();
    private static HashMap<String, String> optionNameToType = new HashMap<>();

    private static String[] optionNameAndDefaults = new String[]{
            "(bool):maxAddedAdventurers=true", "(bool):maxAddedWyrmprints=true", "(bool):maxAddedDragons=true",
            "(bool):maxAddedWeapons=true", "(bool):showOptionsValues=false", "(bool):promptEditOptions=true",
            "(bool):openTeamEditor=false", "(bool):maxDragonBonds=true", "(string):defaultSaveName=?"
    };

    private static String[] portraitAdventurersNameAndDefaults = new String[]{};

    private static final List<String> optionNames = new ArrayList<>();

    private static String optionsPath = "";

    public static void init(String optionsPath) {
        initializePortraitAdventurersNameAndDefaults();
        initializeOptionDefaultMap();
        Options.optionsPath = optionsPath;
        try {
            boolean shouldReExportOptions = !readFromOptionsFile();
            if (shouldReExportOptions) {
                System.out.println("There were missing option values found- re-exporting to file, and" +
                        " prompting editing options.");
                export();
                editBooleanOption("promptEditOptions", true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean readFromOptionsFile() throws IOException {
        boolean toResetAndExportOptions = false; // set to true when there's an error with the options file
        List<String> fieldList = new ArrayList<>(optionNames);
        if (new File(optionsPath).exists()) {
            //read options file
            BufferedReader br = new BufferedReader(new FileReader(optionsPath));
            String out = br.readLine(); // (bool):maxAddedAdventurers=true
            while (out != null) {
                String[] colonSplit = out.split(":"); // (bool), maxAddedAdventurers=true
                // validate the line in the options file
                if (colonSplit.length != 2) {
                    Logging.log("Error when importing options file- could not parse" +
                            " string '" + out + "'");
                    toResetAndExportOptions = true;
                    out = br.readLine();
                    continue;
                }
                // validate the line in the options file
                String[] equalsSplit = colonSplit[1].split("="); // maxedAddedAdventurers, true
                if (equalsSplit.length != 2) {
                    Logging.log("Error when importing options file- could not parse" +
                            " string '" + out + "'");
                    toResetAndExportOptions = true;
                    out = br.readLine();
                    continue;
                }
                String fieldName = equalsSplit[0];
                String fieldValue = equalsSplit[1];
                // validate that the name exists
                if (!optionNames.contains(fieldName)) {
                    Logging.log("Could not find option of name '{0}' when reading options file", fieldName);
                    toResetAndExportOptions = true;
                    out = br.readLine();
                    continue;
                }
                // validate the option value to type
                String optionType = optionNameToType.get(fieldName);
                if(!validateOptionValueToType(optionType, fieldValue)) {
                    Logging.print("For option of name '{0}', failed to validate option value '{1}' for" +
                            " option type '{2}' when reading options file", fieldName, fieldValue, optionType);
                }
                // edit the field corresponding to fieldName, validate that fieldName is a field
                if (!editOption(fieldName, fieldValue)) {
                    System.out.println("Error when importing options file- no field '" + fieldName + "' found.");
                    toResetAndExportOptions = true;
                    out = br.readLine();
                    continue;
                }
                fieldList.remove(fieldName); //rm from field list if found
                out = br.readLine();
            }
        } else {
            System.out.println("No options file found in this directory --> making one.");
            export();
            return true;
        }
        // if there's an issue with the options file, set to default and force export
        if (toResetAndExportOptions) {
            System.out.println("There was either an error in the options file or it needed to be updated. Exporting default options to file.");
            initializeOptionDefaultMap();
            fieldList.clear();
            export();
            return true;
        } else {
            if (getFieldAsBoolean("showOptionsValues")) {
                print();
            }
        }
        // return true --> no missing options
        // return false --> there are missing options
        return fieldList.isEmpty();
    }

    private static final String[][] portraitDefaults = new String[][]{
            new String[]{"Naveed", "Xander", "Bondforged Prince", "Civilian Leif", "Gala Alex"},
            new String[]{"Nobunaga", "Gala Mascula", "Harle", "Gala Luca", "Ieyasu"},
            new String[]{"Summer Mitsuhide", "Mitsuba", "Gala Notte", "Fleur", "Delphi"},
            new String[]{"Gala Gatov", "Lazry", "Valentine's Melody", "Gala Audric", "Dragonyule Victor"},
            new String[]{"Emma", "Xainfried", "Kirsty", "Ryszarda", "Botan"},
            new String[]{"Summer Alex", "Hunter Sarisse", "Hawk", "Summer Cleo", "Halloween Sylas"},
            new String[]{"Seimei", "Gala Emile", "Maribelle", "Bondforged Zethia", "Gala Cleo"},
            new String[]{"Ayaha & Otoha", "Dragonyule Lily", "Formal Noelle", "Gala Zena", "Grace"},
            new String[]{"Dragonyule Ilia", "Lapis", "Formal Joachim", "Halloween Laxi", "Humanoid Zodiark"}
    };

    private static void initializePortraitAdventurersNameAndDefaults() {
        String[] out = new String[45];
        for (int element = 1; element <= 5; element++) {
            for (int weapon = 1; weapon <= 9; weapon++) {
                String eleString = AdventurerMeta.getElementString(element);
                String wepString = AdventurerMeta.getWeaponTypeString(weapon);
                String optionString = "portrait" + eleString + wepString + "Name";
                int index = ((element - 1) * 9) + weapon - 1;
                String outStr = "(adventurer):" + optionString + "=" + portraitDefaults[weapon - 1][element - 1];
                out[index] = outStr;
            }
        }
        portraitAdventurersNameAndDefaults = out;
    }

    private static String[] getAllOptionsAndDefaults () {
        int normalOptionsSize = optionNameAndDefaults.length;
        int portraitOptionsSize = portraitAdventurersNameAndDefaults.length;
        int totalSize = normalOptionsSize + portraitOptionsSize;
        String[] out = new String[totalSize];
        int index = 0;
        for (int i = 0; i < normalOptionsSize; i++) {
            out[index] = optionNameAndDefaults[i];
            index++;
        }
        for (int i = 0; i < portraitOptionsSize; i++) {
            out[index] = portraitAdventurersNameAndDefaults[i];
            index++;
        }
        return out;
    }

    private static void initializeOptionDefaultMap() {
        for (String optionString : getAllOptionsAndDefaults()) { // (bool):maxAddedAdventurers=true
            String[] optionStringSplit = optionString.split("="); // (bool):maxedAddedAdventurers, true
            String optionNameAndType = optionStringSplit[0]; // (bool):maxedAddedAdventurers
            String[] optionNameAndTypeSplit = optionNameAndType.split(":"); // (bool), maxedAddedAdventurers
            String optionType = optionNameAndTypeSplit[0]; // (bool)
            String optionName = optionNameAndTypeSplit[1]; // maxedAddedAdventurers
            String optionDefaultValue = optionStringSplit[1];
            optionMap.put(optionName, optionDefaultValue);
            optionNameToType.put(optionName, optionType);
            optionNames.add(optionName);
        }
    }

    private static boolean validateOptionValueToType(String type, String value) {
        switch (type) {
            case "(bool)":
                return value.equals("true") || value.equals("false");
            case "(string)":
                return true;
            case "(adventurer)":
                return DragaliaData.nameToAdventurer.containsKey(value.toUpperCase(Locale.ROOT));
        }
        Logging.print("Could not find option of type: {0} when calling validateOptionValueToType()", type);
        return false;
    }

    public static boolean editBooleanOption (String fieldName, boolean value) {
        if (!optionMap.containsKey(fieldName)) {
            Logging.print("Option of name: '{0}' could not be found", fieldName);
            return false;
        }
        optionMap.put(fieldName, value ? "true" : "false");
        return true;
    }

    public static boolean editOption (String fieldName, String value) {
        if (!optionMap.containsKey(fieldName)) {
            Logging.print("Option of name: '{0}' could not be found", fieldName);
            return false;
        }
        optionMap.put(fieldName, value);
        return true;
    }

    public static void export() {
        try {
            FileWriter writer = new FileWriter(optionsPath);
            for (String optionName : optionNames) {
                String optionValue = optionMap.get(optionName);
                String optionType = optionNameToType.get(optionName);
                writer.write(optionType + ":" + optionName + "=" + optionValue + "\n");
            }
            writer.close();
            System.out.println("Successfully wrote options to the file: " + optionsPath);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    public static boolean getFieldAsBoolean(String fieldName) {
        if (!optionMap.containsKey(fieldName)) {
            Logging.print("Could not find option named '{0}' when calling getFieldValue()", fieldName);
        }
        return optionMap.get(fieldName).equals("true");
    }

    public static String getFieldAsString (String fieldName) {
        if (!optionMap.containsKey(fieldName)) {
            Logging.print("Could not find option named '{0}' when calling getFieldValue()", fieldName);
        }
        return optionMap.get(fieldName);
    }

    public static void print () {
        for (Map.Entry<String, String> vals : optionMap.entrySet()) {
            String outStr = vals.getKey() + ":" + vals.getValue();
            System.out.println(outStr);
        }
    }

}
