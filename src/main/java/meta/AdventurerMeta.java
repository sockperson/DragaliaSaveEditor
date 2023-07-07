package meta;

import java.util.Locale;

public class AdventurerMeta {

    private String name, title, manaCircleType, elementType, weaponType;
    private int id, elementId, maxHp, maxStr, maxLimitBreakCount, maxA3Level;
    private int minHp3, minHp4, minHp5, minStr3, minStr4, minStr5, baseRarity, kscapeLabelId;

    private boolean hasSkillShare, hasManaSpiral;

    public AdventurerMeta(String name, String title, int id, int elementId, int maxHp, int maxStr,
                          int maxLimitBreakCount, boolean hasSkillShare, boolean hasManaSpiral, int maxA3Level,
                          int minHp3, int minHp4, int minHp5, int minStr3, int minStr4, int minStr5, int baseRarity,
                          String manaCircleType, String elementType, String weaponType, int kscapeLabelId
                          ){
        this.name = name;
        this.title = title;
        this.id = id;
        this.elementId = elementId;
        this.maxHp = maxHp;
        this.maxStr = maxStr;
        this.maxLimitBreakCount = maxLimitBreakCount;
        this.hasSkillShare = hasSkillShare;
        this.hasManaSpiral = hasManaSpiral;
        this.maxA3Level = maxA3Level;
        this.minHp3 = minHp3;
        this.minHp4 = minHp4;
        this.minHp5 = minHp5;
        this.minStr3 = minStr3;
        this.minStr4 = minStr4;
        this.minStr5 = minStr5;
        this.baseRarity = baseRarity;
        this.manaCircleType = manaCircleType;
        this.elementType = elementType;
        this.weaponType = weaponType;
        this.kscapeLabelId = kscapeLabelId;
    }

    public String getName(){ return name; }
    public String getTitle(){ return title; }
    public String getElementType(){ return elementType.toLowerCase(Locale.ROOT); }
    public String getWeaponType(){ return weaponType.toLowerCase(Locale.ROOT); }
    public int getId(){ return id; }
    public int getElementId(){ return elementId; }
    public int getMaxHp(){ return maxHp; }
    public int getMaxStr(){ return maxStr; }
    public int getMaxLimitBreakCount(){ return maxLimitBreakCount; }
    public boolean hasSkillShare(){ return hasSkillShare; }
    public boolean hasManaSpiral(){ return hasManaSpiral; }
    public int getMaxA3Level(){ return maxA3Level; }
    public int getBaseRarity() {return baseRarity; }
    public int getKscapeLabelId() { return kscapeLabelId; }


    public int getMinHp () {
        switch (baseRarity) {
            case 3: return minHp3;
            case 4: return minHp4;
            case 5: return minHp5;
        }
        System.out.println("Invalid base rarity when returning minHp");
        return 0;
    }

    public int getMinStr () {
        switch (baseRarity) {
            case 3: return minStr3;
            case 4: return minStr4;
            case 5: return minStr5;
        }
        System.out.println("Invalid base rarity when returning minStr");
        return 0;
    }

    public int getMinA1Level () {
        switch (manaCircleType) {
            case "MC_0404":
            case "MC_0502":
            case "MC_0504":
            case "MC_0507":
            case "MC_0511":
            case "MC_0513":
            case "MC_0514":
                return 1;
            case "MC_0405":
                return 2;
        }
        return 0;
    }

    public int getMinFsLevel () {
        switch (manaCircleType) {
            case "MC_0504":
            case "MC_0514":
                return 1;
        }
        return 0;
    }
}
