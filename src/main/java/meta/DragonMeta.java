package meta;

import java.util.HashMap;
import java.util.Random;

public class DragonMeta {

    private String name;
    private int baseId, id, elementId, a1Max, a2Max, rarity;
    private boolean has5UB, hasA2;

    public DragonMeta(String name, int baseId, int id, int elementId, int a1Max, int a2Max,
                          int rarity, boolean has5UB, boolean hasA2){
        this.name = name;
        this.baseId = baseId;
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

    // [baseId][01][1/2]
    public int getDragonStoryId(int num) {
        return (baseId * 1000) + (10) + (num);
    }

    private static final int TALONSTONE = 201005001;
    private static final int SUCCULENT_DRAGONFRUIT = 102001003;
    private static final int SUNLIGHT_ORE = 201011001;

    // if youre upgrading dragon bond to max...
    // should add materials from dragons roost
    // values from: https://dragalialost.wiki/w/Dragon%27s_Roost
    public static HashMap<Integer, Integer> getDragonsRoostGifts(int initialLevel) {
        HashMap<Integer, Integer> matIdToValue = new HashMap<>();
        Random rng = new Random();
        int talonstones = 0;
        if (initialLevel < 10) {
            talonstones += 3 + rng.nextInt(3); // 3 - 5
        }
        if (initialLevel < 20) {
            matIdToValue.put(SUCCULENT_DRAGONFRUIT, 2 + rng.nextInt(3)); // 2 - 4
        }
        if (initialLevel < 25) {
             switch(rng.nextInt(3)) { // 5, 7, or 10
                 case 0: talonstones += 5; break;
                 case 1: talonstones += 7; break;
                 case 2: talonstones += 10; break;
             }
        }
        if (initialLevel < 30) {
            matIdToValue.put(SUNLIGHT_ORE, 1); // 1
        }
        matIdToValue.put(TALONSTONE, talonstones);
        return matIdToValue;
    }

}
