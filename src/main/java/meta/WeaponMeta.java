package meta;

public class WeaponMeta {

    private String name, weaponSeries;
    private int id, elementId, weaponTypeId, rarity;

    public WeaponMeta(String name, int id, int elementId, int weaponTypeId, String weaponSeries,
                      int rarity){
        this.name = name;
        this.id = id;
        this.elementId = elementId;
        this.weaponTypeId = weaponTypeId;
        this.weaponSeries = weaponSeries;
        this.rarity = rarity;
    }

    public String getName(){ return name; }
    public int getId(){ return id; }
    public int getElementId(){ return elementId; }
    public int getWeaponTypeId(){ return weaponTypeId; }
    public String getWeaponSeries(){ return weaponSeries; }
    public int getRarity(){ return rarity; }
}
