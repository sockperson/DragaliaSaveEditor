package meta;

public class WeaponSkinMeta {

    private String name;
    private int id, weaponTypeId;
    private boolean isPlayable;

    public WeaponSkinMeta(String name, int id, int weaponTypeId, boolean isPlayable) {
        this.name = name;
        this.id = id;
        this.weaponTypeId = weaponTypeId;
        this.isPlayable = isPlayable;
    }

    public String getName() { return name; }
    public int getId() { return id; }
    public int getWeaponTypeId() { return weaponTypeId; }
    public boolean isPlayable() { return isPlayable; }

    public String getWeaponTypeString() {
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
}
