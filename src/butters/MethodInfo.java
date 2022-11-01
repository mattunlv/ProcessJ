package butters;

import java.util.ArrayList;
import java.util.List;

public class MethodInfo {
    
    List<String> modifiers = new ArrayList<>();
    List<ParamInfo> params = new ArrayList<>();
    String returnType;
    String name;
    
    public MethodInfo() { }
    
    public void addModifier(String modifier) {
        modifiers.add(modifier);
    }
    
    public void addParam(ParamInfo param) {
        params.add(param);
    }
    
    public void addReturnType(String returnType) {
        this.returnType = returnType;
    }
    
    public void addName(String name) {
        this.name = name;
    }
    
    public List<String> getModifiers() {
        return modifiers;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public String getName() {
        return name;
    }
    
    public List<ParamInfo> getParams() {
        return params;
    }
    
    public boolean isConstructorDecl() {
        return returnType == null;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("- MethodDecl:");
        sb.append('\n');
        sb.append(" + modifiers:{");
        int count = modifiers.size();
        for (int i = 0; i < count; ++i) {
            sb.append(modifiers.get(i));
            if (i != count - 1)
                sb.append(", ");
        }
        sb.append('}');
        sb.append('\n');
        sb.append(" + returnType: ");
        sb.append(returnType);
        sb.append('\n');
        sb.append(" + name: ");
        sb.append(name);
        sb.append('\n');
        sb.append(" + params:{");
        count = params.size();
        for (int i = 0; i < count; ++i) {
            sb.append(params.get(i));
            if (i != count - 1)
                sb.append(", ");
        }
        sb.append('}');
        return sb.toString();
    }
}
