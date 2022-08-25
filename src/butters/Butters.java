package butters;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import ast.Compilation;
import ast.Pragma;

public class Butters {

    public static String[] validPragmas = new String[] { "LIBRARY", "LANGUAGE",
            "NATIVE", "NATIVELIB", "FILE" };
    
    private static final String PUBLIC = "public";
//    private static final String PRIVATE = "private";
    
    private static final String CTR_NAME = "make";
    
    private static String convertClassname(String type) {
        int idx = type.lastIndexOf('.');
        if ( idx!=-1 ) {
            type = type.substring(idx+1, type.length());
        }
        return type;
    }
    
    private static final Set<Class<?>> WRAPPER_CLASSES = createWrapperClasses();
    
    private static Set<Class<?>> createWrapperClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(Boolean.class);
        classes.add(boolean.class);
        classes.add(Character.class);
        classes.add(char.class);
        classes.add(Byte.class);
        classes.add(byte.class);
        classes.add(Short.class);
        classes.add(short.class);
        classes.add(Integer.class);
        classes.add(int.class);
        classes.add(Long.class);
        classes.add(long.class);
        classes.add(Float.class);
        classes.add(float.class);
        classes.add(Double.class);
        classes.add(double.class);
        classes.add(Void.class);
        classes.add(void.class);
        classes.add(String.class);
        return classes;
    }
    
    private static boolean isPrimitiveOrWrapperClass(Class<?> cl) {
        return WRAPPER_CLASSES.contains(cl) ||
               (cl.isArray() ? WRAPPER_CLASSES.contains(cl.getComponentType()) : false);
    }
    
    public static void readMyJRE(LibInfo lib, final String path) throws ClassNotFoundException, MalformedURLException {
        int access = Modifier.PRIVATE;
        String name = path.replace("\"", "");
        System.err.println(">>> " + name);
        Class<?> cl = Class.forName(name);
        // --------------------------------------------
        // Get fields
        for (Field f : cl.getFields()) {
            access = f.getModifiers();
            if ( !Modifier.isStatic(access) && !Modifier.isPublic(access) ) {
                continue;
            }
            if ( !isPrimitiveOrWrapperClass(f.getType()) ) {
                continue;
            }
            lib.addField(new FieldInfo(f.getType().getSimpleName(), f.getName()));
        }
        // --------------------------------------------
        // Get constructors
        MethodInfo methodInfo = null;
        for (Constructor<?> c : cl.getConstructors()) {
            access = c.getModifiers();
            if ( !Modifier.isPublic(access) ) {
                continue;
            }
            methodInfo = new MethodInfo();
            methodInfo.addModifier(PUBLIC);
            methodInfo.addName(CTR_NAME + convertClassname(c.getName()));
            // Although constructors do not have a return type, one is
            // always needed for ProcessJ, i.e., a constructor becomes
            // <modifier> native <type> make<type>(...) { } in ProcessJ,
            // where make<type> becomes the name of the ProcessJ procedure
            methodInfo.addReturnType(convertClassname(c.getName()));
            boolean found = true;
            for (Parameter param : c.getParameters()) {
                if ( !isPrimitiveOrWrapperClass(param.getType()) ) {
                    found = false;
                    break;
                }
                methodInfo.addParam(new ParamInfo(param.getType().getSimpleName(), param.getName()));
            }
            if ( found ) {
                lib.addMethod(methodInfo);
            }
        }
        // --------------------------------------------
        // Get methods
        for (Method m : cl.getMethods()) {
            Class<?> rtype = m.getReturnType();
            if ( !isPrimitiveOrWrapperClass(rtype) ) {
                continue;
            }
            access = m.getModifiers(); 
            if ( !Modifier.isStatic(access) && !Modifier.isPublic(access) ) {
                continue;
            }
            methodInfo = new MethodInfo();
            methodInfo.addModifier(PUBLIC);
            methodInfo.addName(m.getName());
            methodInfo.addReturnType(convertClassname(rtype.getSimpleName()));
            boolean found = true;
            for (Parameter param : m.getParameters()) {
                if ( !isPrimitiveOrWrapperClass(param.getType()) ) {
                    found = false;
                    break;
                }
                methodInfo.addParam(new ParamInfo(param.getType().getSimpleName(), param.getName()));
            }
            if ( found ) {
                lib.addMethod(methodInfo);
            }
        }
    }

    // This is just another silly implementation of a visitor that is
    // used to rewrite methods and fields that are 'public', 'public'
    // and 'static', 'public' and 'final', or 'public' 'static' and
    // 'final' for the ProcessJ compiler
    public static void decodePragmas(Compilation c) throws ClassNotFoundException, MalformedURLException {
        LibInfo lib = new LibInfo();
        for (Pragma p : c.pragmas()) {
            String name = p.pname().getname();
            String value = p.value();
            if ( value==null ) {
                // TODO: error out!!??
            } else {
                value = value.replace("\"", "");
            }
            if ( name.equals(validPragmas[0]) ) { // "LIBRARY"
                lib.setClassName(value);
            }
            if ( name.equals(validPragmas[1]) ) {
                lib.setLanguage(value);
            }
            if ( name.equals(validPragmas[3]) ) { // "NATIVELIB
                lib.setNativelib(value);
            }
            if ( name.equals(validPragmas[4]) ) { // "FILE"
                lib.setFile(value);
            }
        }
        lib.setPackage(c.packageName);
        // Read the 'Xxx.class' file
        readMyJRE(lib, lib.getNativelib());
        LibWriter lw = new LibWriter(lib);
        lw.writePJ();
        System.out.println("==============================================================================");
        lw.writeJava();
    }
    
    public static void blah() throws IOException, ClassNotFoundException {
        FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
        byte[] jlo = Files.readAllBytes(fs.getPath("modules", "java.base", "java/lang/String.class"));
        URI uri = fs.getPath("modules", "java.base", "java/lang/String.class").toUri();
        URLClassLoader urlClassLoader = URLClassLoader.newInstance(new URL[] { uri.toURL() });
        Class<?> clazz = urlClassLoader.loadClass("java.lang.String");
        System.err.println(">> " + clazz.getCanonicalName());
    }
    
    public static void main(String[] args) {
        try {
            LibInfo lib = new LibInfo("", "", "");
            lib.setClassName("String");
            String s = "java/lang/String.class";
            readMyJRE(lib, s);
            LibWriter lw = new LibWriter(lib);
            lw.writePJ();
            System.out.println("==============================================================================");
            lw.writeJava();
//            blah();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
