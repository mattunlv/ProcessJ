package utilities;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A singleton class used to compare version strings. The version
 * format is MAJOR.MINOR.PATCH-LABEL, where LABEL is an optional
 * tag for pre-release and build metadata that is available as an
 * extension to MAJOR.MINOR.PATCH. The version number is incremented
 * as follows:
 * 
 *   1.) MAJOR version when making incompatible API changes
 *   2.) MINOR version when adding functionality in a backwards
 *       compatible manner
 *   3.) PATCH version when making backwards compatible bugs fixes
 * 
 * Source: https://semver.org
 * 
 * This class checks the current version of the ProcessJ compiler and
 * its runtime system. Starting from version 2.1.x, the runtime system
 * invokes the compare() method to notify the user of any possible
 * mismatch between the `current' version of the ProcessJ compiler and
 * the runtime system being used. Note, the runtime system should be
 * downloaded and manually linked to the ProcessJ compiler.
 * 
 * @author ben
 */
public enum RuntimeVersion implements Comparator<String> {
    
    INSTANCE;
    
    public final Pattern VALID_VERSION_PATTERN = Pattern.compile("[0-9](\\.[0-9]+)*((\\-[a-z]+(\\.[0-9]+)?)?)");
    public final Pattern VALID_LABEL_PATTER = Pattern.compile("([a-z]+(\\.[0-9]+)?)");
    
    private final int FIRST_GREATER = 1;
    private final int SECOND_GREATER = -1;
    
    @Override
    public int compare(String s1, String s2) {
        // Remove all white spaces
        if (!isEmpty(s1))
            s1 = s1.replaceAll(" ", "");
        if (!isEmpty(s2))
            s2 = s2.replaceAll(" ", "");
        // Split the metadata and build
        String[] v1 = s1.split("-");
        String[] v2 = s2.split("-");
        String build1 = v1.length > 1 ? getPreReleaseBuild(v1[1]) : null;
        String build2 = v2.length > 1 ? getPreReleaseBuild(v2[1]) : null;
        int cmp = compareStrings(v1[0], v2[0]);
        // Compare metadata and build
        if (cmp == 0) {
            if (isEmpty(build1) && isEmpty(build2))
                cmp = 0;
            else if (isEmpty(build1) && !isEmpty(build2))
                cmp = FIRST_GREATER;
            else if (isEmpty(build2) && !isEmpty(build1))
                cmp = SECOND_GREATER;
            else
                cmp = compareStrings(build1, build2);
        }
        return cmp;
    }
    
    private int compareStrings(String s1, String s2) {
        // Split version number
        String[] v1 = s1.split("\\.");
        String[] v2 = s2.split("\\.");
        int length = Math.max(v1.length, v2.length);
        for (int i = 0; i < length; ++i) {
            String c1 = i >= v1.length ? "0" : v1[i];
            String c2 = i >= v2.length ? "0" : v2[i];
            if (isNumeric(c1) && isNumeric(c2)) {
                if (Integer.parseInt(c1) > Integer.parseInt(c2))
                    return FIRST_GREATER;
                if (Integer.parseInt(c2) > Integer.parseInt(c1))
                    return SECOND_GREATER;
                return 0;
            }
            Matcher matcher1 = VALID_LABEL_PATTER.matcher(c1);
            Matcher matcher2 = VALID_LABEL_PATTER.matcher(c2);
            if (!matcher1.matches())
                throw new IllegalArgumentException("Invalid version format found, '" + c1 + "'");
            if (!matcher2.matches())
                throw new IllegalArgumentException("Invalid version format found, '" + c2 + "'");
            int cmp = matcher1.group(1).compareToIgnoreCase(matcher2.group(1));
            if (cmp != 0)
                return cmp;
        }
        return 0;
    }
    
    private String getPreReleaseBuild(String str) {
        if (isEmpty(str))
            return null;
        String[] build = str.split("\\+");
        if (build.length > 0)
            return build[0];
        return null;
    }
    
    private boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
    
    private boolean isNumeric(CharSequence cs) {
        if (cs == null || cs.length() == 0)
            return false;
        for (int i = 0; i < cs.length(); ++i)
            if (!Character.isDigit(cs.charAt(i)))
                return false;
        return true;
    }
}
