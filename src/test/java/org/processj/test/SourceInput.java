package org.processj.test;

import org.processj.ProcessJc;
import javax.tools.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;

/**
 * <p>Represents a ProcessJ-Java source file pair (.pj, .java) along with the corresponding input & output path.
 * This class provides {@link SourceInput#compileJava(SourceInput)} in order to verify syntactic correctness of
 * the ProcessJ input & generated Java output.</p>
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @see TestInput
 * @since 0.1.0
 */
public class SourceInput extends SimpleJavaFileObject implements TestInput {

    /// --------------------
    /// Public Static Fields

    public static String InputPath  = ""    ;
    public static String OutputPath = ""    ;

    /// ------------------------
    /// Protected Static Methods

    protected static String stringOf(final String filePath) {

        String result = "";

        try {

            // Initialize the BufferedReader & StringBuilder
            final BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            final StringBuilder stringBuilder = new StringBuilder();

            // Initialize the line & append while we haven't reached the end of file
            String line; while((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line).append('\n');

            // Set the result
            result = stringBuilder.toString();

        } catch(final IOException ioException) {

            System.out.println(ioException.getMessage());

        }

        return result;

    }

    protected static URI uriOf(final String path) {

        return URI.create("string:///" + path);

    }

    private static JavaCompiler getJavaCompiler() {

        // Attempt to retrieve the java compiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        // If the compiler is null, attempt
        if(compiler == null) try {

            // To retrieve via reflection
            compiler = (JavaCompiler) Class.forName("com.sun.tools.javac.api.JavacTool")
                    .getMethod("create")
                    .invoke(null);

            // Otherwise
        } catch (final Exception exception) {

            // Throw the error
            throw new AssertionError(exception);

        }

        return compiler;

    }

    protected static boolean compileJava(final SourceInput sourceInput) {

        // Initialize the Diagnostics Collector
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

        // Invoke & return the result
        return getJavaCompiler().getTask(
                null, null, diagnostics, null, null, List.of(sourceInput)).call();

    }

    /// --------------
    /// Private Fields

    private final String path               ;
    private final String name               ;
    private final String inputExtension     ;
    private final String outputExtension    ;

    /// ------------
    /// Constructors

    public SourceInput(final String path, final String name,
                       final String inputExtension, final String outputExtension) {
        super(uriOf(OutputPath + name + "." + outputExtension), JavaFileObject.Kind.SOURCE);

        this.path               = path              ;
        this.name               = name              ;
        this.inputExtension     = inputExtension    ;
        this.outputExtension    = outputExtension   ;

    }

    public SourceInput(final String path, final String name,
                       final String inputExtension) {
        this(path, name, inputExtension, JavaFileObject.Kind.SOURCE.extension);
    }

    public SourceInput(final String path, final String name) {
        this(path, name, "pj");
    }

    /// --------------------
    /// SimpleJavaFileObject

    @Override
    public CharSequence getCharContent(final boolean ignoreEncodingErrors) {

        return stringOf(this.getOutputPath());

    }

    /// ---------------------------
    /// org.processj.test.TestInput

    @Override
    public String getName() {

        return this.name;

    }

    /// ---------
    /// Accessors

    public String getInputPath() {

        return InputPath + this.path + this.name + "." + this.inputExtension;

    }

    public String getOutputPath() {

        return OutputPath + this.name + "." + this.outputExtension;

    }

    public CharSequence getInputCharContent() {

        return stringOf(this.getInputPath());

    }

    public boolean successfulJavaCompile() {

        // Transpile with processJ
        ProcessJc.main(new String[]{this.getInputPath()});

        // Compile Java output
        return compileJava(this);

    }

}
