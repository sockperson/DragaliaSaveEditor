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
        return AdventurerMeta.getElementString(elementId);
    }

    public String getWeaponTypeString () {
        return AdventurerMeta.getWeaponTypeString(elementId);
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
