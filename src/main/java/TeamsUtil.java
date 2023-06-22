import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import meta.AdventurerMeta;
import meta.DragonMeta;
import meta.WeaponMeta;
import meta.WeaponSkinMeta;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TeamsUtil {

    private JsonUtils jsonUtils;
    private String teamDataPath;

    private HashMap<String, List<Integer>> talismanMap = new HashMap<>();
    private JsonObject teamsData = new JsonObject();

    public TeamsUtil (JsonUtils util, String teamDataPath) {
        this.jsonUtils = util;
        this.teamDataPath = teamDataPath;
        try {
            readShorthandsData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final HashMap<Integer, String> slot1_idToShorthand = new HashMap<>();
    private final HashMap<String, String> slot1_shorthandToId = new HashMap<>();
    private final HashMap<String, String> slot1_shorthandToIdAlternate = new HashMap<>();

    private final HashMap<Integer, String> slot2_idToShorthand = new HashMap<>();
    private final HashMap<String, String> slot2_shorthandToId = new HashMap<>();
    private final HashMap<String, String> slot2_shorthandToIdAlternate = new HashMap<>();

    private final HashMap<Integer, String> slot3_idToShorthand = new HashMap<>();
    private final HashMap<String, String> slot3_shorthandToId = new HashMap<>();
    private final HashMap<String, String> slot3_shorthandToIdAlternate = new HashMap<>();

    private final String[] fileNames = new String[]{"wyrmprintShorthandsSlot1.txt",
            "wyrmprintShorthandsSlot2.txt", "wyrmprintShorthandsSlot3.txt"};

    private void readShorthandsData() throws IOException {
        parseShorthandsFile(fileNames[0], slot1_idToShorthand, slot1_shorthandToId, slot1_shorthandToIdAlternate);
        parseShorthandsFile(fileNames[1], slot2_idToShorthand, slot2_shorthandToId, slot2_shorthandToIdAlternate);
        parseShorthandsFile(fileNames[2], slot3_idToShorthand, slot3_shorthandToId, slot3_shorthandToIdAlternate);
    }

    private String[] splitCommaSeparatedString(String input) {
        String in = input.replace(" ", "");
        return in.split(",");
    }

    private void parseShorthandsFile (String fileName,
             HashMap<Integer, String> idToShorthand,
             HashMap<String, String> shorthandToId,
             HashMap<String, String> shorthandToIdAlternate) throws IOException {
        BufferedReader br = jsonUtils.getBufferedReader("/team_import_stuff/" + fileName);
        String out = br.readLine();
        while (out != null) {
            if (out.length() == 0) { // empty line
                out = br.readLine();
                continue;
            }
            if (out.charAt(0) == '#') { // comment
                out = br.readLine();
                continue;
            }
            out = out.replace(" ", "");
            String[] split = out.split(":");
            if (split.length != 2) { // validate 1 ":"
                System.out.println("Encountered bad line with split.length = " + split.length + " in " + fileName);
                out = br.readLine();
                continue;
            }
            String shorthandsString = split[0];
            String idsString = split[1];

            String[] shorthands = splitCommaSeparatedString(shorthandsString);
            String[] ids = splitCommaSeparatedString(idsString);

            if (shorthands.length == 0 || ids.length == 0) { // validate array size
                System.out.println("Encountered bad line with bad shorthand/id count in " + fileName);
                out = br.readLine();
                continue;
            }

            for (String shorthand : shorthands) {
                shorthandToId.put(shorthand.toUpperCase(Locale.ROOT), ids[0].toUpperCase(Locale.ROOT));
            }
            if (ids.length > 1) {
                shorthandToIdAlternate.put(shorthands[0].toUpperCase(Locale.ROOT), ids[1].toUpperCase(Locale.ROOT));
            }
            try {
                idToShorthand.put(Integer.parseInt(ids[0]), shorthands[0]);
            } catch (NumberFormatException ignored) {} // ignore getting idToShorthand for String ids (the 'auto' ones)

            out = br.readLine();
        }
    }

    private AdventurerMeta getAdventurerFromNameOrId(String charNameOrId) {
        AdventurerMeta adv;
        int id = -1;
        try {
            id = Integer.parseInt(charNameOrId);
        } catch (NumberFormatException ignored) {}

        if (id != -1) {
            adv = jsonUtils.idToAdventurer.get(id);
        } else {
            adv = jsonUtils.nameToAdventurer.get(charNameOrId.toUpperCase(Locale.ROOT));
        }

        if (adv == null) {
            System.out.println("Error while parsing adventurer id/name string: no adventurer found" +
                    " with id or name: " + charNameOrId);
            return null;
        }
        return adv;
    }

    private void exportTeams () {
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(teamDataPath);
            JsonUtils.GSON.toJson(teamsData, fileWriter);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // String shorthandOrIdCased: the shorthand string or id integer, that refers to a specific wyrmprint
    // ex: "1480118", "strdb" --(with slot 1)--> Brothers in Arms

    // String charNameOrId: the adventurer name, can be an alias
    // ex: "The Prince", "euden"

    // int slot: a number from 1-3 specifying the wyrmprint slot type: 5*, 4*, and sindom

    // List<Integer> history: the past 1 or 2 wyrmprint id's that were obtained from this method
    // needed to prevent the method from returning duplicate wyrmprint id's
    // for ex: for slot 1, you had prints "prep, prep, strdb".
    // don't want to get 2 of The Chocolatiers print, so use a secondary print ID for the second one.

    private int shorthandToId (String shorthandOrIdCased, String charNameOrId, int slot, List<Integer> history) {
        String shorthandOrId = shorthandOrIdCased.toUpperCase(Locale.ROOT);

        try {
            int id = Integer.parseInt(shorthandOrId);
            if(jsonUtils.idToPrint.containsKey(id)) {
                return jsonUtils.idToPrint.get(id).getId();
            } else {
                System.out.println("Unable to find wyrmprint with numeric ID: " + id);
                return 0;
            }
        } catch (NumberFormatException ignored) {}

        AdventurerMeta adv = getAdventurerFromNameOrId(charNameOrId);
       if (adv == null) {
           return 0;
       }
       String weaponString = adv.getWeaponType();
       String elementString = adv.getElementType();

       HashMap<String, String> shorthandToId = new HashMap<>();
       HashMap<String, String> shorthandToIdAlternate = new HashMap<>();

       switch (slot) {
           case 1:
               shorthandToId = slot1_shorthandToId;
               shorthandToIdAlternate = slot1_shorthandToIdAlternate;
               break;
           case 2:
               shorthandToId = slot2_shorthandToId;
               shorthandToIdAlternate = slot2_shorthandToIdAlternate;
               break;
           case 3:
               shorthandToId = slot3_shorthandToId;
               shorthandToIdAlternate = slot3_shorthandToIdAlternate;
               break;
       }

       if (!shorthandToId.containsKey(shorthandOrId)) {
           System.out.println("Unable to find wyrmprint with shorthand name: " + shorthandOrIdCased);
           return 0;
       }

       int returningId = 0;
       String maybeId = shorthandToId.get(shorthandOrId);
       if (maybeId.contains("auto")) {
           String autoWeaponShorthand = (maybeId + "_" + weaponString).toUpperCase(Locale.ROOT);
           if (shorthandToId.containsKey(autoWeaponShorthand)) {
               returningId = Integer.parseInt(shorthandToId.get(autoWeaponShorthand));
           }
           String autoElementShorthand = (maybeId + "_" + elementString).toUpperCase(Locale.ROOT);
           if (shorthandToId.containsKey(autoElementShorthand)) {
               returningId = Integer.parseInt(shorthandToId.get(autoElementShorthand));
           }
           String autoElseShorthand = (maybeId + "_else").toUpperCase(Locale.ROOT);
           if (shorthandToId.containsKey(autoElseShorthand)) {
               returningId = Integer.parseInt(shorthandToId.get(autoElseShorthand));
           }
       } else {
           returningId = Integer.parseInt(maybeId);
       }

       if (returningId == 0) {
           System.out.println("Unable to find wyrmprint with shorthand: " + shorthandOrIdCased);
           return 0;
       }

       if (history.contains(returningId)) {
           // shouldn't really happen...
           if (history.size() > 1) {
               System.out.println("Unable to find alternate print ID with shorthand: " + shorthandOrIdCased);
               return 0;
           }

           // dupe return id... get secondary id
           return Integer.parseInt(shorthandToIdAlternate.get(shorthandOrId));
       } else {
           return returningId;
       }
    }

    private String idToShorthand (int id, int slot) {
        if (id == 0) {
            return "0";
        }
        switch (slot) {
            case 1: return (slot1_idToShorthand.containsKey(id)) ? slot1_idToShorthand.get(id) : String.valueOf(id);
            case 2: return (slot2_idToShorthand.containsKey(id)) ? slot2_idToShorthand.get(id) : String.valueOf(id);
            case 3: return (slot3_idToShorthand.containsKey(id)) ? slot3_idToShorthand.get(id) : String.valueOf(id);
        }
        System.out.println("Bad slot number when calling idToShorthand()");
        return "0";
    }

    private JsonArray getTeamsFromGameData () {
        JsonArray ingameTeams = jsonUtils.getFieldAsJsonArray("data", "party_list");

        JsonArray outTeams = new JsonArray();
        for (JsonElement jsonEle : ingameTeams) {
            JsonObject ingameTeam = jsonEle.getAsJsonObject();

            JsonObject outTeam = new JsonObject();
            JsonArray outMembers = new JsonArray();
            JsonArray ingameTeamMembers = ingameTeam.get("party_setting_list").getAsJsonArray();
            for (JsonElement jsonEle2 : ingameTeamMembers) {
                JsonObject ingameMember = jsonEle2.getAsJsonObject();
                JsonObject outMember = new JsonObject();
                String slot1Prints =
                    idToShorthand(ingameMember.get("equip_crest_slot_type_1_crest_id_1").getAsInt(), 1) + ", " +
                    idToShorthand(ingameMember.get("equip_crest_slot_type_1_crest_id_2").getAsInt(), 1) + ", " +
                    idToShorthand(ingameMember.get("equip_crest_slot_type_1_crest_id_3").getAsInt(), 1);
                String slot2Prints =
                    idToShorthand(ingameMember.get("equip_crest_slot_type_2_crest_id_1").getAsInt(), 2) + ", " +
                    idToShorthand(ingameMember.get("equip_crest_slot_type_2_crest_id_2").getAsInt(), 2);
                String slot3Prints =
                    idToShorthand(ingameMember.get("equip_crest_slot_type_3_crest_id_1").getAsInt(), 3) + ", " +
                    idToShorthand(ingameMember.get("equip_crest_slot_type_3_crest_id_2").getAsInt(), 3);

                int chara_id = ingameMember.get("chara_id").getAsInt();
                int dragon_key_id = ingameMember.get("equip_dragon_key_id").getAsInt();
                int weapon_body_id = ingameMember.get("equip_weapon_body_id").getAsInt();
                DragonMeta dragon = jsonUtils.getDragonFromKeyId(dragon_key_id);

                String charaName = (jsonUtils.idToAdventurer.containsKey(chara_id)) ? (jsonUtils.idToAdventurer.get(chara_id).getName()) : ("0");
                String dragonName = (dragon != null) ? (dragon.getName()) : ("0");
                String weaponName = (jsonUtils.idToWeapon.containsKey(weapon_body_id)) ? (jsonUtils.idToWeapon.get(weapon_body_id).getFunctionalName()) : ("0");

                int ss1_id = ingameMember.get("edit_skill_1_chara_id").getAsInt();
                int ss2_id = ingameMember.get("edit_skill_2_chara_id").getAsInt();

                String ss1_name = (jsonUtils.idToAdventurer.containsKey(ss1_id)) ? (jsonUtils.idToAdventurer.get(ss1_id).getName()) : ("0");
                String ss2_name = (jsonUtils.idToAdventurer.containsKey(ss2_id)) ? (jsonUtils.idToAdventurer.get(ss2_id).getName()) : ("0");

                outMember.addProperty("teamSlot", ingameMember.get("unit_no").getAsInt());
                outMember.addProperty("unitId", charaName);
                outMember.addProperty("dragon", dragonName);
                outMember.addProperty("weapon", weaponName);
                outMember.addProperty("weaponSkin", ingameMember.get("equip_weapon_skin_id").getAsInt());
                outMember.addProperty("slot1Wyrmprints", slot1Prints);
                outMember.addProperty("slot2Wyrmprints", slot2Prints);
                outMember.addProperty("slot3Wyrmprints", slot3Prints);
                outMember.addProperty("talisman", ingameMember.get("equip_talisman_key_id").getAsString());
                outMember.addProperty("sharedSkill1", ss1_name);
                outMember.addProperty("sharedSkill2", ss2_name);
                outMembers.add(outMember);
            }

            outTeam.addProperty("teamId", ingameTeam.get("party_no").getAsInt());
            outTeam.addProperty("teamName", ingameTeam.get("party_name").getAsString());
            outTeam.add("members", outMembers);
            outTeams.add(outTeam);
        }
        return outTeams;
    }

    // a team in teams.json format
    private JsonArray getDefaultTeamMembers () {
        JsonArray outMembers = new JsonArray();
        for (int j = 1; j <= 4; j++) {
            JsonObject outMember = new JsonObject();

            outMember.addProperty("teamSlot", j);
            outMember.addProperty("unitId", (j == 1) ? "euden" : "0");
            outMember.addProperty("dragon", 0);
            outMember.addProperty("weapon", 0);
            outMember.addProperty("weaponSkin", 0);
            outMember.addProperty("slot1Wyrmprints", "0, 0, 0");
            outMember.addProperty("slot2Wyrmprints", "0, 0");
            outMember.addProperty("slot3Wyrmprints", "0, 0");
            outMember.addProperty("talisman", "0");
            outMember.addProperty("sharedSkillIds", "cleo, ranzal");
            outMembers.add(outMember);
        }
        return outMembers;
    }

    // 1-54 teams in teams.json format
    private JsonArray getDefaultTeams () {
        JsonArray outTeams = new JsonArray();
        for (int i = 1; i <= 54; i++) {
            JsonObject outTeam = new JsonObject();
            outTeam.addProperty("teamId", i);
            outTeam.addProperty("teamName", "Cool Team");
            outTeam.add("members", getDefaultTeamMembers());
            outTeams.add(outTeam);
        }
        return outTeams;
    }

    // default "party_setting_list" or team members in savedata.txt format
    private JsonArray getDefaultPartySettingList () {
        JsonArray out = new JsonArray();
        for (int i = 1; i <= 4; i++) {
            int chara_id = (i == 1) ? (10140101) : (0); // Euden or nothing
            out.add(getEmptyMember(i, chara_id));
        }
        return out;
    }

    // teams.json format
    private JsonArray getDefaultTalismans () {
        JsonArray out = new JsonArray();
        JsonObject outTalisman = new JsonObject();
        outTalisman.addProperty("name", "crit easy");
        outTalisman.addProperty("adventurerName", "Ryszarda");
        outTalisman.addProperty("count", 0);
        outTalisman.addProperty("id1", 340000030);
        outTalisman.addProperty("id2", 340000132);
        outTalisman.addProperty("id3", 0);
        out.add(outTalisman);
        return out;
    }

    private JsonObject getEmptyMember (int slot, int chara_id) {
        JsonObject out = new JsonObject();
        out.addProperty("unit_no", slot);
        out.addProperty("chara_id", 0);
        out.addProperty("equip_dragon_key_id", 0);
        out.addProperty("equip_weapon_body_id", 0);
        out.addProperty("equip_weapon_skin_id", 0);
        out.addProperty("equip_crest_slot_type_1_crest_id_1", 0);
        out.addProperty("equip_crest_slot_type_1_crest_id_2", 0);
        out.addProperty("equip_crest_slot_type_1_crest_id_3", 0);
        out.addProperty("equip_crest_slot_type_2_crest_id_1", 0);
        out.addProperty("equip_crest_slot_type_2_crest_id_2", 0);
        out.addProperty("equip_crest_slot_type_3_crest_id_1", 0);
        out.addProperty("equip_crest_slot_type_3_crest_id_2", 0);
        out.addProperty("equip_talisman_key_id", 0);
        out.addProperty("edit_skill_1_chara_id", 0);
        out.addProperty("edit_skill_2_chara_id", 0);
        return out;
    }

    public JsonArray generateNewTeams (boolean toGenerateFromGameData) {
        return (toGenerateFromGameData) ? (getTeamsFromGameData()) : (getDefaultTeams());
    }

    private void initializeTalismans () {
        JsonArray talismans = teamsData.get("talismans").getAsJsonArray();
        for (JsonElement jsonEle : talismans) {
            JsonObject talisman = jsonEle.getAsJsonObject();
            String name = talisman.get("name").getAsString();
            String advNameCased = talisman.get("adventurerName").getAsString();
            String advName = advNameCased.toUpperCase(Locale.ROOT);
            int count = talisman.get("count").getAsInt();

            if (count == 0) { // just dont do anything for 0 count
                continue;
            }

            if (!(1 <= count && count <= 4)) { // validate 'count'
                Logging.print("Failed to import talisman with count: {0}: count must be in range 1 - 4", count);
                continue;
            }

            int portraitID = 0;
            String title = "";
            try {
                int advId = Integer.parseInt(advNameCased);
                if (!jsonUtils.idToAdventurer.containsKey(advId)) {
                    System.out.println("Failed to import talisman with name: '" +
                            name + "': adventurer ID '" + advId + "' not found");
                    continue;
                }
                title = jsonUtils.idToAdventurer.get(advId).getTitle();
                portraitID = jsonUtils.kscapeLabelsMap.get(title);
            } catch (NumberFormatException ignored) {
                if (!jsonUtils.nameToAdventurer.containsKey(advName)) {
                    System.out.println("Failed to import talisman with name: '" +
                            name + "': adventurer name '" + advNameCased + "' not found");
                    continue;
                }
                title = jsonUtils.nameToAdventurer.get(advName).getTitle();
                portraitID = jsonUtils.kscapeLabelsMap.get(title);
            }

            int id1 = talisman.get("id1").getAsInt();
            int id2 = talisman.get("id2").getAsInt();
            int id3 = talisman.get("id3").getAsInt();
            try {
                Integer.parseInt(name);
                System.out.println("Failed to import talisman with name: '" +
                        name + "': talisman names cannot be numeric");
                continue;
            } catch (NumberFormatException ignored) {}

            if (talismanMap.containsKey(name.toUpperCase(Locale.ROOT))) {
                System.out.println("Failed to import talisman with name: '" +
                        name + "': there already exists a talisman with this name");
                continue;
            }

            List<Integer> talismanKeyIds = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                int talismanKeyId = jsonUtils.addTalisman(portraitID, id1, id2, id3);
                talismanKeyIds.add(talismanKeyId);
            }
            talismanMap.put(name.toUpperCase(Locale.ROOT), talismanKeyIds);
        }
    }

    // convert teams.json member format --> savedata.txt format
    // JsonObject input: teams.json unit data
    // int slot: the slot (1-4) of the unit in the team
    // List<Integer> returnedDragonKeyIds: dragon key IDs returned from this method, used to check for dupe dragon key IDs
    private JsonObject convertMemberData(JsonObject input, int slot, List<Integer> returnedDragonKeyIds,
                                         List<Integer> returnedTalismanKeyIds) {
        int unit_no = input.get("teamSlot").getAsInt();
        int chara_id = -1;
        int equip_dragon_key_id = -1;
        int equip_weapon_body_id = -1;
        int equip_weapon_skin_id = -1;
        int equip_talisman_key_id = -1;


        String unitIdString = input.get("unitId").getAsString();
        try { // numeric ID
            int unitIdInt = Integer.parseInt(unitIdString);
            if (unitIdInt != 0) {
                if (!jsonUtils.idToAdventurer.containsKey(unitIdInt)) {
                    Logging.print("Failed to import adventurer with numeric ID: '{0}'; could not find " +
                            "adventurer with this ID", unitIdInt);
                    return (slot == 1) ? (null) : (getEmptyMember(slot, 0));
                } else {
                    chara_id = unitIdInt;
                }
            } else { // empty adventurer
                if (slot == 1) {
                    Logging.print("Failed to import adventurer with numeric ID: '{0}'; ID cannot be 0 for slot 1", unitIdInt);
                    return null;
                }
                chara_id = 0;
            }
        } catch (NumberFormatException ignored) { // name/alias
            String unitIdStringUpper = unitIdString.toUpperCase(Locale.ROOT);
            if (!jsonUtils.nameToAdventurer.containsKey(unitIdStringUpper)) {
                Logging.print("Failed to import adventurer with ID: '{0}'; could not find " +
                        "adventurer with this name", unitIdString);
                return (slot == 1) ? (null) : (getEmptyMember(slot, 0));
            }
            chara_id = jsonUtils.nameToAdventurer.get(unitIdStringUpper).getId();
        }

        // we're given a dragon name/alias OR a key ID... need to get a good key ID
        // if given key ID --> validate the key ID
        // if given dragon name/alias --> validate the name/alias
        // --> find a dragon in dragon_list, prioritizing highest level.
        // --> need to account for duplicate key IDs... so rotate around dragons of same dragon ID
        // --> if not enough dragons of the same dragon ID... return 0 as the dragon key ID
        String dragonString = input.get("dragon").getAsString();
        try { // numeric keyID
            int keyIdInt = Integer.parseInt(dragonString);
            if (keyIdInt != 0) {
                if (!jsonUtils.arrayHasValue("dragon_key_id", keyIdInt, "data", "dragon_list")) {
                    Logging.print("Failed to import dragon with numeric keyID: '{0}'; could not find " +
                            "dragon with this key ID", keyIdInt);
                    equip_dragon_key_id = 0;
                } else {
                    equip_dragon_key_id = keyIdInt;
                }
            } else { // empty dragon
                equip_dragon_key_id = 0;
            }
        } catch (NumberFormatException ignored) { // name/alias
            String dragonStringUpper = dragonString.toUpperCase(Locale.ROOT);
            if (!jsonUtils.nameToDragon.containsKey(dragonStringUpper)) {
                Logging.print("Failed to import dragon with name/alias: '{0}'; could not find " +
                        "dragon with this name", dragonString);
                equip_dragon_key_id = 0;
            } else {
                int dragonID = jsonUtils.nameToDragon.get(dragonStringUpper).getId();
                List<Integer> dragonKeyIds = jsonUtils.getKeyIdListSortedByAttr("dragon_key_id",
                        "level", "dragon_id", dragonID, "data", "dragon_list");
                dragonKeyIds.removeAll(returnedDragonKeyIds); // remove dragon key IDs that were already returned by this method
                if (dragonKeyIds.size() == 0) {
                    Logging.print("Failed to import dragon with name/alias: '{0}'; could not find " +
                            "enough dragons with this name/alias for this team", dragonString);
                    equip_dragon_key_id = 0;
                } else {
                    equip_dragon_key_id = dragonKeyIds.get(0);
                }
            }
        }

        // we're given a weapon skin ID... validate it
        String weaponSkinString = input.get("weaponSkin").getAsString();
        // no support for passing weapon skin name, only ID
        try { // numeric ID
            int weaponSkinIdInt = Integer.parseInt(weaponSkinString);
            if (weaponSkinIdInt != 0) {
                if (!jsonUtils.idToWeaponSkin.containsKey(weaponSkinIdInt)) {
                    Logging.print("Failed to import weapon skin with numeric ID: '{0}'; could not find " +
                            "weapon skin with this ID", weaponSkinIdInt);
                    equip_weapon_skin_id = 0;
                } else {
                    equip_weapon_skin_id = weaponSkinIdInt;
                }
            } else { // empty weapon skin
                equip_weapon_skin_id = 0;
            }
        } catch (NumberFormatException ignored) { // non-numeric
            Logging.print("Failed to import weapon skin with ID: '{0}'; not passing" +
                    " weapon skins as ID is not supported", weaponSkinString);
            equip_weapon_skin_id = 0;
        }

        // we're given a weapon name/alias... validate it
        String weaponString = input.get("weapon").getAsString();
        try { // numeric ID
            int weaponIdInt = Integer.parseInt(weaponString);
            if (weaponIdInt != 0) {
                if (!jsonUtils.idToWeapon.containsKey(weaponIdInt)) {
                    Logging.print("Failed to import weapon with numeric ID: '{0}'; could not find " +
                            "weapon with this ID", weaponIdInt);
                    equip_weapon_body_id = 0;
                    equip_weapon_skin_id = 0; // remove weapon skin if weapon invalid
                } else {
                    equip_weapon_body_id = weaponIdInt;
                }
            } else { // empty weapon
                equip_weapon_body_id = 0;
                equip_weapon_skin_id = 0; // remove weapon skin if weapon invalid
            }
        } catch (NumberFormatException ignored) { // name/alias
            String weaponStringUpper = weaponString.toUpperCase(Locale.ROOT);
            if (!jsonUtils.weaponFunctionalNameToWeapon.containsKey(weaponStringUpper)) {
                Logging.print("Failed to import weapon with name/shorthand: '{0}'; could not find " +
                        "weapon with this name", weaponString);
                equip_weapon_body_id = 0;
                equip_weapon_skin_id = 0; // remove weapon skin if weapon invalid
            } else {
                equip_weapon_body_id = jsonUtils.weaponFunctionalNameToWeapon.get(weaponStringUpper).getId();
            }
        }

        // wyrmprint stuff...

        int[] slot1WyrmprintsOut = new int[]{-1, -1, -1};
        int[] slot2WyrmprintsOut = new int[]{-1, -1, -1};
        int[] slot3WyrmprintsOut = new int[]{-1, -1, -1};

        for (int wpSlot = 1; wpSlot <= 3; wpSlot++) {
            String wyrmprintsString = input.get("slot" + wpSlot + "Wyrmprints").getAsString();
            String[] wyrmprints = splitCommaSeparatedString(wyrmprintsString);
            int[] wyrmprintsOut = new int[]{};

            int printCount = -1;
            switch (wpSlot) {
                case 1:
                    printCount = 3;
                    wyrmprintsOut = slot1WyrmprintsOut;
                    break;
                case 2:
                    printCount = 2;
                    wyrmprintsOut = slot2WyrmprintsOut;
                    break;
                case 3:
                    printCount = 2;
                    wyrmprintsOut = slot3WyrmprintsOut;
                    break;
            }

            if (wyrmprints.length != printCount) { // validate print count
                Logging.print("Failed to import slot {0} wyrmprints with value: '{1}'; amount of wyrmprints " +
                        "should be {2}", String.valueOf(wpSlot), wyrmprintsString, String.valueOf(printCount));
                wyrmprintsOut[0] = 0;
                wyrmprintsOut[1] = 0;
                wyrmprintsOut[2] = 0;
            } else {
                List<Integer> slotHistory = new ArrayList<>();
                for (int wpNum = 0; wpNum < printCount; wpNum++) {
                    String wyrmprintString = wyrmprints[wpNum];
                    try { // numeric ID
                        int wyrmprintIdInt = Integer.parseInt(wyrmprintString);
                        if (wyrmprintIdInt != 0) {
                            if (!jsonUtils.idToPrint.containsKey(wyrmprintIdInt)) {
                                Logging.print("Failed to import wyrmprint with numeric ID: '{0}'; could not find " +
                                        "wyrmprint with this ID", wyrmprintIdInt);
                                wyrmprintsOut[wpNum] = 0;
                            } else {
                                wyrmprintsOut[wpNum] = wyrmprintIdInt;
                            }
                        } else { // empty wyrmprint
                            wyrmprintsOut[wpNum] = 0;
                        }
                    } catch (NumberFormatException ignored) { // shorthand
                        String wyrmprintStringUpper = wyrmprintString.toUpperCase(Locale.ROOT);
                        int wyrmprintId = shorthandToId(wyrmprintStringUpper, unitIdString, wpSlot, slotHistory);
                        if (wyrmprintId == 0) {
                            Logging.print("Failed to import wyrmprint with shorthand: '{0}'; could not find " +
                                    "wyrmprint with this shorthand for slot {1}", wyrmprintString, String.valueOf(wpSlot));
                        } else {
                            slotHistory.add(wyrmprintId);
                        }
                        wyrmprintsOut[wpNum] = wyrmprintId;
                    }
                }
            }
        }

        // we're given a talisman name OR a key ID... need to get a good key ID
        // if given key ID --> validate the key ID
        // if given talisman name --> validate the name
        // --> fetch talisman key ID from map
        String talismanString = input.get("talisman").getAsString();
        try { // numeric ID
            int talismanKeyIdInt = Integer.parseInt(talismanString);
            if (talismanKeyIdInt != 0) {
                if (!jsonUtils.arrayHasValue("talisman_key_id", talismanKeyIdInt, "data", "talisman_list")) {
                    Logging.print("Failed to import talisman with numeric keyID: '{0}'; could not find " +
                            "talisman with this key ID", talismanKeyIdInt);
                    equip_talisman_key_id = 0;
                } else {
                    equip_talisman_key_id = talismanKeyIdInt;
                }
            } else {
                equip_talisman_key_id = 0;
            }
        } catch (NumberFormatException ignored) { // name
            String talismanStringUpper = talismanString.toUpperCase(Locale.ROOT);
            if (!talismanMap.containsKey(talismanStringUpper)) {
                Logging.print("Failed to import talisman with name: '{0}'; could not find " +
                        "talisman with this name", talismanString);
                equip_talisman_key_id = 0;
            } else {
                List<Integer> talismanList = talismanMap.get(talismanStringUpper);
                talismanList.removeAll(returnedTalismanKeyIds);
                if (talismanList.size() == 0) {
                    Logging.print("Failed to import talisman with name: '{0}'; could not find " +
                            "enough talismans with this name for this team", talismanString);
                    equip_talisman_key_id = 0;
                } else {
                    equip_talisman_key_id = talismanList.get(0);
                }
            }
        }

        // we're given shared skill names OR IDs... need to get a good ID
        // if given ID --> validate the ID
        // if given name --> validate the name & fetch

        int[] sharedSkillsOut = new int[]{-1, -1};
        for (int i = 1; i <= 2; i++) {
            String sharedSkillString = input.get("sharedSkill" + i).getAsString();
            int sharedSkillsOutIndex = i - 1;
            try { // numeric ID
                int sharedSkillId = Integer.parseInt(sharedSkillString);
                if (sharedSkillId != 0) {
                    if (!jsonUtils.idToAdventurer.containsKey(sharedSkillId)) {
                        Logging.print("Failed to import shared skill with numeric ID: '{0}'; could not find " +
                                "adventurer with this ID", sharedSkillId);
                        sharedSkillsOut[sharedSkillsOutIndex] = 0;
                    } else {
                        sharedSkillsOut[sharedSkillsOutIndex] = sharedSkillId;
                    }
                } else { // empty shared skill
                    sharedSkillsOut[sharedSkillsOutIndex] = 0;
                }
            } catch (NumberFormatException ignored) { // name/alias
                String sharedSkillStringUpper = sharedSkillString.toUpperCase(Locale.ROOT);
                if (!jsonUtils.nameToAdventurer.containsKey(sharedSkillStringUpper)) {
                    Logging.print("Failed to import shared skill with name/alias: '{0}'; could not find " +
                            "adventurer with this name", sharedSkillString);
                    sharedSkillsOut[sharedSkillsOutIndex] = 0;
                } else {
                    sharedSkillsOut[sharedSkillsOutIndex] = jsonUtils.nameToAdventurer.get(sharedSkillStringUpper).getId();
                }
            }
        }

        JsonObject out = new JsonObject();
        out.addProperty("unit_no", unit_no);
        out.addProperty("chara_id", chara_id);
        out.addProperty("equip_dragon_key_id", equip_dragon_key_id);
        out.addProperty("equip_weapon_body_id", equip_weapon_body_id);
        out.addProperty("equip_weapon_skin_id", equip_weapon_skin_id);
        out.addProperty("equip_crest_slot_type_1_crest_id_1", slot1WyrmprintsOut[0]);
        out.addProperty("equip_crest_slot_type_1_crest_id_2", slot1WyrmprintsOut[1]);
        out.addProperty("equip_crest_slot_type_1_crest_id_3", slot1WyrmprintsOut[2]);
        out.addProperty("equip_crest_slot_type_2_crest_id_1", slot2WyrmprintsOut[0]);
        out.addProperty("equip_crest_slot_type_2_crest_id_2", slot2WyrmprintsOut[1]);
        out.addProperty("equip_crest_slot_type_3_crest_id_1", slot3WyrmprintsOut[0]);
        out.addProperty("equip_crest_slot_type_3_crest_id_2", slot3WyrmprintsOut[1]);
        out.addProperty("equip_talisman_key_id", equip_talisman_key_id);
        out.addProperty("edit_skill_1_chara_id", sharedSkillsOut[0]);
        out.addProperty("edit_skill_2_chara_id", sharedSkillsOut[1]);
        return out;
    }

    public JsonArray convertToPartyList () {
        JsonArray outPartyList = new JsonArray();
        for (JsonElement jsonEle : teamsData.getAsJsonArray("teams")) {
            JsonObject teamOut = new JsonObject();

            JsonObject team = jsonEle.getAsJsonObject();
            int teamId = team.get("teamId").getAsInt();
            String teamName = team.get("teamName").getAsString();

            JsonArray members = team.get("members").getAsJsonArray();
            int memberSlot = 1;

            teamOut.addProperty("party_no", teamId);
            teamOut.addProperty("party_name", teamName);

            List<Integer> returnedDragonKeyIds = new ArrayList<>();
            List<Integer> returnedTalismanKeyIds = new ArrayList<>();

            JsonArray partySettingListOut = new JsonArray();
            for (JsonElement jsonEle2 : members) {
                JsonObject member = jsonEle2.getAsJsonObject();
                JsonObject outMember = convertMemberData(member, memberSlot, returnedDragonKeyIds, returnedTalismanKeyIds);
                if (outMember == null) { // failed to convert member obj (adventurer was invalid?)
                    partySettingListOut = getDefaultPartySettingList();
                    break;
                }
                returnedDragonKeyIds.add(outMember.get("equip_dragon_key_id").getAsInt());
                returnedTalismanKeyIds.add(outMember.get("equip_talisman_key_id").getAsInt());
                memberSlot++;
                partySettingListOut.add(outMember);
            }

            teamOut.add("party_setting_list", partySettingListOut);
            outPartyList.add(teamOut);
        }
        return outPartyList;
    }

    // 1: there are 54 teams, and each team is numbered from 1-54
    // 2: each team has 4 members, and each team member is numbered from 1-4
    // 3: any adventurer, dragon, talisman, weapon, weapon skin in the list must be owned
    // 4: there may not be duplicate wyrmprints on the same adventurer
    // 5: amount of the same wyrmprint on a team must not exceed the owned wyrmprint count
    // 6: amount of the same weapon on a team must not exceed the owned weapon count
    // 7: each adventurer must not have duplicate equipped shared skills
    // 8: there may not be duplicate dragon key IDs on the same team
    // 9: first character of the team must not be empty
    // 10: each chara_id, weapon_body_id, crest_id, edit_skill_id, weapon_skin_id must exist
    // 11: each edit_skill_id must be valid (its adventurer must have a shared skill),
    //     and its adventurer must have the 'is_unlock' flag set in 'party_list'
    // 12: if chara_id is empty, then all other fields must be empty
    // 13: edit_skill_2_chara_id must not be empty
    // 14: equipped weapon type must match weapon type of unit
    // 15: equipped weapon skin must match weapon type of unit

    public boolean validatePartyList (JsonArray partyList) {
        boolean returnVal = true;
        List<Integer> cond1_partyNums = new ArrayList<>();
        for (int i = 1; i <= 54; i++) {
            cond1_partyNums.add(i);
        }

        for (JsonElement jsonEle : partyList) {
            JsonObject party = jsonEle.getAsJsonObject();

            int party_no = party.get("party_no").getAsInt();

            // Condition 1
            if (cond1_partyNums.contains(party_no)) {
                cond1_partyNums.remove((Object) party_no);
            } else {
                Logging.print("Could not validate party_list: " +
                        "there was a duplicate or invalid 'party_no' of {0}", party_no);
                return false;
            }

            // Condition 2
            List<Integer> cond2_partyMemberNums = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                cond2_partyMemberNums.add(i);
            }

            // Condition 5
            HashMap<Integer, Integer> cond5_printIdToCountMap = new HashMap<>();
            // Condition 6
            HashMap<Integer, Integer> cond6_weaponIdToCountMap = new HashMap<>();
            // Condition 8
            List<Integer> cond8_dragonKeyIdList = new ArrayList<>();

            // Iterate through party members
            JsonArray partySettingList = party.get("party_setting_list").getAsJsonArray();
            for (JsonElement jsonEle2 : partySettingList) {
                JsonObject partyMember = jsonEle2.getAsJsonObject();

                int unit_no = partyMember.get("unit_no").getAsInt();
                int chara_id = partyMember.get("chara_id").getAsInt();
                int equip_dragon_key_id = partyMember.get("equip_dragon_key_id").getAsInt();
                int equip_weapon_body_id = partyMember.get("equip_weapon_body_id").getAsInt();
                int equip_weapon_skin_id = partyMember.get("equip_weapon_skin_id").getAsInt();
                int equip_talisman_key_id = partyMember.get("equip_talisman_key_id").getAsInt();

                int[] equip_crest_slot_type_1 = new int[]{
                    partyMember.get("equip_crest_slot_type_1_crest_id_1").getAsInt(),
                    partyMember.get("equip_crest_slot_type_1_crest_id_2").getAsInt(),
                    partyMember.get("equip_crest_slot_type_1_crest_id_3").getAsInt()
                };

                int[] equip_crest_slot_type_2 = new int[]{
                    partyMember.get("equip_crest_slot_type_2_crest_id_1").getAsInt(),
                    partyMember.get("equip_crest_slot_type_2_crest_id_2").getAsInt(),
                };

                int[] equip_crest_slot_type_3 = new int[]{
                    partyMember.get("equip_crest_slot_type_3_crest_id_1").getAsInt(),
                    partyMember.get("equip_crest_slot_type_3_crest_id_2").getAsInt(),
                };

                int[] edit_skill = new int[]{
                    partyMember.get("edit_skill_1_chara_id").getAsInt(),
                    partyMember.get("edit_skill_2_chara_id").getAsInt(),
                };

                // for Condition 14
                AdventurerMeta advMeta = null;
                WeaponMeta weaponMeta = null;
                // for Condition 15
                WeaponSkinMeta weaponSkinMeta = null;

                // Condition 2
                if (cond2_partyMemberNums.contains(unit_no)) {
                    cond2_partyMemberNums.remove((Object) unit_no);
                } else {
                    Logging.print("Could not validate party_list: " +
                            "there was a duplicate or invalid 'unit_no' of {0} in 'party_no' {1} ", unit_no, party_no);
                    returnVal = false;
                }

                // Condition 3
                if (equip_dragon_key_id != 0 &&
                        !jsonUtils.arrayHasValue("dragon_key_id", equip_dragon_key_id, "data", "dragon_list")) {
                    Logging.print("Could not validate party_list: " +
                            "there was a '{0}' of {1} that was not found in '{2}'",
                            "equip_dragon_key_id", String.valueOf(equip_dragon_key_id), "dragon_list");
                    returnVal = false;
                }
                if (chara_id != 0 &&
                        !jsonUtils.arrayHasValue("chara_id", chara_id, "data", "chara_list")) {
                    Logging.print("Could not validate party_list: " +
                                    "there was a '{0}' of {1} that was not found in '{2}'",
                            "chara_id", String.valueOf(chara_id), "chara_list");
                    returnVal = false;
                }
                if (equip_talisman_key_id != 0 &&
                        !jsonUtils.arrayHasValue("talisman_key_id", equip_talisman_key_id, "data", "talisman_list")) {
                    Logging.print("Could not validate party_list: " +
                                    "there was a '{0}' of {1} that was not found in '{2}'",
                            "equip_talisman_key_id", String.valueOf(equip_talisman_key_id), "talisman_list");
                    returnVal = false;
                }
                if (equip_weapon_body_id != 0 &&
                        !jsonUtils.arrayHasValue("weapon_body_id", equip_weapon_body_id, "data", "weapon_body_list")) {
                    Logging.print("Could not validate party_list: " +
                                    "there was a '{0}' of {1} that was not found in '{2}'",
                            "equip_weapon_body_id", String.valueOf(equip_weapon_body_id), "weapon_body_list");
                    returnVal = false;
                }
                if (equip_weapon_skin_id != 0 &&
                        !jsonUtils.arrayHasValue("weapon_skin_id", equip_weapon_skin_id, "data", "weapon_skin_list")) {
                    Logging.print("Could not validate party_list: " +
                                    "there was a '{0}' of {1} that was not found in '{2}'",
                            "equip_weapon_skin_id", String.valueOf(equip_weapon_skin_id), "weapon_skin_list");
                    returnVal = false;
                }

                // Condition 4
                for (int slot = 1; slot <= 3; slot++) {
                    List<Integer> cond4_printIdList = new ArrayList<>();
                    int[] equip_crest_array = new int[]{};
                    switch (slot) {
                        case 1: equip_crest_array = equip_crest_slot_type_1; break;
                        case 2: equip_crest_array = equip_crest_slot_type_2; break;
                        case 3: equip_crest_array = equip_crest_slot_type_3; break;
                    }
                    for (int printID : equip_crest_array) {
                        if (printID != 0) {
                            if (cond4_printIdList.contains(printID)) {
                                Logging.print("Could not validate party_list: " +
                                                "there was a duplicate 'equip_crest' ID {0} found for 'party_no' {1} 'unit_no' {2}",
                                        printID, party_no, unit_no);
                                returnVal = false;
                            }
                            cond4_printIdList.add(printID);
                            incrementCountMap(cond5_printIdToCountMap, printID); // for cond 5
                            // Condition 10 (ID validation)
                            if (!jsonUtils.idToPrint.containsKey(printID)) {
                                Logging.print("Could not validate party_list: " +
                                                "'party_no' {0} 'unit_no' {1} had non-existent 'equip_crest_id' of {2}",
                                        party_no, unit_no, printID);
                                returnVal = false;
                            }
                        }
                    }
                }

                // Condition 6
                incrementCountMap(cond6_weaponIdToCountMap, equip_weapon_body_id);

                // Condition 7
                if (edit_skill[0] != 0) {
                    if (edit_skill[0] == edit_skill[1]) { // check for dupe skill shared IDs
                        Logging.print("Could not validate party_list: " +
                                        "'unit_no' {0} of 'party_no' {1} had duplicate 'edit_skill' ID of {2}",
                                unit_no, party_no, edit_skill[0]);
                        returnVal = false;
                    }
                }

                // Condition 8
                if (equip_dragon_key_id != 0) {
                    if (cond8_dragonKeyIdList.contains(equip_dragon_key_id)) {
                        Logging.print("Could not validate party_list: " +
                                        "'party_no' {0} had duplicate 'equip_dragon_key_id' of {1}",
                                party_no, equip_dragon_key_id);
                        returnVal = false;
                    } else {
                        cond8_dragonKeyIdList.add(equip_dragon_key_id);
                    }
                }

                // Condition 9
                if (unit_no == 1 && chara_id == 0) {
                    Logging.print("Could not validate party_list: " +
                                    "'party_no' {0} had empty chara_id for the first slot",
                            party_no);
                    returnVal = false;
                }

                // Condition 10
                if (chara_id != 0 && !jsonUtils.idToAdventurer.containsKey(chara_id)) {
                    Logging.print("Could not validate party_list: " +
                                    "'party_no' {0} 'unit_no' {1} had non-existent 'chara_id' of {2}",
                            party_no, unit_no, chara_id);
                    returnVal = false;
                } else {
                    advMeta = (chara_id == 0) ? (null) : (jsonUtils.idToAdventurer.get(chara_id));
                }
                if (equip_weapon_body_id != 0 && !jsonUtils.idToWeapon.containsKey(equip_weapon_body_id)) {
                    Logging.print("Could not validate party_list: " +
                                    "'party_no' {0} 'unit_no' {1} had non-existent 'equip_weapon_body_id' of {2}",
                            party_no, unit_no, equip_weapon_body_id);
                    returnVal = false;
                } else {
                    weaponMeta = (equip_weapon_body_id == 0) ? (null) : (jsonUtils.idToWeapon.get(equip_weapon_body_id));
                }
                if (equip_weapon_skin_id != 0 && !jsonUtils.idToWeaponSkin.containsKey(equip_weapon_skin_id)) {
                    Logging.print("Could not validate party_list: " +
                                    "'party_no' {0} 'unit_no' {1} had non-existent 'equip_weapon_skin_id' of {2}",
                            party_no, unit_no, equip_weapon_skin_id);
                    returnVal = false;
                } else {
                    weaponSkinMeta = (equip_weapon_skin_id == 0) ? (null) : (jsonUtils.idToWeaponSkin.get(equip_weapon_skin_id));
                }
                // Wyrmprint ID validation above...
                for (int i = 0; i < 2; i++) {
                    int sharedSkillID = edit_skill[i];
                    if (sharedSkillID != 0) {
                        if (!jsonUtils.idToAdventurer.containsKey(sharedSkillID)) {
                            Logging.print("Could not validate party_list: " +
                                            "'party_no' {0} 'unit_no' {1} had non-existent 'edit_skill_chara_id' of {2}",
                                    party_no, unit_no, sharedSkillID);
                            returnVal = false;
                        } else { // This ID refers to an adventurer...
                            // Condition 11
                            if (!jsonUtils.idToAdventurer.get(sharedSkillID).hasSkillShare()) {
                                Logging.print("Could not validate party_list: " +
                                                "'party_no' {0} 'unit_no' {1} had 'edit_skill_chara_id' of {2}, " +
                                                "which refers to an adventurer without a shared skill",
                                        party_no, unit_no, sharedSkillID);
                                returnVal = false;
                            } else {
                                boolean hasThisSSUnlocked = jsonUtils.getValueFromObjInArray(
                                        "is_unlock_edit_skill", "chara_id", sharedSkillID,
                                        "data", "chara_list"
                                        ) == 1;
                                if (!hasThisSSUnlocked) {
                                    Logging.print("I shit my pants, {0}, {1}", chara_id, sharedSkillID);
                                    Logging.print("Could not validate party_list: " +
                                                    "'party_no' {0} 'unit_no' {1} had 'edit_skill_chara_id' of {2}, " +
                                                    "which refers to an adventurer whose shared skill is not unlocked",
                                            party_no, unit_no, sharedSkillID);
                                    returnVal = false;
                                }
                            }
                        }
                    }
                }

                // Condition 12
                if (chara_id == 0) {
                    boolean valid =
                        equip_dragon_key_id == 0 &&
                        equip_weapon_body_id == 0 &&
                        equip_weapon_skin_id == 0 &&
                        equip_crest_slot_type_1[0] == 0 &&
                        equip_crest_slot_type_1[1] == 0 &&
                        equip_crest_slot_type_1[2] == 0 &&
                        equip_crest_slot_type_2[0] == 0 &&
                        equip_crest_slot_type_2[1] == 0 &&
                        equip_crest_slot_type_3[0] == 0 &&
                        equip_crest_slot_type_3[1] == 0 &&
                        edit_skill[0] == 0 &&
                        edit_skill[1] == 0;
                    if (!valid) {
                        Logging.print("Could not validate party_list: " +
                                        "'party_no' {0} 'unit_no' {1} had empty 'chara_id', " +
                                        "but had another field that was non-empty",
                                party_no, unit_no);
                        returnVal = false;
                    }
                }

                // Condition 13
                if (edit_skill[1] == 0 && chara_id != 0) {
                    Logging.print("Could not validate party_list: " +
                                    "'party_no' {0} 'unit_no' {1} had empty 'edit_skill_2_chara_id'",
                            party_no, unit_no);
                    returnVal = false;
                }

                // Condition 14
                if (advMeta != null && weaponMeta != null) {
                    String advWeaponType = advMeta.getWeaponType();
                    String weaponType = weaponMeta.getWeaponTypeString();
                    if (!advWeaponType.equals(weaponType.toLowerCase(Locale.ROOT))) {
                        Logging.print("Could not validate party_list: " +
                                        "'party_no' {0} 'unit_no' {1} ({2}) had equipped weapon type" +
                                        " ({3}) that did not match its unit",
                                String.valueOf(party_no), String.valueOf(unit_no), advMeta.getName(), weaponType);
                        returnVal = false;
                    }
                }

                // Condition 15
                if (advMeta != null && weaponSkinMeta != null) {
                    String advWeaponType = advMeta.getWeaponType();
                    String weaponType = weaponSkinMeta.getWeaponTypeString();
                    if (!advWeaponType.equals(weaponType.toLowerCase(Locale.ROOT))) {
                        Logging.print("Could not validate party_list: " +
                                        "'party_no' {0} 'unit_no' {1} ({2}) had equipped weapon skin type" +
                                        " ({3}) that did not match its unit",
                                String.valueOf(party_no), String.valueOf(unit_no), advMeta.getName(), weaponType);
                        returnVal = false;
                    }
                }

            }

            // Condition 5
            for (Map.Entry<Integer, Integer> printIdToCount : cond5_printIdToCountMap.entrySet()) {
                int printID = printIdToCount.getKey();
                int count = printIdToCount.getValue();
                int equipableCount = jsonUtils.getValueFromObjInArray("equipable_count",
                        "ability_crest_id", printID, "data", "ability_crest_list");
                if (equipableCount == -1) { // print was not found
                    Logging.print("Could not validate party_list: " +
                                    "wyrmprint of ID {0} was not found in 'ability_crest_list'", printID);
                    returnVal = false;
                } else if (count > equipableCount) {
                    Logging.print("Could not validate party_list: " +
                            "for 'party_no' {0}, wyrmprint of ID {1} had equip count exceeding its 'equipable_count' of {2}",
                            party_no, count, equipableCount);
                    returnVal = false;
                }
            }

            // Condition 6
            for (Map.Entry<Integer, Integer> weaponIdToCount : cond6_weaponIdToCountMap.entrySet()) {
                int weaponID = weaponIdToCount.getKey();
                int count = weaponIdToCount.getValue();
                if (weaponID != 0) {
                    int equipableCount = jsonUtils.getValueFromObjInArray("equipable_count",
                            "weapon_body_id", weaponID, "data", "weapon_body_list");
                    if (equipableCount == -1) { // weapon was not found
                        Logging.print("Could not validate party_list: " +
                                "weapon of ID {0} was not found in 'weapon_body_list'", weaponID);
                        returnVal = false;
                    } else if (count > equipableCount) {
                        Logging.print("Could not validate party_list: " +
                                        "for 'party_no' {0}, weapon of ID {1} had equip count {2} exceeding its 'equipable_count' of {3}",
                                party_no, weaponID, count, equipableCount);
                        returnVal = false;
                    }
                }
            }

            // Condition 2
            if (!cond2_partyMemberNums.isEmpty()) {
                Logging.print("Could not validate party_list: " +
                        "there were 1 or more missing 'unit_no' in 'party_no' {1}, including: {0}",
                        cond2_partyMemberNums.get(0), party_no);
                returnVal = false;
            }

        }

        if (!cond1_partyNums.isEmpty()) {
            Logging.print("Could not validate party_list: " +
                    "there were 1 or more missing 'party_no', including: {0}", cond1_partyNums.get(0));
            returnVal = false;
        }

        return returnVal;
    }

    public void incrementCountMap(HashMap<Integer, Integer> map, int key) {
        if (map.containsKey(key)) {
            int value = map.get(key);
            map.remove(key);
            map.put(key, value + 1);
        } else {
            map.put(key, 1);
        }
    }

    public void run () {
        if (!JsonUtils.checkIfJsonObject(teamDataPath)) {
            System.out.println("Teams data not found... generating new teams data.");
            teamsData.add("talismans", getDefaultTalismans());
            boolean toGenerateFromGameData =
                    SaveEditor.passYesNo("Generate new teams data from savefile (y) or generate blank team data? (n)");
            teamsData.add("teams", generateNewTeams(toGenerateFromGameData));
        } else {
            System.out.println("Importing teams data...");
            teamsData = jsonUtils.getJsonObject(teamDataPath);
            if (SaveEditor.passYesNo("Generate new teams data?")) {
                boolean toGenerateFromGameData =
                        SaveEditor.passYesNo("\tGenerate new teams data from savefile (y) or generate blank team data? (n)");
                teamsData.remove("teams");
                teamsData.add("teams", generateNewTeams(toGenerateFromGameData));
            }
        }

        System.out.println("Exporting teams data...");
        exportTeams();

        if (SaveEditor.passYesNo("Generate new talismans?")) {
            boolean toExit = false;
            while (!toExit) {
                String talismanName = SaveEditor.input("\tEnter talisman name (Enter 'exit' to return)");
                if (talismanName.toLowerCase(Locale.ROOT).equals("exit")) {
                    toExit = true;
                    break;
                }

                String adventurerName = SaveEditor.input("\tEnter adventurer name");
                String countStr = SaveEditor.input("\tEnter talisman count (1-4)");
                String id1Str = SaveEditor.input("\tEnter ability ID 1");
                String id2Str = SaveEditor.input("\tEnter ability ID 2");
                String id3Str = SaveEditor.input("\tEnter ability ID 3");

                int count = -1;
                int id1 = -1;
                int id2 = -1;
                int id3 = -1;

                try {
                    count = Integer.parseInt(countStr);
                    id1 = Integer.parseInt(id1Str);
                    id2 = Integer.parseInt(id2Str);
                    id3 = Integer.parseInt(id3Str);
                } catch (NumberFormatException ignored) {}

                if (count == -1 || id1 == -1 || id2 == -1 || id3 == -1) {
                    System.out.println("Error: Could not parse one of the count or ability ID values as an integer");
                    return;
                }

                JsonObject newTalisman = new JsonObject();
                newTalisman.addProperty("name", talismanName);
                newTalisman.addProperty("adventurerName", adventurerName);
                newTalisman.addProperty("count", count);
                newTalisman.addProperty("id1", id1);
                newTalisman.addProperty("id2", id2);
                newTalisman.addProperty("id3", id3);
                teamsData.get("talismans").getAsJsonArray().add(newTalisman);
                System.out.println("\tAdded new talisman.");
            }
        }

        System.out.println("Initializing talisman data...");
        initializeTalismans();

        if (SaveEditor.passYesNo("Paste teams.json data into savedata party list and export?")) {
            JsonArray partyList = convertToPartyList();
            System.out.println("Validating imported team data...");
            if (!validatePartyList(partyList)) {
                System.out.println("There were 1 or more issues with the exported party data; " +
                        "could not export to the savedata.");
            } else {
                System.out.println("Pasting teams data into savedata...");
                jsonUtils.jsonData.get("data").getAsJsonObject().remove("party_list");
                jsonUtils.jsonData.get("data").getAsJsonObject().add("party_list", partyList);

                System.out.print("Exporting savedata...");
                if(jsonUtils.isSaveData2Present()){
                    SaveEditor.yesNoQuestion(
                            "savedata2.txt already exists in this directory. Would you like to overwrite it?",
                            () -> jsonUtils.setOverwrite(true));
                }
                System.out.println();
                jsonUtils.writeToFile();
            }
        }

        System.exit(99);
    }
}
