package utilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Properties;

/**
 * Loads properties from a file.
 * 
 * @author Ben
 * @version 10/22/2018
 * @since 1.2
 */
public class PropertiesLoader {
    
    public static URL getURL(String fileName) {
        URL url;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        url = cl.getResource(fileName);
        if (url == null) {
            cl = PropertiesLoader.class.getClassLoader();
            url = cl.getResource(fileName);
        }
        return url;
    }
    
    public static Properties loadProperties(File file) {
        Properties properties = new Properties();
        try (InputStream is = Files.newInputStream(file.toPath())) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to load file '%s'.", file));
        }
        
        return properties;
    }
}
