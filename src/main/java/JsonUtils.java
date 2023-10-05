import java.io.*;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import meta.*;

public class JsonUtils {

    private static final int MAX_DRAGON_CAPACITY = 525;

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static String savePath;
    private static String jarPath;

    private static boolean toOverwrite = false;
    private static boolean inJar = true;

    private static final Random rng = new Random();

    //savefile
    public static JsonObject jsonData;

    public static void init(String savePath, String jarPath, boolean inJar) {
        Logging.log("Initializing JsonUtils...");
        JsonUtils.savePath = savePath;      //
        JsonUtils.jarPath = jarPath;        //
        JsonUtils.inJar = inJar;            // used for exporting
        try {
            jsonData = getSaveData().getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to read JSON data!");
            System.exit(99);
        }
    }

    public static void writeToFile() {
        try {
            String newPath;
            String fileName = "savedata2.txt";
            if (isSaveData2Present() && !toOverwrite) {
                int count = 3;
                fileName = "savedata" + count + ".txt";
                while (new File(fileName).exists()) {
                    count++;
                    fileName = "savedata" + count + ".txt";
                }
                newPath = fileName;
            } else {
                newPath = fileName;
            }
            FileWriter fileWriter = new FileWriter(newPath);
            GSON.toJson(jsonData, fileWriter);
            fileWriter.flush();
            fileWriter.close();
            if(inJar) {
                System.out.println("Saved output JSON to " + Paths.get(jarPath, fileName));
            } else {
                System.out.println("Saved output JSON to " + newPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isSaveData2Present() {
        return new File("savedata2.txt").exists();
    }

    public static void setOverwrite(boolean toOverwrite) {
        JsonUtils.toOverwrite = toOverwrite;
    }

    public static double addDoubles(double val1, double val2) {
        //sure hope this prevents floating point inaccuracy
        return ((10.0 * val1) + (10.0 * val2)) / 10;
    }

    private static JsonObject getSaveData() throws IOException {
        JsonReader reader = new JsonReader(new FileReader(savePath));
        return GSON.fromJson(reader, JsonObject.class);
    }

    public static JsonObject getJsonObject (String path) {
        try {
            JsonReader reader = new JsonReader(new FileReader(path));
            return GSON.fromJson(reader, JsonObject.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFieldAsString(String... memberNames) {
        return getField(memberNames).getAsString();
    }

    public static int getFieldAsInt(String... memberNames) {
        return getField(memberNames).getAsInt();
    }

    public static JsonArray getFieldAsJsonArray(String... memberNames) {
        return getField(memberNames).getAsJsonArray();
    }

    public static JsonObject getFieldAsJsonObject(String... memberNames) {
        return getField(memberNames).getAsJsonObject();
    }

    public static List<Integer> jsonArrayToList(JsonArray value){
        ArrayList<Integer> out = new ArrayList<>();
        for (JsonElement jsonEle : value) {
            out.add(jsonEle.getAsInt());
        }
        return out;
    }

    private static void writeInteger(int value, String... memberNames) {
        List<String> memberNameList = new ArrayList<>(Arrays.asList(memberNames));

        JsonElement jsonEle = jsonData;
        for (int i = 0; i < memberNameList.size() - 1; i++) {
            jsonEle = jsonEle.getAsJsonObject().get(memberNameList.get(i));
        }
        String lastMemberName = memberNameList.get(memberNameList.size() - 1);
        JsonObject jsonObj = jsonEle.getAsJsonObject();
        jsonObj.remove(lastMemberName);
        jsonObj.addProperty(lastMemberName, value);
    }

    private static void writeLong(long value, String... memberNames) {
        List<String> memberNameList = new ArrayList<>(Arrays.asList(memberNames));

        JsonElement jsonEle = jsonData;
        for (int i = 0; i < memberNameList.size() - 1; i++) {
            jsonEle = jsonEle.getAsJsonObject().get(memberNameList.get(i));
        }
        String lastMemberName = memberNameList.get(memberNameList.size() - 1);
        JsonObject jsonObj = jsonEle.getAsJsonObject();
        jsonObj.remove(lastMemberName);
        jsonObj.addProperty(lastMemberName, value);
    }

    private static JsonElement getField(String... memberNames) {
        JsonElement jsonEle = jsonData;
        for (String memberName : memberNames) {
            if (jsonEle.isJsonObject()) {
                JsonObject jsonObj = jsonEle.getAsJsonObject();
                if (jsonObj.has(memberName)) {
                    jsonEle = jsonEle.getAsJsonObject().get(memberName);
                } else {
                    Logging.print("Could not find field '{0}' in the save file- this shouldn't happen", memberName);
                    SaveEditor.exit();
                }
            }
        }
        return jsonEle;
    }

    //List Utils
    public static int getSum(JsonObject src, String... memberNames) {
        int sum = 0;
        for (String memberName : memberNames) {
            sum += src.get(memberName).getAsInt();
        }
        return sum;
    }

    public static Set<Integer> getSetFromField(String fieldName, String... memberNames){
        Set<Integer> out = new HashSet<>();
        getFieldAsJsonArray(memberNames).forEach(jsonEle ->
                out.add(jsonEle.getAsJsonObject().get(fieldName).getAsInt()));
        return out;
    }

    //mainly used to get max keyId
    private static int getMaxFromObjListField(String fieldName, String... memberNames){
        int max = -1;
        JsonArray jsonArray = getFieldAsJsonArray(memberNames);
        for (JsonElement jsonEle : jsonArray) {
            JsonObject jsonObj = jsonEle.getAsJsonObject();
            max = Math.max(max, jsonObj.get(fieldName).getAsInt());
        }
        return max;
    }

    // badly named function idk what else to name it
    private static int getIdFromKeyId(String returnIdFieldName, String keyIdFieldName, int targetKeyId, String... memberNames){
        JsonArray jsonArray = getFieldAsJsonArray(memberNames);
        for (JsonElement jsonEle : jsonArray) {
            JsonObject jsonObj = jsonEle.getAsJsonObject();
            if (jsonObj.get(keyIdFieldName).getAsInt() == targetKeyId) {
                return jsonObj.get(returnIdFieldName).getAsInt();
            }
        }
        return -1;
    }

    // find how much this adventurer has contributed to encyclopedia hp
    private static double getAdventurerEncyclopediaHp(JsonObject jsonAdv) {
        // base: +0.1, lv80: +0.1, lv100: +0.1
        int level = jsonAdv.get("level").getAsInt();
        if (level == 100) {
            return 0.3;
        } else if (level >= 80) {
            return 0.2;
        }
        return 0.1;
    }

    // find how much this adventurer has contributed to encyclopedia str
    private static double getAdventurerEncyclopediaStr(JsonObject jsonAdv) {
        // base: +0.1, lv80: +0.1, lv100: +0.1
        JsonArray mc = jsonAdv.get("mana_circle_piece_id_list").getAsJsonArray();
        int mcLevel = mc.size();
        if (mcLevel == 70) {
            return 0.3;
        } else if (mcLevel >= 50) {
            return 0.2;
        }
        return 0.1;
    }

    private static void addAdventurerEncyclopediaBonus(AdventurerMeta adv) {
        boolean hasManaSpiral = adv.hasManaSpiral();
        double bonus = hasManaSpiral ? 0.3 : 0.2;
        int elementID = adv.getElementId();
        if (Options.getFieldAsBoolean("maxAddedAdventurers")) {
            addAdventurerEncyclopediaBonus(elementID, bonus, bonus);
        } else { //bonus from adding new adventurer (no upgrades)
            addAdventurerEncyclopediaBonus(elementID, 0.1, 0.1);
        }
    }

    private static void addAdventurerEncyclopediaBonus(int elementId, double hpBonus, double strBonus) {
        JsonArray albumBonuses = getFieldAsJsonArray("data", "fort_bonus_list", "chara_bonus_by_album");
        for (JsonElement jsonEle : albumBonuses) {
            JsonObject albumBonus = jsonEle.getAsJsonObject();
            if (albumBonus.get("elemental_type").getAsInt() == elementId) {
                double hp = albumBonus.get("hp").getAsDouble();
                double attack = albumBonus.get("attack").getAsDouble();
                double resultHp = addDoubles(hp, hpBonus);
                double resultStr = addDoubles(attack, strBonus);
                albumBonus.remove("hp");
                albumBonus.remove("attack");
                albumBonus.addProperty("hp", resultHp);
                albumBonus.addProperty("attack", resultStr);
            }
        }
    }

    private static void addDragonEncyclopediaBonus(DragonMeta dragon) {
        boolean has5UB = dragon.has5UB();
        double hpBonus = has5UB ? 0.3 : 0.2;
        double strBonus = 0.1;
        int elementID = dragon.getElementId();
        if (Options.getFieldAsBoolean("maxAddedDragons")) {
            addDragonEncyclopediaBonus(elementID, hpBonus, strBonus);
        } else {
            addDragonEncyclopediaBonus(elementID, 0.1, 0.1);
        }
    }

    private static void addDragonEncyclopediaBonus(int elementID, double hpBonus, double strBonus) {
        JsonArray albumBonuses = getFieldAsJsonArray("data", "fort_bonus_list", "dragon_bonus_by_album");
        for (JsonElement jsonEle : albumBonuses) {
            JsonObject albumBonus = jsonEle.getAsJsonObject();
            if (albumBonus.get("elemental_type").getAsInt() == elementID) {
                double hp = albumBonus.get("hp").getAsDouble();
                double attack = albumBonus.get("attack").getAsDouble();
                double resultHp = addDoubles(hp, hpBonus);
                double resultStr = addDoubles(attack, strBonus);
                albumBonus.remove("hp");
                albumBonus.remove("attack");
                albumBonus.addProperty("hp", resultHp);
                albumBonus.addProperty("attack", resultStr);
            }
        }
    }

    private static void addWeaponBonus(WeaponMeta weapon) {
        if(!weapon.hasWeaponBonus()) {
            return;
        }
        String weaponSeries = weapon.getWeaponSeries();
        double bonus = 0.0;
        switch (weaponSeries) {
            case "Core":
            case "Void":
            case "Chimeratech":
                bonus = 0.5;
                break;
            case "High Dragon":
            case "Agito":
            case "Primal Dragon":
                bonus = 1.5;
                break;
        }
        if (bonus == 0.0) {
            return; //no bonus added
        }
        int weaponTypeId = weapon.getWeaponTypeId();
        JsonArray weaponBonuses = getFieldAsJsonArray("data", "fort_bonus_list", "param_bonus_by_weapon");
        for (JsonElement jsonEle : weaponBonuses) {
            JsonObject weaponBonus = jsonEle.getAsJsonObject();
            if (weaponBonus.get("weapon_type").getAsInt() == weaponTypeId) {
                //for weapon bonuses: hp will always be equal to str
                double value = weaponBonus.get("hp").getAsDouble();
                double resultBonus = addDoubles(value, bonus);
                weaponBonus.remove("hp");
                weaponBonus.remove("attack");
                weaponBonus.addProperty("hp", resultBonus);
                weaponBonus.addProperty("attack", resultBonus);
            }
        }
    }

    //Builders

    public static void addTalisman(String advName, int id1, int id2, int id3, int count) {
        int assignedKeyId = 0;
        for(int i = 0; i < count; i++){
            JsonObject out = new JsonObject();
            int keyIdMax = getMaxFromObjListField("talisman_key_id", "data", "talisman_list");
            assignedKeyId = keyIdMax + 200;
            String name = advName.toUpperCase();
            if(!DragaliaData.nameToAdventurer.containsKey(name)){
                System.out.println("No adventurer found for name: " + advName + "!");
                return;
            }
            String label = DragaliaData.nameToAdventurer.get(name).getTitle();
            if(!DragaliaData.kscapeLabelsMap.containsKey(label)){
                System.out.println("No ID found for label:" + label + "!");
            }
            int portraitID = DragaliaData.kscapeLabelsMap.get(label);

            out.addProperty("talisman_key_id", assignedKeyId);
            out.addProperty("talisman_id", portraitID);
            out.addProperty("is_lock", 0);
            out.addProperty("is_new", 1);
            out.addProperty("talisman_ability_id_1", id1);
            out.addProperty("talisman_ability_id_2", id2);
            out.addProperty("talisman_ability_id_3", id3);
            out.addProperty("additional_hp", 0);
            out.addProperty("additional_attack", 0);
            out.addProperty("gettime", Instant.now().getEpochSecond());

            getFieldAsJsonArray("data", "talisman_list").add(out);
        }
    }

    // return keyId of added talisman
    // no validation on labelId
    public static int addTalisman(int portraitID, int id1, int id2, int id3) {
        JsonObject out = new JsonObject();
        int keyIdMax = getMaxFromObjListField("talisman_key_id", "data", "talisman_list");
        int assignedKeyId = keyIdMax + 200;

        out.addProperty("talisman_key_id", assignedKeyId);
        out.addProperty("talisman_id", portraitID);
        out.addProperty("is_lock", 0);
        out.addProperty("is_new", 1);
        out.addProperty("talisman_ability_id_1", id1);
        out.addProperty("talisman_ability_id_2", id2);
        out.addProperty("talisman_ability_id_3", id3);
        out.addProperty("additional_hp", 0);
        out.addProperty("additional_attack", 0);
        out.addProperty("gettime", Instant.now().getEpochSecond());

        getFieldAsJsonArray("data", "talisman_list").add(out);
        return assignedKeyId;
    }

    // returns list of a key ID, sorted by the highest of a certain attr of the object
    // String keyIdName: name of the key ID field
    // String sortFieldName: name of the field to sort by
    // String conditionalField: name of the integer field to check for
    // int conditionalValue: value of the integer field ^ to check for
    // String... fields: field traverse path
    // ex: "dragon_key_id", "level", "dragon_id", 20050522, "data", "dragon_list":
    // returns key ID of dragons with dragon_id == 2005052, sorted by highest level
    public static List<Integer> getKeyIdListSortedByAttr (String keyIdName, String sortFieldName, String conditionalField,
                                                   int conditionalValue, String... fields) {
        JsonArray jsonArray = getFieldAsJsonArray(fields);
        List<int[]> valAndKeyIdPairs = new ArrayList<>();

        // initial loop, add all matching obj key IDs to output
        for (JsonElement jsonEle : jsonArray) {
            JsonObject jsonObj = jsonEle.getAsJsonObject();
            if (jsonObj.get(conditionalField).getAsInt() == conditionalValue) {
                int sortFieldValue = jsonObj.get(sortFieldName).getAsInt();
                int keyId = jsonObj.get(keyIdName).getAsInt();
                int[] valAndKeyIdPair = new int[]{sortFieldValue, keyId};
                valAndKeyIdPairs.add(valAndKeyIdPair);
            }
        }

        // 0 entries
        if (valAndKeyIdPairs.size() == 0) {
            return new ArrayList<>();
        }
        // 1 entry
        if (valAndKeyIdPairs.size() == 1) {
            return Collections.singletonList(valAndKeyIdPairs.get(0)[1]);
        }
        // sort the output
        for (int i = 0; i < valAndKeyIdPairs.size() - 1; i++) {
            int[] pair1 = valAndKeyIdPairs.get(i);
            int[] pair2 = valAndKeyIdPairs.get(i + 1);
            if (pair1[0] < pair2[0]) { // bubble sort...
                valAndKeyIdPairs.set(i, pair2);
                valAndKeyIdPairs.set(i + 1, pair1);
            }
        }
        // get key IDs
        List<Integer> out = new ArrayList<>();
        for (int[] valAndKeyIdPair : valAndKeyIdPairs) {
            out.add(valAndKeyIdPair[1]);
        }
        return out;
    }

    // Returns whether the array at 'fields' contains a 'fieldName' value
    // String fieldName: the field to check for 'value'
    // int value: the value to check for
    // String... fields: field traverse path
    // ex: "dragon_key_id", 1000, "data", "dragon_list": checks whether the dragon list contains dragon with keyID 1000
    public static boolean arrayHasValue (String fieldName, int value, String... fields) {
        JsonArray jsonArray = getFieldAsJsonArray(fields);
        for (JsonElement jsonEle : jsonArray) {
            JsonObject jsonObj = jsonEle.getAsJsonObject();
            if (jsonObj.get(fieldName).getAsInt() == value) {
                return true;
            }
        }
        return false;
    }

    // Deletes an object in 'fields' that has 'fieldName' value
    // String fieldName: the field to check for 'value'
    // int value: the value to check for
    // String... fields: field traverse path
    // ex: "chara_id", 1000, "data", "chara_list": deletes object of "chara_id" 1000
    public static JsonObject arrayDeleteValue (String fieldName, int value, String... fields) {
        JsonArray jsonArray = getFieldAsJsonArray(fields);
        for (JsonElement jsonEle : jsonArray) {
            JsonObject jsonObj = jsonEle.getAsJsonObject();
            if (jsonObj.get(fieldName).getAsInt() == value) {
                jsonArray.remove(jsonObj);
                return jsonObj;
            }
        }
        return null;
    }

    // Returns whether the array at 'fields' contains a duplicate value of 'fieldName'
    // String fieldName: the field to check for
    // String... fields: field traverse path
    // ex: "unit_story_id", "data", "unit_story_list": checks whether the story list has a duplicate story ID
    // returns -1 if none was found
    public static int arrayHasDuplicateValue (String fieldName, String... fields) {
        Set<Integer> set = new HashSet<>();
        JsonArray jsonArray = getFieldAsJsonArray(fields);
        for (JsonElement jsonEle : jsonArray) {
            JsonObject jsonObj = jsonEle.getAsJsonObject();
            int val = jsonObj.get(fieldName).getAsInt();
            if (set.contains(val)) {
                return val;
            }
            set.add(val);
        }
        return -1;
    }

    // Returns value of a field for the object in array corresponding to 'fields',
    // with a value matching the value in 'checkFieldName'
    // String valueFieldName: the field to return value for
    // String checkFieldName: the field to check 'checkFieldValue' for
    // int checkFieldValue: the value to check 'checkFieldName' for
    // String... fields: field traverse path
    // ex: "equipable_count", "ability_crest_id", 100, "data", "ability_crest_list":
    // searches for a wyrmprint of ID == 100, and returns its equipable count
    public static int getValueFromObjInArray (String valueFieldName, String checkFieldName, int checkFieldValue, String... fields) {
        JsonArray jsonArray = getFieldAsJsonArray(fields);
        for (JsonElement jsonEle : jsonArray) {
            JsonObject jsonObj = jsonEle.getAsJsonObject();
            if (jsonObj.get(checkFieldName).getAsInt() == checkFieldValue) {
                return jsonObj.get(valueFieldName).getAsInt();
            }
        }
        return -1;
    }

    // Updates value of a field for the object in array corresponding to 'fields',
    // with a value matching the value in 'checkFieldName'
    // String valueFieldName: the field to update value for
    // String checkFieldName: the field to check 'checkFieldValue' for
    // int checkFieldValue: the value to check 'checkFieldName' for
    // String... fields: field traverse path
    // ex: 30, "reliability_level", "dragon_id", 100, "data", "dragon_reliability_list":
    // searches for a dragon bond object of dragon ID == 100, and updates the bond level to 30
    public static void updateValueOfObjInArray (int updatedValue, String valueFieldName, String checkFieldName, int checkFieldValue, String... fields) {
        JsonArray jsonArray = getFieldAsJsonArray(fields);
        for (JsonElement jsonEle : jsonArray) {
            JsonObject jsonObj = jsonEle.getAsJsonObject();
            if (jsonObj.get(checkFieldName).getAsInt() == checkFieldValue) {
                jsonObj.remove(valueFieldName);
                jsonObj.addProperty(valueFieldName, updatedValue);
            }
        }
    }

    //
    // String fieldName: the field to run 'func' for
    // String... fields: field traverse path
    public static boolean arrayForEachCheck (Function<Integer, Boolean> func, String fieldName, String... fields) {
        boolean returnValue = true;
        JsonArray jsonArray = getFieldAsJsonArray(fields);
        for (JsonElement jsonEle : jsonArray) {
            JsonObject jsonObj = jsonEle.getAsJsonObject();
            int value = jsonObj.get(fieldName).getAsInt();
            if(!func.apply(value)) {
                returnValue = false;
                Logging.print("'{0}' of ID '{1}' should not be valid for '{2}'", fieldName, String.valueOf(value), fields[1]);
            }
        }
        return returnValue;
    }

    private static JsonObject buildTalisman(int portraitId, String[] combo, int keyIdOffset) {
        JsonObject out = new JsonObject();
        int abilityId1 = 0;
        int abilityId2 = 0;
        int abilityId3 = 0;
        for (int i = 0; i < combo.length; i++) {
            int id = DragaliaData.kscapeAbilityMap.get(combo[i]);
            switch (i) {
                case 0:
                    abilityId1 = id;
                    break;
                case 1:
                    abilityId2 = id;
                    break;
                case 2:
                    abilityId3 = id;
                    break;
            }
        }
        int additional_stats = Options.getFieldAsBoolean("doSuperPortraitStats") ? 250_000 : 0;

        out.addProperty("talisman_key_id", 200000 + 100 * keyIdOffset);
        out.addProperty("talisman_id", portraitId);
        out.addProperty("is_lock", 0);
        out.addProperty("is_new", 1);
        out.addProperty("talisman_ability_id_1", abilityId1);
        out.addProperty("talisman_ability_id_2", abilityId2);
        out.addProperty("talisman_ability_id_3", abilityId3);
        out.addProperty("additional_hp", additional_stats);
        out.addProperty("additional_attack", additional_stats);
        out.addProperty("gettime", Instant.now().getEpochSecond());

        return out;
    }

    private static JsonObject buildRandomTalisman(int id, int keyIdOffset) {
        JsonObject out = new JsonObject();
        int totalAbilitiesCount = DragaliaData.abilitiesList.size();

        out.addProperty("talisman_key_id", 200000 + 100 * keyIdOffset);
        out.addProperty("talisman_id", id);
        out.addProperty("is_lock", 0);
        out.addProperty("is_new", 1);
        out.addProperty("talisman_ability_id_1", DragaliaData.abilitiesList.get(rng.nextInt(totalAbilitiesCount)).getAsJsonObject().get("Id").getAsInt());
        out.addProperty("talisman_ability_id_2", DragaliaData.abilitiesList.get(rng.nextInt(totalAbilitiesCount)).getAsJsonObject().get("Id").getAsInt());
        out.addProperty("talisman_ability_id_3", DragaliaData.abilitiesList.get(rng.nextInt(totalAbilitiesCount)).getAsJsonObject().get("Id").getAsInt());
        out.addProperty("additional_hp", 0);
        out.addProperty("additional_attack", 0);
        out.addProperty("gettime", Instant.now().getEpochSecond());

        return out;
    }

    //Returns a built adventurer in savedata.txt format
    private static JsonObject buildUnit(AdventurerMeta adventurerData, int getTime) {
        JsonObject out = new JsonObject();
        if (adventurerData.getName().equals("Puppy")) {
            return null; //no dogs allowed
        }

        //to add new unit as a level 1 un-upgraded unit
        boolean minUnit = getTime == -1 && !Options.getFieldAsBoolean("maxAddedAdventurers");

        boolean hasManaSpiral = adventurerData.hasManaSpiral();
        JsonArray mc = new JsonArray();
        int mcLevel = hasManaSpiral ? 70 : 50;
        for (int i = 1; i <= mcLevel; i++) {
            mc.add(i);
        }
        if (!minUnit) { //maxed unit
            out.addProperty("chara_id", adventurerData.getId());
            out.addProperty("rarity", 5);
            out.addProperty("exp", hasManaSpiral ? 8866950 : 1191950);
            out.addProperty("level", hasManaSpiral ? 100 : 80);
            out.addProperty("additional_max_level", hasManaSpiral ? 20 : 0);
            out.addProperty("hp_plus_count", 100);
            out.addProperty("attack_plus_count", 100);
            out.addProperty("limit_break_count", adventurerData.getMaxLimitBreakCount());
            out.addProperty("is_new", 1);
            out.addProperty("gettime", getTime == -1 ? Instant.now().getEpochSecond() : getTime);
            out.addProperty("skill_1_level", hasManaSpiral ? 4 : 3);
            out.addProperty("skill_2_level", hasManaSpiral ? 3 : 2);
            out.addProperty("ability_1_level", hasManaSpiral ? 3 : 2);
            out.addProperty("ability_2_level", hasManaSpiral ? 3 : 2);
            out.addProperty("ability_3_level", adventurerData.getMaxA3Level()); //this varies per adventurer
            out.addProperty("burst_attack_level", 2);
            out.addProperty("combo_buildup_count", hasManaSpiral ? 1 : 0);
            out.addProperty("hp", adventurerData.getMaxHp());
            out.addProperty("attack", adventurerData.getMaxStr());
            out.addProperty("ex_ability_level", 5);
            out.addProperty("ex_ability_2_level", 5);
            out.addProperty("is_temporary", 0);
            out.addProperty("is_unlock_edit_skill", adventurerData.hasSkillShare() ? 1 : 0);
            out.add("mana_circle_piece_id_list", mc);
            out.addProperty("list_view_flag", 1);
        } else { //un-upgraded unit
            out.addProperty("chara_id", adventurerData.getId());
            out.addProperty("rarity", adventurerData.getBaseRarity());
            out.addProperty("exp", 0);
            out.addProperty("level", 1);
            out.addProperty("additional_max_level", 0);
            out.addProperty("hp_plus_count", 0);
            out.addProperty("attack_plus_count", 0);
            out.addProperty("limit_break_count", 0); //confirm?
            out.addProperty("is_new", 1);
            out.addProperty("gettime", Instant.now().getEpochSecond());
            out.addProperty("skill_1_level", 1); //confirm?
            out.addProperty("skill_2_level", 0);
            out.addProperty("ability_1_level", adventurerData.getMinA1Level());
            out.addProperty("ability_2_level", 0);
            out.addProperty("ability_3_level", 0);
            out.addProperty("burst_attack_level", adventurerData.getMinFsLevel());
            out.addProperty("combo_buildup_count", 0);
            out.addProperty("hp", adventurerData.getMinHp()); //get min
            out.addProperty("attack", adventurerData.getMinStr()); //get min
            out.addProperty("ex_ability_level", 1);
            out.addProperty("ex_ability_2_level", 1);
            out.addProperty("is_temporary", 0);
            out.addProperty("is_unlock_edit_skill", 0);
            out.add("mana_circle_piece_id_list", new JsonArray());
            out.addProperty("list_view_flag", 1);
        }
        return out;
    }

    //Returns a built dragon in savedata.txt format
    private static JsonObject buildDragon(DragonMeta dragonData, int keyIdMin, int keyIdOffset) {
        JsonObject out = new JsonObject();
        boolean has5UB = dragonData.has5UB();
        int xp = dragonData.getMaxXp();
        int level = dragonData.getMaxLevel();

        int a1Level = dragonData.getA1Max();
        int a2Level = dragonData.getA2Max();

        boolean minDragon = !Options.getFieldAsBoolean("maxAddedDragons");
        if (!minDragon) {
            out.addProperty("dragon_key_id", keyIdMin + 200 * keyIdOffset);
            out.addProperty("dragon_id", dragonData.getId());
            out.addProperty("level", level);
            out.addProperty("hp_plus_count", 50);
            out.addProperty("attack_plus_count", 50);
            out.addProperty("exp", xp);
            out.addProperty("is_lock", 0);
            out.addProperty("is_new", 1);
            out.addProperty("get_time", Instant.now().getEpochSecond());
            out.addProperty("skill_1_level", 2);
            out.addProperty("ability_1_level", a1Level);
            out.addProperty("ability_2_level", a2Level);
            out.addProperty("limit_break_count", has5UB ? 5 : 4);
        } else {
            out.addProperty("dragon_key_id", keyIdMin + 200 * keyIdOffset);
            out.addProperty("dragon_id", dragonData.getId());
            out.addProperty("level", 1);
            out.addProperty("hp_plus_count", 0);
            out.addProperty("attack_plus_count", 0);
            out.addProperty("exp", 0);
            out.addProperty("is_lock", 0);
            out.addProperty("is_new", 1);
            out.addProperty("get_time", Instant.now().getEpochSecond());
            out.addProperty("skill_1_level", 1);
            out.addProperty("ability_1_level", 1);
            out.addProperty("ability_2_level", dragonData.hasA2() ? 1 : 0);
            out.addProperty("limit_break_count", 0);
        }
        return out;
    }

    //Returns a built dragon in savedata.txt format
    //Takes in getTime and keyId info and returns maxed out dragon
    //Used to upgrade currently owned dragons
    private static JsonObject buildDragonFromExisting(DragonMeta dragonData, int keyId, int getTime, boolean isLocked) {
        JsonObject out = new JsonObject();
        boolean has5UB = dragonData.has5UB();
        int xp = dragonData.getMaxXp();
        int level = dragonData.getMaxLevel();

        int a1Level = dragonData.getA1Max();
        int a2Level = dragonData.getA2Max();

        out.addProperty("dragon_key_id", keyId);
        out.addProperty("dragon_id", dragonData.getId());
        out.addProperty("level", level);
        out.addProperty("hp_plus_count", 50);
        out.addProperty("attack_plus_count", 50);
        out.addProperty("exp", xp);
        out.addProperty("is_lock", isLocked ? 1 : 0);
        out.addProperty("is_new", 1);
        out.addProperty("get_time", getTime);
        out.addProperty("skill_1_level", 2);
        out.addProperty("ability_1_level", a1Level);
        out.addProperty("ability_2_level", a2Level);
        out.addProperty("limit_break_count", has5UB ? 5 : 4);
        return out;
    }

    private static JsonObject buildDragonAlbumData(DragonMeta dragonData) {
        JsonObject out = new JsonObject();
        boolean has5UB = dragonData.has5UB();
        int level = dragonData.getMaxLevel();

        out.addProperty("dragon_id", dragonData.getId());
        out.addProperty("max_level", level);
        out.addProperty("max_limit_break_count", has5UB ? 5 : 4);
        return out;
    }

    //build facility from existing facility
    private static JsonObject buildFacility(FacilityMeta fac, int keyId, int x, int y){
        JsonObject out = new JsonObject();

        boolean isResourceFacility = fac.isResourceFacility();
        int level = fac.getMaxLevel();
        int id = fac.getId();
        int detailId = Integer.parseInt(fac.getDetailId());

        out.addProperty("build_id", keyId);  //key ID
        out.addProperty("fort_plant_detail_id", detailId); //ID + level
        out.addProperty("position_x", x);
        out.addProperty("position_z", y);
        out.addProperty("build_status", 0);
        out.addProperty("build_start_date", 0);
        out.addProperty("build_end_date", 0);
        out.addProperty("level", level);
        out.addProperty("plant_id", id);  //id
        out.addProperty("is_new", 0);
        out.addProperty("remain_time", 0);
        out.addProperty("last_income_date", isResourceFacility ? (Instant.now().getEpochSecond()) : -1);  //resource facility
        if(isResourceFacility){
            out.addProperty("last_income_time", 200000);  //resource facility
        }
        return out;
    }

    //build a new facility
    private static JsonObject buildFacility(FacilityMeta fac, int keyIdMin, int keyIdOffset){
        JsonObject out = new JsonObject();

        boolean isResourceFacility = fac.isResourceFacility();
        int level = fac.getMaxLevel();
        int id = fac.getId();
        int detailId = Integer.parseInt(fac.getDetailId());

        out.addProperty("build_id", keyIdMin + 200 * keyIdOffset);  //key ID
        out.addProperty("fort_plant_detail_id", detailId); //ID + level
        out.addProperty("position_x", -1); //xz = -1 for facilities in inventory
        out.addProperty("position_z", -1);
        out.addProperty("build_status", 0);
        out.addProperty("build_start_date", 0);
        out.addProperty("build_end_date", 0);
        out.addProperty("level", level);
        out.addProperty("plant_id", id);  //id
        out.addProperty("is_new", 0);
        out.addProperty("remain_time", 0);
        out.addProperty("last_income_date", isResourceFacility ? (Instant.now().getEpochSecond()) : -1);  //resource facility
        if(isResourceFacility){
            out.addProperty("last_income_time", 200000);  //resource facility
        }
        return out;
    }

    private static JsonObject buildWeapon(WeaponMeta weaponData, int getTime) {
        JsonObject out = new JsonObject();
        String weaponSeries = weaponData.getWeaponSeries();
        int rarity = weaponData.getRarity();
        if (rarity == 1) {
            return null; //unused weapons
        }

        boolean isNullElement = weaponData.getElementId() == 99;

        int level = 1;
        int unbinds = 0;
        int refines = 0;
        int fiveStarSlotCount = 0;
        int sindomSlotCount = 0;

        boolean isMegaManWeapon = weaponData.getName().contains("Mega") && weaponData.getElementId() == 99;
        boolean isLuckyPaddle = weaponData.getId() == 30159904;
        // can't make copies of Mega Man collab weapons apparently...
        // and the lucky paddle too apparently
        int copiesCount = (isMegaManWeapon || isLuckyPaddle) ? 1 : 4;
        switch (weaponSeries) {
            case "Core":
                switch (rarity) {
                    case 3:
                        level = 20;
                        break;
                    case 4:
                        level = 30;
                        break;
                    case 5:
                        level = 50;
                        break;
                }
                unbinds = 4;
                if (!isNullElement) {
                    fiveStarSlotCount = 1;
                }
                break;
            case "Void":
            case "Chimeratech":
            case "High Dragon":
                level = 70;
                unbinds = 8;
                refines = 1;
                fiveStarSlotCount = 1;
                break;
            case "Agito":
                level = 90;
                unbinds = 9;
                refines = 2;
                fiveStarSlotCount = 1;
                sindomSlotCount = 2;
                break;
            case "Primal Dragon":
                level = 80;
                unbinds = 8;
                refines = 1;
                fiveStarSlotCount = 1;
                sindomSlotCount = 2;
                break;
            case "Other":
                //hard coded /shrug
                if (weaponData.getName().contains("Mega")) {
                    level = 50;
                    unbinds = 4;
                }
                switch (weaponData.getName()) {
                    case "Soldier's Brand":
                        level = 10;
                        unbinds = 4;
                        break;
                    case "Lucky Hanetsuki Paddle":
                        level = 50;
                        unbinds = 4;
                        break;
                }
                break;
        }

        int passiveAbilityCount = weaponData.getPassiveAbilityIdList().size();
        //too lazy to figure out mapping for these abilities + no one cares honestly
        JsonArray voidWeaponAbilities = new JsonArray();

        JsonArray emptyVoidWeaponAbilities = new JsonArray();
        for (int i = 0; i < 15; i++) {
            voidWeaponAbilities.add(i < passiveAbilityCount ? 1 : 0);
            emptyVoidWeaponAbilities.add(0);
        }

        boolean minWeapon = getTime == -1 && !Options.getFieldAsBoolean("maxAddedWeapons");

        if (!minWeapon) {
            out.addProperty("weapon_body_id", weaponData.getId());                      //ID
            out.addProperty("buildup_count", level);                                    //level
            out.addProperty("limit_break_count", unbinds);                              //unbinds
            out.addProperty("limit_over_count", refines);                               //refines
            out.addProperty("equipable_count", copiesCount);                            //equip count
            out.addProperty("additional_crest_slot_type_1_count", fiveStarSlotCount);   //5* slot count
            out.addProperty("additional_crest_slot_type_2_count", 0);
            out.addProperty("additional_crest_slot_type_3_count", sindomSlotCount);     //sindom slot count
            out.addProperty("additional_effect_count", 0);                         //?
            out.add("unlock_weapon_passive_ability_no_list", voidWeaponAbilities);      //void weapon abilities?
            out.addProperty("fort_passive_chara_weapon_buildup_count", weaponData.hasWeaponBonus() ? 1 : 0);        //weapon bonus
            out.addProperty("is_new", 1);
            out.addProperty("gettime", getTime == -1 ? Instant.now().getEpochSecond() : getTime);
        } else {
            out.addProperty("weapon_body_id", weaponData.getId());                      //ID
            out.addProperty("buildup_count", 0);                                    //level
            out.addProperty("limit_break_count", 0);                              //unbinds
            out.addProperty("limit_over_count", 0);                               //refines
            out.addProperty("equipable_count", 1);                            //equip count
            out.addProperty("additional_crest_slot_type_1_count", 0);   //5* slot count
            out.addProperty("additional_crest_slot_type_2_count", 0);
            out.addProperty("additional_crest_slot_type_3_count", 0);     //sindom slot count
            out.addProperty("additional_effect_count", 0);                         //?
            out.add("unlock_weapon_passive_ability_no_list", emptyVoidWeaponAbilities);      //void weapon abilities?
            out.addProperty("fort_passive_chara_weapon_buildup_count", 0);        //weapon bonus
            out.addProperty("is_new", 1);
            out.addProperty("gettime", Instant.now().getEpochSecond());
        }
        return out;
    }

    // getTime == -1 --> new wyrmprint
    // getTime != -1 --> build wyrmprint from existing
    private static JsonObject buildWyrmprint(WyrmprintMeta printData, int getTime, boolean isFavorite) {
        JsonObject out = new JsonObject();
        int rarity = printData.getRarity();
        int level = 1;
        int augmentCount = 0;

        //to add new print as a level 1 un-upgraded print
        boolean minPrint = getTime == -1 && !Options.getFieldAsBoolean("maxAddedWyrmprints");

        switch (rarity) {
            case 2:
                level = 10;
                augmentCount = 50;
                break;
            case 3:
                level = 20;
                augmentCount = 50;
                break;
            case 4:
                level = 40;
                augmentCount = 50;
                break;
            case 5:
                level = 50;
                augmentCount = 50;
                break;
            case 9: //sindom
                level = 30;
                augmentCount = 40;
                break;
        }
        if (!minPrint) {
            out.addProperty("ability_crest_id", printData.getId());
            out.addProperty("buildup_count", level);
            out.addProperty("limit_break_count", 4);
            out.addProperty("equipable_count", 4);
            out.addProperty("hp_plus_count", augmentCount);
            out.addProperty("attack_plus_count", augmentCount);
            out.addProperty("is_new", 1);
            out.addProperty("is_favorite", isFavorite ? 1 : 0);
            out.addProperty("gettime", getTime == -1 ? Instant.now().getEpochSecond() : getTime);
        } else {
            out.addProperty("ability_crest_id", printData.getId());
            out.addProperty("buildup_count", 0);
            out.addProperty("limit_break_count", 0);
            out.addProperty("equipable_count", 1);
            out.addProperty("hp_plus_count", 0);
            out.addProperty("attack_plus_count", 0);
            out.addProperty("is_new", 1);
            out.addProperty("is_favorite", isFavorite ? 1 : 0);
            out.addProperty("gettime", Instant.now().getEpochSecond());
        }
        return out;
    }

    private static Set<Integer> getOwnedStories() {
        //Compile list of adventurer stories in savedata
        Set<Integer> ownedStories = new HashSet<>();
        getFieldAsJsonArray("data", "unit_story_list").forEach(jsonEle ->
                ownedStories.add(jsonEle.getAsJsonObject().get("unit_story_id").getAsInt()));
        return ownedStories;
    }

    private static void addStory(int id) {
        JsonObject story = new JsonObject();
        story.addProperty("unit_story_id", id);
        story.addProperty("is_read", 0);
        getFieldAsJsonArray("data", "unit_story_list").add(story);
    }

    private static void unlockAdventurerStory(int id, boolean toUnlockFirstStoryOnly) {
        if (id == 10750102 || id == 10140101) {
            return; //Mega Man, Euden have no stories to tell
        }
        //Compile list of adventurer stories in savedata
        Set<Integer> ownedStories = getOwnedStories();

        List<Integer> storyIDs = DragaliaData.adventurerStoryMap.get(id);
        int storyCount = (toUnlockFirstStoryOnly) ? (1) : (5);
        for(int i = 0; i < storyCount; i ++){
            int storyID = storyIDs.get(i);
            if(ownedStories.contains(storyID)){
                continue; //dont add story if u already have it
            }
            addStory(storyID);
        }
    }

    public static void validate() {
        System.out.println("Validating save file...");
        validateIDs();
    }

    private static void validateIDs() {
        boolean validAdventurers = arrayForEachCheck(id ->
                        DragaliaData.idToAdventurer.containsKey(id) ||
                        DragaliaData.unplayableAdventurerIds.contains(id),
                "chara_id", "data", "chara_list");
        boolean validDragons = arrayForEachCheck(id ->
                        DragaliaData.idToDragon.containsKey(id) ||
                        DragaliaData.unplayableDragonIds.contains(id),
                "dragon_id", "data", "dragon_list");
        boolean validWyrmprints = arrayForEachCheck(id -> DragaliaData.idToPrint.containsKey(id),
                "ability_crest_id", "data", "ability_crest_list");
        boolean validFacilities = arrayForEachCheck(id -> DragaliaData.idToFacility.containsKey(id),
                "plant_id", "data", "build_list");
        boolean validWeapons = arrayForEachCheck(id -> DragaliaData.idToWeapon.containsKey(id),
                "weapon_body_id", "data", "weapon_body_list");

        boolean valid = validAdventurers && validDragons && validWyrmprints
                && validFacilities && validWeapons;
        if (!valid) {
            System.out.println("Detected 1 or more issues with the save.");
        }
    }

    public static void applyFixes() {
        fixMissingDragonStories();
        fixMissingFirstAdventurerStories();
    }

    private static void fixMissingDragonStories() {
        List<String> addedDragonStories = new ArrayList<>();
        //Compile a list of ID's from your encyclopedia
        Set<Integer> albumIDSet = getSetFromField("dragon_id", "data", "album_dragon_list");
        for (Integer dragonId : albumIDSet) {
            DragonMeta dragon = DragaliaData.idToDragon.get(dragonId);
            int id = dragon.getId();
            int bondLevel = getValueFromObjInArray("reliability_level", "dragon_id",
                    id, "data", "dragon_reliability_list");
            if (bondLevel >= 5) {
                if(addDragonStory(dragon.getDragonStoryId(1))) {
                    addedDragonStories.add(dragon.getName() +  " Part 1");
                }
            }
            if (bondLevel >= 15) {
                if(addDragonStory(dragon.getDragonStoryId(2))) {
                    addedDragonStories.add(dragon.getName() +  " Part 2");
                }
            }
        }
        if (!addedDragonStories.isEmpty()) {
            Logging.print("Added {0} dragon stories that should have been in the savefile.", addedDragonStories.size());
            Logging.write("Added missing dragon stories", addedDragonStories);
        }
    }

    private static void fixMissingFirstAdventurerStories() {

        JsonArray ownedAdventurers = getFieldAsJsonArray("data", "chara_list");
        Set<Integer> ownedStories = getOwnedStories();

        for (JsonElement jsonEle : ownedAdventurers) {
            JsonObject adventurer = jsonEle.getAsJsonObject();
            int adventurerId = adventurer.get("chara_id").getAsInt();
            if (DragaliaData.adventurerStoryMap.containsKey(adventurerId)) {
                int firstStoryId = DragaliaData.adventurerStoryMap.get(adventurerId).get(0);
                if (!ownedStories.contains(firstStoryId)) {
                    addStory(firstStoryId);
                    String adventurerName = DragaliaData.idToAdventurer.get(adventurerId).getName();
                    Logging.print("Added missing first story of ID '{0}' for adventurer '{1}'",
                            Integer.toString(adventurerId), adventurerName);
                }
            }
        }
    }

    public static void setEpithet (String name) {
        int id = DragaliaData.nameToEpithetId.get(name);
        writeInteger(id, "data", "user_data", "emblem_id");
    }

    // Hacked Utils \\

    private static void addHackedUnit(int id){
        Set<Integer> ownedIdSet = getSetFromField("chara_id", "data", "chara_list");
        if(ownedIdSet.contains(id)){
            return; //dont add if u already have it
        }
        getField("data", "chara_list").getAsJsonArray().add(buildHackedUnit(id));
    }

    //Returns a built adventurer in savedata.txt format
    private static JsonObject buildHackedUnit(int id) {
        //use Euden stats as default
        int hp = 716, str = 480, hasSS = 1, s1Level = 1;

        switch(id){
            case 19900001: //Zethia
                hp = 830;
                str = 466;
                s1Level = 3;
                break;
            case 19900002: //Leif (Light)
            case 19900005: //Leif (Wind)
                hp = 835;
                str = 456;
                break;
        }

        JsonObject out = new JsonObject();
        JsonArray mc = new JsonArray();
        for (int i = 1; i <= 40; i++) {
            mc.add(i);
        }
        out.addProperty("chara_id", id);
        out.addProperty("rarity", 5);
        out.addProperty("exp", 1191950);
        out.addProperty("level", 80);
        out.addProperty("additional_max_level", 0);
        out.addProperty("hp_plus_count", 100);
        out.addProperty("attack_plus_count", 100);
        out.addProperty("limit_break_count", 5);
        out.addProperty("is_new", 1);
        out.addProperty("gettime", Instant.now().getEpochSecond());
        out.addProperty("skill_1_level", s1Level);
        out.addProperty("skill_2_level", 1);
        out.addProperty("ability_1_level", 1);
        out.addProperty("ability_2_level", 1);
        out.addProperty("ability_3_level", 1);
        out.addProperty("burst_attack_level", 2);
        out.addProperty("combo_buildup_count", 0);
        out.addProperty("hp", hp);
        out.addProperty("attack", str);
        out.addProperty("ex_ability_level", 5);
        out.addProperty("ex_ability_2_level", 5);
        out.addProperty("is_temporary", 0);
        out.addProperty("is_unlock_edit_skill", hasSS);
        out.add("mana_circle_piece_id_list", mc);
        out.addProperty("list_view_flag", 1);
        return out;
    }

    private static void addHackedDragon(int id) {
        // add dragon to dragon inventory
        getField("data", "dragon_list").getAsJsonArray().add(buildHackedDragon(id));
        // add bond obj
        if (arrayHasValue("dragon_id", id, "data", "dragon_reliability_list")) {
            return; // dont add anything if bond obj already exists
        }
        getFieldAsJsonArray("data", "dragon_reliability_list").add(buildDragonBondObj(id));
    }

    private static JsonObject buildHackedDragon(int id) {
        JsonObject out = new JsonObject();
        int keyIdMax = getMaxFromObjListField("dragon_key_id", "data", "dragon_list");

        out.addProperty("dragon_key_id", keyIdMax + 10);
        out.addProperty("dragon_id", id);
        out.addProperty("level", 100);
        out.addProperty("hp_plus_count", 50);
        out.addProperty("attack_plus_count", 50);
        out.addProperty("exp", 1240020);
        out.addProperty("is_lock", 0);
        out.addProperty("is_new", 1);
        out.addProperty("get_time", Instant.now().getEpochSecond());
        out.addProperty("skill_1_level", 2);
        out.addProperty("ability_1_level", 1);
        out.addProperty("ability_2_level", 1);
        out.addProperty("limit_break_count", 4);
        return out;
    }

    public static DragonMeta getDragonFromKeyId (int keyId) {
        int id = getIdFromKeyId("dragon_id", "dragon_key_id", keyId,
                "data", "dragon_list");
        if (id == -1) {
            return null;
        } else {
            return DragaliaData.idToDragon.get(id);
        }
    }

    /// Methods \\\
    public static void uncapMana() {
        writeInteger(10_000_000, "data", "user_data", "mana_point");
    }
    public static void setRupies() {
        writeLong(2_000_000_000, "data", "user_data", "coin");
    }

    public static void plunderDonkay() {
        writeInteger(710_000, "data", "user_data", "crystal");

        int keyIdMax = 0;
        JsonArray ticketsList = getFieldAsJsonArray("data", "summon_ticket_list");
        boolean foundSingles = false;
        boolean foundTenfolds = false;
        for (JsonElement jsonEle : ticketsList){
            JsonObject ticketCount = jsonEle.getAsJsonObject();
            int id = ticketCount.get("summon_ticket_id").getAsInt();
            int keyId = ticketCount.get("key_id").getAsInt();
            int quantity = ticketCount.get("quantity").getAsInt();

            if(id == 10101){
                foundSingles = true;
                if(quantity < 2600){
                    ticketCount.remove("quantity");
                    ticketCount.addProperty("quantity", 2600);
                }
            } else if(id == 10102){
                foundTenfolds = true;
                if(quantity < 170){
                    ticketCount.remove("quantity");
                    ticketCount.addProperty("quantity", 170);
                }
            }
            keyIdMax = Math.max(keyIdMax, keyId);
        }
        //eh
        if(!foundSingles){
            JsonObject newTicketCount = new JsonObject();
            newTicketCount.addProperty("key_id", keyIdMax + 200);
            newTicketCount.addProperty("summon_ticket_id", 10101);
            newTicketCount.addProperty("quantity", 2600);
            newTicketCount.addProperty("use_limit_time", 0);
            ticketsList.add(newTicketCount);
        }
        if(!foundTenfolds){
            JsonObject newTicketCount = new JsonObject();
            newTicketCount.addProperty("key_id", keyIdMax + 400);
            newTicketCount.addProperty("summon_ticket_id", 10102);
            newTicketCount.addProperty("quantity", 170);
            newTicketCount.addProperty("use_limit_time", 0);
            ticketsList.add(newTicketCount);
        }
    }

    public static void battleOnTheByroad() {
        writeInteger(10_000_000, "data", "user_data", "dew_point");
    }

    public static int addMissingAdventurers() {
        int count = 0;

        //Compile a list of ID's you have
        Set<Integer> ownedIdSet = getSetFromField("chara_id", "data", "chara_list");

        //Go through a list of all the adventurers in the game
        for(Map.Entry<Integer, AdventurerMeta> entry : DragaliaData.idToAdventurer.entrySet()){
            int id = entry.getKey();
            AdventurerMeta adventurer = entry.getValue();
            if (!ownedIdSet.contains(id)) { //If you don't own this adventurer
                //Construct new unit (Does this unit have a mana spiral?)
                JsonObject newUnit = buildUnit(adventurer, -1);
                //Add it to your roster
                if (newUnit != null) {
                    getField("data", "chara_list").getAsJsonArray().add(newUnit);
                    unlockAdventurerStory(id, !Options.getFieldAsBoolean("maxAddedAdventurers"));
                    addAdventurerEncyclopediaBonus(adventurer);
                    count++;
                    Logging.write(adventurer.getName());
                }
            }
        }
        Logging.flushLog("Added adventurers");
        return count;
    }

    public static void minifyAdventurer(String advName) {
        AdventurerMeta adv = DragaliaData.nameToAdventurer.get(advName);
        int id = adv.getId();

        JsonObject deletedAdventurer = arrayDeleteValue("chara_id", id, "data", "chara_list");
        if (deletedAdventurer != null) {
            // ^ adventurer is deleted

            // delete stories
            if (DragaliaData.adventurerStoryMap.containsKey(id)) {
                List<Integer> stories = DragaliaData.adventurerStoryMap.get(id);
                for (int i = 1; i < stories.size(); i++) {
                    int storyId = stories.get(i);
                    if (arrayDeleteValue("unit_story_id", storyId, "data", "unit_story_list") != null) {
                        Logging.print("Deleted story of ID '{0}'", storyId);
                    }
                }
            }

            // modify encyclopedia bonuses
            double encycloHpMinus = -1 * getAdventurerEncyclopediaHp(deletedAdventurer);
            double encycloStrMinus = -1 * getAdventurerEncyclopediaStr(deletedAdventurer);
            int elementId = adv.getElementId();
            addAdventurerEncyclopediaBonus(elementId, encycloHpMinus, encycloStrMinus);
            System.out.println("Removed HP: " + encycloHpMinus + ", STR: " + encycloStrMinus +
                    " from encyclopedia bonuses");

            // re-add adventurer
            // shouldn't really need to temp edit the options value lol
            // should fix that
            String optionsToMaxAddedAdventurers = Options.getFieldAsString("maxAddedAdventurers");
            Options.editOption("maxAddedAdventurers", "false");
            addAdventurer(advName);
            Options.editOption("maxAddedAdventurers", optionsToMaxAddedAdventurers);

            System.out.println("Minified " + advName + "!");
        } else {
            System.out.println("Adventurer does not exist in 'chara_list'");
        }

    }

    public static void addAdventurer(String advName) {
        AdventurerMeta advData = DragaliaData.nameToAdventurer.get(advName);
        if (advData == null) {
            System.out.println("Can't find adventurer with name '" + advName + "'. Try again!");
            return;
        }
        //Compile a list of ID's you have
        Set<Integer> ownedIdSet = getSetFromField("chara_id", "data", "chara_list");

        int id = advData.getId();
        String name = advData.getName();
        if (ownedIdSet.contains(id)) {
            System.out.println("You already own '" + name + "'!");
            return;
        }
        //Construct new unit (Does this unit have a mana spiral?)
        JsonObject newUnit = buildUnit(advData, -1);
        //Add it to your roster
        if (newUnit != null) {
            getField("data", "chara_list").getAsJsonArray().add(newUnit);
            unlockAdventurerStory(id, !Options.getFieldAsBoolean("maxAddedAdventurers"));
            addAdventurerEncyclopediaBonus(advData);
            System.out.println("Added '" + name + "'!");
        }
    }

    public static int addMissingWyrmprints() {
        int count = 0;
        //Compile a list of ID's you have
        Set<Integer> ownedIdSet = getSetFromField("ability_crest_id", "data", "ability_crest_list");

        //Go through a list of all the wyrmprints in the game
        for (Map.Entry<Integer, WyrmprintMeta> entry : DragaliaData.idToPrint.entrySet()) {
            WyrmprintMeta wyrmprint = entry.getValue();
            int id = entry.getKey();
            if (!ownedIdSet.contains(id)) { //If you don't own this print
                //Construct new print
                JsonObject newPrint = buildWyrmprint(wyrmprint, -1, false);
                //Add it to your inventory
                getField("data", "ability_crest_list").getAsJsonArray().add(newPrint);
                count++;
                Logging.write(wyrmprint.getName() + "(" + wyrmprint.getRarity() + "*)");
            }
        }
        Logging.flushLog("Added wyrmprints");
        return count;
    }

    //return response message
    public static String addMissingDragons(boolean toExcludeLowRarityDragons) {
        int count = 0;
        int expandAmount = 0;
        int keyIdMax = getMaxFromObjListField("dragon_key_id", "data", "dragon_list");

        //Compile a list of ID's you have
        Set<Integer> ownedIdSet = getSetFromField("dragon_id", "data", "dragon_list");
        //Compile a list of ID's from your encyclopedia
        Set<Integer> albumIDSet = getSetFromField("dragon_id", "data", "album_dragon_list");

        //Go through a list of all the dragons in the game
        for (Map.Entry<Integer, DragonMeta> entry : DragaliaData.idToDragon.entrySet()) {
            DragonMeta dragon = entry.getValue();
            int id = dragon.getId();
            int rarity = dragon.getRarity();
            if (toExcludeLowRarityDragons && (rarity == 3 || rarity == 4)) {
                continue; //maybe ignore low rarity dragons
            }
            if (!ownedIdSet.contains(id)) { //If you don't own this dragon
                //Construct new dragon (Does this dragon have 5UB?)
                JsonObject newDragon = buildDragon(dragon, keyIdMax, count + 1);
                //Add it to your roster
                int dragonListSize = getFieldAsJsonArray("data", "dragon_list").size();
                int dragonListCapacity = getFieldAsInt("data", "user_data", "max_dragon_quantity");
                if (dragonListSize == dragonListCapacity) {           //if dragon roster is full...
                    if (dragonListCapacity == MAX_DRAGON_CAPACITY) {  //if dragon capacity is maxed... can't do anything
                        return "Dragon roster capacity is maxed! Unable to add new dragons...";
                    } else {                                        //expand dragon capacity if able to
                        writeInteger(dragonListCapacity + 5, "data", "user_data", "max_dragon_quantity");
                        expandAmount += 5;
                    }
                }
                getFieldAsJsonArray("data", "dragon_list").add(newDragon);

                //If you've never owned this dragon before
                if (!albumIDSet.contains(id)) {
                    //Add to encyclopedia
                    getFieldAsJsonArray("data", "album_dragon_list").add(buildDragonAlbumData(dragon));
                    addDragonEncyclopediaBonus(dragon);
                    //Add dragon bond obj
                    if (id != 20050522) { //Arsene check
                        if (Options.getFieldAsBoolean("maxDragonBonds")) {
                            // add dragon's roost materials
                            HashMap<Integer, Integer> dragonsRoostGifts = DragonMeta.getDragonsRoostGifts(1);
                            addMaterialsFromMap(dragonsRoostGifts);
                            // add dragon stories
                            addDragonStory(dragon.getDragonStoryId(1));
                            addDragonStory(dragon.getDragonStoryId(2));
                        }
                        getField("data", "dragon_reliability_list").getAsJsonArray().add(buildDragonBondObj(id));
                    }
                } else { // if you've owned this dragon before, then update dragon bonds
                    // Update dragon bond obj
                    updateDragonBond(id);
                }
                count++;
                Logging.write(dragon.getName() + "(" + dragon.getRarity() + "*)");
            }
        }
        Logging.flushLog("Added dragons");
        return expandAmount == 0 ?
                "Added " + count + " missing dragons." :
                "Added " + count + " missing dragons. Dragon inventory capacity was raised by " + expandAmount + ".";
    }

    public static void updateDragonBond(int id) {
        // Update dragon bond obj
        if (Options.getFieldAsBoolean("maxDragonBonds")) {
            int bondLevel = getValueFromObjInArray("reliability_level", "dragon_id",
                    id, "data", "dragon_reliability_list");
            // add dragon's roost materials
            HashMap<Integer, Integer> dragonsRoostGifts = DragonMeta.getDragonsRoostGifts(bondLevel);
            addMaterialsFromMap(dragonsRoostGifts);
            // set bond level to 30
            updateValueOfObjInArray(30, "reliability_level", "dragon_id",
                    id, "data", "dragon_reliability_list");
        }
    }

    public static JsonObject buildDragonBondObj(int id) {
        JsonObject out = new JsonObject();
        out.addProperty("dragon_id", id);
        out.addProperty("gettime", Instant.now().getEpochSecond());
        out.addProperty("last_contact_time", Instant.now().getEpochSecond());
        if (Options.getFieldAsBoolean("maxDragonBonds")) {
            out.addProperty("reliability_level", 30);
            out.addProperty("reliability_total_exp", 36300);
        } else {
            out.addProperty("reliability_level", 1);
            out.addProperty("reliability_total_exp", 0);
        }
        return out;
    }

    // returns true if a dragon story was added
    public static boolean addDragonStory(int id) {
        if (arrayHasValue("unit_story_id", id, "data", "unit_story_list")) {
            return false; // dont add anything if story already exists
        }
        JsonObject dragonStory = new JsonObject();
        dragonStory.addProperty("unit_story_id", id);
        dragonStory.addProperty("is_read", 0);
        getFieldAsJsonArray("data", "unit_story_list").add(dragonStory);
        return true;
    }

    public static void addDragon(String drgName) {
        int expandAmount = 0;
        int keyIdMax = getMaxFromObjListField("dragon_key_id", "data", "dragon_list");

        //Compile a list of ID's from your encyclopedia
        Set<Integer> albumIDSet = getSetFromField("dragon_id", "data", "album_dragon_list");

        DragonMeta drgData = DragaliaData.nameToDragon.get(drgName);
        if (drgData == null) {
            System.out.println("Can't find dragon with name '" + drgName + "'. Try again!");
            return;
        }
        int id = drgData.getId();
        String name = drgData.getName();

        //Construct new dragon (Does this dragon have 5UB?)
        JsonObject newDragon = buildDragon(drgData, keyIdMax, 1);
        //Add it to your roster
        int dragonListSize = getFieldAsJsonArray("data", "dragon_list").size();
        int dragonListCapacity = getFieldAsInt("data", "user_data", "max_dragon_quantity");
        if (dragonListSize == dragonListCapacity) {           //if dragon roster is full...
            if (dragonListCapacity == MAX_DRAGON_CAPACITY) {  //if dragon capacity is maxed... can't do anything
                System.out.println("Dragon roster capacity is maxed! Unable to add new dragons...");
                return;
            } else {                                        //expand dragon capacity if able to
                writeInteger(dragonListCapacity + 5, "data", "user_data", "max_dragon_quantity");
                expandAmount += 5;
            }
        }
        getFieldAsJsonArray("data", "dragon_list").add(newDragon);

        //If you've never owned this dragon before
        if (!albumIDSet.contains(id)) {
            //Add to encyclopedia
            getFieldAsJsonArray("data", "album_dragon_list").add(buildDragonAlbumData(drgData));
            addDragonEncyclopediaBonus(drgData);
            //Add dragon bond obj
            if (id != 20050522) { //Arsene check
                getField("data", "dragon_reliability_list").getAsJsonArray().add(buildDragonBondObj(id));
            }
        }

        String out = expandAmount == 0 ?
                "Added '" + name + "'!" :
                "Added '" + name + "'! Dragon inventory capacity was raised by " + expandAmount + ".";
        System.out.println(out);
    }

    public static void addMaterialsFromMap(HashMap<Integer, Integer> matIdToCount) {
        for (Map.Entry<Integer, Integer> entry : matIdToCount.entrySet()) {
            int id = entry.getKey();
            int count = entry.getValue();
            addMaterial(id, count);
        }
    }

    public static void addMaterial(int id, int addCount) {
        JsonArray items = getFieldAsJsonArray("data", "material_list");
        for (JsonElement jsonEle : items) {
            JsonObject jsonObj = jsonEle.getAsJsonObject();
            int matId = jsonObj.get("material_id").getAsInt();
            if (matId == id) {
                int matCount = jsonObj.get("quantity").getAsInt();
                jsonObj.remove("quantity");
                jsonObj.addProperty("quantity", matCount + addCount);
                return;
            }
        }
        // couldnt find item in material list, make a new obj for it
        JsonObject newItem = new JsonObject();
        newItem.addProperty("material_id", id);
        newItem.addProperty("quantity", addCount);
        items.add(newItem);
    }

    public static void addMaterials() {
        JsonArray items = getFieldAsJsonArray("data", "material_list");
        for (JsonElement jsonEle : items) {
            JsonObject jsonObj = jsonEle.getAsJsonObject();
            int count = jsonObj.get("quantity").getAsInt();
            if (count <= 30000) {
                jsonObj.remove("quantity");
                jsonObj.addProperty("quantity", 30000);
            }
        }
        Set<Integer> ownedIdSet = getSetFromField("material_id", "data", "material_list");
        for(Map.Entry<Integer, MaterialMeta> entry : DragaliaData.idToMaterial.entrySet()){
            int id = entry.getKey();
            MaterialMeta mat = entry.getValue();
            if(!ownedIdSet.contains(id)){
                switch(mat.getCategory()){ //ignore certain items
                    case "Raid":
                    case "Raid, Collab":
                    case "Battle Royale":
                    case "Idk":
                        continue;
                }
                JsonObject newItem = new JsonObject();
                newItem.addProperty("material_id", id);
                newItem.addProperty("quantity", 30000);
                items.add(newItem);
                Logging.write(mat.getName());
            }
        }
        Logging.flushLog("Added materials");

        int[] giftIds = new int[]{30001, 30002, 30003, 40001};
        JsonArray giftList = new JsonArray();
        for (Integer giftId : giftIds) {
            JsonObject gift = new JsonObject();
            gift.addProperty("dragon_gift_id", giftId);
            gift.addProperty("quantity", 3000);
            giftList.add(gift);
        }
        getFieldAsJsonObject("data").remove("dragon_gift_list");
        getFieldAsJsonObject("data").add("dragon_gift_list", giftList);

        Logging.flushLog("Added dragon gifts");
    }

    public static void backToTheMines() {
        //for each kscape combo, put new kscape print data for each ele-weapon combo

        String[][] kscapeCombos = KscapeCombos.KSCAPES;
        int keyIdOffset = 1;
        JsonObject jsonData = getField("data").getAsJsonObject();
        jsonData.remove("talisman_list");
        JsonArray talismans = new JsonArray();
        for (String[] kscapeCombo : kscapeCombos) {
            //for each ele-wep combo
            for (int element = 1; element <= 5; element++) {
                for (int weapon = 1; weapon <= 9; weapon++) {
                    String elementString = AdventurerMeta.getElementString(element);
                    String weaponString = AdventurerMeta.getWeaponTypeString(weapon);
                    String optionString = "portrait" + elementString + weaponString + "Name";
                    String adventurerName = Options.getFieldAsString(optionString).toUpperCase(Locale.ROOT);
                    AdventurerMeta adventurer = DragaliaData.nameToAdventurer.get(adventurerName);
                    int portraitId = adventurer.getKscapeLabelId();
                    talismans.add(buildTalisman(portraitId, kscapeCombo, keyIdOffset));
                    keyIdOffset++;
                }
            }
        }
        jsonData.add("talisman_list", talismans);

        //delete equipped kscapes, since old kscape ID's will now point to
        //a kscape that no longer exists
        JsonArray partyList = getFieldAsJsonArray("data", "party_list");
        for (JsonElement jsonEle : partyList) {
            JsonObject party = jsonEle.getAsJsonObject();
            for (JsonElement jsonEle2 : party.getAsJsonArray("party_setting_list")) {
                JsonObject adventurer = jsonEle2.getAsJsonObject();
                adventurer.remove("equip_talisman_key_id");
                adventurer.addProperty("equip_talisman_key_id", 0);
            }
        }
    }

    public static int addMissingWeaponSkins() {
        int count = 0;
        Set<Integer> ownedWeaponSkinIDs = getSetFromField("weapon_skin_id", "data", "weapon_skin_list");

        for (Map.Entry<Integer, WeaponSkinMeta> entry : DragaliaData.idToWeaponSkin.entrySet()) {
            int weaponSkinId = entry.getKey();
            WeaponSkinMeta weaponSkinMeta = entry.getValue();
            if (!ownedWeaponSkinIDs.contains(weaponSkinId)) {
                if (weaponSkinMeta.isPlayable()) {
                    JsonObject newWeaponSkin = new JsonObject();
                    newWeaponSkin.addProperty("weapon_skin_id", weaponSkinId);
                    newWeaponSkin.addProperty("is_new", 1);
                    newWeaponSkin.addProperty("gettime", Instant.now().getEpochSecond());
                    getFieldAsJsonArray("data", "weapon_skin_list").add(newWeaponSkin);
                    count++;
                    Logging.write(weaponSkinMeta.getName().replace(" (Skin)", ""));
                }
            }
        }
        Logging.flushLog("Added weapon skins");
        return count;
    }

    public static int addMissingWeapons() {
        int count = 0;
        //Compile a set of ID's you have
        Set<Integer> ownedIdSet = getSetFromField("weapon_body_id", "data", "weapon_body_list");

        //Go through a list of all the weapons in the game
        for (Map.Entry<Integer, WeaponMeta> entry : DragaliaData.idToWeapon.entrySet()) {
            WeaponMeta weapon = entry.getValue();
            int id = entry.getKey();
            if (!ownedIdSet.contains(id)) { //If you don't own this weapon
                //Construct new weapon
                JsonObject newWeapon = buildWeapon(weapon, -1);
                //Add it to your inventory
                if (newWeapon != null) {
                    getField("data", "weapon_body_list").getAsJsonArray().add(newWeapon);
                    if (Options.getFieldAsBoolean("maxAddedWeapons")) {
                        //Update weapon bonuses
                        addWeaponBonus(weapon);
                        //Update weapon passives
                        updateWeaponPassives(weapon);
                    }
                    count++;
                    Logging.write(weapon.getName() + "(" + weapon.getRarity() + "*, " + weapon.getWeaponSeries() + ")");
                }
            }
        }
        Logging.flushLog("Added weapons");

        if (Options.getFieldAsBoolean("maxAddedWeapons")) {
            Tests.addTestFlag("addMissingWeaponsMaxed");
        }
        return count;
    }

    //doozy
    public static void maxFacilities(){
        int upgradedExistingCount = 0;
        int addedCount = 0;
        int addedDecoCount = 0;

        int keyIdMax = 0;   //need to keep track of keyId so we don't run into dupe keyId issue

        JsonArray newFacilities = new JsonArray();
        JsonArray currentFacilities = getFieldAsJsonArray("data", "build_list");
        HashMap<Integer, Integer> idToBuildCount = new HashMap<>();
        //first pass... upgrade all existing facilities, and count how many of each facility you own
        //also keep track of keyIdMax
        for(JsonElement jsonEle : currentFacilities){
            JsonObject currentFacility = jsonEle.getAsJsonObject();
            int keyId = currentFacility.get("build_id").getAsInt();
            int x = currentFacility.get("position_x").getAsInt();
            int y = currentFacility.get("position_z").getAsInt();
            int id = currentFacility.get("plant_id").getAsInt();
            int level = currentFacility.get("level").getAsInt();
            //check if this facility is maxed
            FacilityMeta fac = DragaliaData.idToFacility.get(id);
            if(level != fac.getMaxLevel()){
                upgradedExistingCount++;
                Logging.write(fac.getName() + ": " + level + " -> " + fac.getMaxLevel());
            }
            keyIdMax = Math.max(keyIdMax, keyId);
            if(idToBuildCount.containsKey(id)){ //increment build count
                int buildCount = idToBuildCount.get(id);
                idToBuildCount.put(id, buildCount + 1);
            } else {
                idToBuildCount.put(id, 1);
            }
            newFacilities.add(buildFacility(DragaliaData.idToFacility.get(id), keyId, x, y));
        }
        Logging.flushLog("Levelled up " + upgradedExistingCount + " facilities");

        //below... might run into issues where players might end up having like 3 or 4 dojos...
        //possibly may want to remove adding missing facilities later

        //compile list of max amounts you can have for each facility
        HashMap<Integer, Integer> idToMaxBuildCount = new HashMap<>();
        DragaliaData.idToFacility.forEach((id, fac) -> idToMaxBuildCount.put(id, fac.getMaxBuildCount()));

        //get diffs for owned fac count and max fac count
        HashMap<Integer, Integer> idToMaxBuildCountDiff = new HashMap<>();
        for(Map.Entry<Integer, Integer> entry : idToMaxBuildCount.entrySet()){
            int id = entry.getKey();
            int maxCount = entry.getValue();
            int ownedCount = idToBuildCount.getOrDefault(id, 0);
            idToMaxBuildCountDiff.put(id, maxCount - ownedCount);
        }

        //second pass... add appropriate amount of each facility missing
        for(Map.Entry<Integer, Integer> entry : idToMaxBuildCountDiff.entrySet()){
            int id = entry.getKey();
            int missingCount = entry.getValue();
            for(int i = 0; i < missingCount; i++){
                newFacilities.add(buildFacility(DragaliaData.idToFacility.get(id), keyIdMax, addedCount + 1));
                if(DragaliaData.idToFacility.get(id).getMaxLevel() == 0){ //max level 0 --> deco
                    addedDecoCount++;
                } else {
                    addedCount++;
                }
            }
            if(missingCount > 0){
                Logging.write(DragaliaData.idToFacility.get(id).getName() + " x" + missingCount);
            }
        }
        Logging.flushLog("Added facilities");
        System.out.println("Upgraded " + upgradedExistingCount + " existing facilities, added " + addedCount +
                " new facilities, and added " + addedDecoCount + " decoration facilities");
        //replace facilities list
        getFieldAsJsonObject("data").remove("build_list");
        getFieldAsJsonObject("data").add("build_list", newFacilities);

        //if you probably got a stat boost, update fort_bonus_list
        //...hardcoded for now
        if(upgradedExistingCount != 0 || addedCount != 0){
            JsonObject bonuses = getFieldAsJsonObject("data", "fort_bonus_list");
            bonuses.remove("param_bonus"); //facility weapon bonuses
            bonuses.remove("element_bonus");
            bonuses.remove("dragon_bonus");
            bonuses.add("param_bonus", DragaliaData.maxedFacilityBonuses.get("param_bonus"));
            bonuses.add("element_bonus", DragaliaData.maxedFacilityBonuses.get("element_bonus"));
            bonuses.add("dragon_bonus", DragaliaData.maxedFacilityBonuses.get("dragon_bonus"));
        }
        //update fort_plant_list... hardcoded for now
        //wtf does this do anyway?
        getFieldAsJsonObject("data").remove("fort_plant_list");
        getFieldAsJsonObject("data").add("fort_plant_list", DragaliaData.maxedFacilityBonuses.get("fort_plant_list"));
    }

    public static void maxAdventurers() {
        JsonArray updatedAdventurers = new JsonArray();
        JsonArray ownedAdventurers = getFieldAsJsonArray("data", "chara_list");

        for(JsonElement jsonEle : ownedAdventurers){
            JsonObject ownedAdventurer = jsonEle.getAsJsonObject();
            int id = ownedAdventurer.get("chara_id").getAsInt();
            int getTime = ownedAdventurer.get("gettime").getAsInt();
            AdventurerMeta adventurer = DragaliaData.idToAdventurer.get(id);
            if(adventurer == null){
                continue;
            }
            //Construct new unit
            JsonObject updatedUnit = buildUnit(adventurer, getTime);
            updatedAdventurers.add(updatedUnit);
            //Update encyclopedia bonus
            int level = ownedAdventurer.get("level").getAsInt();
            int mc = ownedAdventurer.get("mana_circle_piece_id_list").getAsJsonArray().size();
            int elementId = adventurer.getElementId();
            boolean hasManaspiral = adventurer.hasManaSpiral();
            double hpBonus = 0.0;
            double strBonus = 0.0;
            if (hasManaspiral) {
                if (level < 80) { hpBonus = 0.2; }
                else if (level < 100) { hpBonus = 0.1; }
                if (mc < 50) { strBonus = 0.2; }
                else if (mc < 70) { strBonus = 0.1; }
            } else {
                if (level < 80) { hpBonus = 0.1; }
                if (mc < 50) { strBonus = 0.1; }
            }
            addAdventurerEncyclopediaBonus(elementId, hpBonus, strBonus);
            //Unlock adventurer stories
            unlockAdventurerStory(id, false);
        }
        //Replace current adventurer list
        getFieldAsJsonObject("data").remove("chara_list");
        getFieldAsJsonObject("data").add("chara_list", updatedAdventurers);
    }

    public static void maxDragons(){
        JsonArray updatedDragons = new JsonArray();
        JsonArray ownedDragons = getFieldAsJsonArray("data", "dragon_list");

        for(JsonElement jsonEle : ownedDragons){
            JsonObject ownedDragon = jsonEle.getAsJsonObject();
            int id = ownedDragon.get("dragon_id").getAsInt();
            int getTime = ownedDragon.get("get_time").getAsInt();
            int keyId = ownedDragon.get("dragon_key_id").getAsInt();
            boolean isLocked = ownedDragon.get("is_lock").getAsInt() == 1;
            DragonMeta dragon = DragaliaData.idToDragon.get(id);
            if(dragon == null){
                continue;
            }
            // Construct new dragon
            JsonObject updatedUnit = buildDragonFromExisting(dragon, keyId, getTime, isLocked);
            updatedDragons.add(updatedUnit);
            boolean has5UB = dragon.has5UB();

            // Update dragon bond obj
            updateDragonBond(id);

            // add dragon stories
            addDragonStory(dragon.getDragonStoryId(1));
            addDragonStory(dragon.getDragonStoryId(2));

            // Update encyclopedia max level/unbound obj
            for (JsonElement jsonEle2 : getFieldAsJsonArray("data", "album_dragon_list")){
                JsonObject encycloData = jsonEle2.getAsJsonObject();
                if(encycloData.get("dragon_id").getAsInt() != id){
                    continue; // ignore if this isnt the dragon
                }
                int maxLevel = encycloData.get("max_level").getAsInt();
                int maxUnbinds = encycloData.get("max_limit_break_count").getAsInt();
                boolean toUpdateBonuses = maxUnbinds < (has5UB ? 5 : 4); //actual bonuses itself
                boolean toUpdateEncyclo = toUpdateBonuses || maxLevel < dragon.getMaxLevel(); //record obj in album_dragon_list

                if(toUpdateEncyclo){
                    encycloData.remove("max_level");
                    encycloData.remove("max_limit_break_count");
                    encycloData.addProperty("max_level", dragon.getMaxLevel());
                    encycloData.addProperty("max_limit_break_count", has5UB ? 5 : 4);
                    if(toUpdateBonuses){
                        // Update encyclopedia bonus
                        int unbinds = ownedDragon.get("limit_break_count").getAsInt();
                        int elementId = dragon.getElementId();
                        double hpBonus = 0.0;
                        if (has5UB) {
                            if (unbinds < 4) { hpBonus = 0.2; }
                            else if (unbinds < 5) { hpBonus = 0.1; }
                        } else {
                            if (unbinds < 4) { hpBonus = 0.1; }
                        }
                        addDragonEncyclopediaBonus(elementId, hpBonus, 0.0);
                    }
                }
            }
        }
        //Replace current dragon list
        getFieldAsJsonObject("data").remove("dragon_list");
        getFieldAsJsonObject("data").add("dragon_list", updatedDragons);

    }

    public static void maxWeapons(){
        JsonArray updatedWeapons = new JsonArray();
        JsonArray ownedWeapons = getFieldAsJsonArray("data", "weapon_body_list");

        for(JsonElement jsonEle : ownedWeapons){
            JsonObject ownedWeapon = jsonEle.getAsJsonObject();
            int id = ownedWeapon.get("weapon_body_id").getAsInt();
            int getTime = ownedWeapon.get("gettime").getAsInt();
            WeaponMeta weapon = DragaliaData.idToWeapon.get(id);
            //Construct new weapon
            JsonObject updatedWeapon = buildWeapon(weapon, getTime);
            updatedWeapons.add(updatedWeapon);
            //Update weapon bonus
            boolean isWeaponBonusUnlocked = ownedWeapon.get("fort_passive_chara_weapon_buildup_count").getAsInt() == 1;
            if(!isWeaponBonusUnlocked){
                addWeaponBonus(weapon);
            }
            //Update weapon passives
            updateWeaponPassives(weapon);
        }
        //Replace current weapon list
        getFieldAsJsonObject("data").remove("weapon_body_list");
        getFieldAsJsonObject("data").add("weapon_body_list", updatedWeapons);

        Tests.addTestFlag("maxWeapons");
    }

    public static void updateWeaponPassives(WeaponMeta weapon) {
        List<Integer> passiveIdList = weapon.getPassiveAbilityIdList();
        for(Integer id : passiveIdList) {
            JsonObject passiveId = new JsonObject();
            passiveId.addProperty("weapon_passive_ability_id", id);
            JsonArray passiveAbilityList = getFieldAsJsonArray("data", "weapon_passive_ability_list");
            if(!passiveAbilityList.contains(passiveId)){
                passiveAbilityList.add(passiveId);
            }
        }
    }

    public static void maxWyrmprints(){
        JsonArray updatedWyrmprints = new JsonArray();
        JsonArray ownedWyrmprints = getFieldAsJsonArray("data", "ability_crest_list");

        for(JsonElement jsonEle : ownedWyrmprints){
            JsonObject ownedWyrmprint = jsonEle.getAsJsonObject();
            int id = ownedWyrmprint.get("ability_crest_id").getAsInt();
            int getTime = ownedWyrmprint.get("gettime").getAsInt();
            boolean isFavorite = ownedWyrmprint.get("is_favorite").getAsInt() == 1;
            WyrmprintMeta wyrmprint = DragaliaData.idToPrint.get(id);
            //Construct new print
            JsonObject updatedPrint = buildWyrmprint(wyrmprint, getTime, isFavorite);
            updatedWyrmprints.add(updatedPrint);
        }
        //Replace current adventurer list
        getFieldAsJsonObject("data").remove("ability_crest_list");
        getFieldAsJsonObject("data").add("ability_crest_list", updatedWyrmprints);
    }

    //check for temporary adventurers who've been skipped
    //their list_view_flag will be == 0
    public static List<String> checkSkippedTempAdventurers(){
        List<String> skippedAdvs = new ArrayList<>();
        for(JsonElement jsonEle : getFieldAsJsonArray("data", "chara_list")){
            JsonObject adv = jsonEle.getAsJsonObject();
            if(adv.get("list_view_flag").getAsInt() == 0){
                int charaId = adv.get("chara_id").getAsInt();
                String name = DragaliaData.idToAdventurer.getOrDefault(charaId, AdventurerMeta.DUMMY).getName();
                skippedAdvs.add(name);
            }
        }
        return skippedAdvs;
    }

    //set list_view_flag of all adventurers to 1
    public static void setAdventurerVisibleFlags(){
        for(JsonElement jsonEle : getFieldAsJsonArray("data", "chara_list")){
            JsonObject adv = jsonEle.getAsJsonObject();
            if(adv.get("list_view_flag").getAsInt() == 0){
                adv.remove("list_view_flag");
                adv.addProperty("list_view_flag", 1);
            }
        }
    }

    //Hacked options

    public static void addTutorialZethia(){
        addHackedUnit(19900001);
    }

    public static void addStoryLeifs(){
        addHackedUnit(19900002);
        addHackedUnit(19900005);
    }

    public static void addDog() { // Hidden Option
        addHackedUnit(19900004); //Puppy
    }

    public static void addNottes() { // Hidden Option
        addHackedUnit(19900003); //Yellow Notte
        addHackedUnit(19900006); //Blue Notte
    }

    public static void addStoryNPCs(){ // Hidden Option
        for(int i = 0; i < 67; i++){
            addHackedUnit(19100001 + i);
        }
    }

    public static void addGunnerCleo(){
        addHackedUnit(99900009); //Gunner Cleo
    }

    public static void addABR3Stars(){ // Hidden Option
        for(int i = 0; i < 9; i++){
            addHackedUnit(99130001 + i * 100000);
        }
    }

    public static void addUniqueShapeshiftDragons(){
        Arrays.asList(29900006, 29900014, 29900017, 29900018, 29900023).forEach(
                id -> addHackedDragon(id));
    }

    public static void addUnplayableDragons(){ // Hidden Option
        for(int i = 0; i < 27; i++){
            addHackedDragon(29900001 + i);
        }
        addHackedDragon(29800001);
        addHackedDragon(29800002);
        addHackedDragon(29800003);
        for(int i = 0; i < 6; i++){
            addHackedDragon(21000001 + i);
        }
        addHackedDragon(29940301);
        addHackedDragon(29950405);
        addHackedDragon(29950116);
        addHackedDragon(29950522);
        addHackedDragon(29950317);
        addHackedDragon(29950523);
        addHackedDragon(29950518);
        addHackedDragon(29950415);
        addHackedDragon(29950524);
        addHackedDragon(29950416);
        addHackedDragon(29950525);
        addHackedDragon(29950121);
        addHackedDragon(29950320);
    }

    //ehh......
    public static void deleteDupeIds () {
        // soem guy downloaded save data from orchis and found out that
        // they had a lot of dupe weapon skin ids. probably cause they
        // used this program to edit them in, and then made some weapons in the server
        // so lets just delete the dupe ids
        int dupeDragonKeyIdCount = 0;
        int dupeWeaponSkinIdCount = 0;

        // happened with dragon key ids and weapon skin ids...
        List<Integer> keyIds = new ArrayList<>();
        List<JsonElement> toRemove = new ArrayList<>();
        JsonArray dragons = getFieldAsJsonArray("data", "dragon_list");
        for (JsonElement jsonEle : dragons) {
            JsonObject dragon = jsonEle.getAsJsonObject();
            int id = dragon.get("dragon_key_id").getAsInt();
            if (keyIds.contains(id)) {
                dupeDragonKeyIdCount++;
                toRemove.add(dragon);
            } else {
                keyIds.add(id);
            }
        }
        for (JsonElement badDragon : toRemove) {
            dragons.remove(badDragon);
        }

        keyIds.clear();
        toRemove.clear();
        JsonArray weaponSkins = getFieldAsJsonArray("data", "weapon_skin_list");
        for (JsonElement jsonEle : weaponSkins) {
            JsonObject weaponSkin = jsonEle.getAsJsonObject();
            int id = weaponSkin.get("weapon_skin_id").getAsInt();
            if (keyIds.contains(id)) {
                dupeWeaponSkinIdCount++;
                toRemove.add(weaponSkin);
            } else {
                keyIds.add(id);
            }
        }
        for (JsonElement badWeaponSkin : toRemove) {
            weaponSkins.remove(badWeaponSkin);
        }
        
        if(dupeDragonKeyIdCount != 0 || dupeWeaponSkinIdCount != 0) {
            System.out.println("Found dupe ID issues with the save file when importing... this should not happen."
                    + " The save editor will remove these dupe ID's for editing.");
            if (dupeDragonKeyIdCount != 0) {
                System.out.println("Error: Duplicate dragon key ID count: " +  dupeDragonKeyIdCount);
            }
            if (dupeWeaponSkinIdCount != 0) {
                System.out.println("Error: Duplicate weapon skin key ID count: " + dupeWeaponSkinIdCount);
                System.out.println("(This one was most likely caused by downloading save data " +
                        "from a private server that you crafted weapons on");
                System.out.println("after editing in weapon skins" +
                        " using this save editor.)");
            }
            System.out.println();
        }
    }

    public static void kscapeRandomizer() {
        JsonArray talismans = new JsonArray();

        for (int i = 1; i <= 500; i++) {
            //get random adventurer portrait ID
            int portraitListSize = DragaliaData.kscapePortraitIDs.size();
            int portraitID = DragaliaData.kscapePortraitIDs.get(rng.nextInt(portraitListSize));

            //get random talisman
            JsonObject randomTalisman = buildRandomTalisman(portraitID, i);
            talismans.add(randomTalisman);
        }

        getField("data").getAsJsonObject().remove("talisman_list");
        getField("data").getAsJsonObject().add("talisman_list", talismans);

        //delete equipped kscapes, since old kscape ID's will now point to
        //a kscape that no longer exists
        JsonArray partyList = getFieldAsJsonArray("data", "party_list");
        for (JsonElement jsonEle : partyList) {
            JsonObject party = jsonEle.getAsJsonObject();
            for (JsonElement jsonEle2 : party.getAsJsonArray("party_setting_list")) {
                JsonObject adventurer = jsonEle2.getAsJsonObject();
                adventurer.remove("equip_talisman_key_id");
                adventurer.addProperty("equip_talisman_key_id", 0);
            }
        }
    }

    public static void addGoofyKscapes() {
        addTalisman("xander", 806, 805, 721, 4); //(Water) Skill Recharge +65%, Skill Prep +100%
        addTalisman("gatov", 100100205, 1237, 100100204, 4); //ar20 + flame ar20 + ar10
        addTalisman("syasu", 100100205, 1225, 100100204, 4); //ar20 + hp70 ar10 + ar10
        addTalisman("ranzal", 2172, 2175, 721, 4); //bolk
        addTalisman("alia", 1237, 400000822, 400000821, 1); //dyilia
        addTalisman("valyx", 2664, 871, 721, 2); //valyx
        addTalisman("emile", 2579, 2578, 806, 2); //emile
        addTalisman("klaus", 2735, 42960, 721, 1); //ned
        addTalisman("marth", 927, 929, 934, 1); //triple Last (buffer)
        addTalisman("sharena", 902, 746, 934, 1); //triple last (dmg)
        //credit: sinkarth
        addTalisman("galex", 340000132, 934, 291, 1); //"Galex Mega Fod"
        addTalisman("grace", 340000070, 340000134, 927, 1); //"Grace Last Boost"
        addTalisman("xainfried", 2735, 3701, 43160, 1); //"Super Dragon Time"
        addTalisman("yaten", 2664, 2263, 871, 1); //"Energized Boost"
        addTalisman("nino", 340000132, 100100205, 924, 1); //"I tried to fix Nino"
        addTalisman("alia", 2041, 1447, 2045, 1); //"Infinite Critical Damage"
        addTalisman("ao", 340000030, 340000132, 827, 1); //"Better Mars"
        addTalisman("dynef", 1620, 2281, 1440, 1); //"Flurry Freezer & other combo effects"
        addTalisman("grimnir", 1914, 1939, 1966, 1); //"Passive Damage Stacking"
        //credit: Klaus
        addTalisman("delphi", 747, 457, 456, 3); //negative str
    }


    // return 0 --> valid save file
    // return 1 --> file path does not exist
    // return 2 --> file path exists but is not JSON
    public static int checkIfJsonObject(String path) {
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(path));
        } catch (FileNotFoundException ignored) {
            return 1;
        }

        if (GSON.fromJson(reader, JsonObject.class) == null) {
            return 2;
        }

        return 0;
    }

}
