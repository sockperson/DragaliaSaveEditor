import java.io.*;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import meta.*;

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

    //pulled from datamine
    private JsonArray abilitiesList;

    //Ability Name --> Ability ID
    private HashMap<String, Integer> kscapeAbilityMap = new HashMap<>();
    //Adventurer Title --> Portrait Print ID
    private HashMap<String, Integer> kscapeLabelsMap = new HashMap<>();
    private List<Integer> kscapePortraitIDs = new ArrayList<>();
    //Adventurer ID --> Adventurer Story IDs
    private HashMap<Integer, List<Integer>> adventurerStoryMap = new HashMap<>();

    //Maps
    private HashMap<Integer, AdventurerMeta> idToAdventurer = new HashMap<>();
    private HashMap<String, AdventurerMeta> nameToAdventurer = new HashMap<>();

    private HashMap<Integer, DragonMeta> idToDragon = new HashMap<>();
    private HashMap<String, DragonMeta> nameToDragon = new HashMap<>();

    private HashMap<Integer, WeaponMeta> idToWeapon = new HashMap<>();
    private HashMap<Integer, WyrmprintMeta> idToPrint = new HashMap<>();
    private HashMap<Integer, FacilityMeta> idToFacility = new HashMap<>();

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
            readFacilitiesData();
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

    public JsonObject getFieldAsJsonObject(String... memberNames) {
        return getField(memberNames).getAsJsonObject();
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
        for (JsonElement jsonEle : getJsonArray("adventurers.json")) {
            JsonObject adv = jsonEle.getAsJsonObject();
            String name = adv.get("FullName").getAsString().toUpperCase();
            if (name.equals("PUPPY")) {
                continue; //dog check
            }
            //fill idToAdventurer map
            JsonObject out = new JsonObject();
            boolean hasManaSpiral = adv.get("ManaSpiralDate") != null;
            int hp, str;
            int id = adv.get("IdLong").getAsInt();
            if (hasManaSpiral) {
                hp = getSum(adv, "AddMaxHp1", "PlusHp0", "PlusHp1", "PlusHp2", "PlusHp3", "PlusHp4", "PlusHp5", "McFullBonusHp5");
                str = getSum(adv, "AddMaxAtk1", "PlusAtk0", "PlusAtk1", "PlusAtk2", "PlusAtk3", "PlusAtk4", "PlusAtk5", "McFullBonusAtk5");
            } else {
                hp = getSum(adv, "MaxHp", "PlusHp0", "PlusHp1", "PlusHp2", "PlusHp3", "PlusHp4", "McFullBonusHp5");
                str = getSum(adv, "MaxAtk", "PlusAtk0", "PlusAtk1", "PlusAtk2", "PlusAtk3", "PlusAtk4", "McFullBonusAtk5");
            }
            AdventurerMeta unit = new AdventurerMeta(name, id,
                    adv.get("ElementalTypeId").getAsInt(), hp, str,adv.get("MaxLimitBreakCount").getAsInt(),
                    adv.get("EditSkillCost").getAsInt() != 0, hasManaSpiral);
            idToAdventurer.put(id, unit);
            nameToAdventurer.put(name, unit);
        }
    }

    private void readDragonsData() throws IOException {
        for (JsonElement jsonEle : getJsonArray("dragons.json")) {
            JsonObject drg = jsonEle.getAsJsonObject();
            String name = drg.get("FullName").getAsString();
            if (drg.get("IsPlayable").getAsInt() == 0) {
                continue;
            }
            //fill idToDragon
            boolean has5UB = drg.get("MaxLimitBreakCount").getAsInt() == 5;
            int id = drg.get("Id").getAsInt();
            int rarity = drg.get("Rarity").getAsInt();
            int a1Level = has5UB ?
                    6 : drg.get("Abilities15").getAsInt() != 0 ?
                    5 : 0;
            int a2Level = has5UB ?
                    6 : drg.get("Abilities25").getAsInt() != 0 ?
                    5 : 0;
            DragonMeta unit = new DragonMeta(name, id, drg.get("ElementalTypeId").getAsInt(),
                a1Level, a2Level, rarity, has5UB);
            idToDragon.put(id, unit);
            //
            switch (name) { //add aliases
                case "Puppy":
                    continue; //no dogs allowed
                case "Ars\u00e8ne":
                    nameToDragon.put("ARSENE", unit);
                    break;
                case "Gala Cat S\u00ecth":
                    nameToDragon.put("GALA CAT SITH", unit);
                    break;
                case "Lumi\u00e8re Pandora":
                    nameToDragon.put("LUMIERE PANDORA", unit);
                    break;
                case "Poli\u02bbahu":
                    nameToDragon.put("POLI'AHU", unit);
                    break;
                case "Summer Cat S\u00ecth":
                    nameToDragon.put("SUMMER CAT SITH", unit);
                    break;
            }
            nameToDragon.put(name.toUpperCase(), unit);
        }
    }

    private void readAbilitiesData() throws IOException {
        abilitiesList = getJsonArray("abilities.json");
    }

    private void readFacilitiesData() throws IOException {
        HashMap<Integer, JsonObject> facilitiesMap = new HashMap<>();
        //pull
        getJsonArray("facilities.json").forEach(jsonEle -> {
            JsonObject facility = jsonEle.getAsJsonObject();
            int id = facility.get("Id").getAsInt();
            facilitiesMap.put(id, facility);
        });

        BufferedReader br = new BufferedReader(new FileReader(Paths.get(rsrcPath, "FortPlantDetail.txt").toFile()));
        br.readLine(); //ignore first line
        String out = br.readLine();

        int id = 1337;
        int maxLevel = -1;
        //this code sucks balls who wrote it
        while (out != null) {
            String[] split = out.split(",");
            String longID = split[0]; //longID is facility ID AAAAAA + level BB

            int newId = Integer.parseInt(longID.substring(0,6));
            if(id != newId){ //starting to read thru data for next facility... assign facility max level to list
                if(id != 1337){
                    boolean isResourceFacility = id == 100101 || id == 100201 || id == 100301; //halidom, mine, dragontree
                    FacilityMeta fac = new FacilityMeta(facilitiesMap.get(id).get("Name").getAsString(), id, maxLevel,
                            isResourceFacility, facilitiesMap.get(id).get("Available").getAsInt());
                    idToFacility.put(id, fac);
                    maxLevel = -1;
                }
                id = newId;
            }
            maxLevel = Math.max(Integer.parseInt(split[3]), maxLevel);
            out = br.readLine();
        }
    }

    private void readWeaponsData() throws IOException {
        for(JsonElement jsonEle : getJsonArray("weapons.json")){
            JsonObject weaponData = jsonEle.getAsJsonObject();
            int id = weaponData.get("Id").getAsInt();
            WeaponMeta weapon = new WeaponMeta(weaponData.get("Name").getAsString(), id,
                    weaponData.get("ElementalTypeId").getAsInt(), weaponData.get("WeaponTypeId").getAsInt(),
                    weaponData.get("WeaponSeries").getAsString(), weaponData.get("Rarity").getAsInt());
            idToWeapon.put(id, weapon);
        }
    }

    private void readPrintsData() throws IOException {
        for(JsonElement jsonEle : getJsonArray("prints.json")){
            JsonObject printData = jsonEle.getAsJsonObject();
            int id = printData.get("Id").getAsInt();
            WyrmprintMeta print = new WyrmprintMeta(printData.get("Name").getAsString(),
                    id, printData.get("Rarity").getAsInt());
            idToPrint.put(id, print);
        }
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
        for(Map.Entry<String, JsonElement> entry : getJsonObject("CharaStories.json").entrySet()){
            int id = Integer.parseInt(entry.getKey());
            JsonObject stories = entry.getValue().getAsJsonObject();
            List<Integer> storyIDs = new ArrayList<>();
            storyIDs.add(stories.get("0").getAsInt());
            storyIDs.add(stories.get("1").getAsInt());
            storyIDs.add(stories.get("2").getAsInt());
            storyIDs.add(stories.get("3").getAsInt());
            storyIDs.add(stories.get("4").getAsInt());
            adventurerStoryMap.put(id, storyIDs);
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

    private void addAdventurerEncyclopediaBonus(AdventurerMeta adv) {
        boolean hasManaSpiral = adv.hasManaSpiral();
        double bonus = hasManaSpiral ? 0.3 : 0.2;
        int elementID = adv.getElementId();
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

    private void addAdventurerEncyclopediaBonus(int elementId, double hpBonus, double strBonus) {
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

    private void addDragonEncyclopediaBonus(DragonMeta dragon) {
        boolean has5UB = dragon.has5UB();
        double hpBonus = has5UB ? 0.3 : 0.2;
        double strBonus = 0.1;
        int elementID = dragon.getElementId();
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

    private void addDragonEncyclopediaBonus(int elementID, double hpBonus, double strBonus) {
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

    private void addWeaponBonus(WeaponMeta weapon) {
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
    private JsonObject buildUnit(AdventurerMeta adventurerData, int getTime) {
        JsonObject out = new JsonObject();
        if (adventurerData.getName().equals("Puppy")) {
            return null; //no dogs allowed
        }
        boolean hasManaSpiral = adventurerData.hasManaSpiral();
        JsonArray mc = new JsonArray();
        int mcLevel = hasManaSpiral ? 70 : 50;
        for (int i = 1; i <= mcLevel; i++) {
            mc.add(i);
        }
        out.addProperty("chara_id", adventurerData.getId());
        out.addProperty("rarity", 5);
        out.addProperty("exp", hasManaSpiral ? 8866950 : 1191950);
        out.addProperty("level", hasManaSpiral ? 100 : 80);
        out.addProperty("additional_max_level", hasManaSpiral ? 20 : 0);
        out.addProperty("hp_plus_count", 100);
        out.addProperty("attack_plus_count", 100);
        out.addProperty("limit_break_count", adventurerData.getBaseRarity());
        out.addProperty("is_new", 1);
        out.addProperty("gettime", getTime == -1 ? Instant.now().getEpochSecond() : getTime);
        out.addProperty("skill_1_level", hasManaSpiral ? 4 : 3);
        out.addProperty("skill_2_level", hasManaSpiral ? 3 : 2);
        out.addProperty("ability_1_level", hasManaSpiral ? 3 : 2);
        out.addProperty("ability_2_level", hasManaSpiral ? 3 : 2);
        out.addProperty("ability_3_level", 2);
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
        return out;
    }

    //Returns a built dragon in savedata.txt format
    private JsonObject buildDragon(DragonMeta dragonData, int keyIdMin, int keyIdOffset) {
        JsonObject out = new JsonObject();
        boolean has5UB = dragonData.has5UB();
        int xp = dragonData.getMaxXp();
        int level = dragonData.getMaxLevel();
        int rarity = dragonData.getRarity();

        int a1Level = dragonData.getA1Max();
        int a2Level = dragonData.getA2Max();

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
        return out;
    }

    //Returns a built dragon in savedata.txt format
    private JsonObject buildDragon2(DragonMeta dragonData, int keyId, int getTime) {
        JsonObject out = new JsonObject();
        boolean has5UB = dragonData.has5UB();
        int xp = 0;
        int level = 0;
        int rarity = dragonData.getRarity();
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
        int a1Level = dragonData.getA1Max();
        int a2Level = dragonData.getA2Max();

        out.addProperty("dragon_key_id", keyId);
        out.addProperty("dragon_id", dragonData.getId());
        out.addProperty("level", level);
        out.addProperty("hp_plus_count", 50);
        out.addProperty("attack_plus_count", 50);
        out.addProperty("exp", xp);
        out.addProperty("is_lock", 0);
        out.addProperty("is_new", 1);
        out.addProperty("get_time", getTime);
        out.addProperty("skill_1_level", 2);
        out.addProperty("ability_1_level", a1Level);
        out.addProperty("ability_2_level", a2Level);
        out.addProperty("limit_break_count", has5UB ? 5 : 4);
        return out;
    }

    private JsonObject buildDragonAlbumData(DragonMeta dragonData) {
        JsonObject out = new JsonObject();
        boolean has5UB = dragonData.has5UB();
        int level = 0;
        int rarity = dragonData.getRarity();
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
        out.addProperty("dragon_id", dragonData.getId());
        out.addProperty("max_level", level);
        out.addProperty("max_limit_break_count", has5UB ? 5 : 4);
        return out;
    }

    private JsonObject buildFacility(FacilityMeta fac, int keyIdMin, int keyIdOffset, int x, int y){
        JsonObject out = new JsonObject();

        boolean isResourceFacility = false;
        int level = fac.getMaxLevel();
        int id = fac.getId();
        switch(id){
            case 100101: //Halidom
            case 100201: //Rupie Mine
            case 100301: //Dragontree
                isResourceFacility = true;
        }
        String detailId = id + "" + (level > 9 ? level : "0" + level); //AAAAAABB

        out.addProperty("build_id", keyIdMin + keyIdOffset * 200);  //key ID
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
        out.addProperty("last_income_time", isResourceFacility ? 200000 : -1);  //resource facility
        return out;
    }

    private JsonObject buildWeapon(WeaponMeta weaponData, int getTime) {
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
        //can't make copies of Mega Man collab weapons apparently...
        int copiesCount = weaponData.getName().contains("Mega") ? 1 : 4;
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
        out.addProperty("fort_passive_chara_weapon_buildup_count", 1);        //weapon bonus
        out.addProperty("is_new", 1);
        out.addProperty("gettime", getTime == -1 ? Instant.now().getEpochSecond() : getTime);
        return out;
    }

    private JsonObject buildWyrmprint(WyrmprintMeta printData, int getTime) {
        JsonObject out = new JsonObject();
        int rarity = printData.getRarity();
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

        out.addProperty("ability_crest_id", printData.getId());
        out.addProperty("buildup_count", level);
        out.addProperty("limit_break_count", 4);
        out.addProperty("equipable_count", 4);
        out.addProperty("hp_plus_count", augmentCount);
        out.addProperty("attack_plus_count", augmentCount);
        out.addProperty("is_new", 1);
        out.addProperty("is_favorite", 0);
        out.addProperty("gettime", getTime == -1 ? Instant.now().getEpochSecond() : getTime);
        return out;
    }

    private void unlockAdventurerStory(int id) {
        if (id == 10750102 || id == 10140101) {
            return; //Mega Man, Euden have no stories to tell
        }
        //Compile list of adventurer stories in savedata
        Set<Integer> ownedStories = new HashSet<>();
        getFieldAsJsonArray("data", "unit_story_list").forEach(jsonEle ->
                ownedStories.add(jsonEle.getAsJsonObject().get("unit_story_id").getAsInt()));

        List<Integer> storyIDs = adventurerStoryMap.get(id);
        for(int i = 0; i < 5; i ++){
            int storyID = storyIDs.get(i);
            if(ownedStories.contains(storyID)){
                continue; //dont add story if u already have it
            }
            JsonObject story = new JsonObject();
            story.addProperty("unit_story_id", storyID);
            story.addProperty("is_read", 0);
            getFieldAsJsonArray("data", "unit_story_list").add(story);
        }
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
        for(Map.Entry<Integer, AdventurerMeta> entry : idToAdventurer.entrySet()){
            int id = entry.getKey();
            AdventurerMeta adventurer = entry.getValue();
            if (!ownedIdList.contains(id)) { //If you don't own this adventurer
                //Construct new unit (Does this unit have a mana spiral?)
                JsonObject newUnit = buildUnit(adventurer, -1);
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
        AdventurerMeta advData = nameToAdventurer.get(advName);
        if (advData == null) {
            System.out.println("Can't find adventurer with name '" + advName + "'. Try again!");
            return;
        }
        //Compile a list of ID's you have
        Set<Integer> ownedIdSet = new HashSet<>();
        getFieldAsJsonArray("data", "chara_list").forEach(jsonEle ->
                ownedIdSet.add(jsonEle.getAsJsonObject().get("chara_id").getAsInt()));
        int id = advData.getId();
        if (ownedIdSet.contains(id)) {
            System.out.println("You already own '" + advName + "'!");
            return;
        }
        //Construct new unit (Does this unit have a mana spiral?)
        JsonObject newUnit = buildUnit(advData, -1);
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
        for (Map.Entry<Integer, WyrmprintMeta> entry : idToPrint.entrySet()) {
            WyrmprintMeta wyrmprint = entry.getValue();
            int id = entry.getKey();
            if (!ownedIdSet.contains(id)) { //If you don't own this print
                //Construct new print
                JsonObject newPrint = buildWyrmprint(wyrmprint, -1);
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
        for (Map.Entry<Integer, DragonMeta> entry : idToDragon.entrySet()) {
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

        DragonMeta drgData = nameToDragon.get(drgName);
        if (drgData == null) {
            System.out.println("Can't find dragon with name '" + drgName + "'. Try again!");
            return;
        }
        int id = drgData.getId();

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
        for (String[] kscapeCombo : kscapeCombos) {
            //for each ele-wep combo
            for (String[] kscapeLabel : kscapeLabels) {
                for (String label : kscapeLabel) {
                    talismans.add(buildTalisman(label, kscapeCombo, keyIdOffset));
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
        for (Map.Entry<Integer, WeaponMeta> entry : idToWeapon.entrySet()) {
            WeaponMeta weapon = entry.getValue();
            int id = entry.getKey();
            if (!ownedIdSet.contains(id)) { //If you don't own this weapon
                //Construct new weapon
                JsonObject newWeapon = buildWeapon(weapon, -1);
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

    //can't implement rn... until I figure out where facility inventory is stored in savefile
    public void maxFacilities(){
        int count = 0;
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
            int y = currentFacility.get("position_y").getAsInt();
            int id = currentFacility.get("plant_id").getAsInt();
            keyIdMax = Math.max(keyIdMax, keyId);
            if(idToBuildCount.containsKey(id)){ //increment build count
                int buildCount = idToBuildCount.get(id);
                idToBuildCount.put(id, buildCount + 1);
            } else {
                idToBuildCount.put(id, 1);
            }
            newFacilities.add(buildFacility(idToFacility.get(id), keyIdMax, count + 1, x, y));
        }

        //second pass... add appropriate amount of each facility missing
        //update fort_bonus_list... hardcoded for now
        //update fort_plant_list... hardcoded for now
        //update
    }

    public void maxAdventurers() {
        JsonArray updatedAdventurers = new JsonArray();
        JsonArray ownedAdventurers = getFieldAsJsonArray("data", "chara_list");

        for(JsonElement jsonEle : ownedAdventurers){
            JsonObject ownedAdventurer = jsonEle.getAsJsonObject();
            int id = ownedAdventurer.get("chara_id").getAsInt();
            int getTime = ownedAdventurer.get("gettime").getAsInt();
            AdventurerMeta adventurer = idToAdventurer.get(id);
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
            unlockAdventurerStory(id);
        }
        //Replace current adventurer list
        getFieldAsJsonObject("data").remove("chara_list");
        getFieldAsJsonObject("data").add("chara_list", updatedAdventurers);
    }

    public void maxDragons(){
        JsonArray updatedDragons = new JsonArray();
        JsonArray ownedDragons = getFieldAsJsonArray("data", "dragon_list");

        for(JsonElement jsonEle : ownedDragons){
            JsonObject ownedDragon = jsonEle.getAsJsonObject();
            int id = ownedDragon.get("dragon_id").getAsInt();
            int getTime = ownedDragon.get("get_time").getAsInt();
            int keyId = ownedDragon.get("dragon_key_id").getAsInt();
            DragonMeta dragon = idToDragon.get(id);

            //Construct new dragon
            JsonObject updatedUnit = buildDragon2(dragon, keyId, getTime);
            updatedDragons.add(updatedUnit);
            boolean has5UB = dragon.has5UB();

            //Update encyclopedia max level/unbound obj
            for (JsonElement jsonEle2 : getFieldAsJsonArray("data", "album_dragon_list")){
                JsonObject encycloData = jsonEle2.getAsJsonObject();
                if(encycloData.get("dragon_id").getAsInt() != id){
                    continue; //ignore if this isnt the dragon
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
                        //Update encyclopedia bonus
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

    public void maxWeapons(){
        JsonArray updatedWeapons = new JsonArray();
        JsonArray ownedWeapons = getFieldAsJsonArray("data", "weapon_body_list");

        for(JsonElement jsonEle : ownedWeapons){
            JsonObject ownedWeapon = jsonEle.getAsJsonObject();
            int id = ownedWeapon.get("weapon_body_id").getAsInt();
            int getTime = ownedWeapon.get("gettime").getAsInt();
            WeaponMeta weapon = idToWeapon.get(id);
            //Construct new weapon
            JsonObject updatedWeapon = buildWeapon(weapon, getTime);
            updatedWeapons.add(updatedWeapon);
            //Update weapon bonus
            boolean isWeaponBonusUnlocked = ownedWeapon.get("fort_passive_chara_weapon_buildup_count").getAsInt() == 1;
            if(!isWeaponBonusUnlocked){
                addWeaponBonus(weapon);
            }
        }
        //Replace current adventurer list
        getFieldAsJsonObject("data").remove("weapon_body_list");
        getFieldAsJsonObject("data").add("weapon_body_list", updatedWeapons);
    }

    public void maxWyrmprints(){
        JsonArray updatedWyrmprints = new JsonArray();
        JsonArray ownedWyrmprints = getFieldAsJsonArray("data", "ability_crest_list");

        for(JsonElement jsonEle : ownedWyrmprints){
            JsonObject ownedWyrmprint = jsonEle.getAsJsonObject();
            int id = ownedWyrmprint.get("ability_crest_id").getAsInt();
            int getTime = ownedWyrmprint.get("gettime").getAsInt();
            WyrmprintMeta wyrmprint = idToPrint.get(id);
            //Construct new print
            JsonObject updatedPrint = buildWyrmprint(wyrmprint, getTime);
            updatedWyrmprints.add(updatedPrint);
        }
        //Replace current adventurer list
        getFieldAsJsonObject("data").remove("ability_crest_list");
        getFieldAsJsonObject("data").add("ability_crest_list", updatedWyrmprints);
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
        for(int i = 0; i < 4; i++){
            addTalisman("Born Ruler", 805, 806, 721); //(Water) Skill Recharge +65%, Skill Prep +100%
        }

        addTalisman("The Blazewolf", 100100205, 1237, 100100204); //ar20 + flame ar20 + ar10
        addTalisman("Summertime Boar", 100100205, 1225, 100100204); //ar20 + hp70 ar10 + ar10
    }
}
