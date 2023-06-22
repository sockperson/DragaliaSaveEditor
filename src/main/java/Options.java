import sun.awt.windows.WBufferStrategy;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Options {

    private boolean maxAddedAdventurers = true;
    private boolean maxAddedWyrmprints = true;
    private boolean maxAddedDragons = true;
    private boolean maxAddedWeapons = true;
    //private boolean maxAddedFacilities = true;

    private boolean showOptionsValues = false; // mainly for debug, not prompted to user
    private boolean promptEditOptions = true;

    // hidden options
    private boolean openTeamEditor = false;

    private String optionsPath = "";

    private List<String> missingOptions = new ArrayList<>();

    public Options () {}

    public Options (String optionsPath) throws IOException {
        boolean toResetAndExportOptions = false; // set to true when there's an error with the options file
        List<String> fieldList = new ArrayList<>();
        if (new File(optionsPath).exists()) {
            //compile list of bool fields
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getType() == boolean.class ) {
                    fieldList.add(field.getName());
                }
            }

            //read options file
            BufferedReader br = new BufferedReader(new FileReader(optionsPath));
            String out = br.readLine();
            while (out != null) {
                String[] split = out.split(":");
                // validate the line in the options file
                if (split.length != 2) {
                    System.out.println("Error when importing options file- could not parse" +
                            " string '" + out + "'");
                    toResetAndExportOptions = true;
                    out = br.readLine();
                    continue;
                }
                String fieldName = split[0];
                boolean fieldValue = Boolean.parseBoolean(split[1]);
                // validate the boolean string
                if (!(split[1].equals("true") || split[1].equals("false"))) {
                    System.out.println("Error when importing options file- could not parse" +
                            " boolean string '" +  fieldName + "'.");
                    toResetAndExportOptions = true;
                    out = br.readLine();
                    continue;
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
        }
        this.missingOptions = fieldList;
        this.optionsPath = optionsPath;
        // if there's an issue with the options file, set to default and force export
        if (toResetAndExportOptions) {
            System.out.println("There was an error with the options file... loading default options and exporting...");
            missingOptions.clear();
            setFieldsToDefault();
            export();
        } else {
            if (showOptionsValues) {
                System.out.println(this);
            }
        }
    }

    public boolean toMaxAddedAdventurers () { return maxAddedAdventurers; }
    //public boolean toMaxAddedWyrmprints () { return maxAddedWyrmprints; }
    public boolean toMaxAddedDragons () { return maxAddedDragons; }
    //public boolean toMaxAddedWeapons () { return maxAddedWeapons; }
    //public boolean toMaxAddedFacilities () { return maxAddedFacilities; }

    //reflection is really cool!
    public boolean editOption (String fieldName, boolean value) {
        Field field = null;
        try {
            field = this.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setBoolean(this, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
        return true;
    }

    public void export() {
        try {
            FileWriter writer = new FileWriter(optionsPath);
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getType() == boolean.class ) { //we only care about exporting bool fields
                    String fieldName = field.getName();
                    boolean fieldValue = Boolean.parseBoolean(field.get(this).toString());
                    writer.write(fieldName + ":" + fieldValue + "\n");
                }
            }
            writer.close();
            System.out.println("Successfully wrote options to the file: " + optionsPath);
        } catch (IOException | IllegalAccessException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    public boolean getFieldValue (String fieldName) {
        try {
            return Boolean.parseBoolean(this.getClass().getDeclaredField(fieldName).get(this).toString());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean toPromptEditOptions () { return promptEditOptions; }

    public boolean hasMissingOptions () { return missingOptions.size() != 0; }

    public void setFieldsToDefault () {
        maxAddedAdventurers = true;
        maxAddedWyrmprints = true;
        maxAddedDragons = true;
        maxAddedWeapons = true;
        showOptionsValues = false;
        promptEditOptions = true;
        openTeamEditor = false;
    }

    public String toString () {
        return
            "maxAddedAdventurers: " + maxAddedAdventurers +
            ",\nmaxAddedWyrmprints: " + maxAddedWyrmprints +
            ",\nmaxAddedDragons: " + maxAddedDragons +
            ",\nmaxAddedWeapons: " + maxAddedWeapons +
            //",\nmaxAddedFacilities: " + maxAddedFacilities +
            ",\npromptEditOptions: " + promptEditOptions +
            ",\nshowOptionsValues: " + showOptionsValues;


    }
    /**
     *
     * min version:
     *
     * - query dragon min a2 level and correctly set it
     * - don't apply weapon bonuses
     * - apply correctly encyclopedia bonus for dragon and ad
     */




}
