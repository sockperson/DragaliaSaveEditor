import java.io.*;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

public class JsonUtils {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final String path;
    private String basePath = "";
    private final String rsrcPath;
    private JsonObject jsonData;
    private JsonArray adventurerData;

    //Ability Name --> Ability ID
    private HashMap<String, Integer> kscapeAbilityMap = new HashMap<>();
    //Adventurer Title --> Portrait Print ID
    private HashMap<String, Integer> kscapeLabelsMap = new HashMap<>();
    //Adventurer ID --> Adventurer Story ID
    private HashMap<Integer, Integer> adventurerStoryMap = new HashMap<>();

    private Set<Integer> weaponSkinSet = new HashSet<>();

    public JsonUtils(String path){
        this.path = path;
        try {
            basePath = path.substring(0, path.indexOf("savedata"));
        } catch (StringIndexOutOfBoundsException e){
            System.out.println("savedata.txt not found in directory!");
            System.exit(99);
        }
        rsrcPath = basePath + "src/resources/";
        try {
            this.jsonData = getSaveData().getAsJsonObject();
            readAdventurerData();
            readKscapeData();
            readKscapeLabels();
            readStoryData();
            readWeaponSkinData();
        } catch (IOException e) {
            System.out.println("Unable to read JSON data!");
            System.exit(99);
        }

    }

    public void writeToFile(){
        try {
            String newPath = basePath + "savedata2.txt";

            FileWriter fileWriter = new FileWriter(newPath);
            GSON.toJson(jsonData, fileWriter);
            fileWriter.flush();
            fileWriter.close();
            System.out.println("Saved output JSON to " + newPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JsonObject getJsonData(){ return jsonData; }

    private JsonObject getSaveData() throws IOException {
        JsonReader reader = new JsonReader(new FileReader(path));
        return GSON.fromJson(reader, JsonObject.class);
    }

    private JsonObject getJsonObject(String more) throws IOException {
        JsonReader reader = new JsonReader(new FileReader(rsrcPath + more));
        return GSON.fromJson(reader, JsonObject.class);
    }

    private JsonArray getJsonArray(String more) throws IOException {
        JsonReader reader = new JsonReader(new FileReader(rsrcPath + more));
        return GSON.fromJson(reader, JsonArray.class);
    }

    public String getFieldAsString(String... memberNames){
        return getField(memberNames).getAsString();
    }

    public int getFieldAsInt(String... memberNames){
        return getField(memberNames).getAsInt();
    }

    public JsonArray getFieldAsJsonArray(String... memberNames){
        return getField(memberNames).getAsJsonArray();
    }

    private void writeInteger(int value, String... memberNames){
        List<String> memberNameList = new ArrayList<>(Arrays.asList(memberNames));

        JsonElement jsonEle = jsonData;
        for(int i = 0; i < memberNameList.size() - 1; i++){
            jsonEle = jsonEle.getAsJsonObject().get(memberNameList.get(i));
        }
        String lastMemberName = memberNameList.get(memberNameList.size() - 1);
        JsonObject jsonObj = jsonEle.getAsJsonObject();
        jsonObj.remove(lastMemberName);
        jsonObj.addProperty(lastMemberName, value);
    }

    private JsonElement getField(String... memberNames){
        JsonElement jsonEle = jsonData;
        for(String memberName : memberNames){
            if(jsonEle.isJsonObject()){
                jsonEle = jsonEle.getAsJsonObject().get(memberName);
            }
        }
        return jsonEle;
    }

    private int getSum(JsonObject src, String... memberNames){
        int sum = 0;
        for(String memberName : memberNames){
            sum += src.get(memberName).getAsInt();
        }
        return sum;
    }

    private void readAdventurerData() throws IOException {
        System.out.println("Reading adventurers JSON...");
        adventurerData = getJsonArray("adventurers.json");
    }

    private void readKscapeData() throws IOException {
        System.out.println("Reading Kscape Abilities JSON...");
        JsonObject kscapeJson = getJsonObject("kscape.json");
        for(Map.Entry<String, JsonElement> entry : kscapeJson.entrySet()){
            kscapeAbilityMap.put(entry.getKey(), entry.getValue().getAsInt());
        }
    }

    private void readKscapeLabels() throws IOException {
        System.out.println("Reading Kscape Labels TXT...");
        BufferedReader br = new BufferedReader(new FileReader(Paths.get(rsrcPath, "kscapeLabels.txt").toFile()));
        String out = br.readLine();
        while(out != null){
            String[] split1 = out.split("\t");
            int id = Integer.parseInt(split1[0].split("_")[2]);
            String label = split1[2];
            kscapeLabelsMap.put(label, id);
            out = br.readLine();
        }
    }

    private void readStoryData() throws IOException {
        System.out.println("Reading adventurer stories TXT...");
        BufferedReader br = new BufferedReader(new FileReader(Paths.get(rsrcPath, "UnitStory.txt").toFile()));
        br.readLine(); //ignore first line of txt file
        String out = br.readLine();
        while(out != null){
            String[] fields = out.split(",");
            int storyID = Integer.parseInt(fields[0]);
            int advID = Integer.parseInt(fields[7]);
            out = br.readLine();
            if(adventurerStoryMap.containsKey(advID)){
                continue; //ignore if unit already has a story added
            }
            adventurerStoryMap.put(advID, storyID);
        }
    }

    private void readWeaponSkinData() throws IOException {
        System.out.println("Reading weapon skins TXT...");
        BufferedReader br = new BufferedReader(new FileReader(Paths.get(rsrcPath, "WeaponSkin.txt").toFile()));
        br.readLine(); //ignore first line of txt file
        String out = br.readLine();
        while(out != null){
            String[] fields = out.split(",");
            int skinID = Integer.parseInt(fields[0]);
            boolean isPlayable = fields[13].equals("1");
            out = br.readLine();
            if(!isPlayable){
                continue; //ignore if skin is unplayable
            }
            weaponSkinSet.add(skinID);
        }
    }

    private JsonObject buildTalisman(String label, String[] combo, int keyIdOffset){
        JsonObject out = new JsonObject();
        int abilityId1 = 0;
        int abilityId2 = 0;
        int abilityId3 = 0;
        for(int i = 0; i < combo.length; i++){
            int id = kscapeAbilityMap.get(combo[i]);
            switch(i){
                case 0 -> abilityId1 = id;
                case 1 -> abilityId2 = id;
                case 2 -> abilityId3 = id;
            }
        }

        out.addProperty("talisman_key_id", 200000 + 100 * keyIdOffset);
        out.addProperty("talisman_id", kscapeLabelsMap.get(label));
        out.addProperty("is_lock", 0);
        out.addProperty("is_new", 1);
        out.addProperty("talisman_ability_id_1", abilityId1);
        out.addProperty("talisman_ability_id_2", abilityId2);
        out.addProperty("talisman_ability_id_3", abilityId3);
        out.addProperty("additional_hp", 0);
        out.addProperty("additional_attack", 0);
        out.addProperty("gettime", Instant.now().getEpochSecond());

        return out;
    }

    //Returns a built adventurer in savedata.txt format
    private JsonObject buildUnit(JsonObject adventurerData){
        JsonObject out = new JsonObject();
        if(adventurerData.get("FullName").getAsString().equals("Puppy")){
            return null; //no dogs allowed
        }
        boolean hasManaSpiral = adventurerData.get("ManaSpiralDate") != null;
        int hp, str;
        if(hasManaSpiral){
            hp = getSum(adventurerData, "AddMaxHp1", "PlusHp0", "PlusHp1", "PlusHp2", "PlusHp3", "PlusHp4", "PlusHp5", "McFullBonusHp5");
            str = getSum(adventurerData, "AddMaxAtk1", "PlusAtk0", "PlusAtk1", "PlusAtk2", "PlusAtk3", "PlusAtk4", "PlusAtk5", "McFullBonusAtk5");
        } else {
            hp = getSum(adventurerData, "MaxHp", "PlusHp0", "PlusHp1", "PlusHp2", "PlusHp3", "PlusHp4", "McFullBonusHp5");
            str = getSum(adventurerData, "MaxAtk", "PlusAtk0", "PlusAtk1", "PlusAtk2", "PlusAtk3", "PlusAtk4", "McFullBonusAtk5");
        }
        JsonArray mc = new JsonArray();
        int mcLevel = hasManaSpiral ? 70 : 50;
        for(int i = 1; i <= mcLevel; i++){
            mc.add(i);
        }
        out.addProperty("chara_id", adventurerData.get("IdLong").getAsInt());
        out.addProperty("rarity", 5);
        out.addProperty("exp", hasManaSpiral ? 8866950 : 1191950);
        out.addProperty("level", hasManaSpiral ? 100 : 80);
        out.addProperty("additional_max_level", hasManaSpiral ? 20 : 0);
        out.addProperty("hp_plus_count", 100);
        out.addProperty("attack_plus_count", 100);
        out.addProperty("limit_break_count", adventurerData.get("MaxLimitBreakCount").getAsInt());
        out.addProperty("is_new", 1);
        out.addProperty("gettime", Instant.now().getEpochSecond());
        out.addProperty("skill_1_level", hasManaSpiral ? 4 : 3);
        out.addProperty("skill_2_level", hasManaSpiral ? 3 : 2);
        out.addProperty("ability_1_level", hasManaSpiral ? 3 : 2);
        out.addProperty("ability_2_level", hasManaSpiral ? 3 : 2);
        out.addProperty("ability_3_level", 2);
        out.addProperty("burst_attack_level", 2);
        out.addProperty("combo_buildup_count", hasManaSpiral ? 1 : 0);
        out.addProperty("hp", hp);
        out.addProperty("attack", str);
        out.addProperty("ex_ability_level", 5);
        out.addProperty("ex_ability_2_level", 5);
        out.addProperty("is_temporary", 0);
        out.addProperty("is_unlock_edit_skill", adventurerData.get("EditSkillCost").getAsInt() == 0 ? 0 : 1);
        out.add("mana_circle_piece_id_list", mc);
        out.addProperty("list_view_flag", 1);
        return out;
    }

    private void unlockAdventurerStory(int id){
        int storyID = adventurerStoryMap.get(id);
        JsonObject story = new JsonObject();
        story.addProperty("unit_story_id", storyID);
        story.addProperty("is_read", 0);
        getFieldAsJsonArray("data", "unit_story_list").add(story);
    }

    /// Util Methods \\\
    public void uncapMana(){ writeInteger(10_000_000, "data", "user_data", "mana_point"); }
    public void plunderDonkay(){ writeInteger(1_000_000, "data", "user_data", "crystal"); }
    public void battleOnTheByroad(){ writeInteger(10_000_000, "data", "user_data", "dew_point"); }

    public int addMissingAdventurers(){
        int count = 0;
        //Compile a list of ID's you have
        List<Integer> ownedIdList = new ArrayList<>();
        JsonArray ownedAdventurers = getFieldAsJsonArray("data", "chara_list");
        for(JsonElement jsonEle : ownedAdventurers){
            JsonObject adventurer = jsonEle.getAsJsonObject();
            ownedIdList.add(adventurer.get("chara_id").getAsInt());
        }

        //Go through a list of all the adventurers in the game
        for(JsonElement jsonEle : adventurerData){
            JsonObject adventurer = jsonEle.getAsJsonObject();
            int id = adventurer.get("IdLong").getAsInt();
            if(!ownedIdList.contains(id)){ //If you don't own this adventurer
                //Construct new unit (Does this unit have a mana spiral?)
                JsonObject newUnit = buildUnit(adventurer);
                //Add it to your roster
                if(newUnit != null){
                    getField("data", "chara_list").getAsJsonArray().add(newUnit);
                    unlockAdventurerStory(id);
                    count++;
                }
            }
        }
        return count;
    }

    public void addItems(){
        JsonArray items = getFieldAsJsonArray("data", "material_list");
        for(JsonElement jsonEle : items){
            JsonObject jsonObj = jsonEle.getAsJsonObject();
            int count = jsonObj.get("quantity").getAsInt();
            if(count <= 30000){
                jsonObj.remove("quantity");
                jsonObj.addProperty("quantity", 30000);
            }
        }
    }

    public void backToTheMines(){
        //for each kscape combo, put new kscape print data for each ele-weapon combo

        String[][] kscapeCombos = KscapeCombos.KSCAPES;
        String[][] kscapeLabels = KscapeCombos.KSCAPE_LABELS;
        int keyIdOffset = 0;
        JsonObject jsonData = getField("data").getAsJsonObject();
        jsonData.remove("talisman_list");
        JsonArray talismans = new JsonArray();
        for(int i = 0; i < kscapeCombos.length; i++){
                //for each ele-wep combo
                for(int j = 0; j < kscapeLabels.length; j++){
                    for(int k = 0; k < kscapeLabels[j].length; k++){
                        talismans.add(buildTalisman(kscapeLabels[j][k], kscapeCombos[i], keyIdOffset));
                        keyIdOffset++;
                    }
                }

        }
        jsonData.add("talisman_list", talismans);

        //delete equipped kscapes, since old kscape ID's will now point to
        //a kscape that no longer exists
        JsonArray partyList = getFieldAsJsonArray("data", "party_list");
        for(JsonElement jsonEle : partyList){
            JsonObject party = jsonEle.getAsJsonObject();
            for(JsonElement jsonEle2 : party.getAsJsonArray("party_setting_list")){
                JsonObject adventurer = jsonEle2.getAsJsonObject();
                adventurer.remove("equip_talisman_key_id");
                adventurer.addProperty("equip_talisman_key_id", 0);
            }
        }
    }

    public int addMissingWeaponSkins(){
        int count = 0;
        List<Integer> ownedWeaponSkinIDs = new ArrayList<>();
        getFieldAsJsonArray("data", "weapon_skin_list").forEach(jsonEle ->
                ownedWeaponSkinIDs.add(jsonEle.getAsJsonObject().get("weapon_skin_id").getAsInt()));
        for(Integer weaponSkinId : weaponSkinSet){
            if(!ownedWeaponSkinIDs.contains(weaponSkinId)){
                JsonObject newWeaponSkin = new JsonObject();
                newWeaponSkin.addProperty("weapon_skin_id", weaponSkinId);
                newWeaponSkin.addProperty("is_new", 1);
                newWeaponSkin.addProperty("gettime", Instant.now().getEpochSecond());
                getFieldAsJsonArray("data", "weapon_skin_list").add(newWeaponSkin);
                count++;
            }
        }
        return count;
    }

}
