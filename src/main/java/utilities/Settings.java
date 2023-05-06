package utilities;
import java.io.File;

/**
 * @author Ben
 * @version 08/30/2018
 * @since 1.2
 */
public class Settings {

    public static String    absolutePath                = new File("").getAbsolutePath() + File.separator;
    public static String    includeDir                  = "include";
    public static Language  language                    = Language.JVM;
    // Notice: This was in the cppruntime branch to allow c++ output. The old branch's language default
    // was c++, but this contradicts the default language output for java. I  kept this here for reference
    // purposes. - Carlos
    // public static Language language                      = Language.CPLUS;
    public static boolean   showColor                   = false;
    public static final String VERSION                  = "2.1.1";
    public static final String IMPORT_FILE_EXTENSSION   = "pj";
    public static final String LEGACY_FILE_EXTENSION    = "pj";
}
