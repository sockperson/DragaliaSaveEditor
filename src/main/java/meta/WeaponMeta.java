package meta;

import java.util.List;

public class WeaponMeta {

    private String name, weaponSeries;
    private int id, elementId, weaponTypeId, rarity;
    private List<Integer> passiveAbilityIdList;
    private boolean hasWeaponBonus;

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
    }

    public String getName(){ return name; }
    public int getId(){ return id; }
    public int getElementId(){ return elementId; }
    public int getWeaponTypeId(){ return weaponTypeId; }
    public String getWeaponSeries(){ return weaponSeries; }
    public int getRarity(){ return rarity; }
    public List<Integer> getPassiveAbilityIdList(){ return passiveAbilityIdList; }
    public boolean hasWeaponBonus(){ return hasWeaponBonus; }

}
