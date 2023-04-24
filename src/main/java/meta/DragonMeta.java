package meta;

public class DragonMeta {

    private String name;
    private int id, elementId, a1Max, a2Max, rarity;
    private boolean has5UB, hasA2;

    public DragonMeta(String name, int id, int elementId, int a1Max, int a2Max,
                          int rarity, boolean has5UB, boolean hasA2){
        this.name = name;
        this.id = id;
        this.elementId = elementId;
        this.a1Max = a1Max;
        this.a2Max = a2Max;
        this.rarity = rarity;
        this.has5UB = has5UB;
        this.hasA2 = hasA2;
    }

    public String getName(){ return name; }
    public int getId(){ return id; }
    public int getElementId(){ return elementId; }
    public int getA1Max(){ return a1Max; }
    public int getA2Max(){ return a2Max; }
    public int getRarity(){ return rarity; }
    public boolean has5UB(){ return has5UB; }
    public boolean hasA2(){ return hasA2; }

    public int getMaxLevel(){
        switch(rarity){
            case 3: return 60;
            case 4: return 80;
            case 5:
                if(has5UB){
                    return 120;
                }
                return 100;
        }
        return -1;
    }

    public int getMaxXp(){
        switch(rarity){
            case 3: return 277320;
            case 4: return 625170;
            case 5:
                if(has5UB){
                    return 3365620;
                }
                return 1240020;
        }
        return -1;
    }

}
