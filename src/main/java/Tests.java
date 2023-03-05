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
        boolean[] out = new boolean[]{true};
        String[] oopsID = new String[]{""};

        JsonArray charaList = jsonData.get("data").getAsJsonObject().get("chara_list").getAsJsonArray();
        Set<Integer> charaIds = new HashSet<>();
        charaList.forEach(jsonEle -> {
            JsonObject chara = jsonEle.getAsJsonObject();
            int charaId = chara.get("chara_id").getAsInt();
            if(charaIds.contains(charaId)){
                out[0] = false;
                oopsID[0] = Integer.toString(charaId);
            }
            charaIds.add(charaId);
        });
        return boolToString(out[0], oopsID[0]);
    }

    public String noDupeDragonKeyIdTest(){
        boolean[] out = new boolean[]{true};
        String[] oopsID = new String[]{""};

        JsonArray list = jsonData.get("data").getAsJsonObject().get("dragon_list").getAsJsonArray();
        Set<Integer> ids = new HashSet<>();
        list.forEach(jsonEle -> {
            JsonObject chara = jsonEle.getAsJsonObject();
            int id = chara.get("dragon_key_id").getAsInt();
            if(ids.contains(id)){
                out[0] = false;
            }
            ids.add(id);
        });
        return boolToString(out[0], oopsID[0]);
    }

    public String noDupeTalismanKeyIdTest(){
        boolean[] out = new boolean[]{true};
        String[] oopsID = new String[]{""};

        JsonArray list = jsonData.get("data").getAsJsonObject().get("talisman_list").getAsJsonArray();
        Set<Integer> ids = new HashSet<>();
        list.forEach(jsonEle -> {
            JsonObject chara = jsonEle.getAsJsonObject();
            int id = chara.get("talisman_key_id").getAsInt();
            if(ids.contains(id)){
                out[0] = false;
            }
            ids.add(id);
        });
        return boolToString(out[0], oopsID[0]);
    }

    public String noDupeWeaponSkinIdTest(){
        boolean[] out = new boolean[]{true};
        String[] oopsID = new String[]{""};

        JsonArray list = jsonData.get("data").getAsJsonObject().get("weapon_skin_list").getAsJsonArray();
        Set<Integer> ids = new HashSet<>();
        list.forEach(jsonEle -> {
            JsonObject chara = jsonEle.getAsJsonObject();
            int id = chara.get("weapon_skin_id").getAsInt();
            if(ids.contains(id)){
                out[0] = false;
            }
            ids.add(id);
        });
        return boolToString(out[0], oopsID[0]);
    }

    public String noDupeCrestIdTest(){
        boolean[] out = new boolean[]{true};
        String[] oopsID = new String[]{""};

        JsonArray list = jsonData.get("data").getAsJsonObject().get("ability_crest_list").getAsJsonArray();
        Set<Integer> ids = new HashSet<>();
        list.forEach(jsonEle -> {
            JsonObject chara = jsonEle.getAsJsonObject();
            int id = chara.get("ability_crest_id").getAsInt();
            if(ids.contains(id)){
                out[0] = false;
            }
            ids.add(id);
        });
        return boolToString(out[0], oopsID[0]);
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

    public boolean getIfAllPassed(){
        return didAllPass;
    }

}
