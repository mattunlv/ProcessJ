package org.processj.utilities;

import java.io.File;

public class Files {

    /**
     * <p>Returns a flag indicating if the specified {@link String} path exists & is a directory.</p>
     * @param path {@link String} value representing a path.
     * @return Flag indicating if the specified {@link String} path exists & is a directory.
     * @since 0.1.0
     */
    public static boolean IsDirectory(final String path) {

        // Initialize the result
        boolean result = path != null;

        // If the path is valid
        if(result) {

            // Instantiate a file
            final File file = new File(path);

            // Set the final result
            result = file.exists() && file.isDirectory();

        }

        // Return the result
        return result;

    }

}
