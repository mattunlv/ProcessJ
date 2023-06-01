package org.processj.utilities;

import org.processj.phases.Imports;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    /**
     * <p>Returns a flag indicating if the specified {@link String} path exists & is a file.</p>
     * @param path {@link String} value representing a path.
     * @return Flag indicating if the specified {@link String} path exists & is a file.
     * @since 0.1.0
     */
    public static boolean IsFile(final String path) {

        // Initialize the result
        boolean result = path != null;

        // If the path is valid
        if(result) {

            // Instantiate a file
            final File file = new File(path);

            // Set the final result
            result = file.exists() && file.isFile();

        }

        // Return the result
        return result;

    }

    /**
     * <p>Returns a {@link String} value of the specified path ending with a forward slash.</p>
     * @param path The {@link String} value of the path to check.
     * @return {@link String} value of the valid path.
     * @since 0.1.0
     */
    public static String ValidPath(final String path) {

        // Assert that the path ends with a forward slash
        return (path == null) ? "" : path + ((path.charAt(path.length() - 1) == '/') ? "" : '/');

    }

    /**
     * <p>Searches {@link List} of path {@link String} values for all pairs of directories that exist with the
     * specified relative path {@link String} value. If the specified path {@link String} value is null, this returns
     * an empty {@link List}.</p>
     * @param path The {@link String} value of the path to check against all prefix paths.
     * @param paths The {@link List} of path {@link String} values to search.
     * @return The set of existing {@link String} paths.
     * @since 0.1.0
     */
    public static List<String> SearchPathsForDirectory(final String path, final List<String> paths) {

        // For each path prefix in the Symbol Path
        return path != null ? paths.stream()
                // Skip null values
                .filter(Objects::nonNull)
                // Join each path pair
                .map(prefix -> ValidPath(prefix) + path)
                // Keep the existing & valid directories
                .filter(Files::IsDirectory)
                // Return the results
                .collect(Collectors.toList()) : List.of();

    }

    /**
     * <p>Searches {@link List} of path {@link String} values for all pairs of files that exist with the
     * specified relative path {@link String} value. If the specified path {@link String} value is null, this returns
     * an empty {@link List}.</p>
     * @param path The {@link String} value of the path to check against all prefix paths.
     * @param paths The {@link List} of path {@link String} values to search.
     * @return The set of existing {@link String} paths.
     * @since 0.1.0
     */
    public static List<String> SearchPathsForFile(final String path, final List<String> paths) {

        // For each path prefix in the Symbol Path
        return path != null ? paths.stream()
                // Skip null values
                .filter(Objects::nonNull)
                // Join each pair
                .map(prefix -> ValidPath(prefix) + path)
                // Keep the existing & valid directories
                .filter(Files::IsFile)
                // Return the results
                .collect(Collectors.toList()) : List.of();

    }

    /**
     * <p>Returns a {@link List} of {@link String} values corresponding to each file contained in the specified
     * {@link String} path that match the specified regex.</p>
     * @param path The {@link String} value of the path to extract a {@link List} of {@link String} paths.
     * @param regex The {@link String} value of the regex to test each file path against.
     * @return {@link List} of {@link String} values corresponding to each file contained in the specified
     * {@link String} path that match the specified regex as a {@link String} value.
     * @since 0.1.0
     */
    public static List<String> FilesOf(final String path, final String regex) {

        // Initialize the list of files
        final File[] files = new File(path).listFiles();

        // For each file
        return (files != null) ? Arrays.stream(files)
                // Omit null values
                .filter(Objects::nonNull)
                // Extract each name
                .map(File::getName)
                // Keep the files that match
                .filter(name -> name.matches(regex))
                // Return the results
                .collect(Collectors.toList()) : List.of();

    }

}
