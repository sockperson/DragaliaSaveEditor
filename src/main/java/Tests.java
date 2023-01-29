import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Set;

public class Tests {

    private JsonUtils jsonUtils;
    private JsonObject jsonData;
    private boolean didAllPass = true;

    public Tests(JsonUtils jsonUtils){
        this.jsonUtils = jsonUtils;
        jsonData = jsonUtils.getJsonData();
    }

    public String noDupeCharaIdTest(){
        boolean[] out = new boolean[]{true};

        JsonArray charaList = jsonData.get("data").getAsJsonObject().get("chara_list").getAsJsonArray();
        Set<Integer> charaIds = new HashSet<>();
        charaList.forEach(jsonEle -> {
            JsonObject chara = jsonEle.getAsJsonObject();
            int charaId = chara.get("chara_id").getAsInt();
            if(charaIds.contains(charaId)){
                out[0] = false;
            }
            charaIds.add(charaId);
        });
        return boolToString(out[0]);
    }

    public String noDupeDragonKeyIdTest(){
        boolean[] out = new boolean[]{true};

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
        return boolToString(out[0]);
    }

    public String noDupeTalismanKeyIdTest(){
        boolean[] out = new boolean[]{true};

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
        return boolToString(out[0]);
    }

    public String noDupeWeaponSkinIdTest(){
        boolean[] out = new boolean[]{true};

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
        return boolToString(out[0]);
    }

    public String noDupeCrestIdTest(){
        boolean[] out = new boolean[]{true};

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
        return boolToString(out[0]);
    }

    private boolean assertEquals(Object val1, Object val2){
        return val1.equals(val2);
    }

    private String boolToString(boolean val){
        if(!val){
            didAllPass = false;
        }
        return val ? "PASSED" : "FAILED";
    }

    public boolean getIfAllPassed(){
        return didAllPass;
    }

}
