package org.processj;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.processj.ast.AST;
import org.processj.ast.Compilation;
import org.processj.ast.SymbolMap;
import org.processj.butters.Butters;
import org.processj.codegen.Helper;
import org.processj.codegen.CodeGenJava;
import org.processj.codegen.CodeGenCPP;
import org.processj.phases.*;
import org.processj.utilities.*;
import org.processj.utilities.printers.ParseTreePrinter;
import org.processj.rewriters.IOCallsRewrite;

/**
 * ProcessJ compiler.
 *
 * @author ben
 */
public class ProcessJc extends Phases.Executor {

    // Kinds of available options for the ProcessJ compiler
    public enum OptionType {
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


    /// ----------------------
    /// Private Static Methods

    /**
     * Program execution begins here.
     *
     * @param args
     *          A vector of command arguments passed to the compiler.
     */
    public static void main(String[] args) throws Phase.Error, MalformedURLException, ClassNotFoundException {

        // Send frequency file over HTTP to the ProcessJ server but
        // only if the size of the error file is 1MB or more.
        // Otherwise, we ignore the request made by the compiler
        // FrequencyFileProcessing.updateFrequency();
        ProcessJc pJc = new ProcessJc(args);

        // Do we have any arguments??
        if(args.length == 2) // @0: -include, @1: path
            PJBugManager.ReportErrorAndExitWithUsage(VisitorMessageNumber.RESOLVE_IMPORTS_100);

        else if(pJc.help) {
            printUsage();

            // TODO: Remove this, this is bad form
            return;

        }

        // Dump log messages if true
        if(pJc.visitAll)
            Log.startLogging();

        if(pJc.version)
            pJc.version();

        // Process Butters source file, one by one
        else if(pJc.install) {

            // Process nativelib
            if(pJc.nativelib) {

                final Compilation compilation = pJc.getPreliminaryCompilationFor(pJc.pjfile);

                Butters.decodePragmas(compilation);

                System.out.println("** LIBRARY COMPLETED SUCCESSFULLY **");

            } else if(pJc.userlib){

                // TODO: move files to the correct directory??

            } else {

                PJBugManager.ReportMessageAndExit("Must specify if the org.processj.library is 'native' or 'user-defined'");

            }

        } else if(pJc.userlib || pJc.nativelib)
            PJBugManager.ReportMessageAndExit("Missing command '-install'");

        AST root = null;

        final Phase.Listener listener = new Phase.Listener();

        // Process source file, one by one
        for(final String filePath: pJc.inputFiles) {

            // Retrieve this file's Compilation; Important that from here on out the path returned by
            // the ProcessJSource file is used since all the SymbolPaths were checked and it could have a different
            // prefix than the one specified
            final ProcessJSourceFile    processJSourceFile = Request.Open(filePath);
            final Compilation           compilation        = pJc.getPreliminaryCompilationFor(processJSourceFile.getPath());

            // Dump generated AST
            if(pJc.showTree) compilation.visit(new ParseTreePrinter());

            // Set the absolute path, file, and package name from where this
            System.out.println("Completing: '" + filePath + "'.");

            // TODO: These will get removed in favor of using the Executor interface
            final ResolveImports resolveImports = new ResolveImports(listener);
            final NameChecker    nameChecker    = new NameChecker(listener);
            final TypeChecker    typeChecker    = new TypeChecker(listener);

            // Execute
            resolveImports.execute(processJSourceFile);
            nameChecker.execute(processJSourceFile);
            typeChecker.execute(processJSourceFile);

            try {

                // Visit and re-construct record types correctly
                if (pJc.showMessage)
                    System.out.println("-- Reconstructing records.");
                compilation.visit(new RecordRewrite(listener));

                // Visit and re-construct protocol types correctly
                if (pJc.showMessage)
                    System.out.println("-- Reconstructing protocols.");
                compilation.visit(new ProtocolRewrite(listener));

                // Visit cast-rewrite
                if (pJc.showMessage)
                    System.out.println("-- Rewriting cast-expressions.");
                compilation.visit(new CastRewrite(listener));

                // Visit a switch statement case
                if (pJc.showMessage)
                    System.out.println("-- Checking break for protocols.");
                compilation.visit(new org.processj.rewriters.SwitchStmtRewrite());

                if (pJc.showMessage)
                    System.out.println("-- Checking return statements in alts");
                compilation.visit(new org.processj.semanticcheck.AltReturnCheck(listener));

                // Visit org.processj.reachability
                if (pJc.showMessage)
                    System.out.println("-- Computing org.processj.reachability.");
                compilation.visit(new org.processj.reachability.Reachability());

                // Visit parallel usage
                if (pJc.showMessage)
                    System.out.println("-- Performing parallel usage check.");
//            c.visit(new org.processj.parallel_usage_check.ParallelUsageCheck());

                if (pJc.showMessage)
                    System.out.println("-- Checking literal inits are free of channel communication.");
                compilation.visit(new org.processj.semanticcheck.LiteralInits());

                if (pJc.showMessage)
                    System.out.println("-- Checking replicated Alt inits.");
                compilation.visit(new org.processj.semanticcheck.ReplicatedAlts());

                if (pJc.showMessage)
                    System.out.println("-- Rewriting infinite loops.");
                new org.processj.rewriters.InfiniteLoopRewrite().go(compilation);

                // <--
                System.out.println("-- Rewriting channel arrays local decls");
//            new org.processj.rewriters.ChannelArrayDeclRewrite().go(c);
                // -->

                if (pJc.showMessage)
                    System.out.println("-- Rewriting loops.");
                compilation.visit(new org.processj.rewriters.UnrollLoopRewrite());

                if (pJc.showMessage)
                    System.out.println("-- Performing alt statement usage check.");
                compilation.visit(new org.processj.rewriters.AltStatRewrite());

//            Log.doLog = true;
                if (pJc.showMessage)
                    System.out.println("-- Rewriting yielding expressions.");
                compilation.visit(new org.processj.rewriters.ChannelRead());
//            Log.doLog = false;
                if (pJc.showMessage)
                    System.out.println("-- Rewriting parblocks statements");
                compilation.visit(new org.processj.rewriters.ParBlockRewrite());

                //System.out.println("Lets reprint it all");
                //c.visit(new org.processj.printers.ParseTreePrinter());
                //c.visit(new org.processj.printers.PrettyPrinter());
                if (pJc.showMessage)
                    System.out.println("-- Checking break and continue labels.");
                new org.processj.semanticcheck.LabeledBreakContinueCheck().go(compilation);

                if (pJc.showMessage)
                    System.out.println("-- Collecting left-hand sides for par for code generation.");
                compilation.visit(new org.processj.rewriters.ParFor());

                // Terminate if we have any errors
                if (PJBugManager.INSTANCE.getErrorCount() > 0) {
                    PJBugManager.ReportMessageAndExit("Errors: " + PJBugManager.INSTANCE.getErrorCount());
                }

                // If we're generating C++ code, we need to rewrite print/println statements
                if (pJc.target/*Settings.language*/ == Language.CPLUS) {
                    System.out.println("-- Rewriting calls to print() and println().");
                    compilation.visit(new IOCallsRewrite());
                }

                // Run the code generator for the known (specified) target language
                if (pJc.target == Language.CPLUS || pJc.target == Language.JVM/*Settings.language==pJc.target*/)
                    if (pJc.target == Language.JVM/*Settings.language == Language.JVM*/) {
                        //pJc.generateCodeJava(compilation, inFile, globalTypeTable);
                    } else if (pJc.target == Language.CPLUS/*Settings.language == Language.CPLUS*/) {
                        Log.startLogging();
                        //pJc.generateCodeCPP(compilation, inFile, globalTypeTable);
                    } else {
                        // Unknown target language so abort/terminate program
                        PJBugManager.ReportMessageAndExit("Invalid target language!");
                    }
            } catch (SymbolMap.Context.ContextDoesNotDefineScopeException contextDoesNotDefineScopeException) {

                // Ignore

            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            System.out.println("** COMPILATION COMPLETED SUCCESSFULLY **");
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
        String code = null;
        try {
            code = (String) c.visit(codeGen);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        // Write the output to a file
        Helper.writeToFile(code, c.fileNoExtension(), codeGen.workingDir(), ".java");
    }

    private void generateCodeCPP(Compilation c, File inFile, SymbolTable s) {
        Properties p = org.processj.utilities.ConfigFileReader.getProcessJConfig();
        CodeGenCPP codeGen = new CodeGenCPP(s);
        codeGen.setWorkingDir(p.getProperty("workingdir"));
        // codeGen.sourceProgam(c.fileNoExtension());
        String code = null;
        try {
            code = (String) c.visit(codeGen);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
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

    private void execute(final List<String> inputFiles) throws Phase.Error {

        // Process every file
        if(inputFiles != null) for(final String path: inputFiles) {

            // Open the file
            final ProcessJSourceFile processJSourceFile = Request.Open(path);

            // Request an initial Compilation
            this.getPreliminaryCompilationFor(path);

            // Request the next phase
            Phase nextPhase = RequestPhase.For(processJSourceFile);

            // Loop until we're done
            while(nextPhase != null) {

                // Execute the Phase
                nextPhase.execute(processJSourceFile);

                // Retrieve the next one
                nextPhase = RequestPhase.For(processJSourceFile);

            }

        }

    }

}
