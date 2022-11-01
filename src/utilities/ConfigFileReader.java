package utilities;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class ConfigFileReader {
    
    /** Local path to configuration file */
    private final static String PATH = "resources/properties/PJConfig.properties";
    
    public static Properties openConfiguration() {
        URL url = PropertiesLoader.getURL(PATH);
        String path = PATH;
        FileInputStream in = null;
        Properties config = null;
        
        if (url != null)
            path = url.getFile();
        try {
            in = new FileInputStream(path);
            config = new Properties();
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return config;
    }
    
    public static void closeConfiguration(Properties configFile) {
        URL url = PropertiesLoader.getURL(PATH);
        String path = PATH;
        FileOutputStream out = null;
        
        if (url != null)
            path = url.getFile();
        
        try {
            out = new FileOutputStream(path);
            configFile.store(out, path);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static Properties getProcessJConfig() {
        Properties p = null;
        InputStream in = null;
        
        try {
            String home = System.getProperty("user.home");
            String config = home + "/processjrc";
            in = new FileInputStream(config);
            p = new Properties();
            p.load(in);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    System.out.print(e);
                    System.exit(1);
                }
            }
        }
        
        return p;
    }
}