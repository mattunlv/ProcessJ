package org.processj;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.processj.ast.AST;
import org.processj.ast.Compilation;
import org.processj.butters.Butters;
import org.processj.codegen.Helper;
import org.processj.codegen.java.CodeGenJava;
import org.processj.codegen.cpp.CodeGenCPP;
import org.processj.library.Library;
import org.processj.namechecker.ResolveImports;
import org.processj.parser.Parser;
import org.processj.printers.ParseTreePrinter;
import org.processj.rewriters.CastRewrite;
import org.processj.rewriters.IOCallsRewrite;
import org.processj.lexer.Lexer;
import org.processj.utilities.PJBugManager;
import org.processj.utilities.ConfigFileReader;
import org.processj.utilities.Language;
import org.processj.utilities.Log;
import org.processj.utilities.Settings;
import org.processj.utilities.SymbolTable;
import org.processj.utilities.VisitorMessageNumber;

/**
 * ProcessJ compiler.
 *
 * @author ben
 */
public class ProcessJc {

    // Kinds of available options for the ProcessJ compiler
    public static enum OptionType {
        STRING,
        BOOLEAN;
    }

    public static class Option {
        protected String fieldName;
        protected String optionName;
        protected OptionType optionType;
        protected String description;

        public Option(String field, String option, OptionType type, String desc) {
            this.fieldName = field;
            this.optionName = option;
            this.optionType = type;
            this.description = desc;
        }
        
        public Option(String field, String option, String desc) {
            this(field, option, OptionType.BOOLEAN, desc);
        }
    }

    // List of available options for the ProcessJ compiler
    public static final Option[] OPTIONS = {
            new Option("showColor", "-showColor", "Use color on terminals that support ansi espace codes"),
            new Option("help", "-help", "Show this help message and exit"),
            new Option("include", "-include", OptionType.STRING, "Override the default include directory"),
            new Option("showMessage", "-showMessage", "Show all error and warning messages when available"),
            new Option("target", "-target", OptionType.STRING, "Specify the target language -- C++, Java (default)"),
            new Option("version", "-version", "Print version information and exit"),
            new Option("visitAll", "-visitAll", "Generate all parse tree visitors"),
            new Option("showTree", "-showTree", "Show the AST constructed from the parser"),
            new Option("install", "-install", "Install an existing or user-defined org.processj.library"),
            new Option("nativelib", "-nativelib", ""),
            new Option("userlib", "-userlib", ""),
            new Option("pjfile", "-pjfile", OptionType.STRING, ""),
            new Option("javafile", "-javafile", OptionType.STRING, ""),
    };

    // <--
    // Fields used by the ProcessJ compiler
    public boolean showColor = false;
    public boolean help = false;
    public String include = null;
    public boolean showMessage = true;
    public Language target = Settings.language;
    public boolean version = false;
    public boolean visitAll = false;
    public boolean showTree = false;
    // -->

    // <--
    // Field used by the Butters tool
    public boolean install = false;
    public boolean userlib = false;
    public boolean nativelib = false;
    public String pjfile = null;
    public String javafile = null;
    // -->

    private List<String> inputFiles = new ArrayList<>();
    private String[] args = null;
    private Properties config = ConfigFileReader.openConfiguration();

    /**
     * Program execution begins here.
     *
     * @param args
     *          A vector of command arguments passed to the compiler.
     */
    public static void main(String[] args) {
        // Send frequency file over HTTP to the ProcessJ server but
        // only if the size of the error file is 1MB or more.
        // Otherwise, we ignore the request made by the compiler
//        FrequencyFileProcessing.updateFrequency();
        ProcessJc pJc = new ProcessJc(args);
        // Do we have any arguments??
        if(args.length == 2) { // @0: -include, @1: path

            PJBugManager.ReportErrorAndExitWithUsage(VisitorMessageNumber.RESOLVE_IMPORTS_100);

        }

        if(pJc.help) {

            printUsage();

            // TODO: Remove this, this is bad form
            return;

        }
        if ( pJc.version ) {
            pJc.version();
        }
        // TODO: This emits a null pointer exception
        //Settings.includeDir = pJc.include;
        AST root = null;

        // Process Butters source file, one by one
        if ( pJc.install ) {
            // Process nativelib
            if ( pJc.nativelib ) {
                File inFile = new File(pJc.pjfile);
                Lexer s = null;
                Parser p = null;
                try {
                    String absoluteFilePath = inFile.getAbsolutePath();
                    // Set the package and filename
                    PJBugManager.INSTANCE.setFileName(absoluteFilePath);
                    PJBugManager.INSTANCE.setPackageName(absoluteFilePath);
                    s = new Lexer(new java.io.FileReader(absoluteFilePath));
                    p = new Parser(s);
                } catch(Exception e) {

                    PJBugManager.ReportMessageAndExit(e.getMessage());

                }
                try {
                    java_cup.runtime.Symbol r = ((Parser) p).parse();
                    root = (AST) r.value;
                    Compilation c = ((Compilation) root);
                    // This is needed in order to resolve the name of the package
                    // the user-defined or native org.processj.library belongs to
                    if ( c.getPackageName()!=null ) {
                        c.packageName = ResolveImports.packageNameToString(c.getPackageName());
                    }
                    Butters.decodePragmas(c);
                    System.out.println("** LIBRARY COMPLITED SUCCESSFULLY **");
                } catch (Exception e) {

                    //e.printStackTrace
                    PJBugManager.ReportMessageAndExit(e.getMessage());

                }
            } else if ( pJc.userlib ){
                // TODO: move files to the correct directory??
            } else {

                PJBugManager.ReportMessageAndExit("Must specify if the org.processj.library is 'native' or 'user-defined'");

            }
        }
        if(pJc.userlib || pJc.nativelib) {

            PJBugManager.ReportMessageAndExit("Missing command '-install'");

        }
        // Process source file, one by one
        for(String f : pJc.inputFiles) {
            File inFile = new File(f);
            Lexer s = null;
            Parser p = null;
            try {
                String absoluteFilePath = inFile.getAbsolutePath();
                // Set the package and filename
                PJBugManager.INSTANCE.setFileName(absoluteFilePath);
                PJBugManager.INSTANCE.setPackageName(absoluteFilePath);
                s = new Lexer(new java.io.FileReader(absoluteFilePath));
                p = new Parser(s);
            } catch (Exception e) {
                PJBugManager.ReportMessageAndExit(e.getMessage());
            }

            try {
                java_cup.runtime.Symbol r = ((Parser) p).parse();
                root = (AST) r.value;
                //TODO: handle org.processj.syntax error!!
            } catch (Exception e) {
                PJBugManager.ReportMessageAndExit(e.getMessage());
            }

            // Cast the result from the parse to a Compilation -- this is
            // the root of the tree
            Compilation c = (Compilation) root;
            // Set the absolute path, file, and package name from where this
            // compilation is created
            System.out.println("-- Setting absolute path, file and package name for '" + inFile.getName() + "'.");
            c.fileName = inFile.getName();
            // The parent's path of the compiled file
            String parentPath = inFile.getAbsolutePath();
            // The parent's absolute path of the compiled file
            c.path = parentPath.substring(0, parentPath.lastIndexOf(File.separator));
            // A package declaration is optional -- this can be null
            if(c.getPackageName()   != null) {
                c.packageName = ResolveImports.packageNameToString(c.getPackageName());
            }
            // Decode pragmas -- these are used for generating stubs from libraries.
            // No regular program would have them
            Library.decodePragmas(c);
            Library.generateLibraries(c);

            // This table will hold all the top level types
            SymbolTable globalTypeTable = new SymbolTable("Main file: " + PJBugManager.INSTANCE.getFileName());

            // Dump log messages if true
            if(pJc.visitAll) {
                Log.startLogging();
            }

            // Dump generated AST
            if(pJc.showTree) {
                c.visit(new ParseTreePrinter());
            }

            SymbolTable.hook = null;

            // Visit import declarations
            if(pJc.showMessage)
                System.out.println("-- Resolving imports.");
            c.visit(new org.processj.namechecker.ResolveImports<>(globalTypeTable));
            globalTypeTable.printStructure("");

            // Visit top-level declarations
            if(pJc.showMessage)
                System.out.println("-- Declaring Top Level Declarations.");
            c.visit(new org.processj.namechecker.TopLevelDecls<>(globalTypeTable));

            // Visit and re-construct record types correctly
            if ( pJc.showMessage )
                System.out.println("-- Reconstructing records.");
            c.visit(new org.processj.rewriters.RecordRewrite(globalTypeTable));

            // Visit and re-construct protocol types correctly
            if ( pJc.showMessage )
                System.out.println("-- Reconstructing protocols.");
            c.visit(new org.processj.rewriters.ProtocolRewrite(globalTypeTable));

            // Visit and re-construct if-stmt, while-stmt, for-stmt, and do-stmt
            if ( pJc.showMessage )
                System.out.println("-- Reconstructing statements.");
            c.visit(new org.processj.rewriters.StatementRewrite());

            // Visit and resolve import for top-level declarations
            if ( pJc.showMessage )
                System.out.println("-- Checking native Top Level Declarations.");
            c.visit(new org.processj.namechecker.ResolveNativeImports());

            // Visit and resolve types from imported packages
            if ( pJc.showMessage )
                System.out.println("-- Resolving imported types.");
            c.visit(new org.processj.namechecker.ResolvePackageTypes());

            // Visit name checker
            if ( pJc.showMessage )
                System.out.println("-- Checking name usage.");
            c.visit(new org.processj.namechecker.NameChecker<AST>(globalTypeTable));

            // Visit and re-construct array types correctly
            if ( pJc.showMessage )
                System.out.println("-- Reconstructing array types.");
            root.visit(new org.processj.namechecker.ArrayTypeConstructor());

            // Visit and re-construct array literals
            if ( pJc.showMessage )
                System.out.println("-- Reconstructing array literals.");
            c.visit(new org.processj.rewriters.ArraysRewrite());

            // Visit resolve named type
            if ( pJc.showMessage )
                System.out.println("-- Resolving named type.");
            c.visit(new org.processj.typechecker.ResolveNamedType(globalTypeTable));

            // Visit type checker
            if ( pJc.showMessage )
                System.out.println("-- Checking types.");
            c.visit(new org.processj.typechecker.TypeChecker(globalTypeTable));

            // Visit a switch statement case
            if ( pJc.showMessage )
                System.out.println("-- Checking break for protocols.");
            c.visit(new org.processj.rewriters.SwitchStmtRewrite());

            // Visit cast-rewrite
            if ( pJc.showMessage )
                System.out.println("-- Rewriting cast-expressions.");
            c.visit(new CastRewrite());
            
            if ( pJc.showMessage )
                System.out.println("-- Checking return statements in alts");
            c.visit(new org.processj.semanticcheck.AltReturnCheck());

            // Visit org.processj.reachability
            if ( pJc.showMessage )
                System.out.println("-- Computing org.processj.reachability.");
            c.visit(new org.processj.reachability.Reachability());

            // Visit parallel usage
            if ( pJc.showMessage )
                System.out.println("-- Performing parallel usage check.");
//            c.visit(new org.processj.parallel_usage_check.ParallelUsageCheck());

            // Visit org.processj.yield
            if ( pJc.showMessage )
                System.out.println("-- Annotating procedures that may issue a org.processj.yield call.");
            c.visit(new org.processj.yield.Yield());
            
            if ( pJc.showMessage )
                System.out.println("-- Marking yielding statements and expressions.");
            c.visit(new org.processj.rewriters.Yield());
            
            if ( pJc.showMessage )
                System.out.println("-- Checking literal inits are free of channel communication.");
            c.visit(new org.processj.semanticcheck.LiteralInits());
            
            if ( pJc.showMessage )
                System.out.println("-- Checking replicated Alt inits.");
            c.visit(new org.processj.semanticcheck.ReplicatedAlts());
            
            if ( pJc.showMessage )
                System.out.println("-- Rewriting infinite loops.");
            new org.processj.rewriters.InfiniteLoopRewrite().go(c);
            
            // <--
            System.out.println("-- Rewriting channel arrays local decls");
//            new org.processj.rewriters.ChannelArrayDeclRewrite().go(c);
            // -->
            
            if ( pJc.showMessage )
                System.out.println("-- Rewriting loops.");
            c.visit(new org.processj.rewriters.UnrollLoopRewrite());
            
            if ( pJc.showMessage )
                System.out.println("-- Performing alt statement usage check.");
            c.visit(new org.processj.rewriters.AltStatRewrite());

//            Log.doLog = true;
            if ( pJc.showMessage )
                System.out.println("-- Rewriting yielding expressions.");
            c.visit(new org.processj.rewriters.ChannelRead());
//            Log.doLog = false;
            if ( pJc.showMessage )
                System.out.println("-- Rewriting parblocks statements");
            c.visit(new org.processj.rewriters.ParBlockRewrite());

            //System.out.println("Lets reprint it all");
            //c.visit(new org.processj.printers.ParseTreePrinter());
            //c.visit(new org.processj.printers.PrettyPrinter());
            if ( pJc.showMessage )
                System.out.println("-- Checking break and continue labels.");
            new org.processj.semanticcheck.LabeledBreakContinueCheck().go(c);

            if ( pJc.showMessage )
                System.out.println("-- Collecting left-hand sides for par for code generation.");
            c.visit(new org.processj.rewriters.ParFor());
            
            // Terminate if we have any errors
            if(PJBugManager.INSTANCE.getErrorCount() > 0) {
                PJBugManager.ReportMessageAndExit("Errors: " + PJBugManager.INSTANCE.getErrorCount());
            }

            // If we're generating C++ code, we need to rewrite print/println statements
            if (pJc.target/*Settings.language*/ == Language.CPLUS) {
                System.out.println("-- Rewriting calls to print() and println().");
                c.visit(new IOCallsRewrite());
            }
            
            // Run the code generator for the known (specified) target language
            if (pJc.target == Language.CPLUS || pJc.target == Language.JVM/*Settings.language==pJc.target*/ )
                if (pJc.target == Language.JVM/*Settings.language == Language.JVM*/) {
                    pJc.generateCodeJava(c, inFile, globalTypeTable);
                } else if (pJc.target == Language.CPLUS/*Settings.language == Language.CPLUS*/) {
                    Log.startLogging();
                    pJc.generateCodeCPP(c, inFile, globalTypeTable);
                }
            else {
                // Unknown target language so abort/terminate program
                PJBugManager.ReportMessageAndExit("Invalid target language!");
            }

            System.out.println("** COMPILATION COMPLITED SUCCESSFULLY **");
        }
    }

    /**
     * Given a ProcessJ Compilation unit, e.g., an abstract org.processj.syntax tree object,
     * we will generate the code for the JVM. The source range for this type of
     * tree is the entire source file, not including leading and trailing
     * whitespace characters and comments.
     *
     * @param c
     *          A Compilation unit consisting of a single file.
     * @param inFile
     *              The compiled file.
     * @param s
     *          A symbol table consisting of all the top level types.
     */
    private void generateCodeJava(Compilation c, File inFile, SymbolTable s) {
        Properties p = org.processj.utilities.ConfigFileReader.getProcessJConfig();
        // Run the code generator to decode pragmas, generate libraries,
        // resolve types, and set the symbol table for top level declarations
        CodeGenJava codeGen = new CodeGenJava(s);
        // Set the user working directory
        codeGen.workingDir(p.getProperty("workingdir"));
        // Set the working source file
        codeGen.sourceProgam(c.fileNoExtension());
        // Visit this compilation unit and recursively build the program
        // after returning strings rendered by the string template
        String code = (String) c.visit(codeGen);
        // Write the output to a file
        Helper.writeToFile(code, c.fileNoExtension(), codeGen.workingDir(), ".java");
    }

    private void generateCodeCPP(Compilation c, File inFile, SymbolTable s) {
        Properties p = org.processj.utilities.ConfigFileReader.getProcessJConfig();
        CodeGenCPP codeGen = new CodeGenCPP(s);
        codeGen.setWorkingDir(p.getProperty("workingdir"));
        // codeGen.sourceProgam(c.fileNoExtension());
        String code = (String) c.visit(codeGen);
        Helper.writeToFile(code, c.fileNoExtension(), codeGen.getWorkingDir(), ".cpp");
    }

    public ProcessJc(String[] args) {

        this.args = args;

        Settings.showColor = Boolean.valueOf(config.getProperty("color"));

        // Parse command-line arguments
        parseArgs();

        // Switch to turn color mode ON/OFF
        ANSIColorMode();

    }

    public void ANSIColorMode() {
        // Check default value before switching mode to on/off
        if ( !Settings.showColor && this.showColor ) {// Color mode 'ON'
            config.setProperty("color", String.valueOf(Boolean.TRUE));
        } else if ( Settings.showColor && this.showColor ) {// Color mode 'OFF'
            config.setProperty("color", String.valueOf(Boolean.FALSE));
        }
        Settings.showColor = Boolean.valueOf(config.getProperty("color"));
        ConfigFileReader.closeConfiguration(config);
    }

    public void parseArgs() {
        for (int pos=0; pos<args.length;) {
            String arg = args[pos++];
            if ( arg.charAt(0)!='-' ) {
                // We found a 'Xxx.pj' file
                if ( !inputFiles.contains(arg) ) {
                    inputFiles.add(arg);
                }
            } else {
                boolean foundOption = false;
                for (Option o : OPTIONS) {
                    if ( arg.equals(o.optionName) ) {
                        foundOption = true;
                        String optionValue = null;
                        if ( o.optionType!=OptionType.BOOLEAN )
                            optionValue = args[pos++];
                        // Same as before with Java reflection
                        Class<? extends ProcessJc> c = this.getClass();
                        try {
                            Field f = c.getField(o.fieldName);
                            if ( optionValue!=null ) {
                                if ( f.getType() instanceof Class && ((Class<?>) f.getType()).isEnum())
                                    setEnumField(f, optionValue, String.class);
                                else
                                    f.set(this, optionValue);
                            } else
                                f.set(this, true);
                        } catch (Exception e) {
                            // Exit code 101
                            PJBugManager.ReportMessageAndExit(e.getMessage() + "\n" + "Failed to access field '" + o.fieldName + "'");
                        }
                        break;
                    }
                }
                if(!foundOption) {
                    // Exit code 101
                    PJBugManager.ReportMessageAndExit("Invalid option '" + arg + "' found.");
                }
            }
        }
    }
    
    // This method must be used to get values from 'enum' types
    private void setEnumField(Field f, Object arg, Class<?> type) throws Exception {
        Method valueOf = f.getType().getMethod("getValueOf", type);
        Object value = valueOf.invoke(f.get(this), arg);
        f.set(this, value);
    }

    public static void printUsage() {

        for(Option o : OPTIONS)
            System.out.println(String.format("%-20s %s", o.optionName, o.description));

    }

    public void printUsageAndExit() {
        for(Option o : OPTIONS)
            System.out.println(String.format("%-20s %s", o.optionName, o.description));
    }

    public void version() {
        String msg = "ProcessJ Version: " + Settings.VERSION;
        System.err.println(msg);
    }

}
