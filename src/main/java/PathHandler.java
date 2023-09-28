import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class PathHandler {

    public static String getProgramPath() {
        String programPath = null;
        try {
            programPath = new File(SaveEditor.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return programPath;
    }

    public static boolean getIsOutOfIDE() {
        // probably jank
        String programPath = getProgramPath();
        int length = programPath.length();
        String extension = programPath.substring(length - 4, length);
        return extension.equals(".jar") || extension.equals(".exe");
    }

    public static String getPath(String path, String more, boolean isOutOfIDE) {
        if(isOutOfIDE){
            return Paths.get(new File(path).getParent(), more).toString();
        } else {
            return getPathInIDE(path, more);
        }
    }

    public static String getPathInIDE(String programPath, String more) {
        int indexOfDir = programPath.indexOf("DragaliaSaveEditor");
        if(indexOfDir == -1){
            System.out.println("Directory 'DragaliaSaveEditor' not found!");
            System.exit(98);
        }
        String editorPath = Paths.get(programPath.substring(0, indexOfDir), "DragaliaSaveEditor").toString();
        return Paths.get(editorPath, more).toString();
    }

    private static final String[] saveNames = new String[]{"savedata", "save"};
    private static final String[] saveExtensions = new String[]{".txt", ".json"};

    public static String validateDefaultSavePaths(String programPath, boolean isOutOfIDE) {
        String outMsg = "Could not automatically find a save file.";
        boolean foundValidSave = false;

        String savePath = "";

        for (String saveName : saveNames) {
            for (String saveExtension : saveExtensions) {
                if (foundValidSave) { // break out of loops if save is found
                    break;
                }

                String fileName = saveName + saveExtension;
                savePath = getPath(programPath, fileName, isOutOfIDE);

                int fileCheckCode = JsonUtils.checkIfJsonObject(savePath);
                boolean isFilePathInvalid = fileCheckCode != 0;
                if (isFilePathInvalid) {
                    if (fileCheckCode == 2) { // file exists but is not JSON
                        outMsg = Logging.formatString("savedata at path: '{0}' does not appear to be in JSON format!\n", savePath);
                    }
                } else {
                    foundValidSave = true;
                    outMsg = Logging.formatString("savedata found at path: '{0}'!\n", savePath);
                    break;
                }
            }
        }

        System.out.println(outMsg);

        if (foundValidSave) {
            return savePath;
        } else {
            return "";
        }

    }


}
