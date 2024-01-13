import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import meta.*;

import java.time.Instant;
import java.util.Random;

public class Builders {

    public static final Random rng = new Random();

    public static JsonObject buildTalisman(int portraitId, String[] combo, int keyIdOffset) {
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

    public static JsonObject buildRandomTalisman(int id, int keyIdOffset) {
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
    public static JsonObject buildUnit(AdventurerMeta adventurerData, int getTime) {
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
            out.addProperty("skill_1_level", adventurerData.getMaxS1Level());
            out.addProperty("skill_2_level", adventurerData.getMaxS2Level());
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
            out.addProperty("is_unlock_edit_skill", adventurerData.hasSkillShareByDefault() ? 1 : 0);
            out.add("mana_circle_piece_id_list", new JsonArray());
            out.addProperty("list_view_flag", 1);
        }
        return out;
    }

    //Returns a built dragon in savedata.txt format
    public static JsonObject buildDragon(DragonMeta dragonData, int keyIdMin, int keyIdOffset) {
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
    public static JsonObject buildDragonFromExisting(DragonMeta dragonData, int keyId, int getTime, boolean isLocked) {
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

    public static JsonObject buildDragonAlbumData(DragonMeta dragonData) {
        JsonObject out = new JsonObject();
        boolean has5UB = dragonData.has5UB();
        int level = dragonData.getMaxLevel();

        out.addProperty("dragon_id", dragonData.getId());
        out.addProperty("max_level", level);
        out.addProperty("max_limit_break_count", has5UB ? 5 : 4);
        return out;
    }

    //build facility from existing facility
    public static JsonObject buildFacility(FacilityMeta fac, int keyId, int x, int y){
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
    public static JsonObject buildFacility(FacilityMeta fac, int keyIdMin, int keyIdOffset){
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

    public static JsonObject buildWeapon(WeaponMeta weaponData, int getTime) {
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
    public static JsonObject buildWyrmprint(WyrmprintMeta printData, int getTime, boolean isFavorite) {
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
}
