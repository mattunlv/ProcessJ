package org.processj.compiler.utilities;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Files {

    /**
     * <p>Returns a flag indicating if the specified {@link String} value has content; i.e. if it's not null,
     * is not empty, & doesn't exclusively contain whitespace.</p>
     * @param string The {@link String} value to check.
     * @return Flag indicating if the specified {@link String} value has content.
     * @since 0.1.0
     */
    public static boolean HasContent(final String string) {

        return (string != null) && !string.isEmpty() && !string.isBlank();

    }

    /**
     * <p>Returns a {@link String} that has all characters to the right of, including the {@link String} sequence
     * removed; relative to the specified {@link String}.</p>
     * @param string The path {@link String} value to truncate
     * @return {@link String} that has all characters to the right of, including the {@link String} sequence removed.
     * @since 0.1.0
     */
    public static String TruncateRightOf(final String string, final String sequence) {

        return (HasContent(string) && HasContent(sequence) && string.contains(sequence))
                ? string.substring(0, string.lastIndexOf(sequence)) : ((string != null) ? string : "");

    }

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
        return (HasContent(path))
                ? path + ((path.charAt(path.length() - 1) == '/') ? "" : '/') : "";

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
    public static List<String> RetrieveDirectoryListFrom(final String path, final List<String> paths) {

        // For each path prefix in the Symbol Path
        return path != null ? paths.stream()
                // Skip null values
                .filter(Objects::nonNull)
                // Join each path pair
                .map(prefix -> ValidPath(prefix) + path)
                // Keep the existing & valid directories
                .filter(Files::IsDirectory)
                // Return the results
                .collect(Collectors.toList()) : new ArrayList<>();

    }

    /**
     * <p>Returns a {@link List} of path {@link String} values corresponding to each file in the specified
     * path {@link String} value. If the path {@link String} value is invalid, or no files exist, this will return an
     * empty {@link List}.</p>
     * @param path The path {@link String} value to create a {@link String} file path {@link List} from.
     * @return {@link List} containing all file paths in the specified path as {@link String} values.
     * @since 0.1.0
     */
    public static List<String> RetrieveFileListFrom(final String path) {

        // Initialize the result
        List<String> result = new ArrayList<>();

        // If we have a valid directory
        if(IsDirectory(path)) {

            // Retrieve the preliminary
            final File[] preliminary = new File(path).listFiles();

            // Set the result
            result = (preliminary != null)
                    ? Arrays.stream(preliminary)
                    .filter(Objects::nonNull)
                    .map(File::getName)
                    .map(filename -> ValidPath(path) + filename)
                    .collect(Collectors.toList()) : new ArrayList<>();

        }

        // Return the result
        return result;

    }

    /**
     * <p>Returns a {@link List} of path {@link String} values corresponding to each file in the specified
     * path {@link String} value that match the specified regex {@link String}. If the path {@link String} value or
     * regex {@link String} value is invalid, or no files exist, this will return an empty {@link List}.</p>
     * @param path The path {@link String} value to create a {@link String} file path {@link List} from.
     * @param regex The {@link String} regex that each file name must match.
     * @return {@link List} containing all file paths in the specified path as {@link String} values.
     * @since 0.1.0
     */
    public static List<String> RetrieveMatchingFilesListFrom(final String path, final String regex) {

        // Initialize the result
        List<String> result = new ArrayList<>();

        // If we have a valid directory & regex
        if(IsDirectory(path) && HasContent(regex)) {

            // Retrieve the preliminary
            final File[] preliminary = new File(path).listFiles();

            // Set the result
            result = (preliminary != null)
                    ? Arrays.stream(preliminary)
                    .filter(Objects::nonNull)
                    .map(File::getName)
                    .filter(name -> name.matches(regex))
                    .map(filename -> ValidPath(path) + filename)
                    .collect(Collectors.toList()) : new ArrayList<>();

        }

        // Return the result
        return result;

    }

    /**
     * <p>Returns a {@link List} of path {@link String} values corresponding to each file in the specified
     * path {@link String} value that match the specified regex {@link String} & {@link String} name without extension.
     * If the path {@link String} value, regex {@link String} value, or name {@link String} value are invalid,
     * or no files exist, this will return an empty {@link List}.</p>
     * @param path The path {@link String} value to create a {@link String} file path {@link List} from.
     * @param filename The {@link String} value of the name to match
     * @param regex The {@link String} regex that each file name must match.
     * @return {@link List} containing all file paths in the specified path as {@link String} values.
     * @since 0.1.0
     */
    public static List<String> RetrieveMatchingFilesListWithName(final String path, final String filename, final String regex) {

        // Initialize the result
        List<String> result = new ArrayList<>();

        // If we have a valid directory, filename, & regex
        if(IsDirectory(path) && HasContent(filename) && HasContent(regex)) {

            // Retrieve the preliminary
            final File[] preliminary = new File(path).listFiles();

            // Set the result
            result = (preliminary != null)
                    ? Arrays.stream(preliminary)
                    .filter(Objects::nonNull)
                    .map(File::getName)
                    .filter(name -> name.matches(regex))
                    .filter(name -> TruncateRightOf(name, ".").matches(TruncateRightOf(filename, ".")))
                    .map(name -> ValidPath(path) + name)
                    .collect(Collectors.toList()) : new ArrayList<>();

        }

        // Return the result
        return result;

    }

    /**
     * <p>Returns a {@link List} of path {@link String} values corresponding to each file in each pair of prefix
     * (contained in the list) & specified path {@link String} value. If the path {@link String} value is invalid,
     * or no files exist, this will return an empty {@link List}.</p>
     * @param path The path {@link String} value to create a {@link String} file path {@link List} from.
     * @param prefixes The {@link List} of path {@link String} prefixes to retrieve files from
     * @return {@link List} containing all file paths in the specified paths as {@link String} values.
     * @since 0.1.0
     */
    public static List<String> RetrieveFileListFrom(final String path, final List<String> prefixes) {

        // Initialize the result
        final List<String> result = new ArrayList<>();

        // If we have a valid set of prefixes, add each of the files found in every prefix pair
        if(prefixes != null) prefixes
                .forEach(prefix -> result.addAll(RetrieveFileListFrom(ValidPath(prefix) + path)));

        // Return the result
        return result;

    }

    /**
     * <p>Returns a {@link List} of {@link String} path values corresponding to each file that exists in the paths
     * that are the concatenation of a {@link String} path prefix & the specified {@link String} path.
     * If the concatenation of a {@link String} path prefix & the specified {@link String} path is invalid,
     * or if no files exist, this will return an empty {@link List}.</p>
     * @param path The path {@link String} value to create a {@link String} file path {@link List} from.
     * @param prefixes The {@link List} of path {@link String} prefixes to retrieve files from.
     * @param regex The {@link String} regex that each file name must match.
     * @return {@link List} containing all file paths in the specified paths as {@link String} values.
     * @since 0.1.0
     */
    public static List<String> RetrieveMatchingFilesListFrom(final String path, final Set<String> prefixes, final String regex) {

        // Initialize the result
        final List<String> result = new ArrayList<>();

        // If we have a valid set of prefixes, add each of the files found in every prefix pair
        if(prefixes != null) prefixes.forEach(prefix ->
                result.addAll(RetrieveMatchingFilesListFrom(ValidPath(prefix) + path, regex)));

        // Return the result
        return result;

    }

    /**
     * <p>Returns a {@link List} of {@link String} path values corresponding to each file that exists in the paths
     * that are the concatenation of a {@link String} path prefix & the specified {@link String} path.
     * If the concatenation of a {@link String} path prefix & the specified {@link String} path is invalid,
     * or if no files exist, this will return an empty {@link List}.</p>
     * @param path The path {@link String} value to create a {@link String} file path {@link List} from.
     * @param prefixes The {@link List} of path {@link String} prefixes to retrieve files from.
     * @param regex The {@link String} regex that each file name must match.
     * @return {@link List} containing all file paths in the specified paths as {@link String} values.
     * @since 0.1.0
     */
    public static List<String> RetrieveMatchingFilesListWithName(final String path, final String filename,
                                                                 final Set<String> prefixes, final String regex) {

        // Initialize the result
        final List<String> result = new ArrayList<>();

        // If we have a valid set of prefixes, add each of the files found in every prefix pair
        if(prefixes != null) prefixes.forEach(prefix ->
                result.addAll(RetrieveMatchingFilesListWithName(ValidPath(prefix) + path, filename, regex)));

        // Return the result
        return result;

    }

}
