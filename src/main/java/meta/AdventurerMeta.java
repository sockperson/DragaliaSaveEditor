package meta;

public class AdventurerMeta {

    private String name;
    private int id, elementId, maxHp, maxStr, baseRarity;
    private boolean hasSkillShare, hasManaSpiral;

    public AdventurerMeta(String name, int id, int elementId, int maxHp, int maxStr,
                          int baseRarity, boolean hasSkillShare, boolean hasManaSpiral){
        this.name = name;
        this.id = id;
        this.elementId = elementId;
        this.maxHp = maxHp;
        this.maxStr = maxStr;
        this.baseRarity = baseRarity;
        this.hasSkillShare = hasSkillShare;
        this.hasManaSpiral = hasManaSpiral;
    }

    public String getName(){ return name; }
    public int getId(){ return id; }
    public int getElementId(){ return elementId; }
    public int getMaxHp(){ return maxHp; }
    public int getMaxStr(){ return maxStr; }
    public int getBaseRarity(){ return baseRarity; }
    public boolean hasSkillShare(){ return hasSkillShare; }
    public boolean hasManaSpiral(){ return hasManaSpiral; }

}