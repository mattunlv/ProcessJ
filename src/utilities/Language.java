package utilities;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This enum defines various types of programming languages available
 * in the ProcessJ compiler.
 *
 * @author ben
 * @version 08/30/2018
 * @since 1.2
 */
public enum Language implements EnumGet<String> {

    JVM ("JVM"),
    CPLUS ("C++"),
    JS ("JS");

    private final String language;
    private static final Map<String, Language> MAP;

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
    
    static {
        Map<String, Language> map = new HashMap<>();
        for (Language valueOf : Language.values())
            map.put(valueOf.getLanguage(), valueOf);
        MAP = Collections.unmodifiableMap(map);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Enum<E>> E getValueOf(String name) {
        return (E) MAP.get(name);
    }
}
