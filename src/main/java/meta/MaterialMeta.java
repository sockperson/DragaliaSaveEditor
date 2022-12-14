package meta;

public class MaterialMeta {

    private String name, category;
    private int id;

    public MaterialMeta(String name, int id, String category){
        this.name = name;
        this.id = id;
        this.category = category;
    }

    public String getName(){ return name; }
    public int getId(){ return id; }
    public String getCategory(){ return category; }

}
