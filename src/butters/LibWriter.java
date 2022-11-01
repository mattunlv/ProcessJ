package butters;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public class LibWriter {
    
    // Name of primitive data types in ProcessJ that can be
    // represented by wrapper classes in Java; note that there
    // isn't a wrapper class for a String in Java
    static final String[] WRAPPERS = new String[] { "String", 
            "Character", "Byte", "Short", "Integer", "Long", 
            "Float", "Double", "Boolean" };
    
    // Name of primitives data types in ProcessJ
    static final String[] PRIMITIVES = new String[] { "string", 
            "char", "byte", "short", "int", "long", 
            "float", "double", "boolean" };
    
    // Conversion table from Java to ProcessJ types: wrapper -> primitive
    static final Hashtable<String, String> ht = new Hashtable<>();
    
    // String template file locator
    final String STG_NATIVELIB = "resources/stringtemplates/butters/nativelib.stg";
    
    // Collection of templates
    STGroup stGroup;
    
    // This instance holds information with respect to the
    // native ProcessJ library
    LibInfo lib;
    
    static {
        for (int i = 0; i<WRAPPERS.length; ++i) {
            ht.put(WRAPPERS[i], PRIMITIVES[i]);
        }
    }
    
    private String convertClassname(String type) {
        if ( ht.containsKey(type) ) {
            return ht.get(type);
        }
        return type;
    }
    
    public LibWriter(LibInfo lib) {
        this.lib = lib;
        stGroup = new STGroupFile(STG_NATIVELIB);
    }
    
    public void writePJ() {
        ST stNativelib = stGroup.getInstanceOf("Nativelib");
        ST stPragmas = stGroup.getInstanceOf("Pragmas");
        
        stPragmas.add("class", lib.getClassName());
        stPragmas.add("file", lib.getFile());
        stPragmas.add("path", lib.getNativelib());
        stPragmas.add("language", lib.getLanguage());
        stPragmas.add("package", lib.getPackage());
        
        List<String> fields = new ArrayList<>();
        List<String> methods = new ArrayList<>();
        
        if ( !lib.getFields().isEmpty() ) {
            // Grab formal fields
            ST stFieldDecl = stGroup.getInstanceOf("FieldDecl");
            for (FieldInfo f : lib.getFields()) {
                stFieldDecl.add("type", f.getType());
                stFieldDecl.add("name", f.getName());
                stFieldDecl.add("value", f.getValue());
                fields.add(stFieldDecl.render());
            }
        }
        
        if ( !lib.getMethods().isEmpty() ) {
            // Grab formal parameters
            for (MethodInfo md : lib.getMethods()) {
                ST stParams = stGroup.getInstanceOf("ParamDecls");
                List<String> types = new ArrayList<>();
                List<String> names = new ArrayList<>();
                for (ParamInfo param : md.getParams()) {
                    types.add(convertClassname(param.getType()));
                    names.add(param.getName());
                }
                stParams.add("types", types);
                stParams.add("names", names);
                // Grab method signature
                ST stMethodDecls = stGroup.getInstanceOf("MethodDecls");
                String type = md.getReturnType()==null? md.getName() : md.getReturnType();
                stMethodDecls.add("type", convertClassname(type));
                stMethodDecls.add("name", convertClassname(md.getName()));
                stMethodDecls.add("ref", convertClassname(lib.getClassName()));
                if ( !md.getParams().isEmpty() )
                    stMethodDecls.add("params", stParams.render());
                methods.add(stMethodDecls.render());
            }
        }
        
        stNativelib.add("pragmas", stPragmas.render());
        stNativelib.add("fields", fields);
        stNativelib.add("methods", methods);
        System.out.println(stNativelib.render());
    }
    
    public void writeJava() {
        ST stClasslib = stGroup.getInstanceOf("Classlib");
        
        List<String> fields = new ArrayList<>();
        List<String> methods = new ArrayList<>();
        
        if ( !lib.getFields().isEmpty() ) {
            // Grab formal fields
            ST stFieldDef = stGroup.getInstanceOf("FieldDef");
            for (FieldInfo f : lib.getFields()) {
                stFieldDef.add("type", f.getType());
                stFieldDef.add("name", f.getName());
                stFieldDef.add("value", f.getValue());
                fields.add(stFieldDef.render());
            }
        }
        
        if ( !lib.getMethods().isEmpty() ) {
            // Grab formal parameters
            for (MethodInfo md : lib.getMethods()) {
                ST stParams = stGroup.getInstanceOf("ParamDecls");
                ST stParamDef = stGroup.getInstanceOf("ParamDef");
                List<String> types = new ArrayList<>();
                List<String> names = new ArrayList<>();
                for (ParamInfo param : md.getParams()) {
                    types.add(convertClassname(param.getType()));
                    names.add(param.getName());
                }
                stParams.add("types", types);
                stParams.add("names", names);
                stParamDef.add("names", names);
                // Grab method signature
                ST stMethodDef = stGroup.getInstanceOf("MethodDef");
                String type = md.getReturnType()==null? md.getName() : md.getReturnType();
                stMethodDef.add("type", type);
                stMethodDef.add("name", md.getName());
                stMethodDef.add("ref", lib.getClassName());
                if ( !type.equals("void") )
                    stMethodDef.add("return", true);
                if ( !md.getParams().isEmpty() ) {
                    stMethodDef.add("actuals", stParams.render());
                    stMethodDef.add("formals", stParamDef.render());
                }
                methods.add(stMethodDef.render());
            }
        }
        
        stClasslib.add("package", lib.getPackage());
        stClasslib.add("name", lib.getFile());
        stClasslib.add("fields", fields);
        stClasslib.add("methods", methods);
        System.out.println(stClasslib.render());
    }
}
