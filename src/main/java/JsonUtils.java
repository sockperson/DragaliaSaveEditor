import java.io.*;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

public class JsonUtils {

    private static final int MAX_DRAGON_CAPACITY = 525;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final String path;
    private String basePath = "";
    private final String rsrcPath;
    private boolean toOverwrite = false;

    private Random rng = new Random();

    //savefile
    private JsonObject jsonData;

    //pulled from wiki
    private JsonArray adventurerData;
    private JsonArray dragonsData;
    private JsonArray weaponsData;
    private JsonArray printsData;

    //pulled from datamine
    private JsonArray abilitiesList;

    //Ability Name --> Ability ID
    private HashMap<String, Integer> kscapeAbilityMap = new HashMap<>();
    //Adventurer Title --> Portrait Print ID
    private HashMap<String, Integer> kscapeLabelsMap = new HashMap<>();
    private List<Integer> kscapePortraitIDs = new ArrayList<>();
    //Adventurer ID --> Adventurer Story ID
    private HashMap<Integer, Integer> adventurerStoryMap = new HashMap<>();

    //Adventurer Name --> Adventurer Obj
    private HashMap<String, JsonObject> adventurerMap = new HashMap<>();
    //Dragon Name --> Dragon Obj
    private HashMap<String, JsonObject> dragonMap = new HashMap<>();

    private Set<Integer> weaponSkinSet = new HashSet<>();

    public JsonUtils(String path) {
        this.path = path;
        try {
            basePath = path.substring(0, path.indexOf("savedata"));
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println("savedata.txt not found in directory!");
            System.exit(99);
        }
        rsrcPath = Paths.get(basePath, "src", "resources").toString();
        try {
            this.jsonData = getSaveData().getAsJsonObject();
            readAdventurerData();
            readDragonsData();
            readKscapeData();
            readKscapeLabels();
            readStoryData();
            readWeaponSkinData();
            readWeaponsData();
            readPrintsData();
            readAbilitiesData();
        } catch (IOException e) {
            System.out.println("Unable to read JSON data!");
            System.exit(99);
        }

    }

    public void writeToFile() {
        try {
            String newPath;
            if (isSaveData2Present() && !toOverwrite) {
                int count = 3;
                String fileName = "savedata" + count + ".txt";
                while (new File(Paths.get(basePath, fileName).toString()).exists()) {
                    count++;
                }
                newPath = Paths.get(basePath, fileName).toString();
            } else {
                newPath = Paths.get(basePath, "savedata2.txt").toString();
            }
            FileWriter fileWriter = new FileWriter(newPath);
            GSON.toJson(jsonData, fileWriter);
            fileWriter.flush();
            fileWriter.close();
            System.out.println("Saved output JSON to " + newPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isSaveData2Present() {
        return new File(Paths.get(basePath, "savedata2.txt").toString()).exists();
    }

    public void setOverwrite(boolean toOverwrite) {
        this.toOverwrite = toOverwrite;
    }

    public double addDoubles(double val1, double val2) {
        //sure hope this prevents floating point inaccuracy
        return ((10.0 * val1) + (10.0 * val2)) / 10;
    }

    public JsonObject getJsonData() {
        return jsonData;
    }

    private JsonObject getSaveData() throws IOException {
        if(!new File(path).exists()){
            System.out.println("savedata.txt not found at path: '" + path + "'!");
            throw new IOException();
        }
        JsonReader reader = new JsonReader(new FileReader(path));
        return GSON.fromJson(reader, JsonObject.class);
    }

    private JsonObject getJsonObject(String more) throws IOException {
        JsonReader reader = new JsonReader(new FileReader(Paths.get(rsrcPath, more).toString()));
        return GSON.fromJson(reader, JsonObject.class);
    }

    private JsonArray getJsonArray(String more) throws IOException {
        JsonReader reader = new JsonReader(new FileReader(Paths.get(rsrcPath, more).toString()));
        return GSON.fromJson(reader, JsonArray.class);
    }

    public String getFieldAsString(String... memberNames) {
        return getField(memberNames).getAsString();
    }

    public int getFieldAsInt(String... memberNames) {
        return getField(memberNames).getAsInt();
    }

    public JsonArray getFieldAsJsonArray(String... memberNames) {
        return getField(memberNames).getAsJsonArray();
    }

    private void writeInteger(int value, String... memberNames) {
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

    private JsonElement getField(String... memberNames) {
        JsonElement jsonEle = jsonData;
        for (String memberName : memberNames) {
            if (jsonEle.isJsonObject()) {
                jsonEle = jsonEle.getAsJsonObject().get(memberName);
            }
        }
        return jsonEle;
    }

    private int getSum(JsonObject src, String... memberNames) {
        int sum = 0;
        for (String memberName : memberNames) {
            sum += src.get(memberName).getAsInt();
        }
        return sum;
    }

    private void readAdventurerData() throws IOException {
        adventurerData = getJsonArray("adventurers.json");
        for (JsonElement jsonEle : adventurerData) {
            JsonObject adv = jsonEle.getAsJsonObject();
            String name = adv.get("FullName").getAsString().toUpperCase();
            if (name.equals("PUPPY")) {
                continue; //dog check
            }
            adventurerMap.put(name, adv);
        }
    }

    private void readDragonsData() throws IOException {
        dragonsData = getJsonArray("dragons.json");
        for (JsonElement jsonEle : dragonsData) {
            JsonObject drg = jsonEle.getAsJsonObject();
            String name = drg.get("FullName").getAsString();
            if (drg.get("IsPlayable").getAsInt() == 0) {
                continue;
            }
            switch (name) { //add aliases
                case "Puppy":
                    continue; //no dogs allowed
                case "Ars\u00e8ne":
                    dragonMap.put("ARSENE", drg);
                    break;
                case "Gala Cat S\u00ecth":
                    dragonMap.put("GALA CAT SITH", drg);
                    break;
                case "Lumi\u00e8re Pandora":
                    dragonMap.put("LUMIERE PANDORA", drg);
                    break;
                case "Poli\u02bbahu":
                    dragonMap.put("POLI'AHU", drg);
                    break;
                case "Summer Cat S\u00ecth":
                    dragonMap.put("SUMMER CAT SITH", drg);
                    break;
            }
            dragonMap.put(name.toUpperCase(), drg);
        }
    }

    private void readAbilitiesData() throws IOException {
        abilitiesList = getJsonArray("abilities.json");
    }

    private void readWeaponsData() throws IOException {
        weaponsData = getJsonArray("weapons.json");
    }

    private void readPrintsData() throws IOException {
        printsData = getJsonArray("prints.json");
    }

    private void readKscapeData() throws IOException {
        JsonObject kscapeJson = getJsonObject("kscape.json");
        for (Map.Entry<String, JsonElement> entry : kscapeJson.entrySet()) {
            kscapeAbilityMap.put(entry.getKey(), entry.getValue().getAsInt());
        }
    }

    private void readKscapeLabels() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(Paths.get(rsrcPath, "kscapeLabels.txt").toFile()));
        String out = br.readLine();
        while (out != null) {
            String[] split1 = out.split("\t");
            int id = Integer.parseInt(split1[0].split("_")[2]);
            String label = split1[2];
            kscapeLabelsMap.put(label, id);
            kscapePortraitIDs.add(id);
            out = br.readLine();
        }
    }

    private void readStoryData() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(Paths.get(rsrcPath, "UnitStory.txt").toFile()));
        br.readLine(); //ignore first line of txt file
        String out = br.readLine();
        while (out != null) {
            String[] fields = out.split(",");
            int storyID = Integer.parseInt(fields[0]);
            int advID = Integer.parseInt(fields[7]);
            out = br.readLine();
            if (adventurerStoryMap.containsKey(advID)) {
                continue; //ignore if unit already has a story added
            }
            adventurerStoryMap.put(advID, storyID);
        }
    }

    private void readWeaponSkinData() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(Paths.get(rsrcPath, "WeaponSkin.txt").toFile()));
        br.readLine(); //ignore first line of txt file
        String out = br.readLine();
        while (out != null) {
            String[] fields = out.split(",");
            int skinID = Integer.parseInt(fields[0]);
            boolean isPlayable = fields[13].equals("1");
            out = br.readLine();
            if (!isPlayable) {
                continue; //ignore if skin is unplayable
            }
            weaponSkinSet.add(skinID);
        }
    }

    private void addAdventurerEncyclopediaBonus(JsonObject adv) {
        boolean hasManaSpiral = adv.get("ManaSpiralDate") != null;
        double bonus = hasManaSpiral ? 0.3 : 0.2;
        int elementID = adv.get("ElementalTypeId").getAsInt();
        JsonArray albumBonuses = getFieldAsJsonArray("data", "fort_bonus_list", "chara_bonus_by_album");
        for (JsonElement jsonEle : albumBonuses) {
            JsonObject albumBonus = jsonEle.getAsJsonObject();
            if (albumBonus.get("elemental_type").getAsInt() == elementID) {
                double hp = albumBonus.get("hp").getAsDouble();
                double attack = albumBonus.get("attack").getAsDouble();
                double resultHp = addDoubles(hp, bonus);
                double resultStr = addDoubles(attack, bonus);
                albumBonus.remove("hp");
                albumBonus.remove("attack");
                albumBonus.addProperty("hp", resultHp);
                albumBonus.addProperty("attack", resultStr);
            }
        }
    }

    private void addDragonEncyclopediaBonus(JsonObject dragon) {
        boolean has5UB = dragon.get("MaxLimitBreakCount").getAsInt() == 5;
        double hpBonus = has5UB ? 0.3 : 0.2;
        double strBonus = 0.1;
        int elementID = dragon.get("ElementalTypeId").getAsInt();
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

    private void addWeaponBonus(JsonObject weapon) {
        String weaponSeries = weapon.get("WeaponSeries").getAsString();
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
        int weaponTypeId = weapon.get("WeaponTypeId").getAsInt();
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

    private void addTalisman(String label, int id1, int id2, int id3) {
        JsonObject out = new JsonObject();

        int keyIdMax = 0;   //need to keep track of keyId
        //Obtain keyIdMax
        JsonArray ownedTalismans = getFieldAsJsonArray("data", "talisman_list");
        for (JsonElement jsonEle : ownedTalismans) {
            JsonObject ownedTalisman = jsonEle.getAsJsonObject();
            keyIdMax = Math.max(keyIdMax, ownedTalisman.get("talisman_key_id").getAsInt());
        }

        int portraitID = kscapeLabelsMap.get(label);

        out.addProperty("talisman_key_id", keyIdMax + 200);
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

    private JsonObject buildTalisman(String label, String[] combo, int keyIdOffset) {
        JsonObject out = new JsonObject();
        int abilityId1 = 0;
        int abilityId2 = 0;
        int abilityId3 = 0;
        for (int i = 0; i < combo.length; i++) {
            int id = kscapeAbilityMap.get(combo[i]);
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

    private JsonObject buildRandomTalisman(int id, int keyIdOffset) {
        JsonObject out = new JsonObject();
        int totalAbilitiesCount = abilitiesList.size();

        out.addProperty("talisman_key_id", 200000 + 100 * keyIdOffset);
        out.addProperty("talisman_id", id);
        out.addProperty("is_lock", 0);
        out.addProperty("is_new", 1);
        out.addProperty("talisman_ability_id_1", abilitiesList.get(rng.nextInt(totalAbilitiesCount)).getAsJsonObject().get("Id").getAsInt());
        out.addProperty("talisman_ability_id_2", abilitiesList.get(rng.nextInt(totalAbilitiesCount)).getAsJsonObject().get("Id").getAsInt());
        out.addProperty("talisman_ability_id_3", abilitiesList.get(rng.nextInt(totalAbilitiesCount)).getAsJsonObject().get("Id").getAsInt());
        out.addProperty("additional_hp", 0);
        out.addProperty("additional_attack", 0);
        out.addProperty("gettime", Instant.now().getEpochSecond());

        return out;
    }

    //Returns a built adventurer in savedata.txt format
    private JsonObject buildUnit(JsonObject adventurerData) {
        JsonObject out = new JsonObject();
        if (adventurerData.get("FullName").getAsString().equals("Puppy")) {
            return null; //no dogs allowed
        }
        boolean hasManaSpiral = adventurerData.get("ManaSpiralDate") != null;
        int hp, str;
        if (hasManaSpiral) {
            hp = getSum(adventurerData, "AddMaxHp1", "PlusHp0", "PlusHp1", "PlusHp2", "PlusHp3", "PlusHp4", "PlusHp5", "McFullBonusHp5");
            str = getSum(adventurerData, "AddMaxAtk1", "PlusAtk0", "PlusAtk1", "PlusAtk2", "PlusAtk3", "PlusAtk4", "PlusAtk5", "McFullBonusAtk5");
        } else {
            hp = getSum(adventurerData, "MaxHp", "PlusHp0", "PlusHp1", "PlusHp2", "PlusHp3", "PlusHp4", "McFullBonusHp5");
            str = getSum(adventurerData, "MaxAtk", "PlusAtk0", "PlusAtk1", "PlusAtk2", "PlusAtk3", "PlusAtk4", "McFullBonusAtk5");
        }
        JsonArray mc = new JsonArray();
        int mcLevel = hasManaSpiral ? 70 : 50;
        for (int i = 1; i <= mcLevel; i++) {
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

    //Returns a built dragon in savedata.txt format
    private JsonObject buildDragon(JsonObject dragonData, int keyIdMin, int keyIdOffset) {
        JsonObject out = new JsonObject();
        boolean has5UB = dragonData.get("MaxLimitBreakCount").getAsInt() == 5;
        int xp = 0;
        int level = 0;
        int rarity = dragonData.get("Rarity").getAsInt();
        switch (rarity) {
            case 3:
                xp = 0; //todo? idk lol
                level = 60;
                break;
            case 4:
                xp = 625170;
                level = 80;
                break;
            case 5:
                if (has5UB) {
                    xp = 3365620;
                    level = 120;
                } else {
                    xp = 1240020;
                    level = 100;
                }
                break;
        }
        int a1Level = has5UB ?
                6 : dragonData.get("Abilities15").getAsInt() != 0 ?
                5 : 0;
        int a2Level = has5UB ?
                6 : dragonData.get("Abilities25").getAsInt() != 0 ?
                5 : 0;

        out.addProperty("dragon_key_id", keyIdMin + 200 * keyIdOffset);
        out.addProperty("dragon_id", dragonData.get("Id").getAsInt());
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
        return out;
    }

    private JsonObject buildDragonAlbumData(JsonObject dragonData) {
        JsonObject out = new JsonObject();
        boolean has5UB = dragonData.get("MaxLimitBreakCount").getAsInt() == 5;
        int level = 0;
        int rarity = dragonData.get("Rarity").getAsInt();
        switch (rarity) {
            case 3:
                level = 60;
                break;
            case 4:
                level = 80;
                break;
            case 5:
                if (has5UB) {
                    level = 120;
                } else {
                    level = 100;
                }
                break;
        }
        out.addProperty("dragon_id", dragonData.get("Id").getAsInt());
        out.addProperty("max_level", level);
        out.addProperty("max_limit_break_count", has5UB ? 5 : 4);
        return out;
    }

    private JsonObject buildWeapon(JsonObject weaponData) {
        JsonObject out = new JsonObject();
        String weaponSeries = weaponData.get("WeaponSeries").getAsString();
        int rarity = weaponData.get("Rarity").getAsInt();
        if (rarity == 1) {
            return null; //unused weapons
        }

        boolean isNullElement = weaponData.get("ElementalTypeId").getAsInt() == 99;

        int level = 1;
        int unbinds = 0;
        int refines = 0;
        int fiveStarSlotCount = 0;
        int sindomSlotCount = 0;
        //can't make copies of Mega Man collab weapons apparently...
        int copiesCount = weaponData.get("Name").getAsString().contains("Mega") ? 1 : 4;
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
                break;
            //too lazy to find out numbers for these

        }
        //too lazy to figure out mapping for these abilities + no one cares honestly
        JsonArray voidWeaponAbilities = new JsonArray();
        for (int i = 0; i < 15; i++) {
            voidWeaponAbilities.add(0);
        }

        out.addProperty("weapon_body_id", weaponData.get("Id").getAsInt());         //ID
        out.addProperty("buildup_count", level);                                    //level
        out.addProperty("limit_break_count", unbinds);                              //unbinds
        out.addProperty("limit_over_count", refines);                               //refines
        out.addProperty("equipable_count", copiesCount);                            //equip count
        out.addProperty("additional_crest_slot_type_1_count", fiveStarSlotCount);   //5* slot count
        out.addProperty("additional_crest_slot_type_2_count", 0);
        out.addProperty("additional_crest_slot_type_3_count", sindomSlotCount);     //sindom slot count
        out.addProperty("additional_effect_count", 0);                         //?
        out.add("unlock_weapon_passive_ability_no_list", voidWeaponAbilities);      //void weapon abilities?
        out.addProperty("fort_passive_chara_weapon_buildup_count", 1);        //weapon bonus
        out.addProperty("is_new", 1);
        out.addProperty("gettime", Instant.now().getEpochSecond());
        return out;
    }

    private JsonObject buildWyrmprint(JsonObject printData) {
        JsonObject out = new JsonObject();
        int rarity = printData.get("Rarity").getAsInt();
        int level = 1;
        int augmentCount = 0;
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

        out.addProperty("ability_crest_id", printData.get("Id").getAsInt());
        out.addProperty("buildup_count", level);
        out.addProperty("limit_break_count", 4);
        out.addProperty("equipable_count", 4);
        out.addProperty("hp_plus_count", augmentCount);
        out.addProperty("attack_plus_count", augmentCount);
        out.addProperty("is_new", 1);
        out.addProperty("is_favorite", 0);
        out.addProperty("gettime", Instant.now().getEpochSecond());
        return out;
    }

    private void unlockAdventurerStory(int id) {
        if (id == 10750102) {
            return; //Mega Man has no adventurer story
        }
        int storyID = adventurerStoryMap.get(id);
        JsonObject story = new JsonObject();
        story.addProperty("unit_story_id", storyID);
        story.addProperty("is_read", 0);
        getFieldAsJsonArray("data", "unit_story_list").add(story);
    }

    /// Util Methods \\\
    public void uncapMana() {
        writeInteger(10_000_000, "data", "user_data", "mana_point");
    }

    public void plunderDonkay() {
        writeInteger(1_000_000, "data", "user_data", "crystal");
    }

    public void battleOnTheByroad() {
        writeInteger(10_000_000, "data", "user_data", "dew_point");
    }

    public int addMissingAdventurers() {
        int count = 0;
        //Compile a list of ID's you have
        List<Integer> ownedIdList = new ArrayList<>();
        JsonArray ownedAdventurers = getFieldAsJsonArray("data", "chara_list");
        for (JsonElement jsonEle : ownedAdventurers) {
            JsonObject adventurer = jsonEle.getAsJsonObject();
            ownedIdList.add(adventurer.get("chara_id").getAsInt());
        }

        //Go through a list of all the adventurers in the game
        for (JsonElement jsonEle : adventurerData) {
            JsonObject adventurer = jsonEle.getAsJsonObject();
            int id = adventurer.get("IdLong").getAsInt();
            if (!ownedIdList.contains(id)) { //If you don't own this adventurer
                //Construct new unit (Does this unit have a mana spiral?)
                JsonObject newUnit = buildUnit(adventurer);
                //Add it to your roster
                if (newUnit != null) {
                    getField("data", "chara_list").getAsJsonArray().add(newUnit);
                    unlockAdventurerStory(id);
                    addAdventurerEncyclopediaBonus(adventurer);
                    count++;
                }
            }
        }
        return count;
    }

    public void addAdventurer(String advName) {
        JsonObject advData = adventurerMap.get(advName);
        if (advData == null) {
            System.out.println("Can't find adventurer with name '" + advName + "'. Try again!");
            return;
        }
        //Compile a list of ID's you have
        Set<Integer> ownedIdSet = new HashSet<>();
        getFieldAsJsonArray("data", "chara_list").forEach(jsonEle ->
                ownedIdSet.add(jsonEle.getAsJsonObject().get("chara_id").getAsInt()));
        int id = advData.get("IdLong").getAsInt();
        if (ownedIdSet.contains(id)) {
            System.out.println("You already own '" + advName + "'!");
            return;
        }
        //Construct new unit (Does this unit have a mana spiral?)
        JsonObject newUnit = buildUnit(advData);
        //Add it to your roster
        if (newUnit != null) {
            getField("data", "chara_list").getAsJsonArray().add(newUnit);
            unlockAdventurerStory(id);
            addAdventurerEncyclopediaBonus(advData);
            System.out.println("Added '" + advName + "'!");
        }
    }

    public int addMissingWyrmprints() {
        int count = 0;
        //Compile a list of ID's you have
        Set<Integer> ownedIdSet = new HashSet<>();
        getFieldAsJsonArray("data", "ability_crest_list").forEach(jsonEle ->
                ownedIdSet.add(jsonEle.getAsJsonObject().get("ability_crest_id").getAsInt()));

        //Go through a list of all the wyrmprints in the game
        for (JsonElement jsonEle : printsData) {
            JsonObject wyrmprint = jsonEle.getAsJsonObject();
            int id = wyrmprint.get("Id").getAsInt();
            if (!ownedIdSet.contains(id)) { //If you don't own this print
                //Construct new print
                JsonObject newPrint = buildWyrmprint(wyrmprint);
                //Add it to your inventory
                getField("data", "ability_crest_list").getAsJsonArray().add(newPrint);
                count++;
            }
        }
        return count;
    }

    //return response message
    public String addMissingDragons(boolean toExcludeLowRarityDragons) {
        int count = 0;
        int expandAmount = 0;
        int keyIdMax = 0;   //need to keep track of keyId so we don't run into dupe keyId issue when adding new dragons...
        //Compile a list of ID's you have
        Set<Integer> ownedIdSet = new HashSet<>();
        JsonArray ownedDragons = getFieldAsJsonArray("data", "dragon_list");
        for (JsonElement jsonEle : ownedDragons) {
            JsonObject dragon = jsonEle.getAsJsonObject();
            ownedIdSet.add(dragon.get("dragon_id").getAsInt());
            keyIdMax = Math.max(keyIdMax, dragon.get("dragon_key_id").getAsInt());
        }
        //Compile a list of ID's from your encyclopedia
        Set<Integer> albumIDSet = new HashSet<>();
        getFieldAsJsonArray("data", "album_dragon_list").forEach(jsonEle ->
                albumIDSet.add(jsonEle.getAsJsonObject().get("dragon_id").getAsInt()));

        //Go through a list of all the dragons in the game
        for (JsonElement jsonEle : dragonsData) {
            JsonObject dragon = jsonEle.getAsJsonObject();
            int id = dragon.get("Id").getAsInt();
            int rarity = dragon.get("Rarity").getAsInt();
            boolean isPlayable = dragon.get("IsPlayable").getAsInt() == 1;
            if (!isPlayable || (toExcludeLowRarityDragons && (rarity == 3 || rarity == 4))) {
                continue; //ignore un-playable dragons or maybe low rarity dragons
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
                        JsonObject dragonBond = new JsonObject();
                        dragonBond.addProperty("dragon_id", id);
                        dragonBond.addProperty("gettime", Instant.now().getEpochSecond());
                        dragonBond.addProperty("reliability_level", 1);
                        dragonBond.addProperty("reliability_total_exp", 0);
                        dragonBond.addProperty("last_contact_time", Instant.now().getEpochSecond());
                        getField("data", "dragon_reliability_list").getAsJsonArray().add(dragonBond);
                    }
                }
                count++;
            }
        }
        return expandAmount == 0 ?
                "Added " + count + " missing dragons." :
                "Added " + count + " missing dragons. Dragon inventory capacity was raised by " + expandAmount + ".";
    }

    public void addDragon(String drgName) {
        int expandAmount = 0;
        int keyIdMax = 0;   //need to keep track of keyId so we don't run into dupe keyId issue when adding new dragons...

        //Obtain keyIdMax
        JsonArray ownedDragons = getFieldAsJsonArray("data", "dragon_list");
        for (JsonElement jsonEle : ownedDragons) {
            JsonObject dragon = jsonEle.getAsJsonObject();
            keyIdMax = Math.max(keyIdMax, dragon.get("dragon_key_id").getAsInt());
        }
        //Compile a list of ID's from your encyclopedia
        Set<Integer> albumIDSet = new HashSet<>();
        getFieldAsJsonArray("data", "album_dragon_list").forEach(jsonEle ->
                albumIDSet.add(jsonEle.getAsJsonObject().get("dragon_id").getAsInt()));

        JsonObject drgData = dragonMap.get(drgName);
        if (drgData == null) {
            System.out.println("Can't find dragon with name '" + drgName + "'. Try again!");
            return;
        }
        int id = drgData.get("Id").getAsInt();

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
                JsonObject dragonBond = new JsonObject();
                dragonBond.addProperty("dragon_id", id);
                dragonBond.addProperty("gettime", Instant.now().getEpochSecond());
                dragonBond.addProperty("reliability_level", 1);
                dragonBond.addProperty("reliability_total_exp", 0);
                dragonBond.addProperty("last_contact_time", Instant.now().getEpochSecond());
                getField("data", "dragon_reliability_list").getAsJsonArray().add(dragonBond);
            }
        }

        String out = expandAmount == 0 ?
                "Added '" + drgName + "'!" :
                "Added '" + drgName + "'! Dragon inventory capacity was raised by " + expandAmount + ".";
        System.out.println(out);
    }

    public void addItems() {
        JsonArray items = getFieldAsJsonArray("data", "material_list");
        for (JsonElement jsonEle : items) {
            JsonObject jsonObj = jsonEle.getAsJsonObject();
            int count = jsonObj.get("quantity").getAsInt();
            if (count <= 30000) {
                jsonObj.remove("quantity");
                jsonObj.addProperty("quantity", 30000);
            }
        }
    }

    public void backToTheMines() {
        //for each kscape combo, put new kscape print data for each ele-weapon combo

        String[][] kscapeCombos = KscapeCombos.KSCAPES;
        String[][] kscapeLabels = KscapeCombos.KSCAPE_LABELS;
        int keyIdOffset = 1;
        JsonObject jsonData = getField("data").getAsJsonObject();
        jsonData.remove("talisman_list");
        JsonArray talismans = new JsonArray();
        for (int i = 0; i < kscapeCombos.length; i++) {
            //for each ele-wep combo
            for (int j = 0; j < kscapeLabels.length; j++) {
                for (int k = 0; k < kscapeLabels[j].length; k++) {
                    talismans.add(buildTalisman(kscapeLabels[j][k], kscapeCombos[i], keyIdOffset));
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

    public int addMissingWeaponSkins() {
        int count = 0;
        List<Integer> ownedWeaponSkinIDs = new ArrayList<>();
        getFieldAsJsonArray("data", "weapon_skin_list").forEach(jsonEle ->
                ownedWeaponSkinIDs.add(jsonEle.getAsJsonObject().get("weapon_skin_id").getAsInt()));
        for (Integer weaponSkinId : weaponSkinSet) {
            if (!ownedWeaponSkinIDs.contains(weaponSkinId)) {
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

    public int addMissingWeapons() {
        int count = 0;
        //Compile a set of ID's you have
        Set<Integer> ownedIdSet = new HashSet<>();
        getFieldAsJsonArray("data", "weapon_body_list").forEach(jsonEle ->
                ownedIdSet.add(jsonEle.getAsJsonObject().get("weapon_body_id").getAsInt()));

        //Go through a list of all the weapons in the game
        for (JsonElement jsonEle : weaponsData) {
            JsonObject weapon = jsonEle.getAsJsonObject();
            int id = weapon.get("Id").getAsInt();
            if (!ownedIdSet.contains(id)) { //If you don't own this weapon
                //Construct new weapon
                JsonObject newWeapon = buildWeapon(weapon);
                //Add it to your inventory
                if (newWeapon != null) {
                    getField("data", "weapon_body_list").getAsJsonArray().add(newWeapon);
                    addWeaponBonus(weapon);
                    count++;
                }
            }
        }
        return count;
    }

    //Hacked options

    public void kscapeRandomizer() {
        JsonArray talismans = new JsonArray();

        for (int i = 1; i <= 500; i++) {
            //get random adventurer portrait ID
            int portraitListSize = kscapePortraitIDs.size();
            int portraitID = kscapePortraitIDs.get(rng.nextInt(portraitListSize));

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

    public void addGoofyKscapes() {
        addTalisman("Born Ruler", 805, 806, 721); //(Water) Skill Recharge +65%, Skill Prep +100%
        addTalisman("The Blazewolf", 100100205, 1237, 100100204); //ar20 + flame ar20 + ar10
        addTalisman("Summertime Boar", 100100205, 1225, 100100204); //ar20 + hp70 ar10 + ar10
    }
}
