package meta;

public class WyrmprintMeta {

    private String name;
    private int id, rarity;

    public WyrmprintMeta(String name, int id, int rarity){
        this.name = name;
        this.id = id;
        this.rarity = rarity;
    }

    public String getName(){ return name; }
    public int getId(){ return id; }
    public int getRarity(){ return rarity; }
}
