package meta;

import java.util.List;

public class WeaponMeta {

    private String name, weaponSeries;
    private int id, elementId, weaponTypeId, rarity;
    private List<Integer> passiveAbilityIdList;
    private boolean hasWeaponBonus;

    private String functionalName;

    public WeaponMeta(String name, int id, int elementId, int weaponTypeId, String weaponSeries,
                      int rarity, List<Integer> passiveAbilityIdList, boolean hasWeaponBonus){
        this.name = name;
        this.id = id;
        this.elementId = elementId;
        this.weaponTypeId = weaponTypeId;
        this.weaponSeries = weaponSeries;
        this.rarity = rarity;
        this.passiveAbilityIdList = passiveAbilityIdList;
        this.hasWeaponBonus = hasWeaponBonus;

        this.functionalName = genFunctionalName();
    }

    public String getName(){ return name; }
    public int getId(){ return id; }
    public int getElementId(){ return elementId; }
    public int getWeaponTypeId(){ return weaponTypeId; }
    public String getWeaponSeries(){ return weaponSeries; }
    public int getRarity(){ return rarity; }
    public List<Integer> getPassiveAbilityIdList(){ return passiveAbilityIdList; }
    public boolean hasWeaponBonus(){ return hasWeaponBonus; }
    public String getFunctionalName(){ return functionalName; }

    public String getElementString () {
        switch (elementId) {
            case 1: return "Flame";
            case 2: return "Water";
            case 3: return "Wind";
            case 4: return "Light";
            case 5: return "Shadow";
            case 99: return "Null";
        }
        return "?";
    }

    public String getWeaponTypeString () {
        switch (weaponTypeId) {
            case 1: return "Sword";
            case 2: return "Blade";
            case 3: return "Dagger";
            case 4: return "Axe";
            case 5: return "Lance";
            case 6: return "Bow";
            case 7: return "Wand";
            case 8: return "Staff";
            case 9: return "Manacaster";
        }
        return "?";
    }

    private String genFunctionalName () {
        String element = getElementString();
        String weaponType = getWeaponTypeString();
        String seriesShorthand;

        switch (weaponSeries) {
            case "Core": seriesShorthand = "Core"; break;
            case "Void": seriesShorthand = "Core"; break;
            case "Chimeratech": seriesShorthand = "Chimeratech"; break;
            case "High Dragon": seriesShorthand = "HDT"; break;
            case "Agito": seriesShorthand = "Agito"; break;
            case "Primal Dragon": seriesShorthand = "PDT"; break;
            default: seriesShorthand = "?";
        }

        if (seriesShorthand.equals("?")) {
            return name;
        }

        String out = element + " " + seriesShorthand + " " + weaponType;
        return (out.contains("?")) ? (null) : (out);
    }

}
