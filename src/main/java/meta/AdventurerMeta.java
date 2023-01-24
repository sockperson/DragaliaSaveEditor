package meta;

public class AdventurerMeta {

    private String name, title;
    private int id, elementId, maxHp, maxStr, baseRarity, maxA3Level;
    private boolean hasSkillShare, hasManaSpiral;

    public static AdventurerMeta DUMMY = new AdventurerMeta("UNKNOWN", "", 0,
            0, 0, 0, 0, false, false, 0);

    public AdventurerMeta(String name, String title, int id, int elementId, int maxHp, int maxStr,
                          int baseRarity, boolean hasSkillShare, boolean hasManaSpiral, int maxA3Level){
        this.name = name;
        this.title = title;
        this.id = id;
        this.elementId = elementId;
        this.maxHp = maxHp;
        this.maxStr = maxStr;
        this.baseRarity = baseRarity;
        this.hasSkillShare = hasSkillShare;
        this.hasManaSpiral = hasManaSpiral;
        this.maxA3Level = maxA3Level;
    }

    public String getName(){ return name; }
    public String getTitle(){ return title; }
    public int getId(){ return id; }
    public int getElementId(){ return elementId; }
    public int getMaxHp(){ return maxHp; }
    public int getMaxStr(){ return maxStr; }
    public int getBaseRarity(){ return baseRarity; }
    public boolean hasSkillShare(){ return hasSkillShare; }
    public boolean hasManaSpiral(){ return hasManaSpiral; }
    public int getMaxA3Level(){ return maxA3Level; }

}
