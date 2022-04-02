package utilities;

/**
 * This enum defines various types of programming languages available
 * in the ProcessJ compiler.
 *
 * @author ben
 * @version 08/30/2018
 * @since 1.2
 */
public enum Language {

    JVM ("JVM"),
    CPLUS ("CPLUS"),
    JS ("JS");

    private final String language;

    Language(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        return language;
    }
}
