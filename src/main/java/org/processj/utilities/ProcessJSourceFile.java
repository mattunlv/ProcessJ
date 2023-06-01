package org.processj.utilities;

import org.processj.Phase;
import org.processj.ast.Compilation;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * <p>Encapsulates a ProcessJ source file that containing structures produced by compilation {@link Phase}s &
 * keeps track of which compilation {@link Phase}s have successfully operated on the contents.</p>
 * @see Phase
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 0.1.0
 */
public class ProcessJSourceFile implements Comparable<ProcessJSourceFile> {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link Set} of {@link Phase}s the {@link ProcessJSourceFile} has been processed through.</p>
     */
    private final Set<Class<? extends Phase>>   completedPhases     ;

    /**
     * <p>The {@link File} object representing the ProcessJ source file.</p>
     */
    private final File                          file                ;

    /**
     * <p>A {@link List} containing the set of import statement paths as {@link String}s.</p>
     */
    private final List<String>                  importFiles         ;

    /**
     * <p>The {@link Set} of import paths to check for duplicates.</p>
     */
    private final Set<String>                   importSet           ;

    /**
     * <p>The most recent {@link Phase} that operated or validated the {@link ProcessJSourceFile}.</p>
     */
    private Phase                               lastCompletedPhase  ;

    /**
     * <p>Initially the result of the parsing phase. This instance gets transformed as it propagates through
     * the toolchain.</p>
     */
    private Compilation                         compilation         ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ProcessJSourceFile} to its' default state.</p>
     * @param inputPath The {@link String} value of the input path corresponding with the processj source file.
     * @since 0.1.0
     */
    public ProcessJSourceFile(final String inputPath) {

        // Initialize the file & Compilation
        this.completedPhases    = new HashSet<>()       ;
        this.importFiles        = new ArrayList<>()     ;
        this.importSet          = new HashSet<>()       ;
        this.file               = new File(inputPath)   ;
        this.compilation        = null                  ;
        this.lastCompletedPhase = null                  ;

    }

    /// --------------------
    /// java.lang.Comparable

    /**
     * <p>Returns an {@link Integer} value corresponding to the comparison of the {@link ProcessJSourceFile}'s
     * most recent completed {@link Phase}.</p>
     * @param that the object to be compared.
     * @return {@link Integer} value corresponding to the comparison of the {@link ProcessJSourceFile}'s
     * most recent completed {@link Phase}.
     * @since 0.1.0
     */
    @Override
    public final int compareTo(final ProcessJSourceFile that) {

        // Return the result; null condition is handled further down
        return this.lastCompletedPhase.compareTo((that != null) ? that.lastCompletedPhase : null);

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Marks the {@link ProcessJSourceFile} as processed by the specified {@link Phase}.</p>
     * @param phase The {@link Phase} that has processed the {@link ProcessJSourceFile}.
     * @since 0.1.0
     */
    public final void setCompletedPhase(final Phase phase) {

        if(phase != null) {

            // TODO: Make sure that the last completed phase isn't set to a value less than the current
            // Update the set & the last completed phase
            this.completedPhases.add(phase.getClass());
            this.lastCompletedPhase = phase;

        }

    }

    /**
     * <p>Mutates the current state of the {@link ProcessJSourceFile}'s {@link Compilation}.</p>
     * @param compilation The {@link Compilation} to set.
     * @since 0.1.0
     */
    public final void setCompilation(final Compilation compilation) {

        this.compilation = compilation;

    }

    /**
     * <p>Aggregates the {@link List} of paths into the {@link ProcessJSourceFile}'s import file list.</p>
     * @param paths The {@link List} of {@link String} paths to aggregate.
     * @since 0.1.0
     */
    public final void addImport(final List<String> paths) {

        // If the specified collection is valid, add all non-existent paths
        if(paths != null)
            this.importFiles.addAll(
                    paths.stream()
                            .filter(this.importSet::add)
                            .toList());

    }

    /**
     * <p>Returns a flag indicating if the specified {@link Phase} has processed the {@link ProcessJSourceFile}.</p>
     * @param phase The {@link Phase} to check for completion
     * @return Flag indicating if the specified {@link Phase} has processed the {@link ProcessJSourceFile}.
     * @since 0.1.0
     */
    public final boolean hasBeenCompletedBy(final Phase phase) {

        return (phase != null) && this.completedPhases.contains(phase.getClass());

    }

    /**
     * <p>Returns a flag indicating if the {@link ProcessJSourceFile} contains a valid {@link Compilation}.</p>
     * @return flag indicating if the {@link ProcessJSourceFile} contains a valid {@link Compilation}.
     * @since 0.1.0
     */
    public final boolean containsCompilation() {

        return this.compilation != null;

    }

    /**
     * <p>Returns a flag indicating if the {@link ProcessJSourceFile} has resolved import file paths. This returns
     * true if the compilation does not have any {@link org.processj.ast.Import} statements defined or if the
     * {@link ProcessJSourceFile#importFiles} field is not empty.</p>
     * @return Flag indicating if the {@link ProcessJSourceFile} has resolved import file paths.
     * @since 0.1.0
     */
    public final boolean hasResolvedImports() {

        return !this.compilation.definesImports() || !this.importFiles.isEmpty();

    }

    /**
     * <p>Returns the name corresponding to the {@link ProcessJSourceFile}.</p>
     * @return {@link String} value of the name corresponding to the {@link ProcessJSourceFile}.
     * @since 0.1.0
     */
    public final String getName() {

        return this.file.getName();

    }

    /**
     * <p>Retrieves {@link java.io.FileReader} corresponding with the {@link ProcessJSourceFile}.</p>
     * @return {@link java.io.FileReader} containing the contents of the {@link ProcessJSourceFile}.
     * @throws IOException if the {@link FileReader} failed to open the file.
     * @since 0.1.0
     */
    public final FileReader getCorrespondingFileReader() throws IOException {

        return new FileReader(this.file.getAbsolutePath());

    }

    /**
     * <p>Retrieves the {@link Compilation} corresponding with the {@link ProcessJSourceFile}.</p>
     * @return {@link Compilation}
     * @since 0.1.0
     */
    public final Compilation getCompilation() {

        return this.compilation;

    }

    /**
     * <p>Returns the most recent {@link Phase} that operated or validated the {@link ProcessJSourceFile}.</p>
     * @return The most recent {@link Phase} that operated or validated the {@link ProcessJSourceFile}.
     * @see Phase
     * @since 0.1.0
     */
    public final Phase getLastCompletedPhase() {

        return this.lastCompletedPhase;

    }

    /**
     * <p>Returns the set of import file paths corresponding to the {@link ProcessJSourceFile}.</p>
     * @return {@link List} containing all the import file paths.
     * @since 0.1.0
     */
    public final List<String> getImportFiles() {

        return this.importFiles;

    }

    /// ---------------------
    /// Functional Interfaces

    /**
     * <p>Defines a functional interface for a method to be specified to the {@link ProcessJSourceFile} class that
     * provides {@link Integer} values that correspond with a {@link Phase}'s order to operate on or validate
     * a {@link ProcessJSourceFile}.</p>
     * @see Phase
     * @see ProcessJSourceFile
     * @author Carlos L. Cuenca
     * @version 1.0.0
     * @since 0.1.0
     */
    @FunctionalInterface
    public interface Order {

        Integer RequestFor(final Class<? extends Phase> phase);

    }

}
