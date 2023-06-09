package org.processj.compiler;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.processj.compiler.ast.Compilation;
import org.processj.compiler.phases.Phases;
import org.processj.compiler.phases.phase.*;

/**
 * <p>ProcessJ Compiler</p>.
 *
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @since 0.1.0
 * @version 1.0.0
 */
public class Compiler extends Phases.Executor {

    /// ---------------------
    /// Public Static Methods

    /**
     * <p>Execution entry point. Instantiates an instance of the ProcessJ initializes the environment from the specified
     * arguments, initializes a {@link Compiler} instance, & executes the {@link Compiler}.</p>
     * @param arguments The command-line arguements specified by the user.
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

        // TODO: Write to File with working directory and extension in utf-8 encoding

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
