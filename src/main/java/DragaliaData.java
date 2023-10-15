import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import meta.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

public class DragaliaData {

    //pulled from datamine
    public static JsonArray abilitiesList;

    //Ability Name --> Ability ID
    public static HashMap<String, Integer> kscapeAbilityMap = new HashMap<>();
    //Adventurer Title --> Portrait Print ID
    public static HashMap<String, Integer> kscapeLabelsMap = new HashMap<>();
    public static HashMap<Integer, AdventurerMeta> kscapeLabelIdToAdventurer = new HashMap<>();
    public static List<Integer> kscapePortraitIDs = new ArrayList<>();
    //Adventurer ID --> Adventurer Story IDs
    public static HashMap<Integer, List<Integer>> adventurerStoryMap = new HashMap<>();

    //Maps
    public static HashMap<Integer, AdventurerMeta> idToAdventurer = new HashMap<>();
    public static HashMap<String, AdventurerMeta> nameToAdventurer = new HashMap<>();

    public static HashMap<Integer, DragonMeta> idToDragon = new HashMap<>();
    public static HashMap<String, DragonMeta> nameToDragon = new HashMap<>();

    public static HashMap<Integer, WeaponMeta> idToWeapon = new HashMap<>();
    public static HashMap<String, WeaponMeta> weaponFunctionalNameToWeapon = new HashMap<>();
    public static HashMap<Integer, WeaponSkinMeta> idToWeaponSkin = new HashMap<>();
    public static HashMap<Integer, WyrmprintMeta> idToPrint = new HashMap<>();
    public static HashMap<String, WyrmprintMeta> nameToPrint = new HashMap<>(); // no spaces in key! also uppercased
    public static HashMap<Integer, FacilityMeta> idToFacility = new HashMap<>();
    public static HashMap<Integer, MaterialMeta> idToMaterial = new HashMap<>();
    public static HashMap<Integer, String> idToAbilityName = new HashMap<>();
    public static HashMap<String, Integer> nameToEpithetId = new HashMap<>();

    // Lists
    public static List<Integer> unplayableAdventurerIds = new ArrayList<>();
    public static List<Integer> unplayableDragonIds = new ArrayList<>();

    //Alias Maps
    public static HashMap<String, List<String>> adventurerAliases = new HashMap<>();
    public static HashMap<String, List<String>> dragonAliases = new HashMap<>();

    public static JsonObject maxedFacilityBonuses;

    private static final List<String> storyAdventurerNames = Arrays.asList("The Prince",
        "Elisanne", "Ranzal", "Cleo", "Luca", "Alex", "Laxi", "Chelle", "Zena");

    public static void init() {
        Logging.log("Initializing DragaliaData...");
        try {
            readAliasesData();
            readKscapeLabels();
            readAdventurerData();
            readDragonsData();
            readKscapeData();
            readStoryData();
            readWeaponSkinData();
            readWeaponsData();
            readPrintsData();
            readAbilitiesData();
            readFacilitiesData();
            readMaterialsData();
            readAbilityData();
            readEpithetsData();

            literallyUnplayable();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void literallyUnplayable() {
        // Adventurers
        List<Integer> some = new ArrayList<>(Arrays.asList(19900001, 19900002,
                19900005, 19900004, 19900003, 19900006, 99900009));
        unplayableAdventurerIds.addAll(some);
        for (int i = 0; i < 67; i++) {
            unplayableAdventurerIds.add(19100001 + i);
        }
        for(int i = 0; i < 9; i++){
            unplayableAdventurerIds.add(99130001 + i * 100000);
        }

        // Dragons
        List<Integer> someMore = new ArrayList<>(Arrays.asList(29900006, 29900014,
                29900017, 29900018, 29900023));
        unplayableDragonIds.addAll(someMore);
        for(int i = 0; i < 27; i++){
            unplayableDragonIds.add(29900001 + i);
        }
        List<Integer> moreMore = new ArrayList<>(Arrays.asList(29800001, 29800002,
                29800003, 29940301, 29950405, 29950116, 29950522, 29950317,
                29950523, 29950518, 29950415, 29950524, 29950416, 29950525,
                29950121, 29950320));
        unplayableDragonIds.addAll(moreMore);
        for(int i = 0; i < 6; i++){
            unplayableDragonIds.add(21000001 + i);
        }
    }

    //Reads
    private static void readAliasesData() throws IOException {
        //Get aliases
        BufferedReader br = getBufferedReader("adventurerAliases.txt");
        String out = br.readLine();
        while (out != null) {
            String[] split = out.split(",");
            String name = split[0].toUpperCase();
            List<String> advAliases = new ArrayList<>();
            for(int i = 1; i < split.length; i++){
                advAliases.add(split[i].toUpperCase());
            }
            adventurerAliases.put(name, advAliases);
            out = br.readLine();
        }
        br = getBufferedReader("dragonAliases.txt");
        out = br.readLine();
        while (out != null) {
            String[] split = out.split(",");
            String name = split[0].toUpperCase();
            List<String> drgAliases = new ArrayList<>();
            for(int i = 1; i < split.length; i++){
                drgAliases.add(split[i].toUpperCase());
            }
            dragonAliases.put(name, drgAliases);
            out = br.readLine();
        }
    }

    private static void readAdventurerData() throws IOException {
        for (JsonElement jsonEle : getJsonArrayFromRsrc("adventurers.json")) {
            JsonObject adv = jsonEle.getAsJsonObject();
            String baseName = adv.get("FullName").getAsString();
            String name = baseName.toUpperCase();
            if (name.equals("PUPPY")) {
                continue; //dog check //...i should just remove this
            }
            //fill idToAdventurer map
            boolean hasManaSpiral = !(adv.get("ManaSpiralDate") instanceof JsonNull);
            int hp, str;
            int id = adv.get("IdLong").getAsInt();
            if (hasManaSpiral) {
                hp = JsonUtils.getSum(adv, "AddMaxHp1", "PlusHp0", "PlusHp1", "PlusHp2", "PlusHp3", "PlusHp4", "PlusHp5", "McFullBonusHp5");
                str = JsonUtils.getSum(adv, "AddMaxAtk1", "PlusAtk0", "PlusAtk1", "PlusAtk2", "PlusAtk3", "PlusAtk4", "PlusAtk5", "McFullBonusAtk5");
            } else {
                hp = JsonUtils.getSum(adv, "MaxHp", "PlusHp0", "PlusHp1", "PlusHp2", "PlusHp3", "PlusHp4", "McFullBonusHp5");
                str = JsonUtils.getSum(adv, "MaxAtk", "PlusAtk0", "PlusAtk1", "PlusAtk2", "PlusAtk3", "PlusAtk4", "McFullBonusAtk5");
            }

            int maxA3Level = 1;
            if(adv.get("Abilities32").getAsInt() != 0){
                maxA3Level = 2;
                if(adv.get("Abilities33").getAsInt() != 0){
                    maxA3Level = 3;
                }
            }

            String manaCircleType = adv.get("ManaCircleName").getAsString();

            String title = adv.get("Title").getAsString();

            int kscapeLabelId = kscapeLabelsMap.get(title);

            boolean isStoryAdventurer = storyAdventurerNames.contains(baseName);

            AdventurerMeta unit = new AdventurerMeta(baseName, title, id,
                    adv.get("ElementalTypeId").getAsInt(), hp, str,adv.get("MaxLimitBreakCount").getAsInt(),
                    adv.get("EditSkillCost").getAsInt() != 0, hasManaSpiral, maxA3Level,
                    adv.get("MinHp3").getAsInt(), adv.get("MinHp4").getAsInt(), adv.get("MinHp5").getAsInt(),
                    adv.get("MinAtk3").getAsInt(), adv.get("MinAtk4").getAsInt(), adv.get("MinAtk5").getAsInt(),
                    adv.get("Rarity").getAsInt(), manaCircleType, adv.get("ElementalType").getAsString(),
                    adv.get("WeaponType").getAsString(), kscapeLabelId, isStoryAdventurer
            );
            idToAdventurer.put(id, unit);
            nameToAdventurer.put(name, unit);
            kscapeLabelIdToAdventurer.put(kscapeLabelId, unit);
            if(adventurerAliases.containsKey(name)){
                adventurerAliases.get(name).forEach(alias -> nameToAdventurer.put(alias.toUpperCase(), unit));
            }
        }
    }

    private static void readDragonsData() throws IOException {
        for (JsonElement jsonEle : getJsonArrayFromRsrc("dragons.json")) {
            JsonObject drg = jsonEle.getAsJsonObject();
            String baseName = drg.get("FullName").getAsString();
            String name = baseName.toUpperCase();
            if (drg.get("IsPlayable").getAsInt() == 0) {
                continue;
            }
            //fill idToDragon
            boolean has5UB = drg.get("MaxLimitBreakCount").getAsInt() == 5;
            int id = drg.get("Id").getAsInt();
            int baseId = drg.get("BaseId").getAsInt();
            int rarity = drg.get("Rarity").getAsInt();
            int a1Level = has5UB ?
                    6 : drg.get("Abilities15").getAsInt() != 0 ?
                    5 : 0;
            int a2Level = has5UB ?
                    6 : drg.get("Abilities25").getAsInt() != 0 ?
                    5 : 0;
            boolean hasA2 = drg.get("Abilities21").getAsInt() != 0;
            DragonMeta unit = new DragonMeta(baseName, baseId, id, drg.get("ElementalTypeId").getAsInt(),
                    a1Level, a2Level, rarity, has5UB, hasA2);
            idToDragon.put(id, unit);
            nameToDragon.put(name, unit);
            if(dragonAliases.containsKey(name)){
                dragonAliases.get(name).forEach(alias -> nameToDragon.put(alias.toUpperCase(), unit));
            }
        }
    }

    private static void readAbilitiesData() throws IOException {
        abilitiesList = getJsonArrayFromRsrc("abilities.json");
    }

    private static void readMaterialsData() throws IOException {
        getJsonArrayFromRsrc("materials.json").forEach(jsonEle -> {
            JsonObject mat = jsonEle.getAsJsonObject();
            String name = mat.get("Name").getAsString();
            int id = mat.get("Id").getAsInt();
            String category;
            if(mat.has("Category")){
                category = mat.get("Category").getAsString();
            } else {
                category = "Idk";
            }
            idToMaterial.put(id, new MaterialMeta(name, id, category));
        });
    }

    private static void readFacilitiesData() throws IOException {
        HashMap<Integer, JsonObject> facilitiesMap = new HashMap<>();
        //pull wiki facilities data
        getJsonArrayFromRsrc("facilities.json").forEach(jsonEle -> {
            JsonObject facility = jsonEle.getAsJsonObject();
            int id = facility.get("Id").getAsInt();
            facilitiesMap.put(id, facility);
        });

        BufferedReader br = getBufferedReader("FortPlantDetail.txt");
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

        //get max facility bonus data
        maxedFacilityBonuses = getJsonObjectFromRsrc("maxedFacilityBonuses.json");
    }

    private static void readWeaponsData() throws IOException {
        for(JsonElement jsonEle : getJsonArrayFromRsrc("weapons.json")){
            JsonObject weaponData = jsonEle.getAsJsonObject();
            int id = weaponData.get("Id").getAsInt();
            List<Integer> passiveIds = JsonUtils.jsonArrayToList(weaponData.get("PassiveAbilities").getAsJsonArray());
            WeaponMeta weapon = new WeaponMeta(weaponData.get("Name").getAsString(), id,
                    weaponData.get("ElementalTypeId").getAsInt(), weaponData.get("WeaponTypeId").getAsInt(),
                    weaponData.get("WeaponSeries").getAsString(), weaponData.get("Rarity").getAsInt(),
                    passiveIds, weaponData.get("HasWeaponBonus").getAsBoolean());
            idToWeapon.put(id, weapon);
            weaponFunctionalNameToWeapon.put(weapon.getFunctionalName().toUpperCase(Locale.ROOT), weapon);
        }
    }

    private static void readPrintsData() throws IOException {
        for(JsonElement jsonEle : getJsonArrayFromRsrc("prints.json")){
            JsonObject printData = jsonEle.getAsJsonObject();
            int id = printData.get("Id").getAsInt();
            String name = printData.get("Name").getAsString();
            WyrmprintMeta print = new WyrmprintMeta(name,
                    id, printData.get("Rarity").getAsInt());
            idToPrint.put(id, print);
            name = name.replace(" ", "").replace("&amp;", "&").toUpperCase(Locale.ROOT);
            nameToPrint.put(name, print);
        }
    }

    private static void readKscapeData() throws IOException {
        JsonObject kscapeJson = getJsonObjectFromRsrc("kscape.json");
        for (Map.Entry<String, JsonElement> entry : kscapeJson.entrySet()) {
            kscapeAbilityMap.put(entry.getKey(), entry.getValue().getAsInt());
        }
    }

    private static void readKscapeLabels() throws IOException {
        BufferedReader br = getBufferedReader("kscapeLabels.txt");
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

    private static void readAbilityData() throws IOException {
        for(JsonElement jsonEle : getJsonArrayFromRsrc("abilities.json")){
            JsonObject abilityData = jsonEle.getAsJsonObject();
            int id = abilityData.get("Id").getAsInt();
            String nameRaw = abilityData.get("Name").getAsString();
            int val0 = (int) abilityData.get("Val0").getAsDouble();
            int val1 = (int) abilityData.get("Val1").getAsDouble();
            int val2 = (int) abilityData.get("Val2").getAsDouble();
            nameRaw = nameRaw.replace("{ability_val0}", String.valueOf(val0));
            nameRaw = nameRaw.replace("{ability_val1}", String.valueOf(val1));
            String name = nameRaw.replace("{ability_val2}", String.valueOf(val2));
            idToAbilityName.put(id, name);
        }
    }

    private static void readStoryData() throws IOException {
        for(Map.Entry<String, JsonElement> entry : getJsonObjectFromRsrc("CharaStories.json").entrySet()){
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

    private static void readWeaponSkinData() throws IOException {
        for(JsonElement jsonEle : getJsonArrayFromRsrc("weaponSkins.json")){
            JsonObject weaponSkin = jsonEle.getAsJsonObject();
            int id = weaponSkin.get("Id").getAsInt();
            String name = weaponSkin.get("Name").getAsString();
            int weaponTypeId = weaponSkin.get("WeaponTypeId").getAsInt();
            boolean isPlayable = weaponSkin.get("IsPlayable").getAsInt() == 1;
            WeaponSkinMeta weaponSkinMeta = new WeaponSkinMeta(name, id, weaponTypeId, isPlayable);
            idToWeaponSkin.put(id, weaponSkinMeta);
        }
    }

    private static void readEpithetsData() {
        JsonObject epithets = getJsonObjectFromRsrc("epithets.json");
        for (Map.Entry<String, JsonElement> entry : epithets.entrySet()) {
            String nameUpper = entry.getKey().toUpperCase(Locale.ROOT);
            JsonElement jsonEle = entry.getValue();

            int id = -1;
            try {
                id = jsonEle.getAsInt();
            } catch (NumberFormatException poop) {
                continue; // ignore the id->name mappings
            }

            nameToEpithetId.put(nameUpper, id);
        }
    }

    // Util

    private static JsonArray getJsonArrayFromRsrc(String more) {
        JsonReader reader = new JsonReader(getRsrcReader(more));
        return JsonUtils.GSON.fromJson(reader, JsonArray.class);
    }

    private static JsonObject getJsonObjectFromRsrc(String more) {
        JsonReader reader = new JsonReader(getRsrcReader(more));
        return JsonUtils.GSON.fromJson(reader, JsonObject.class);
    }

    private static InputStreamReader getRsrcReader(String... more){
        //getResourceAsStream() doesn't like backslashes i think...?
        //hope this doesn't break on other OS...
        String path = (File.separator + Paths.get("rsrc", more)).replace("\\", "/");
        InputStream in;
        in = JsonUtils.class.getResourceAsStream(path);
        if(in == null){
            System.out.println(path);
            in = JsonUtils.class.getClassLoader().getResourceAsStream(path);
        }
        if(in == null){
            System.out.println("Could not load resource!");
            System.exit(92);
        }
        return new InputStreamReader(in, StandardCharsets.UTF_8);
    }

    public static BufferedReader getBufferedReader(String... more){
        return new BufferedReader(getRsrcReader(more));
    }

}
