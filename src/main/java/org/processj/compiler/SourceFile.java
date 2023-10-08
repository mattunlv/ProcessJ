package org.processj.compiler;

import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.Compilation;

import java.io.*;
import java.util.*;

/**
 * <p>Encapsulates a ProcessJ source file that containing structures produced by compilation {@link Phase}s &
 * keeps track of which compilation {@link Phase}s have successfully operated on the contents.</p>
 * @see Phase
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 0.1.0
 */
public class SourceFile {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link Set} of {@link Phase} class objects the {@link SourceFile} has been processed through.</p>
     * @since 0.1.0
     * @see Set
     */
    private final Set<Class<? extends Phase>> completedPhases;

    /**
     * <p>The {@link File} object representing the ProcessJ source file.</p>
     * @since 0.1.0
     * @see File
     */
    private final File file;

    /**
     * <p>The class object corresponding to the most recent {@link Phase} that operated or validated the
     * {@link SourceFile}.</p>
     * @since 0.1.0
     * @see Class
     * @see Phase
     */
    private Class<? extends Phase> lastCompletedPhase ;

    /**
     * <p>Initially the result of the parsing phase. This instance gets transformed as it propagates through
     * the toolchain.</p>
     * @since 0.1.0
     * @see Compilation
     */
    private Compilation compilation ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link SourceFile} to its' default state.</p>
     * @param inputPath The {@link String} value of the input path corresponding with the processj source file.
     * @since 0.1.0
     */
    public SourceFile(final String inputPath) {

        // Initialize the file & Compilation
        this.completedPhases    = new HashSet<>()       ;
        this.file               = new File(inputPath)   ;
        this.compilation        = null                  ;
        this.lastCompletedPhase = null                  ;

    }

    /// ------
    /// Object

    /**
     * <p>Returns the {@link SourceFile} contents as a {@link String} value.</p>
     * @return The {@link SourceFile} contents as a {@link String} value.
     * @since 0.1.0
     * @see String
     */
    @Override
    public String toString() {

        // Initialize a handle to the result
        String result = "";

        // Attempt to
        try {

            // Initialize the FileReader and StringBuilder
            final BufferedReader fileReader = new BufferedReader(new FileReader(this.file.getPath()));
            final StringBuilder stringBuilder = new StringBuilder();

            // Initialize a handle to the line and separator
            String separator = System.getProperty("line.separator");
            String line;

            // Append the contents
            while((line = fileReader.readLine()) != null)
                stringBuilder.append(line).append(separator);

            // Remove the last separator
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);

            // Update the result
            result = stringBuilder.toString();

        } catch(final IOException ioException) { /* Empty */ }

        // Return the result
        return result;

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Updates {@link SourceFile#lastCompletedPhase} with the class object corresponding to the specified
     * {@link Phase}.</p>
     * @param phase The {@link Phase} that has processed the {@link SourceFile}.
     * @since 0.1.0
     */
    public final void setCompletedPhase(final Phase phase) {

        // If the Phase is not null, attempt to add the Phase's class object to the set of completed Phases
        if((phase != null) && this.completedPhases.add(phase.getClass())) {

            // And if not already completed by the Phase, update the latest completed phase
            this.lastCompletedPhase = phase.getClass();

        }

    }

    /**
     * <p>Mutates the current state of the {@link SourceFile}'s {@link Compilation}.</p>
     * @param compilation The {@link Compilation} to set.
     * @since 0.1.0
     */
    public final void setCompilation(final Compilation compilation) {

        if(compilation != null) this.compilation = compilation;

    }

    /**
     * <p>Returns a flag indicating if the specified {@link Phase} has processed the {@link SourceFile}.</p>
     * @param phase The {@link Phase} to check for completion
     * @return Flag indicating if the specified {@link Phase} has processed the {@link SourceFile}.
     * @since 0.1.0
     */
    public final boolean hasBeenCompletedBy(final Phase phase) {

        return (phase != null) && this.completedPhases.contains(phase.getClass());

    }

    /**
     * <p>Returns a flag indicating if the {@link SourceFile} is not bound to a {@link Compilation}.</p>
     * @return flag indicating if the {@link SourceFile} is not bound to a {@link Compilation}.
     * @since 0.1.0
     */
    public final boolean isNotBoundedToCompilation() {

        return this.compilation == null;

    }

    /**
     * <p>Returns the name corresponding to the {@link SourceFile}.</p>
     * @return {@link String} value of the name corresponding to the {@link SourceFile}.
     * @since 0.1.0
     */
    public final String getName() {

        return this.file.getName();

    }

    /**
     * <p>Returns the path corresponding to the {@link SourceFile}.</p>
     * @return {@link String} value of the path corresponding to the {@link SourceFile}.
     * @since 0.1.0
     */
    public final String getPath() {

        return this.file.getPath();

    }

    /**
     * <p>Returns the {@link String} value corresponding to the {@link SourceFile}'s {@link Compilation}'s
     * package name, or blank if a {@link Compilation} is not bound to the {@link SourceFile}.</p>
     * @return the {@link String} value corresponding to the {@link SourceFile}'s {@link Compilation}'s
     * package name, or blank.
     * @since 0.1.0
     */
    public final String getPackageName() {

        return (this.compilation != null) ? this.compilation.getPackageName() : "";

    }

    /**
     * <p>Retrieves {@link java.io.FileReader} corresponding with the {@link SourceFile}.</p>
     * @return {@link java.io.FileReader} containing the contents of the {@link SourceFile}.
     * @throws IOException if the {@link FileReader} failed to open the file.
     * @since 0.1.0
     */
    public final FileReader getFileReader() throws IOException {

        return new FileReader(this.file.getPath());

    }

    /**
     * <p>Retrieves {@link java.io.FileInputStream} corresponding with the {@link SourceFile}.</p>
     * @return {@link java.io.FileInputStream} containing the contents of the {@link SourceFile}.
     * @throws IOException if the {@link java.io.FileInputStream} failed to open the file.
     * @since 0.1.0
     */
    public final FileInputStream getFileInputStream() throws IOException {

        return new FileInputStream(this.file.getPath());

    }

    /**
     * <p>Retrieves the {@link Compilation} corresponding with the {@link SourceFile}.</p>
     * @return {@link Compilation}
     * @since 0.1.0
     */
    public final Compilation getCompilation() {

        return this.compilation;

    }

    /**
     * <p>Returns the class object of the most recent {@link Phase} that operated or validated the {@link SourceFile}
     * or null if the {@link SourceFile} has not been processed by any {@link Phase}.</p>
     * @return The class object of the most recent {@link Phase} that operated or validated the {@link SourceFile}
     * or null.
     * @see Phase
     * @since 0.1.0
     */
    public final Class<? extends Phase> getLastCompletedPhase() {

        return this.lastCompletedPhase;

    }

}
