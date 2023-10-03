import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tests {

    private static boolean didAllPass = true;

    private static List<String> testFlags = new ArrayList<>();

    public static String noDupeCharaIdTest(){
        int offendingId = JsonUtils.arrayHasDuplicateValue("chara_id", "data", "chara_list");
        return boolToString(offendingId == -1, offendingId);
    }

    public static String noDupeDragonKeyIdTest(){
        int offendingId = JsonUtils.arrayHasDuplicateValue("dragon_key_id", "data", "dragon_list");
        return boolToString(offendingId == -1, offendingId);
    }

    public static String noDupeTalismanKeyIdTest(){
        int offendingId = JsonUtils.arrayHasDuplicateValue("talisman_key_id", "data", "talisman_list");
        return boolToString(offendingId == -1, offendingId);
    }

    public static String noDupeWeaponSkinIdTest(){
        int offendingId = JsonUtils.arrayHasDuplicateValue("weapon_skin_id", "data", "weapon_skin_list");
        return boolToString(offendingId == -1, offendingId);
    }

    public static String noDupeCrestIdTest(){
        int offendingId = JsonUtils.arrayHasDuplicateValue("ability_crest_id", "data", "ability_crest_list");
        return boolToString(offendingId == -1, offendingId);
    }

    public static String weaponPassivesIdTest() {
        if (!hasFlags("maxWeapons", "addMissingWeapons")) {
            return "N/A";
        }
        JsonArray list = JsonUtils.jsonData.get("data").getAsJsonObject().get("weapon_passive_ability_list").getAsJsonArray();
        return boolToString(list.size() == 423, Integer.toString(list.size()));
    }

    public static String weaponPassivesIdPerWeaponTest() {
        if (!hasFlags("maxWeapons", "addMissingWeaponsMaxed")) {
            return "N/A";
        }
        JsonArray list = JsonUtils.jsonData.get("data").getAsJsonObject().get("weapon_body_list").getAsJsonArray();
        int abilityCount = 0;
        for (JsonElement jsonEle : list) {
            JsonObject jsonObj = jsonEle.getAsJsonObject();
            JsonArray idList = jsonObj.get("unlock_weapon_passive_ability_no_list").getAsJsonArray();
            for (JsonElement jsonEle2 : idList) {
                if (jsonEle2.getAsInt() == 1) {
                    abilityCount++;
                }
            }
        }
        return boolToString(abilityCount == 423, Integer.toString(abilityCount));
    }

    public static String noDupeStoryIdTest() {
        int offendingId = JsonUtils.arrayHasDuplicateValue("unit_story_id", "data", "unit_story_list");
        return boolToString(offendingId == -1, offendingId);
    }

    public static boolean hasFlags(String... flags) {
        for (String flag : flags) {
            if(!testFlags.contains(flag)) {
                return false;
            }
        }
        return true;
    }

    private static String boolToString(boolean val, String oops){
        if(!val){
            didAllPass = false;
        }
        return val ? "PASSED" : ("FAILED: " + oops);
    }

    private static String boolToString(boolean val, int oops){
        if(!val){
            didAllPass = false;
        }
        return val ? "PASSED" : ("FAILED: " + oops);
    }

    public static void addTestFlag(String flag) {
        testFlags.add(flag);
    }

    public static boolean didTestsPass(){
        Logging.log("noDupeCharaIdTest(): " + Tests.noDupeCharaIdTest());
        Logging.log("noDupeDragonKeyIdTest(): " + Tests.noDupeDragonKeyIdTest());
        Logging.log("noDupeTalismanKeyIdTest(): " + Tests.noDupeTalismanKeyIdTest());
        Logging.log("noDupeWeaponSkinIdTest(): " + Tests.noDupeWeaponSkinIdTest());
        Logging.log("noDupeCrestIdTest(): " + Tests.noDupeCrestIdTest());
        Logging.log("noDupeStoryIdTest(): " + Tests.noDupeStoryIdTest());
        Logging.log("weaponPassivesIdTest(): " + Tests.weaponPassivesIdTest());
        Logging.log("weaponPassivesIdPerWeaponTest(): " + Tests.weaponPassivesIdPerWeaponTest());
        return didAllPass;
    }

}
