package org.processj.test;

import java.io.*;

/**
 * <p>Represents a ProcessJ test input file with a corresponding set of input & output paths and extensions.</p>
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 0.1.0
 */
public class TestInputFile {

    /// --------------------
    /// Public Static Fields

    public static String BaseInputPath  = "src/test/resources/"     ;
    public static String BaseOutputPath = "/Users/cuenca/workingpj/";

    /// ------------------------
    /// Protected Static Methods

    public static String stringOf(final String filePath) {

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

    /// --------------
    /// Private Fields

    private final String relativePath       ;
    private final String name               ;
    private final String inputExtension     ;
    private final String outputExtension    ;

    /// ------------
    /// Constructors

    public TestInputFile(final String relativePath, final String name,
                         final String inputExtension, final String outputExtension) {

        this.relativePath       = relativePath                                                                  ;
        this.name               = name                                                                          ;
        this.inputExtension     = inputExtension                                                                ;
        this.outputExtension    = outputExtension                                                               ;

    }

    /// ----------------
    /// java.lang.Object

    @Override
    public String toString() {

        return stringOf(this.getAbsoluteInputFilePath());

    }

    @Override
    public boolean equals(final Object that) {

        return this.toString().equals(that.toString());

    }

    /// ---------
    /// Accessors

    public String getName() {

        return this.name;

    }

    public String getAbsoluteInputFilePath() {

        return BaseInputPath + this.relativePath + this.name + "." + this.inputExtension;

    }

    public String getAbsoluteOutputFilePath() {

        return BaseOutputPath + this.relativePath + this.name + "." + this.outputExtension;

    }

    public String getAbsoluteOutputPath() {

        return BaseOutputPath + this.relativePath;

    }

    public String getAbsoluteInputPath() {

        return BaseInputPath + this.relativePath;

    }

    public String getRelativePath() {

        return this.relativePath;

    }

    /// --------------
    /// Public Methods

    public void write() {

        // Attempt
        try {

            // Create any missing intermediate directories
            new File(this.getAbsoluteOutputPath()).mkdirs();

            // To initialize the File Writer
            final FileWriter fileWriter = new FileWriter(this.getAbsoluteOutputFilePath());

            // Write the contents
            fileWriter.write(this.toString());

            // Close the file writer
            fileWriter.close();

        } catch(final IOException ioException) {

            // Print the error message
            System.out.println(ioException.getMessage());

        }

    }

}
