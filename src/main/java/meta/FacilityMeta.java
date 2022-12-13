package meta;

public class FacilityMeta {

    private String name;
    private int id, maxLevel, maxBuildCount;
    private boolean isResourceFacility;

    public FacilityMeta(String name, int id, int maxLevel,
                        boolean isResourceFacility, int maxBuildCount){
        this.name = name;
        this.id = id;
        this.maxLevel = maxLevel;
        this.isResourceFacility = isResourceFacility;
        this.maxBuildCount = maxBuildCount;
    }

    public String getName(){ return name; }
    public int getId(){ return id; }
    public int getMaxLevel(){ return maxLevel; }
    public boolean isResourceFacility(){ return isResourceFacility; }
    public int getMaxBuildCount(){ return maxBuildCount; }

    public String getDetailId(){ return id + "" + (maxLevel > 9 ? maxLevel : "0" + maxLevel); } //AAAAAABB
}
