package butters;

import java.util.ArrayList;
import java.util.List;

public class FieldInfo {
    
    List<String> modifiers = new ArrayList<>();
    String type;
    String name;
    String value;
    
    public FieldInfo(String type, String name) {
        this.type = type;
        this.name = name;
    }
    
    public void addModifier(String modifier) {
        modifiers.add(modifier);
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<String> getModifiers() {
        return modifiers;
    }
    
    public String getType() {
        return type;
    }
    
    public String getName() {
        return name;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("- FieldDecl:");
        sb.append('\n');
        sb.append(" + modifiers:{");
        int count = modifiers.size();
        for (int i = 0; i < count; ++i) {
            sb.append(modifiers.get(i));
            if ( i!=count - 1 )
                sb.append(", ");
        }
        sb.append('}');
        sb.append('\n');
        sb.append(" + type: ");
        sb.append(type);
        sb.append('\n');
        sb.append(" + name: ");
        sb.append(name);
        sb.append('\n');
        sb.append(" + value: ");
        sb.append(value);
        return sb.toString();
    }
}
