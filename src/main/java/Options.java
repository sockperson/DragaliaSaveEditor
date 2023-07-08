import java.io.*;
import java.util.*;

public class Options {

    private HashMap<String, String> optionMap = new HashMap<>();
    private HashMap<String, String> optionNameToType = new HashMap<>();

    private String[] optionNameAndDefaults = new String[]{
            "(bool):maxAddedAdventurers=true", "(bool):maxAddedWyrmprints=true", "(bool):maxAddedDragons=true",
            "(bool):maxAddedWeapons=true", "(bool):showOptionsValues=false", "(bool):promptEditOptions=true",
            "(bool):openTeamEditor=false", "(bool):maxDragonBonds=true", "(string):defaultSaveName=?"
    };

    private List<String> optionNames = new ArrayList<>();

    private String optionsPath = "";

    private List<String> missingOptions = new ArrayList<>();
    private boolean hasMissingOptions = false;

    public Options (String optionsPath) {
        initializeOptionDefaultMap();
        this.optionsPath = optionsPath;
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

    private boolean readFromOptionsFile() throws IOException {
        boolean toResetAndExportOptions = false; // set to true when there's an error with the options file
        if (new File(optionsPath).exists()) {
            List<String> fieldList = new ArrayList<>(optionNames);
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
                    Logging.print("Failed to validate option of name '{0}' and value '{1}' when" +
                            " reading options file", fieldName, fieldValue);
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
            missingOptions = fieldList;
        } else {
            System.out.println("No options file found in this directory --> making one.");
            export();
            return true;
        }
        // if there's an issue with the options file, set to default and force export
        if (toResetAndExportOptions) {
            System.out.println("There was either an error in the options file or it needed to be updated. Exporting default options to file.");
            missingOptions.clear();
            initializeOptionDefaultMap();
            export();
            return true;
        } else {
            if (getFieldAsBoolean("showOptionsValues")) {
                System.out.println(this);
            }
        }
        // return true --> no missing options
        // return false --> there are missing options
        return missingOptions.isEmpty();
    }

    private void initializeOptionDefaultMap() {
        for (String optionString : optionNameAndDefaults) { // (bool):maxAddedAdventurers=true
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

    private boolean validateOptionValueToType(String type, String value) {
        switch (type) {
            case "(bool)":
                return value.equals("true") || value.equals("false");
            case "(string)":
                return true;
        }
        Logging.print("Could not find option of type: {0} when calling validateOptionValueToType()", type);
        return false;
    }

    public boolean editBooleanOption (String fieldName, boolean value) {
        if (!optionMap.containsKey(fieldName)) {
            Logging.print("Option of name: '{0}' could not be found", fieldName);
            return false;
        }
        optionMap.put(fieldName, value ? "true" : "false");
        return true;
    }

    public boolean editOption (String fieldName, String value) {
        if (!optionMap.containsKey(fieldName)) {
            Logging.print("Option of name: '{0}' could not be found", fieldName);
            return false;
        }
        optionMap.put(fieldName, value);
        return true;
    }

    public void export() {
        try {
            FileWriter writer = new FileWriter(optionsPath);
            for (Map.Entry<String, String> optionsAndValues : optionMap.entrySet()) {
                String optionName = optionsAndValues.getKey();
                String optionValue = optionsAndValues.getValue();
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

    public boolean getFieldAsBoolean(String fieldName) {
        if (!optionMap.containsKey(fieldName)) {
            Logging.print("Could not find option named '{0}' when calling getFieldValue()", fieldName);
        }
        return optionMap.get(fieldName).equals("true");
    }

    public String getFieldAsString (String fieldName) {
        if (!optionMap.containsKey(fieldName)) {
            Logging.print("Could not find option named '{0}' when calling getFieldValue()", fieldName);
        }
        return optionMap.get(fieldName);
    }

    public boolean hasMissingOptions () { return hasMissingOptions; }

    public String toString () {
        StringBuilder bobTheBuilder = new StringBuilder();
        for (Map.Entry<String, String> vals : optionMap.entrySet()) {
            String outStr = vals.getKey() + ":" + vals.getValue();
            bobTheBuilder.append(outStr);
            bobTheBuilder.append("\n");
        }
        return bobTheBuilder.toString();
    }

}
