package butters;

public class ParamInfo {
    
    String type;
    String name;
    
    public ParamInfo(String type, String name) {
        this.type = type;
        this.name = name;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public String getName() {
        return name;
    }
    
    public String toString() {
        return "ParamDecl:[type: " + type + ", name: " + name + "]";
    }
}
