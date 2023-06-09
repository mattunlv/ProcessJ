package org.processj.compiler;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.processj.compiler.ast.Compilation;
import org.processj.compiler.phases.Phases;
import org.processj.compiler.phases.phase.*;

/**
 * ProcessJ compiler.
 *
 * @author ben
 */
public class Compiler extends Phases.Executor {

    /// ---------------------
    /// Public Static Methods

    /**
     * Program execution begins here.
     *
     * @param arguments
     *          A vector of command arguments passed to the compiler.
     */
    public static void main(final String[] arguments)
            throws Phase.Error, MalformedURLException, ClassNotFoundException, InvocationTargetException,
            IllegalAccessException {

        // Assert the arguments are valid
        if(!Phases.SetEnvironment(arguments)) {

            // Initialize an instance to the compiler
            final Compiler compiler = new Compiler();

            // Execute the compilation with the requested FileSet
            // TODO: Request from Phases
            compiler.execute(new ArrayList<>());

        }

        /*
        new InfiniteLoopRewrite().go(compilation);
        compilation.visit(new UnrollLoopRewrite());
        compilation.visit(new ChannelRead());

        if(pJc.target == Language.CPLUS) compilation.visit(new IOCallsRewrite());

        // Run the code generator to decode pragmas, generate libraries,
        // resolve types, and set the symbol table for top level declarations
        CodeGenJava codeGen = new CodeGenJava(new SymbolTable());
        CodeGenCPP cppCodeGen = new CodeGenCPP(new SymbolTable());

        // Set the working source file
        codeGen.sourceProgam(compilation.fileNoExtension());
        // Visit this compilation unit and recursively build the program
        // after returning strings rendered by the string template

        String code = (String) compilation.visit(codeGen);
        String cppFile = (String) compilation.visit(cppCodeGen);

        // TODO: Write to File with working directory and extension in utf-8 encoding

        }
        */

    }

    /// ---------------
    /// Phases.Executor

    /**
     * <p>Constructs a preliminary {@link Compilation} that has not undergone any type resolution or checking.</p>
     * @param filePath The path {@link String} value corresponding to the resultant {@link Compilation}
     * @return A preliminary {@link Compilation}
     * @since 0.1.0
     */
    @Override
    protected Compilation onRequestCompilation(final String filePath, final String packageName) throws Phase.Error {

        Compilation compilation = Phases.Executor.GetImported.Compilation(packageName);

        // Assert the Compilation is valid
        if(compilation == null) {

            // Initialize a Phase listener & a ProcessJSourceFile
            // Initialize the phases TODO: Merge Validate Pragmas
            final ProcessJSourceFile processJSourceFile = RequestOpen.File(filePath);
            final ProcessJParser processJParser     = new ProcessJParser(this);
            final ValidatePragmas validatePragmas   = new ValidatePragmas(this);

            // Tokenize & parse the input file
            processJParser.execute(processJSourceFile);
            validatePragmas.execute(processJSourceFile);

            // Update the result
            compilation = processJSourceFile.getCompilation();

            // Map the Compilation
            Phases.Executor.SetImported.Compilation(packageName, compilation);

        }

        // Return the transformed, preliminary result
        return compilation;

    }

    /// ---------------
    /// Private Methods

    private Phase processPhaseFor(final ProcessJSourceFile processJSourceFile) throws Phase.Error {

        // Request the current Phase
        final Phase currentPhase = Phases.Executor.RequestPhase.For(processJSourceFile);

        // Assert the current Phase is valid, execute it
        if(currentPhase != null)
            currentPhase.execute(processJSourceFile);

        // Initialize a handle to the Compilation
        final Compilation compilation = processJSourceFile.getCompilation();

        // Assert the compilation is valid TODO: Maybe set Imported Once since this gets invoked every iteration
        if(compilation != null)
            Phases.Executor.SetImported.Compilation(compilation.getPackageName(), compilation);

        // TODO: Check Error Stack here

        // Return the next Phase, if any
        return (currentPhase != null) ? Phases.Executor.RequestPhase.For(processJSourceFile) : null;

    }

    private void execute(final List<String> inputFiles) throws Phase.Error {

        // Process every file
        for(final String path: inputFiles) {

            // Open the file
            final ProcessJSourceFile processJSourceFile = RequestOpen.File(path);

            // Iterate
            Phase phase; do { phase = this.processPhaseFor(processJSourceFile); } while(phase != null);

        }

    }

}
