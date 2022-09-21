package butters;

import java.util.ArrayList;
import java.util.List;

public class LibInfo {
    
    List<FieldInfo> fields = new ArrayList<>();
    List<MethodInfo> methods = new ArrayList<>();
    
    String className;
    String nativelib;
    String file;
    String language;
    String pkg;
    
    public LibInfo() {
        this("<classname>", "<file>", "<language>", "<pkg>");
    }
    
    public LibInfo(String className, String file, String language, String pkg) {
        this.className = className;
        this.file = file;
        this.language = language;
        this.pkg = pkg;
    }
    
    public LibInfo(String file, String language, String pkg) {
        this.file = file;
        this.language = language;
        this.pkg = pkg;
    }
    
    public void addField(FieldInfo fd) {
        fields.add(fd);
    }
    
    public void addMethod(MethodInfo md) {
        methods.add(md);
    }
    
    public List<FieldInfo> getFields() {
        return fields;
    }
    
    public List<MethodInfo> getMethods() {
        return methods;
    }
    
    public void setClassName(String name) {
        className = name;
    }
    
    public void setNativelib(String nativelib) {
        this.nativelib = nativelib;
    }
    
    public void setFile(String file) {
        this.file = file;
    }
    
    public void setPackage(String pkg) {
        this.pkg = pkg;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getFile() {
        return file;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public String getPackage() {
        return pkg;
    }
    
    public String getNativelib() {
        return nativelib;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("==== FIELDS ====");
        sb.append('\n');
        for (FieldInfo fd : fields) {
            sb.append(fd);
            sb.append('\n');
        }
        sb.append("==== METHODS ====");
        sb.append('\n');
        for (MethodInfo md : methods) {
            sb.append(md);
            sb.append('\n');
        }
        return sb.toString();
    }
}
