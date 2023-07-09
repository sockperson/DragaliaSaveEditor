import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tests {

    private JsonUtils jsonUtils;
    private JsonObject jsonData;
    private boolean didAllPass = true;

    private List<String> testFlags;

    public Tests(JsonUtils jsonUtils){
        this.jsonUtils = jsonUtils;
        jsonData = jsonUtils.getJsonData();
        this.testFlags = jsonUtils.getTestFlags();
    }

    public String noDupeCharaIdTest(){
        int offendingId = jsonUtils.arrayHasDuplicateValue("chara_id", "data", "chara_list");
        return boolToString(offendingId == -1, offendingId);
    }

    public String noDupeDragonKeyIdTest(){
        int offendingId = jsonUtils.arrayHasDuplicateValue("dragon_key_id", "data", "dragon_list");
        return boolToString(offendingId == -1, offendingId);
    }

    public String noDupeTalismanKeyIdTest(){
        int offendingId = jsonUtils.arrayHasDuplicateValue("talisman_key_id", "data", "talisman_list");
        return boolToString(offendingId == -1, offendingId);
    }

    public String noDupeWeaponSkinIdTest(){
        int offendingId = jsonUtils.arrayHasDuplicateValue("weapon_skin_id", "data", "weapon_skin_list");
        return boolToString(offendingId == -1, offendingId);
    }

    public String noDupeCrestIdTest(){
        int offendingId = jsonUtils.arrayHasDuplicateValue("ability_crest_id", "data", "ability_crest_list");
        return boolToString(offendingId == -1, offendingId);
    }

    public String weaponPassivesIdTest() {
        if (!hasFlags("maxWeapons", "addMissingWeapons")) {
            return "N/A";
        }
        JsonArray list = jsonData.get("data").getAsJsonObject().get("weapon_passive_ability_list").getAsJsonArray();
        return boolToString(list.size() == 423, Integer.toString(list.size()));
    }

    public String weaponPassivesIdPerWeaponTest() {
        if (!hasFlags("maxWeapons", "addMissingWeapons")) {
            return "N/A";
        }
        JsonArray list = jsonData.get("data").getAsJsonObject().get("weapon_body_list").getAsJsonArray();
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

    public String noDupeStoryIdTest() {
        int offendingId = jsonUtils.arrayHasDuplicateValue("unit_story_id", "data", "unit_story_list");
        return boolToString(offendingId == -1, offendingId);
    }

    public boolean hasFlags(String... flags) {
        for (String flag : flags) {
            if(!testFlags.contains(flag)) {
                return false;
            }
        }
        return true;
    }

    private boolean assertEquals(Object val1, Object val2){
        return val1.equals(val2);
    }

    private String boolToString(boolean val, String oops){
        if(!val){
            didAllPass = false;
        }
        return val ? "PASSED" : ("FAILED: " + oops);
    }

    private String boolToString(boolean val, int oops){
        if(!val){
            didAllPass = false;
        }
        return val ? "PASSED" : ("FAILED: " + oops);
    }

    public boolean getIfAllPassed(){
        return didAllPass;
    }

}
