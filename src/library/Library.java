package library;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Hashtable;

import ast.AST;
import ast.Compilation;
import ast.ConstantDecl;
import ast.Modifier;
import ast.ParamDecl;
import ast.Pragma;
import ast.PrimitiveType;
import ast.ProcTypeDecl;
import ast.ProtocolTypeDecl;
import ast.RecordTypeDecl;
import ast.Type;
import utilities.PJBugManager;
import utilities.Log;
import utilities.MessageType;
import utilities.PJMessage;
import utilities.Visitor;

/**
 * Fields related to pragma values. If new pragmas are added to the
 * language, the decodePragmas() must be rewritten and fields added
 * for the new pragmas.
 */
public class Library {
    
    public static String[] validPragmas = new String[] { "LIBRARY", "LANGUAGE",
            "NATIVE", "NATIVELIB", "FILE" };
    public static int[] pragmaArgCount = new int[] { 0, 1, 0, 1, 1 };
    public static String[][] pragmaArgValues = new String[][] { {},
            { "C", "PROCESSJ", "JAVASCRIPT", "JAVA" }, {}, {}, {} };
    public static Hashtable<String, Integer> ht = new Hashtable<String, Integer>();
    public static Hashtable<String, String> pragmaTable = new Hashtable<String, String>();
    static {
        int index = 0;
        for (String s : validPragmas)
            ht.put(s, index++);
    }

    /**
     * This method <b>must</b> be called before any of the other methods in this class.
     */
    public static void decodePragmas(Compilation c) {
        // Fill the pragmaTable hash table with values.
        //System.out.println("is c == null?" + (c==null) + c.pragmas());

        //System.out.println("is c.pragmas() == null?" + c.pragmas()==null);
        for (Pragma p : c.pragmas()) {
            String name = p.pname().getname().toUpperCase(); // TODO: perhaps error if pragma names are lowercase....
            Log.log("Looking up pragma '" + name + "'.");
            if (!ht.containsKey(name))
                PJBugManager.INSTANCE.reportMessageAndExit(
                        new PJMessage.Builder()
                        .addArguments("Illegal pragma '" + name + "'.")
                        .build(), MessageType.PRINT_STOP);
            int argCount = pragmaArgCount[ht.get(name)];
            if (argCount != 0 && p.value() == null)
                PJBugManager.INSTANCE.reportMessageAndExit(
                        new PJMessage.Builder()
                        .addArguments("Pragma '" + name + "' requires 1 parameter, none was given.")
                        .build(), MessageType.PRINT_STOP);
            if (argCount == 0 && p.value() != null)
                PJBugManager.INSTANCE.reportMessageAndExit(
                        new PJMessage.Builder()
                        .addArguments("Pragma '" + name + "' does not require any parameters.")
                        .build(), MessageType.PRINT_STOP);
            if (pragmaTable.containsKey(name))
                PJBugManager.INSTANCE.reportMessageAndExit(
                        new PJMessage.Builder()
                        .addArguments("Pragma '" + name + "' repeated.")
                        .build(), MessageType.PRINT_STOP);
            else {
                Log.log("Entering <"
                        + name
                        + ","
                        + (p.value() != null ? p.value().substring(1,
                        p.value().length() - 1) : "")
                        + "> into pragmaTable.");
                pragmaTable.put(
                        name,
                        p.value() != null ? p.value().substring(1,
                                p.value().length() - 1) : "");
            }
            // TODO: check values against pragmaArgValues[][];
        }
    }

    public static void generateLibraries(Compilation c) {
        // There are two types of libraries:
        // - Libraries written in ProcessJ
        //   ProcessJ libraries must set the #LIBRARY flag
        // - Libraries written in some native language
        //   Native libraries should set the #LIBRARY flag
        //   and also the #NATIVE "language" string.
        //   It may use #INCLUDE "module" to include modules in the
        //   native language.
        //

        // There are three different kinds of libraries:
        // NATIVELIB libraries that map directly to a different language's library like e.g., math.h
        //   NATIVELIB libraries require the following pragmas set:
        //     LIBRARY
        //     NATIVELIB "name of the native library" (e.g. "math.h")
        //     LANGUAGE "name of the native language" (e.g. "C")
        //     FILE "name of the pj library" (e.g. "math")
        // NATIVE libraries are libraries written in the language set by the "LANGUAGE" pragma.
        //   NATIVE libraries require the following pragmas set:
        //     LIBRARY
        //     NATIVE
        //     LANGUAGE
        //     FILE
        // ProcessJ libraries are 100% written in ProcessJ.
        //   ProcessJ libraries require the following pragmas set:
        //     LIBRARY
        //     FILE
        if (pragmaTable.containsKey("LIBRARY")) {
            // FILE and LANGUAGE pragmas must be set.
            if (!pragmaTable.containsKey("LANGUAGE"))
                PJBugManager.INSTANCE.reportMessageAndExit(
                        new PJMessage.Builder()
                        .addArguments("Missing LANGUAGE pragma.")
                        .build(), MessageType.PRINT_STOP);
            if (!pragmaTable.containsKey("FILE"))
                PJBugManager.INSTANCE.reportMessageAndExit(
                        new PJMessage.Builder()
                        .addArguments("Missing FILE pragma.")
                        .build(), MessageType.PRINT_STOP);
            if (c.packageName() == null)
                PJBugManager.INSTANCE.reportMessageAndExit(
                        new PJMessage.Builder()
                        .addArguments("Library files must declare a package name.")
                        .build(), MessageType.PRINT_STOP);

            System.out.println("Library.java: LIBRARY pragma detected; generating library code.");
            if (pragmaTable.containsKey("NATIVELIB")) {
                Log.log("Library.java: NATIVELIB pragma detected; mapping library to existing native library.");
                // NATIVE cannot be set
                if (pragmaTable.containsKey("NATIVE"))
                    PJBugManager.INSTANCE.reportMessageAndExit(
                            new PJMessage.Builder()
                            .addArguments("pragmas NATIVE and NATIVELIB cannot be used together.")
                            .build(), MessageType.PRINT_STOP);
                // pragma LANGUAGE cannot be PROCESSJ.
                if (pragmaTable.get("LANGUAGE").equals("PROCESSJ"))
                    PJBugManager.INSTANCE.reportMessageAndExit(
                            new PJMessage.Builder()
                            .addArguments("The implementation language for a NATIVE library cannot be ProcessJ.")
                            .build(), MessageType.PRINT_STOP);

                String language = pragmaTable.get("LANGUAGE");
                if (language.equals("C")) {
                    c.visit(new CheckProcedures<AST>(true));
                    generateNativeCLibFiles(c);
                } else
                    PJBugManager.INSTANCE.reportMessageAndExit(
                            new PJMessage.Builder()
                            .addArguments("Unknown native language '" + language + "'.")
                            .build(), MessageType.PRINT_STOP);
            } else if (pragmaTable.containsKey("NATIVE")) {
                Log.log("Library.java: NATIVE pragma detected; generating native header and implementation files and ProcessJ header file.");
                // NATIVELIB cannot be set
                if (pragmaTable.containsKey("NATIVELIB"))
                    PJBugManager.INSTANCE.reportMessageAndExit(
                            new PJMessage.Builder()
                            .addArguments("pragmas NATIVE and NATIVELIB cannot be used together.")
                            .build(), MessageType.PRINT_STOP);
                if (pragmaTable.get("LANGUAGE").equals("PROCESSJ"))
                    PJBugManager.INSTANCE.reportMessageAndExit(
                            new PJMessage.Builder()
                            .addArguments("The implementation language for a NATIVE library cannot be ProcessJ.")
                            .build(), MessageType.PRINT_STOP);

                String language = pragmaTable.get("LANGUAGE");
                if (language.equals("C")) {
                    c.visit(new Library.CheckProcedures<AST>(true));
                    generateNativeFiles(c);
                } else
                    PJBugManager.INSTANCE.reportMessageAndExit(
                            new PJMessage.Builder()
                            .addArguments("Unknown native language '" + language + "'.")
                            .build(), MessageType.PRINT_STOP);
            } else {
                Log.log("Library.java: Generating ProcessJ library");
                c.visit(new CheckProcedures<AST>(false));
                generateProcessJFiles(c);
            }
        } else {
            Log.log("Library.java: Not a library file.");
        }
    }

    /**
     * Checks for correct use of the modifier 'native' and for the presence or absence of procedure bodies.
     *
     * @author Matt Pedersen
     *
     */
    private static class CheckProcedures<T extends AST> extends Visitor<T> {
        private boolean nativeLib = false;

        public CheckProcedures(boolean nativeLib) {
            this.nativeLib = nativeLib;
        }

        public T visitProcTypeDecl(ProcTypeDecl pd) {
            if (nativeLib) {
                // All NATIVELIB and NATIVE files cannot contain procedures with ProcessJ bodies.
                if (pd.body() != null)
                    PJBugManager.INSTANCE.reportMessageAndExit(
                            new PJMessage.Builder()
                            .addAST(pd)
                            .addArguments("Procedure '" + pd.name().getname()
                            + "' is cannot have a body in a non-ProcessJ library file.")
                            .build(), MessageType.PRINT_STOP);
                boolean nativeModifierFound = false;
                // TODO: just call the correct method in Modifier.java
                for (Modifier m : pd.modifiers())
                    nativeModifierFound |= (m.getModifier() == Modifier.NATIVE);
                if (!nativeModifierFound)
                    PJBugManager.INSTANCE.reportMessageAndExit(
                            new PJMessage.Builder()
                            .addAST(pd)
                            .addArguments("Procedure '" + pd.name().getname() + "' must be declared native.")
                            .build(), MessageType.PRINT_STOP);
            } else {
                // Regular ProcessJ Library
                if (pd.body() == null)
                    PJBugManager.INSTANCE.reportMessageAndExit(
                            new PJMessage.Builder()
                            .addAST(pd)
                            .addArguments("Procedure '" + pd.name().getname() + "' must have a body.")
                            .build(), MessageType.PRINT_STOP);

                boolean nativeModifierFound = false;
                for (Modifier m : pd.modifiers())
                    nativeModifierFound |= (m.getModifier() == Modifier.NATIVE);
                if (nativeModifierFound)
                    PJBugManager.INSTANCE.reportMessageAndExit(
                            new PJMessage.Builder()
                            .addAST(pd)
                            .addArguments("Procedure '" + pd.name().getname() + "' cannot be declared native.")
                            .build(), MessageType.PRINT_STOP);
            }
            return null;
        }

        public T visitRecordTypeDecl(RecordTypeDecl rd) {
            if (nativeLib)
                PJBugManager.INSTANCE.reportMessageAndExit(
                        new PJMessage.Builder()
                        .addArguments("Native libraries cannot contain record type declarations ('"
                        + rd.name().getname() + "'.")
                        .build(), MessageType.PRINT_STOP);
            return null;
        }

        public T visitProtocolTypeDecl(ProtocolTypeDecl pd) {
            if (nativeLib)
                PJBugManager.INSTANCE.reportMessageAndExit(
                        new PJMessage.Builder()
                        .addArguments("Native libraries cannot contain protocol type declarations ('"
                        + pd.name().getname() + "'.")
                        .build(), MessageType.PRINT_STOP);
            return null;
        }

        public T visitConstantDecl(ConstantDecl cd) {
            boolean nativeModifierFound = false;
            for (Modifier m : cd.modifiers())
                nativeModifierFound |= (m.getModifier() == Modifier.NATIVE);

            if (nativeLib) {
                if (!nativeModifierFound)
                    PJBugManager.INSTANCE.reportMessageAndExit(
                            new PJMessage.Builder()
                            .addAST(cd)
                            .addArguments("Constant declaration '"
                            + cd.var().name().getname()
                            + "' must be declared native.")
                            .build(), MessageType.PRINT_STOP);
                if (cd.var().init() != null)
                    PJBugManager.INSTANCE.reportMessageAndExit(
                            new PJMessage.Builder()
                            .addAST(cd)
                            .addArguments("Native constant declaration '"
                            + cd.var().name().getname()
                            + "' cannot have an initializer.")
                            .build(), MessageType.PRINT_STOP);
            } else {
                if (nativeModifierFound)
                    PJBugManager.INSTANCE.reportMessageAndExit(
                            new PJMessage.Builder()
                            .addAST(cd)
                            .addArguments("Constant declaration '"
                            + cd.var().name().getname()
                            + "' cannot declared native.")
                            .build(), MessageType.PRINT_STOP);
                if (cd.var().init() == null)
                    ;
                PJBugManager.INSTANCE.reportMessageAndExit(
                        new PJMessage.Builder()
                        .addAST(cd)
                        .addArguments("Constant declaration '"
                        + cd.var().name().getname()
                        + "' must have an initializer.")
                        .build(), MessageType.PRINT_STOP);
            }
            return null;
        }
    }

    /**
     * Generates the header (.h) and (empty) implementation file (.c) for a NATIVELIB library
     *
     * For exampe for a math.pj with the following pragmas: #pragma LIBRARY; #pragma FILE "math"; #pragma NATIVELIB
     * "math.h"; #pragma LANGUAGE "C";
     *
     * in package 'std'
     *
     * .h file: #ifndef _LIB_STD_MATH_ #define _LIB_STD_MATH_ #include <math.h> #endif
     *
     * .c file: #ifndef _STD_MATH_H #define _STD_MATH_H #include "std_math.h" #endif
     *
     * @param c
     *            Compilation
     */
    public static void generateNativeCLibFiles(Compilation c) {
        String libFileName = pragmaTable.get("NATIVELIB"); // name of the existing native library file
        Log.log("Library.generateNativeCLibFiles: Native Library: "
                + libFileName);

        String pjClibFileName = c.packageName().getname() + "_"
                + pragmaTable.get("FILE");
        String pjHeaderFileName = pragmaTable.get("FILE");
        Log.log("Library.generateNativeCLibFiles: ProcessJ C header file: "
                + pjClibFileName + ".h");
        Log.log("Library.generateNativeCLibFiles: ProcessJ C implementation file: "
                + pjClibFileName + ".c");
        Log.log("Library.generateNativeCLibFiles: ProcessJ header file: "
                + pjHeaderFileName + ".pj");

        //Generate the .h & .c file
        FileWriter hfw = null, fw = null;
        BufferedWriter headerFile = null, file = null;
        ;
        try {
            hfw = new FileWriter(pjClibFileName + ".h");
            fw = new FileWriter(pjClibFileName + ".c");
            headerFile = new BufferedWriter(hfw);
            file = new BufferedWriter(fw);

            // Write the header file (.h)
            headerFile.write("#ifndef _LIB_"
                    + pjClibFileName.toUpperCase().replace(".", "_") + "_");
            headerFile.newLine();
            headerFile.write("#define _LIB_"
                    + pjClibFileName.toUpperCase().replace(".", "_") + "_");
            headerFile.newLine();
            headerFile.write("#include <" + libFileName + ">");
            headerFile.newLine();
            headerFile.write("#endif");
            headerFile.newLine();
            headerFile.newLine();
            headerFile.close();

            // write the implementation file (.c)
            file.write("#ifndef _"
                    + pjClibFileName.toUpperCase().replace(".", "_") + "_H");
            file.newLine();
            file.write("#define _"
                    + pjClibFileName.toUpperCase().replace(".", "_") + "_H");
            file.newLine();
            file.write("#include \"" + pjClibFileName + ".h\"");
            file.newLine();
            file.write("#endif");
            file.newLine();
            file.close();

            Log.log("Generated file \"" + pjClibFileName
                    + ".h\" - this file must be moved to lib/C/include/");
            Log.log("Generated file \"" + pjClibFileName
                    + ".c\" - this file must be moved to lib/C/include/");
            Log.log("Provided file \"" + pjHeaderFileName
                    + ".pj must be moved to inlcude/C/"
                    + c.packageName().getname() + "/" + pjHeaderFileName
                    + ".inc");

            headerFile.close();
            file.close();
        } catch (Exception e) {
            System.err
                    .println("Error encountered while writing library stub files.");
            e.printStackTrace();
        }
    }

    public static void generateNativeFiles(Compilation c) {
        c.visit(new Library.GenerateNativeCode<AST>());
    }

    private static class GenerateNativeCode<T extends AST> extends Visitor<T> {
        private String pjClibFileName;
        private String pjHeaderFileName;
        private FileWriter hfw = null, fw = null;
        private BufferedWriter headerFile = null, file = null;
        private String packageName;

        public T visitCompilation(Compilation c) {
            pjClibFileName = c.packageName().getname() + "_"
                    + pragmaTable.get("FILE");
            pjHeaderFileName = pragmaTable.get("FILE");
            System.out.println("Library.GenerateNativeCode.visitCompilation: ProcessJ C header file: "
                            + pjClibFileName + ".h");
            System.out.println("Library.GenerateNativeCode.visitCompilation: ProcessJ C implementation file: "
                            + pjClibFileName + ".c");
            System.out.println("Library.GenerateNativeCode.visitCompilation: ProcessJ header file: "
                            + pjHeaderFileName + ".pj");
            packageName = c.packageName().getname();

            //Generate the .h and .c file
            try {
                hfw = new FileWriter(pjClibFileName + ".h");
                fw = new FileWriter(pjClibFileName + ".c");
                headerFile = new BufferedWriter(hfw);
                file = new BufferedWriter(fw);

                // Write the header file (.h)
                headerFile.write("#ifndef _LIB_"
                        + pjClibFileName.toUpperCase().replace(".", "_") + "_");
                headerFile.newLine();
                headerFile.write("#define _LIB_"
                        + pjClibFileName.toUpperCase().replace(".", "_") + "_");
                headerFile.newLine();
                headerFile.newLine();
                headerFile
                        .write("// Add #include statements and constants here");
                headerFile.newLine();
                headerFile.newLine();
                // write the implementation file (.c)
                file.write("#ifndef _"
                        + pjClibFileName.toUpperCase().replace(".", "_") + "_H");
                file.newLine();
                file.write("#define _"
                        + pjClibFileName.toUpperCase().replace(".", "_") + "_H");
                file.newLine();
                file.write("#include \"" + pjClibFileName + ".h\"");
                file.newLine();

                c.typeDecls().visit(this);

                headerFile.write("#endif");
                headerFile.newLine();
                headerFile.newLine();
                headerFile.close();
                file.write("#endif");
                file.newLine();
                file.close();

                System.out.println("Generated file \"" + pjClibFileName
                        + ".h\" - this file must be moved to lib/C/include/");
                System.out.println("Generated file \"" + pjClibFileName
                        + ".c\" - this file must be moved to lib/C/include/");
                System.out.println("Provided file \"" + pjHeaderFileName
                        + ".pj must be moved to inlcude/C/"
                        + c.packageName().getname() + "/" + pjHeaderFileName
                        + ".inc");

                headerFile.close();
                file.close();
            } catch (Exception e) {
                System.err.println("Error encountered while writing library stub files.");
                e.printStackTrace();
            }
            return null;
        }

        public T visitConstantDecl(ConstantDecl cd) {
            boolean nativeModifierFound = false;
            for (Modifier m : cd.modifiers())
                nativeModifierFound |= (m.getModifier() == Modifier.NATIVE);

            if (nativeModifierFound) {
                PJBugManager.INSTANCE.reportMessageAndExit(
                        new PJMessage.Builder()
                        .addAST(cd)
                        .addArguments("Native keyword not allowed in non NATIVELIB library constants.")
                        .build(), MessageType.PRINT_STOP);
            }
            return null;
        }

        public T visitProcTypeDecl(ProcTypeDecl pd) {
            try {
                String procedure = "";
                // Only primitive types (not Timer or Barrier) can be used as parameter and return types
                Type returnType = pd.returnType();
                if (!(returnType instanceof PrimitiveType))
                    PJBugManager.INSTANCE.reportMessageAndExit(
                            new PJMessage.Builder()
                            .addAST(pd)
                            .addArguments("Native C library procedures must return a primitive type.")
                            .build(), MessageType.PRINT_STOP);
                PrimitiveType pt = (PrimitiveType) returnType;
                if (pt.getKind() == PrimitiveType.BarrierKind
                        || pt.getKind() == PrimitiveType.TimerKind)
                    PJBugManager.INSTANCE.reportMessageAndExit(
                            new PJMessage.Builder()
                            .addAST(pd)
                            .addArguments("Native C library procedures cannot return barrier or timer types.")
                            .build(), MessageType.PRINT_STOP);
                if (pt.getKind() == PrimitiveType.StringKind)
                    procedure += "char* ";
                else if (pt.getKind() == PrimitiveType.BooleanKind)
                    procedure += "int ";
                else
                    procedure += pt.typeName() + " ";

                // Procedure Name
                // packagename_procname_signature
                procedure += packageName + "_" + pd.name().getname() + "_";
                for (ParamDecl param : pd.formalParams()) {
                    if (!(param.type() instanceof PrimitiveType))
                        PJBugManager.INSTANCE.reportMessageAndExit(
                                new PJMessage.Builder()
                                .addAST(pd)
                                .addArguments("Native C library procedures can only "
                                +"accept primitive types as parameters."
                                + param.type().typeName())
                                .build(), MessageType.PRINT_STOP);
                    pt = (PrimitiveType) param.type();
                    if (pt.getKind() == PrimitiveType.BarrierKind
                            || pt.getKind() == PrimitiveType.TimerKind)
                        PJBugManager.INSTANCE.reportMessageAndExit(
                                new PJMessage.Builder()
                                .addAST(pd)
                                .addArguments("Native C library procedures cannot accept "
                                +"barrier or timer types as parameters.")
                                .build(), MessageType.PRINT_STOP);
                    procedure += pt.signature();
                }
                procedure += "(";
                int i = 0;
                for (ParamDecl param : pd.formalParams()) {
                    pt = (PrimitiveType) param.type();
                    if (pt.getKind() == PrimitiveType.StringKind)
                        procedure += "char*";
                    else if (pt.getKind() == PrimitiveType.BooleanKind)
                        procedure += "int";
                    else
                        procedure += pt.typeName();
                    procedure += " " + param.name();
                    if (i < pd.formalParams().size() - 1)
                        procedure += ", ";
                    i++;
                }
                procedure += ") ";
                headerFile.write(procedure + ";");
                headerFile.newLine();
                headerFile.newLine();
                file.write(procedure + "{");
                file.newLine();
                file.write("  // implementation code goes here.");
                file.newLine();
                file.write("}");
                file.newLine();
                file.newLine();
            } catch (Exception e) {
                System.err.println("Error encountered while writing library stub files.");
                e.printStackTrace();
            }
            return null;
        }
    }

    public static void generateProcessJFiles(Compilation c) {
        // TODO: nothing??
    }
}